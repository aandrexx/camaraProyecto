const API = {
    companies: "/api/empresas",
    cameras: "/api/camaras"
};

const state = {
    companies: [],
    cameras: [],
    editingCameraId: null
};

const elements = {
    companySelect: document.querySelector("#company-select"),
    cameraBody: document.querySelector("#camera-table-body"),
    emptyState: document.querySelector("#empty-state"),
    emptyTitle: document.querySelector("#empty-title"),
    emptyDescription: document.querySelector("#empty-description"),
    errorBanner: document.querySelector("#error-banner"),
    connectionLabel: document.querySelector("#connection-label"),
    connectionIndicator: document.querySelector(".connection-indicator"),
    cameraDialog: document.querySelector("#camera-dialog"),
    cameraForm: document.querySelector("#camera-form"),
    cameraFormError: document.querySelector("#camera-form-error"),
    companyDialog: document.querySelector("#company-dialog"),
    companyForm: document.querySelector("#company-form"),
    companyFormError: document.querySelector("#company-form-error"),
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
        const detail = typeof data === "string" ? data : data?.message || data?.error || "";
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

async function loadCompanies(preferredId) {
    const companies = await request(API.companies);
    if (!Array.isArray(companies)) throw new Error("La API devolvió un formato inesperado al cargar empresas.");
    state.companies = companies;
    elements.companySelect.replaceChildren();

    if (companies.length === 0) {
        const option = document.createElement("option");
        option.value = "";
        option.textContent = "Agrega una empresa";
        elements.companySelect.append(option);
        state.cameras = [];
        renderCameras();
        return;
    }

    companies.forEach(company => {
        const option = document.createElement("option");
        option.value = company.id;
        option.textContent = company.nombre;
        elements.companySelect.append(option);
    });

    const selectedId = preferredId ?? elements.companySelect.value;
    elements.companySelect.value = companies.some(company => String(company.id) === String(selectedId))
        ? String(selectedId)
        : String(companies[0].id);
    await loadCameras();
}

async function loadCameras() {
    const companyId = elements.companySelect.value;
    if (!companyId) {
        state.cameras = [];
        renderCameras();
        return;
    }
    const cameras = await request(`${API.cameras}/empresa/${encodeURIComponent(companyId)}`);
    if (!Array.isArray(cameras)) throw new Error("La API devolvió un formato inesperado al cargar cámaras.");
    state.cameras = cameras;
    renderCameras();
}

function statusDetails(status) {
    if (status === "ACTIVA") return { text: "Activa", className: "status-active" };
    if (status === "INACTIVA") return { text: "Inactiva", className: "status-inactive" };
    return { text: "Desconocido", className: "status-unknown" };
}

function renderCameras() {
    const query = document.querySelector("#search-input").value.trim().toLocaleLowerCase("es");
    const filtered = state.cameras.filter(camera =>
        [camera.nombre, camera.ip, camera.ubicacion, camera.macEsperada, camera.ultimaMacDetectada]
            .some(value => String(value ?? "").toLocaleLowerCase("es").includes(query))
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
    document.querySelector("#table-result-count").textContent =
        `${filtered.length} ${filtered.length === 1 ? "dispositivo" : "dispositivos"}`;

    elements.cameraBody.replaceChildren();
    elements.emptyState.hidden = filtered.length !== 0;
    if (filtered.length === 0) {
        const hasSearch = query.length > 0;
        elements.emptyTitle.textContent = hasSearch ? "No hay resultados" :
            state.cameras.length === 0 ? "Todavía no hay cámaras" : "No se encontraron cámaras";
        elements.emptyDescription.textContent = hasSearch
            ? "Prueba con otro nombre, dirección IP o ubicación."
            : state.cameras.length === 0 ? "Registra una cámara para comenzar a monitorear la red." : "No hay cámaras para mostrar.";
        document.querySelector("#empty-add-camera").hidden = state.companies.length === 0 || hasSearch;
        return;
    }

    filtered.forEach(camera => {
        const status = statusDetails(camera.estado);
        const macWarning = camera.macCoincide === false
            ? '<span class="mac-warning">⚠ No coincide</span>'
            : "";
        const ipWarning = duplicateIps.has(String(camera.ip ?? "").trim())
            ? '<span class="ip-warning">⚠ IP repetida en esta empresa</span>'
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
            </div></td>`;
        elements.cameraBody.append(row);
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
        await loadCompanies(elements.companySelect.value || undefined);
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
    if (state.companies.length === 0) {
        showError("Primero agrega una empresa para poder registrar cámaras.");
        return;
    }
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
        macEsperada: formData.get("macEsperada").trim() || null,
        empresaId: Number(elements.companySelect.value)
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

async function saveCompany(event) {
    event.preventDefault();
    elements.companyFormError.hidden = true;
    const formData = new FormData(elements.companyForm);
    const payload = {
        nombre: formData.get("nombre").trim(),
        direccion: formData.get("direccion").trim() || null,
        contactoTecnico: formData.get("contactoTecnico").trim() || null
    };
    const saveButton = elements.companyForm.querySelector('[type="submit"]');
    saveButton.disabled = true;
    try {
        const company = await request(API.companies, { method: "POST", body: JSON.stringify(payload) });
        elements.companyDialog.close();
        elements.companyForm.reset();
        await loadCompanies(company.id);
        clearError();
        setConnection(true);
        updateRefreshTime();
    } catch (error) {
        elements.companyFormError.textContent = `No se pudo guardar la empresa: ${error.message}`;
        elements.companyFormError.hidden = false;
    } finally {
        saveButton.disabled = false;
    }
}

async function openHistory(cameraId) {
    const camera = state.cameras.find(item => String(item.id) === String(cameraId));
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
                <div><h3>${escapeHtml(title)}</h3><p>${escapeHtml(formatDate(event.fechaCambio))}</p></div>`;
            elements.historyContent.append(item);
        });
    } catch (error) {
        elements.historyContent.textContent = `No se pudo cargar el historial: ${error.message}`;
    }
}

document.querySelector("#refresh-button").addEventListener("click", refreshDashboard);
document.querySelector("#add-camera-button").addEventListener("click", () => openCameraDialog());
document.querySelector("#empty-add-camera").addEventListener("click", () => openCameraDialog());
document.querySelector("#add-company-button").addEventListener("click", () => {
    elements.companyForm.reset();
    elements.companyFormError.hidden = true;
    elements.companyDialog.showModal();
});
elements.companySelect.addEventListener("change", async () => {
    clearError();
    try {
        await loadCameras();
        setConnection(true);
        updateRefreshTime();
    } catch (error) {
        setConnection(false);
        showError(`No se pudieron cargar las cámaras: ${error.message}`);
    }
});
document.querySelector("#search-input").addEventListener("input", renderCameras);
elements.cameraForm.addEventListener("submit", saveCamera);
elements.companyForm.addEventListener("submit", saveCompany);
elements.cameraBody.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const camera = state.cameras.find(item => String(item.id) === button.dataset.id);
    if (!camera) return;
    if (button.dataset.action === "edit") openCameraDialog(camera);
    if (button.dataset.action === "history") openHistory(camera.id);
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
