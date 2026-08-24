
const API_URL = "http://localhost:8080/api";

let empresaSeleccionada = null;
let camaras = [];

const empresaSelect = document.getElementById("empresaSelect");
const cameraList = document.getElementById("cameraList");

const totalCamaras = document.getElementById("totalCamaras");
const camarasActivas = document.getElementById("camarasActivas");
const camarasInactivas = document.getElementById("camarasInactivas");

const lastUpdate = document.getElementById("lastUpdate");
const refreshButton = document.getElementById("refreshButton");

const modal = document.getElementById("modal");
const closeModal = document.getElementById("closeModal");

const modalTitle = document.getElementById("modalTitle");
const cameraDetail = document.getElementById("cameraDetail");
const historyList = document.getElementById("historyList");

const connectionDot = document.getElementById("connectionDot");
const connectionText = document.getElementById("connectionText");


// ===============================
// INICIO
// ===============================

document.addEventListener("DOMContentLoaded", () => {
    cargarEmpresas();
});


// ===============================
// EMPRESAS
// ===============================

async function cargarEmpresas() {

    try {

        const response = await fetch(`${API_URL}/empresas`);

        if (!response.ok) {
            throw new Error("No se pudieron obtener las empresas");
        }

        const empresas = await response.json();

        empresaSelect.innerHTML =
            '<option value="">Selecciona una empresa</option>';

        empresas.forEach(empresa => {

            const option = document.createElement("option");

            option.value = empresa.id;
            option.textContent = empresa.nombre;

            empresaSelect.appendChild(option);
        });

        mostrarConexion(true);

    } catch (error) {

        console.error(error);

        mostrarConexion(false);
    }
}


// ===============================
// CAMBIO DE EMPRESA
// ===============================

empresaSelect.addEventListener("change", async () => {

    empresaSeleccionada = empresaSelect.value;

    if (!empresaSeleccionada) {

        camaras = [];

        mostrarCamaras();
        actualizarEstadisticas();

        return;
    }

    await cargarCamaras();
});


// ===============================
// CARGAR CAMARAS
// ===============================

async function cargarCamaras() {

    if (!empresaSeleccionada) {
        return;
    }

    try {

        const response = await fetch(
            `${API_URL}/camaras/empresa/${empresaSeleccionada}`
        );

        if (!response.ok) {
            throw new Error("No se pudieron obtener las cámaras");
        }

        camaras = await response.json();

        mostrarCamaras();
        actualizarEstadisticas();

        actualizarHora();

        mostrarConexion(true);

    } catch (error) {

        console.error(error);

        mostrarConexion(false);

        cameraList.innerHTML = `
            <div class="empty">
                No se pudieron cargar las cámaras.
            </div>
        `;
    }
}


// ===============================
// MOSTRAR CAMARAS
// ===============================

function mostrarCamaras() {

    if (camaras.length === 0) {

        cameraList.innerHTML = `
            <div class="empty">
                Esta empresa no tiene cámaras registradas.
            </div>
        `;

        return;
    }

    cameraList.innerHTML = "";

    camaras.forEach(camara => {

        const card = document.createElement("div");

        card.className = "camera-card";

        card.addEventListener("click", () => {
            abrirDetalle(camara);
        });

        card.innerHTML = `

            <div class="camera-info">

                <span class="status-dot ${camara.estado}"></span>

                <div>

                    <div class="camera-name">
                        ${escapeHtml(camara.nombre)}
                    </div>

                    <div class="camera-details">
                        ${escapeHtml(camara.ip)}:${camara.puerto}
                        · 📍 ${escapeHtml(camara.ubicacion || "Sin ubicación")}
                    </div>

                </div>

            </div>

            <div class="status ${camara.estado}">
                ${camara.estado}
            </div>

        `;

        cameraList.appendChild(card);
    });
}


// ===============================
// ESTADISTICAS
// ===============================

function actualizarEstadisticas() {

    const total = camaras.length;

    const activas = camaras.filter(
        camara => camara.estado === "ACTIVA"
    ).length;

    const inactivas = camaras.filter(
        camara => camara.estado === "INACTIVA"
    ).length;

    totalCamaras.textContent = total;
    camarasActivas.textContent = activas;
    camarasInactivas.textContent = inactivas;
}


