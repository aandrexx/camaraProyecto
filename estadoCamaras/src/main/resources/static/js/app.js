const API = {
    cameras: "/api/camaras",
    retiredCameras: "/api/camaras/retiradas"
};

const state = {
    cameras: [],
    retiredCameras: [],
    editingCameraId: null
};

const elements = {
    cameraBody: document.querySelector("#camera-table-body"),
    retiredCameraBody: document.querySelector("#retired-camera-table-body"),
    retiredEmptyState: document.querySelector("#retired-empty-state"),
    retiredSection: document.querySelector("#retired-section"),
    emptyState: document.querySelector("#empty-state"),
    emptyTitle: document.querySelector("#empty-title"),
    emptyDescription: document.querySelector("#empty-description"),
    errorBanner: document.querySelector("#error-banner"),
    connectionLabel: document.querySelector("#connection-label"),
    connectionIndicator: document.querySelector(".connection-indicator"),
    cameraDialog: document.querySelector("#camera-dialog"),
    cameraForm: document.querySelector("#camera-form"),
    cameraFormError: document.querySelector("#camera-form-error"),
    historyDialog: document.querySelector("#history-dialog"),
    historyContent: document.querySelector("#history-content")
};

async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers
        }
    });
    const responseText = await response.text();
    let data = null;
    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }
    if (!response.ok) {
        const detail = typeof data === "string"
            ? (/^\s*<(?:!doctype|html)/i.test(data) ? "" : data)
            : data?.message || data?.error || "";
        throw new Error(detail || `Error HTTP ${response.status}`);
    }
    return data;
}

function showError(message) {
    elements.errorBanner.textContent = message;
    elements.errorBanner.hidden = false;
}

function clearError() {
    elements.errorBanner.hidden = true;
    elements.errorBanner.textContent = "";
}

function setConnection(connected) {
    elements.connectionLabel.textContent = connected ? "API conectada" : "Sin conexión con la API";
    elements.connectionIndicator.classList.toggle("online", connected);
    elements.connectionIndicator.classList.toggle("offline", !connected);
}

function formatDate(value) {
    if (!value) return "Sin registro";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "Sin registro";
    return new Intl.DateTimeFormat("es-MX", {
        dateStyle: "short",
        timeStyle: "short"
    }).format(date);
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#39;"
    })[character]);
}

async function loadCameras() {
    const [cameras, retiredCameras] = await Promise.all([
        request(API.cameras),
        request(API.retiredCameras)
    ]);
    if (!Array.isArray(cameras) || !Array.isArray(retiredCameras)) {
        throw new Error("La API devolvió un formato inesperado al cargar cámaras.");
    }
    state.cameras = cameras;
    state.retiredCameras = retiredCameras;
    renderCameras();
    renderRetiredCameras();
}

function statusDetails(status) {
    if (status === "ACTIVA") return { text: "Activa", className: "status-active" };
    if (status === "INACTIVA") return { text: "Inactiva", className: "status-inactive" };
    if (status === "DEGRADADA") return { text: "Degradada", className: "status-degraded" };
    if (status === "ALERTA") return { text: "Alerta", className: "status-alert" };
    return { text: "Desconocida", className: "status-unknown" };
}

