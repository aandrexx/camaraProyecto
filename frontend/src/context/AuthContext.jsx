import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { authApi, setAuthToken, setUnauthorizedHandler } from '../api/client.js'

const SESSION_KEY = 'estadoCamarasSession'
const ROLES = ['ADMIN', 'USER']

const AuthContext = createContext(null)

function readStoredSession() {
  const stored = sessionStorage.getItem(SESSION_KEY)
  if (!stored) return null
  try {
    const value = JSON.parse(stored)
    if (typeof value?.token === 'string' && value.token
      && typeof value.email === 'string'
      && ROLES.includes(value.rol)) {
      return value
    }
  } catch {
    // valor corrupto: se descarta abajo
  }
  sessionStorage.removeItem(SESSION_KEY)
  return null
}

function initialSession() {
  const session = readStoredSession()
  setAuthToken(session?.token ?? null)
  return session
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(initialSession)
  const [notice, setNotice] = useState('')

  const saveSession = useCallback((value) => {
    setAuthToken(value?.token ?? null)
    if (value) sessionStorage.setItem(SESSION_KEY, JSON.stringify(value))
    else sessionStorage.removeItem(SESSION_KEY)
    setSession(value)
  }, [])

  const logout = useCallback((message = '') => {
    saveSession(null)
    setNotice(message)
  }, [saveSession])

  useEffect(() => {
    setUnauthorizedHandler(() => logout('Tu sesión terminó. Inicia sesión nuevamente.'))
  }, [logout])

  const login = useCallback(async (email, password) => {
    const response = await authApi.login(email, password)
    if (typeof response?.token !== 'string' || !response.token || !ROLES.includes(response.rol)) {
      throw new Error('La respuesta de autenticación no contiene un token y rol válidos.')
    }
    setNotice('')
    saveSession({ token: response.token, rol: response.rol, email })
  }, [saveSession])

  const register = useCallback((email, password) => authApi.register(email, password), [])

  const value = useMemo(() => ({
    session,
    isAdmin: session?.rol === 'ADMIN',
    notice,
    login,
    register,
    logout,
  }), [session, notice, login, register, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// oxlint-disable-next-line react/only-export-components
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return context
}