// ===============================
// DETALLE DE CAMARA
// ===============================

async function abrirDetalle(camara) {

    modal.classList.remove("hidden");

    modalTitle.textContent = camara.nombre;

    cameraDetail.innerHTML = `

        <div class="camera-detail">

            <div class="detail-status ${camara.estado}">
                ${obtenerIconoEstado(camara.estado)}
                ${camara.estado}
            </div>

            <div class="detail-grid">

                <div class="detail-item">
                    <span>IP</span>
                    <strong>${escapeHtml(camara.ip)}</strong>
                </div>

                <div class="detail-item">
                    <span>Puerto</span>
                    <strong>${camara.puerto}</strong>
                </div>

                <div class="detail-item">
                    <span>Ubicación</span>
                    <strong>
                        ${escapeHtml(camara.ubicacion || "Sin ubicación")}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Última verificación</span>
                    <strong>
                        ${formatearFecha(camara.ultimaVerificacion)}
                    </strong>
                </div>

            </div>

        </div>
    `;

    historyList.innerHTML = "Cargando historial...";

    await cargarHistorial(camara.id);
}


// ===============================
// HISTORIAL
// ===============================

async function cargarHistorial(camaraId) {

    try {

        const response = await fetch(
            `${API_URL}/camaras/${camaraId}/historial`
        );

        if (!response.ok) {
            throw new Error("No se pudo obtener el historial");
        }

        const historial = await response.json();

        if (historial.length === 0) {

            historyList.innerHTML = `
                <div class="empty">
                    No hay cambios de estado registrados.
                </div>
            `;

            return;
        }

        historyList.innerHTML = "";

        historial.forEach(item => {

            const div = document.createElement("div");

            div.className = "history-item";

            div.innerHTML = `

                <strong>
                    ${obtenerIconoEstado(item.estadoNuevo)}
                    ${item.estadoAnterior || "DESCONOCIDO"}
                    →
                    ${item.estadoNuevo}
                </strong>

                <div class="history-date">
                    ${formatearFecha(item.fechaCambio)}
                </div>

            `;

            historyList.appendChild(div);
        });

    } catch (error) {

        console.error(error);

        historyList.innerHTML = `
            <div class="empty">
                No se pudo cargar el historial.
            </div>
        `;
    }
}


// ===============================
// ACTUALIZACION AUTOMATICA
// ===============================

setInterval(async () => {

    if (empresaSeleccionada) {
        await cargarCamaras();
    }

}, 10000);


// ===============================
// BOTON ACTUALIZAR
// ===============================

refreshButton.addEventListener("click", async () => {

    if (empresaSeleccionada) {
        await cargarCamaras();
    }

});


// ===============================
// MODAL
// ===============================

closeModal.addEventListener("click", () => {

    modal.classList.add("hidden");

});

modal.addEventListener("click", (event) => {

    if (event.target === modal) {
        modal.classList.add("hidden");
    }

});


// ===============================
// UTILIDADES
// ===============================

function actualizarHora() {

    const ahora = new Date();

    lastUpdate.textContent =
        `Última actualización: ${ahora.toLocaleTimeString()}`;
}


function formatearFecha(fecha) {

    if (!fecha) {
        return "Sin información";
    }

    const date = new Date(fecha);

    return date.toLocaleString("es-PE");
}


function obtenerIconoEstado(estado) {

    if (estado === "ACTIVA") {
        return "🟢";
    }

    if (estado === "INACTIVA") {
        return "🔴";
    }

    return "🟡";
}


function mostrarConexion(conectado) {

    if (conectado) {

        connectionDot.style.background = "#22c55e";
        connectionText.textContent = "API conectada";

    } else {

        connectionDot.style.background = "#ef4444";
        connectionText.textContent = "API desconectada";
    }
}


// Evita insertar HTML proveniente directamente de la API.
function escapeHtml(valor) {

    if (valor === null || valor === undefined) {
        return "";
    }

    return String(valor)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