function renderCameras() {
    const query = document.querySelector("#search-input").value.trim().toLocaleLowerCase("es");
    const statusFilter = document.querySelector("#status-filter").value;
    const filtered = state.cameras.filter(camera =>
        [camera.nombre, camera.ip, camera.ubicacion, camera.macEsperada, camera.ultimaMacDetectada]
            .some(value => String(value ?? "").toLocaleLowerCase("es").includes(query))
        && (!statusFilter || camera.estado === statusFilter)
    );
    const active = state.cameras.filter(camera => camera.estado === "ACTIVA").length;
    const inactive = state.cameras.filter(camera => camera.estado === "INACTIVA").length;
    const macAlerts = state.cameras.filter(camera => camera.macCoincide === false).length;
    const ipCounts = new Map();
    state.cameras.forEach(camera => {
        const ip = String(camera.ip ?? "").trim();
        if (ip) ipCounts.set(ip, (ipCounts.get(ip) ?? 0) + 1);
    });
    const duplicateIps = new Set([...ipCounts].filter(([, count]) => count > 1).map(([ip]) => ip));

    document.querySelector("#total-count").textContent = state.cameras.length;
    document.querySelector("#active-count").textContent = active;
    document.querySelector("#inactive-count").textContent = inactive;
    document.querySelector("#mac-alert-count").textContent = macAlerts;
    document.querySelector("#duplicate-ip-count").textContent = duplicateIps.size;
    document.querySelector("#nav-camera-count").textContent = state.cameras.length;
    document.querySelector("#nav-retired-count").textContent = state.retiredCameras.length;
    document.querySelector("#table-result-count").textContent =
        `${filtered.length} ${filtered.length === 1 ? "dispositivo" : "dispositivos"}`;

    elements.cameraBody.replaceChildren();
    elements.emptyState.hidden = filtered.length !== 0;
    if (filtered.length === 0) {
        const hasSearch = query.length > 0;
        const hasFilter = statusFilter.length > 0;
        elements.emptyTitle.textContent = hasSearch ? "No hay resultados" :
            hasFilter ? "No hay cámaras con ese estado" :
            state.cameras.length === 0 ? "Todavía no hay cámaras" : "No se encontraron cámaras";
        elements.emptyDescription.textContent = hasSearch || hasFilter
            ? "Prueba con otro nombre, dirección IP o ubicación."
            : state.cameras.length === 0 ? "Registra una cámara para comenzar a monitorear la red." : "No hay cámaras para mostrar.";
        document.querySelector("#empty-add-camera").hidden = hasSearch || hasFilter;
        return;
    }

    filtered.forEach(camera => {
        const status = statusDetails(camera.estado);
        const macWarning = camera.macCoincide === false
            ? '<span class="mac-warning">⚠ No coincide</span>'
            : "";
        const ipWarning = duplicateIps.has(String(camera.ip ?? "").trim())
            ? '<span class="ip-warning">⚠ IP repetida</span>'
            : "";
        const expectedMac = camera.macEsperada
            ? `<span class="mac-address">${escapeHtml(camera.macEsperada)}</span>`
            : '<span class="mac-address empty">No configurada</span>';
        const detectedMac = camera.ultimaMacDetectada
            ? `<span class="mac-address">${escapeHtml(camera.ultimaMacDetectada)}</span>`
            : '<span class="mac-address empty">Sin detectar</span>';
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><div class="camera-name"><strong>${escapeHtml(camera.nombre)}</strong><span>${escapeHtml(camera.ubicacion || "Ubicación no definida")}</span></div></td>
            <td><span class="ip-address">${escapeHtml(camera.ip || "—")}${camera.puerto ? `:${escapeHtml(camera.puerto)}` : ""}</span>${ipWarning}</td>
            <td><span class="status-badge ${status.className}">${status.text}</span></td>
            <td>${expectedMac}</td>
            <td>${detectedMac}${macWarning}</td>
            <td><span class="timestamp">${escapeHtml(formatDate(camera.ultimaVerificacion))}</span></td>
            <td><div class="row-actions">
                <button class="row-action" type="button" data-action="history" data-id="${escapeHtml(camera.id)}" title="Ver historial" aria-label="Ver historial">◷</button>
                <button class="row-action" type="button" data-action="edit" data-id="${escapeHtml(camera.id)}" title="Editar cámara" aria-label="Editar cámara">✎</button>
                <button class="row-action row-action-danger" type="button" data-action="delete" data-id="${escapeHtml(camera.id)}" title="Eliminar cámara" aria-label="Eliminar cámara">×</button>
            </div></td>`;
        elements.cameraBody.append(row);
    });
}

function renderRetiredCameras() {
    const count = state.retiredCameras.length;
    document.querySelector("#retired-count").textContent = count;
    document.querySelector("#retired-result-count").textContent = `${count} ${count === 1 ? "cámara" : "cámaras"}`;
    elements.retiredCameraBody.replaceChildren();
    elements.retiredEmptyState.hidden = count !== 0;
    elements.retiredSection.classList.toggle("has-retired-cameras", count > 0);

    state.retiredCameras.forEach(camera => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><div class="camera-name"><strong>${escapeHtml(camera.nombre)}</strong><span>${escapeHtml(camera.ubicacion || "Ubicación no definida")}</span></div></td>
            <td><span class="ip-address">${escapeHtml(camera.ip || "—")}${camera.puerto ? `:${escapeHtml(camera.puerto)}` : ""}</span></td>
            <td><span class="timestamp">${escapeHtml(formatDate(camera.ultimaVerificacion))}</span></td>
            <td><div class="row-actions">
                <button class="row-action" type="button" data-action="history" data-id="${escapeHtml(camera.id)}" title="Ver historial" aria-label="Ver historial">◷</button>
                <button class="button button-restore" type="button" data-action="restore" data-id="${escapeHtml(camera.id)}">Restaurar</button>
            </div></td>`;
        elements.retiredCameraBody.append(row);
    });
}

