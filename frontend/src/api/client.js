// Cliente HTTP para la API de Spring Boot.
// En desarrollo las rutas son relativas y Vite las reenvía al backend (ver vite.config.js).
// Para producción con el backend en otro dominio, define VITE_API_URL (p. ej. http://servidor:8080).
const API_BASE = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')

let authToken = null
let onUnauthorized = () => {}

export function setAuthToken(token) {
  authToken = token
}

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

function errorDetail(data, status) {
  if (typeof data === 'string') {
    return /^\s*<(?:!doctype|html)/i.test(data) ? '' : data
  }
  const validationErrors = Array.isArray(data?.errors)
    ? data.errors
        .map((error) => [error.field, error.message].filter(Boolean).join(': '))
        .filter(Boolean)
        .join('; ')
    : ''
  const detail = [data?.message || data?.detail || data?.error, validationErrors]
    .filter(Boolean)
    .join(' ')
  return detail || `Error HTTP ${status}`
}

export async function request(path, { body, headers, ...options } = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    body: body === undefined ? undefined : JSON.stringify(body),
    headers: {
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
      ...headers,
    },
  })

  if (response.status === 401 && authToken) {
    onUnauthorized()
  }

  const text = await response.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = text
    }
  }

  if (!response.ok) {
    throw new Error(errorDetail(data, response.status) || `Error HTTP ${response.status}`)
  }
  return data
}

const id = (value) => encodeURIComponent(value)

export const authApi = {
  login: (email, password) => request('/auth/login', { method: 'POST', body: { email, password } }),
  register: (email, password) => request('/auth/register', { method: 'POST', body: { email, password } }),
}

export const camarasApi = {
  listar: () => request('/api/camaras'),
  listarRetiradas: () => request('/api/camaras/retiradas'),
  crear: (camara) => request('/api/camaras', { method: 'POST', body: camara }),
  actualizar: (camaraId, camara) => request(`/api/camaras/${id(camaraId)}`, { method: 'PUT', body: camara }),
  retirar: (camaraId) => request(`/api/camaras/${id(camaraId)}`, { method: 'DELETE' }),
  restaurar: (camaraId) => request(`/api/camaras/${id(camaraId)}/restaurar`, { method: 'POST' }),
  historial: (camaraId) => request(`/api/camaras/${id(camaraId)}/historial`),
}

export const tecnicosApi = {
  listar: () => request('/api/tecnicos'),
  crear: (tecnico) => request('/api/tecnicos', { method: 'POST', body: tecnico }),
  actualizar: (tecnicoId, tecnico) => request(`/api/tecnicos/${id(tecnicoId)}`, { method: 'PUT', body: tecnico }),
  desactivar: (tecnicoId) => request(`/api/tecnicos/${id(tecnicoId)}`, { method: 'DELETE' }),
  reactivar: (tecnicoId) => request(`/api/tecnicos/${id(tecnicoId)}/reactivar`, { method: 'POST' }),
}
