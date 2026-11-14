const API = {
    register: "/auth/register",
    login: "/auth/login"
};

const SESSION_KEY = "estadoCamarasSession";
let session = readStoredSession();
let onSessionChange = () => {};

const elements = {
    authScreen: document.querySelector("#auth-screen"),
    appShell: document.querySelector("#app-shell"),
    loginForm: document.querySelector("#login-form"),
    registerForm: document.querySelector("#register-form"),
    authMessage: document.querySelector("#auth-message"),
    sessionIdentity: document.querySelector("#session-identity"),
    addCameraButton: document.querySelector("#add-camera-button")
};

function readStoredSession() {
    const stored = sessionStorage.getItem(SESSION_KEY);
    if (!stored) return null;
    try {
        const value = JSON.parse(stored);
        if (typeof value?.token === "string" && value.token
            && typeof value.email === "string"
            && ["ADMIN", "USER"].includes(value.rol)) {
            return value;
        }
    } catch {
        sessionStorage.removeItem(SESSION_KEY);
        return null;
    }
    sessionStorage.removeItem(SESSION_KEY);
    return null;
}

export function getSession() {
    return session;
}

export async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
            ...options.headers
        }
    });
    if (response.status === 401 && session) {
        clearSession();
        showAuthScreen("Tu sesión terminó. Inicia sesión nuevamente.");
    }

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
        const validationErrors = Array.isArray(data?.errors)
            ? data.errors
                .map(error => [error.field, error.message].filter(Boolean).join(": "))
                .filter(Boolean)
                .join("; ")
            : "";
        const detail = typeof data === "string"
            ? (/^\s*<(?:!doctype|html)/i.test(data) ? "" : data)
            : [data?.message || data?.detail || data?.error, validationErrors]
                .filter(Boolean)
                .join(" ");
        throw new Error(detail || `Error HTTP ${response.status}`);
    }
    return data;
}

function showAuthMessage(message, success = false) {
    elements.authMessage.textContent = message;
    elements.authMessage.classList.toggle("success", success);
    elements.authMessage.hidden = false;
}

function clearAuthMessage() {
    elements.authMessage.hidden = true;
    elements.authMessage.textContent = "";
    elements.authMessage.classList.remove("success");
}

function setAuthMode(mode) {
    const registering = mode === "register";
    elements.loginForm.hidden = registering;
    elements.registerForm.hidden = !registering;
    document.querySelector("#login-mode-button").classList.toggle("active", !registering);
    document.querySelector("#register-mode-button").classList.toggle("active", registering);
    document.querySelector("#login-mode-button").setAttribute("aria-selected", String(!registering));
    document.querySelector("#register-mode-button").setAttribute("aria-selected", String(registering));
    document.querySelector("#auth-title").textContent = registering ? "Crea tu cuenta" : "Inicia sesión";
    document.querySelector("#auth-description").textContent = registering
        ? "Regístrate para consultar el panel de monitoreo."
        : "Accede al panel de monitoreo de cámaras.";
    clearAuthMessage();
}

function showAuthScreen(message) {
    elements.authScreen.hidden = false;
    elements.appShell.hidden = true;
    elements.sessionIdentity.textContent = "";
    elements.addCameraButton.hidden = true;
    setAuthMode("login");
    if (message) showAuthMessage(message);
}

function clearSession() {
    sessionStorage.removeItem(SESSION_KEY);
    session = null;
    onSessionChange(null);
}

function showApp() {
    elements.authScreen.hidden = true;
    elements.appShell.hidden = false;
    elements.sessionIdentity.textContent = `${session.email} · ${session.rol}`;
    elements.addCameraButton.hidden = session.rol !== "ADMIN";
    clearAuthMessage();
    onSessionChange(session);
}

function startSession(authResponse, email) {
    if (typeof authResponse?.token !== "string" || !authResponse.token
        || !["ADMIN", "USER"].includes(authResponse.rol)) {
        throw new Error("La respuesta de autenticación no contiene un token y rol válidos.");
    }
    session = { token: authResponse.token, rol: authResponse.rol, email };
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
    showApp();
}

async function submitLogin(event) {
    event.preventDefault();
    clearAuthMessage();
    const submitButton = document.querySelector("#login-submit");
    submitButton.disabled = true;
    const formData = new FormData(elements.loginForm);
    const email = String(formData.get("email")).trim();
    try {
        const response = await request(API.login, {
            method: "POST",
            body: JSON.stringify({ email, password: formData.get("password") })
        });
        startSession(response, email);
        elements.loginForm.reset();
    } catch (error) {
        showAuthMessage(`No se pudo iniciar sesión: ${error.message}`);
    } finally {
        submitButton.disabled = false;
    }
}

async function submitRegistration(event) {
    event.preventDefault();
    clearAuthMessage();
    const submitButton = document.querySelector("#register-submit");
    const formData = new FormData(elements.registerForm);
    const email = String(formData.get("email")).trim();
    const password = String(formData.get("password"));
    if (password !== formData.get("confirmPassword")) {
        showAuthMessage("Las contraseñas no coinciden.");
        return;
    }

    submitButton.disabled = true;
    let accountCreated = false;
    try {
        await request(API.register, {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        accountCreated = true;
        const response = await request(API.login, {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        startSession(response, email);
        elements.registerForm.reset();
    } catch (error) {
        if (accountCreated) {
            setAuthMode("login");
            document.querySelector("#login-form [name=email]").value = email;
            showAuthMessage(`La cuenta se creó, pero no se pudo iniciar sesión: ${error.message}`);
        } else {
            showAuthMessage(`No se pudo crear la cuenta: ${error.message}`);
        }
    } finally {
        submitButton.disabled = false;
    }
}

export function initializeAuth(onChange) {
    onSessionChange = onChange;
    elements.loginForm.addEventListener("submit", submitLogin);
    elements.registerForm.addEventListener("submit", submitRegistration);
    document.querySelector("#login-mode-button").addEventListener("click", () => setAuthMode("login"));
    document.querySelector("#register-mode-button").addEventListener("click", () => setAuthMode("register"));
    document.querySelector("#logout-button").addEventListener("click", () => {
        clearSession();
        showAuthScreen();
    });

    if (session) {
        showApp();
    } else {
        showAuthScreen();
        onSessionChange(null);
    }
}