function updateRefreshTime() {
    document.querySelector("#last-updated").textContent = `Actualizado ${new Intl.DateTimeFormat("es-MX", {
        hour: "2-digit", minute: "2-digit", second: "2-digit"
    }).format(new Date())}`;
}

async function refreshDashboard() {
    clearError();
    const refreshButton = document.querySelector("#refresh-button");
    refreshButton.disabled = true;
    try {
        await loadCameras();
        updateRefreshTime();
        setConnection(true);
    } catch (error) {
        setConnection(false);
        showError(`No se pudo actualizar el panel: ${error.message}`);
        if (state.cameras.length === 0) {
            elements.cameraBody.innerHTML = '<tr><td colspan="7" class="loading-cell">No fue posible cargar los dispositivos.</td></tr>';
        }
    } finally {
        refreshButton.disabled = false;
    }
}

function openCameraDialog(camera) {
    state.editingCameraId = camera?.id ?? null;
    elements.cameraForm.reset();
    elements.cameraFormError.hidden = true;
    document.querySelector("#camera-dialog-title").textContent = camera ? "Editar cámara" : "Nueva cámara";
    document.querySelector("#save-camera-button").textContent = camera ? "Guardar cambios" : "Guardar cámara";
    if (camera) {
        for (const field of ["nombre", "ip", "puerto", "ubicacion", "macEsperada"]) {
            elements.cameraForm.elements[field].value = camera[field] ?? "";
        }
    }
    elements.cameraDialog.showModal();
}

