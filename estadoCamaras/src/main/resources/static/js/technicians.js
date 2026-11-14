import { request } from "./auth.js";

const API = "/api/tecnicos";
const state = {
    technicians: [],
    editingId: null,
    enabled: false
};

const elements = {
    navLink: document.querySelector("#technicians-nav-link"),
    section: document.querySelector("#technicians-section"),
    tableBody: document.querySelector("#technician-table-body"),
    emptyState: document.querySelector("#technician-empty-state"),
    emptyTitle: document.querySelector("#technician-empty-title"),
    emptyDescription: document.querySelector("#technician-empty-description"),
    error: document.querySelector("#technicians-error"),
    dialog: document.querySelector("#technician-dialog"),
    form: document.querySelector("#technician-form"),
    formError: document.querySelector("#technician-form-error")
};

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#39;"
    })[character]);
}

function showError(message) {
    elements.error.textContent = message;
    elements.error.hidden = false;
}

function clearError() {
    elements.error.textContent = "";
    elements.error.hidden = true;
}

function renderTechnicians() {
    const query = document.querySelector("#technician-search").value.trim().toLocaleLowerCase("es");
    const statusFilter = document.querySelector("#technician-status-filter").value;
    const filtered = state.technicians.filter(technician =>
        [technician.nombre, technician.telefono]
            .some(value => String(value ?? "").toLocaleLowerCase("es").includes(query))
        && (!statusFilter || String(technician.activo) === statusFilter)
    );

    document.querySelector("#nav-technician-count").textContent =
        state.technicians.filter(technician => technician.activo).length;
    document.querySelector("#technician-result-count").textContent =
        `${filtered.length} ${filtered.length === 1 ? "técnico" : "técnicos"}`;
    elements.tableBody.replaceChildren();
    elements.emptyState.hidden = filtered.length !== 0;
    if (filtered.length === 0) {
        const hasFilter = query.length > 0 || statusFilter.length > 0;
        elements.emptyTitle.textContent = hasFilter ? "No se encontraron técnicos" : "Todavía no hay técnicos";
        elements.emptyDescription.textContent = hasFilter
            ? "Prueba con otro nombre, teléfono o filtro."
            : "Agrega técnicos para que reciban las notificaciones de WhatsApp.";
        document.querySelector("#empty-add-technician").hidden = hasFilter;
        return;
    }

    filtered.forEach(technician => {
        const action = technician.activo
            ? `<button class="row-action row-action-danger" type="button" data-action="deactivate" data-id="${escapeHtml(technician.id)}" title="Desactivar técnico" aria-label="Desactivar ${escapeHtml(technician.nombre)}">×</button>`
            : `<button class="button button-restore" type="button" data-action="reactivate" data-id="${escapeHtml(technician.id)}">Reactivar</button>`;
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><div class="camera-name"><strong>${escapeHtml(technician.nombre)}</strong></div></td>
            <td><span class="ip-address">${escapeHtml(technician.telefono)}</span></td>
            <td><span class="status-badge ${technician.activo ? "technician-active" : "technician-inactive"}">${technician.activo ? "Activo" : "Inactivo"}</span></td>
            <td><div class="row-actions">
                <button class="row-action" type="button" data-action="edit" data-id="${escapeHtml(technician.id)}" title="Editar técnico" aria-label="Editar ${escapeHtml(technician.nombre)}">✎</button>
                ${action}
            </div></td>`;
        elements.tableBody.append(row);
    });
}

async function loadTechnicians() {
    if (!state.enabled) return;
    clearError();
    const refreshButton = document.querySelector("#refresh-technicians-button");
    refreshButton.disabled = true;
    try {
        const technicians = await request(API);
        if (!Array.isArray(technicians)) {
            throw new Error("La API devolvió un formato inesperado al cargar técnicos.");
        }
        state.technicians = technicians;
        renderTechnicians();
    } catch (error) {
        showError(`No se pudieron cargar los técnicos: ${error.message}`);
    } finally {
        refreshButton.disabled = false;
    }
}

function openTechnicianDialog(technician = null) {
    state.editingId = technician?.id ?? null;
    elements.form.reset();
    elements.formError.hidden = true;
    document.querySelector("#technician-dialog-title").textContent =
        technician ? "Editar técnico" : "Nuevo técnico";
    document.querySelector("#save-technician-button").textContent =
        technician ? "Guardar cambios" : "Guardar técnico";
    if (technician) {
        elements.form.elements.nombre.value = technician.nombre;
        elements.form.elements.telefono.value = technician.telefono;
    }
    elements.dialog.showModal();
}

async function saveTechnician(event) {
    event.preventDefault();
    elements.formError.hidden = true;
    const formData = new FormData(elements.form);
    const payload = {
        nombre: String(formData.get("nombre")).trim(),
        telefono: String(formData.get("telefono")).trim()
    };
    const button = document.querySelector("#save-technician-button");
    button.disabled = true;
    try {
        const url = state.editingId
            ? `${API}/${encodeURIComponent(state.editingId)}`
            : API;
        await request(url, {
            method: state.editingId ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        elements.dialog.close();
        await loadTechnicians();
    } catch (error) {
        elements.formError.textContent = `No se pudo guardar el técnico: ${error.message}`;
        elements.formError.hidden = false;
    } finally {
        button.disabled = false;
    }
}

async function changeTechnicianStatus(technician, action, button) {
    const activating = action === "reactivate";
    const confirmation = activating
        ? `¿Reactivar a ${technician.nombre}? Volverá a recibir notificaciones.`
        : `¿Desactivar a ${technician.nombre}? Dejará de recibir notificaciones.`;
    if (!window.confirm(confirmation)) return;

    button.disabled = true;
    clearError();
    try {
        const url = activating
            ? `${API}/${encodeURIComponent(technician.id)}/reactivar`
            : `${API}/${encodeURIComponent(technician.id)}`;
        await request(url, { method: activating ? "POST" : "DELETE" });
        await loadTechnicians();
    } catch (error) {
        showError(`No se pudo ${activating ? "reactivar" : "desactivar"} el técnico: ${error.message}`);
    } finally {
        if (button.isConnected) button.disabled = false;
    }
}

export function setTechniciansEnabled(enabled) {
    state.enabled = enabled;
    elements.navLink.hidden = !enabled;
    elements.section.hidden = !enabled;
    if (enabled) {
        void loadTechnicians();
    } else {
        state.technicians = [];
        clearError();
        elements.tableBody.replaceChildren();
        renderTechnicians();
    }
}

document.querySelector("#add-technician-button").addEventListener("click", () => openTechnicianDialog());
document.querySelector("#empty-add-technician").addEventListener("click", () => openTechnicianDialog());
document.querySelector("#refresh-technicians-button").addEventListener("click", loadTechnicians);
document.querySelector("#technician-search").addEventListener("input", renderTechnicians);
document.querySelector("#technician-status-filter").addEventListener("change", renderTechnicians);
elements.form.addEventListener("submit", saveTechnician);
elements.tableBody.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const technician = state.technicians.find(item => String(item.id) === button.dataset.id);
    if (!technician) return;
    if (button.dataset.action === "edit") openTechnicianDialog(technician);
    if (button.dataset.action === "deactivate" || button.dataset.action === "reactivate") {
        void changeTechnicianStatus(technician, button.dataset.action, button);
    }
});