async function saveCamera(event) {
    event.preventDefault();
    elements.cameraFormError.hidden = true;
    const formData = new FormData(elements.cameraForm);
    const payload = {
        nombre: formData.get("nombre").trim(),
        ip: formData.get("ip").trim(),
        puerto: formData.get("puerto") ? Number(formData.get("puerto")) : null,
        ubicacion: formData.get("ubicacion").trim() || null,
        macEsperada: formData.get("macEsperada").trim() || null
    };
    const saveButton = document.querySelector("#save-camera-button");
    saveButton.disabled = true;
    try {
        const url = state.editingCameraId
            ? `${API.cameras}/${encodeURIComponent(state.editingCameraId)}`
            : API.cameras;
        await request(url, {
            method: state.editingCameraId ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        elements.cameraDialog.close();
        await refreshDashboard();
    } catch (error) {
        elements.cameraFormError.textContent = `No se pudo guardar la cámara: ${error.message}`;
        elements.cameraFormError.hidden = false;
    } finally {
        saveButton.disabled = false;
    }
}

async function openHistory(cameraId) {
    const camera = [...state.cameras, ...state.retiredCameras]
        .find(item => String(item.id) === String(cameraId));
    document.querySelector("#history-title").textContent = camera
        ? `Historial · ${camera.nombre}` : "Historial de cámara";
    elements.historyContent.innerHTML = '<span class="loading-spinner"></span> Cargando historial...';
    elements.historyDialog.showModal();
    try {
        const history = await request(`${API.cameras}/${encodeURIComponent(cameraId)}/historial`);
        if (!Array.isArray(history)) throw new Error("La API devolvió un formato inesperado.");
        if (history.length === 0) {
            elements.historyContent.innerHTML = '<div class="history-empty">Esta cámara todavía no tiene eventos registrados.</div>';
            return;
        }
        elements.historyContent.replaceChildren();
        history.forEach(event => {
            const title = event.tipoEvento === "ALERTA_MAC" ? "Alerta de identidad MAC"
                : `Cambio de estado: ${event.estadoAnterior || "Sin estado"} → ${event.estadoNuevo || "Sin estado"}`;
            const item = document.createElement("article");
            item.className = "history-entry";
            item.innerHTML = `<span class="history-marker">${event.tipoEvento === "ALERTA_MAC" ? "⚠" : "↻"}</span>
                <div><h3>${escapeHtml(title)}</h3><p>${escapeHtml(formatDate(event.fechaCambio))}</p>
                    <p class="history-reason">${escapeHtml(event.motivo || "Sin detalle registrado para este evento.")}</p></div>`;
            elements.historyContent.append(item);
        });
    } catch (error) {
        elements.historyContent.textContent = `No se pudo cargar el historial: ${error.message}`;
    }
}

async function deleteCamera(camera) {
    if (!window.confirm(`¿Retirar la cámara "${camera.nombre}"? Dejará de aparecer y de monitorearse, pero su historial se conservará.`)) {
        return;
    }
    clearError();
    try {
        await request(`${API.cameras}/${encodeURIComponent(camera.id)}`, { method: "DELETE" });
        await refreshDashboard();
    } catch (error) {
        showError(`No se pudo retirar la cámara: ${error.message}`);
    }
}

async function restoreCamera(camera, button) {
    button.disabled = true;
    clearError();
    try {
        await request(`${API.cameras}/${encodeURIComponent(camera.id)}/restaurar`, { method: "POST" });
        await refreshDashboard();
    } catch (error) {
        showError(`No se pudo restaurar la cámara: ${error.message}`);
    } finally {
        if (button.isConnected) button.disabled = false;
    }
}

document.querySelector("#refresh-button").addEventListener("click", refreshDashboard);
document.querySelector("#add-camera-button").addEventListener("click", () => openCameraDialog());
document.querySelector("#empty-add-camera").addEventListener("click", () => openCameraDialog());
document.querySelector("#search-input").addEventListener("input", renderCameras);
document.querySelector("#status-filter").addEventListener("change", renderCameras);
document.querySelector("#retired-nav-link").addEventListener("click", event => {
    event.preventDefault();
    elements.retiredSection.open = true;
    elements.retiredSection.scrollIntoView({ behavior: "smooth", block: "start" });
});
elements.cameraForm.addEventListener("submit", saveCamera);
elements.cameraBody.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const camera = state.cameras.find(item => String(item.id) === button.dataset.id);
    if (!camera) return;
    if (button.dataset.action === "edit") openCameraDialog(camera);
    if (button.dataset.action === "history") openHistory(camera.id);
    if (button.dataset.action === "delete") deleteCamera(camera);
});
elements.retiredCameraBody.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const camera = state.retiredCameras.find(item => String(item.id) === button.dataset.id);
    if (!camera) return;
    if (button.dataset.action === "history") openHistory(camera.id);
    if (button.dataset.action === "restore") restoreCamera(camera, button);
});
document.querySelectorAll(".close-dialog").forEach(button => {
    button.addEventListener("click", () => button.closest("dialog").close());
});
document.querySelectorAll("dialog").forEach(dialog => {
    dialog.addEventListener("click", event => {
        if (event.target === dialog) dialog.close();
    });
});

refreshDashboard();
window.setInterval(refreshDashboard, 60_000);
