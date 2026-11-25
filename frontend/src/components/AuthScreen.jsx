import { useState } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import Brand from './Brand.jsx'

export default function AuthScreen() {
  const { login, register, notice } = useAuth()
  const [mode, setMode] = useState('login')
  const [message, setMessage] = useState(notice)
  const [busy, setBusy] = useState(false)
  const [loginEmail, setLoginEmail] = useState('')

  const registering = mode === 'register'

  function changeMode(next) {
    setMode(next)
    setMessage('')
  }

  async function submitLogin(event) {
    event.preventDefault()
    setMessage('')
    const form = new FormData(event.currentTarget)
    const email = loginEmail.trim()
    setBusy(true)
    try {
      await login(email, String(form.get('password')))
    } catch (error) {
      setMessage(`No se pudo iniciar sesión: ${error.message}`)
      setBusy(false)
    }
  }

  async function submitRegister(event) {
    event.preventDefault()
    setMessage('')
    const form = new FormData(event.currentTarget)
    const email = String(form.get('email')).trim()
    const password = String(form.get('password'))
    if (password !== form.get('confirmPassword')) {
      setMessage('Las contraseñas no coinciden.')
      return
    }

    setBusy(true)
    let accountCreated = false
    try {
      await register(email, password)
      accountCreated = true
      await login(email, password)
    } catch (error) {
      if (accountCreated) {
        setMode('login')
        setLoginEmail(email)
        setMessage(`La cuenta se creó, pero no se pudo iniciar sesión: ${error.message}`)
      } else {
        setMessage(`No se pudo crear la cuenta: ${error.message}`)
      }
      setBusy(false)
    }
  }

  return (
    <section className="auth-screen">
      <Brand className="auth-brand" />
      <div className="auth-card">
        <p className="eyebrow">ACCESO SEGURO</p>
        <h1>{registering ? 'Crea tu cuenta' : 'Inicia sesión'}</h1>
        <p className="auth-description">
          {registering
            ? 'Regístrate para consultar el panel de monitoreo.'
            : 'Accede al panel de monitoreo de cámaras.'}
        </p>

        <div className="auth-tabs" role="tablist" aria-label="Acceso a la cuenta">
          <button className={`auth-tab ${registering ? '' : 'active'}`} type="button" role="tab"
            aria-selected={!registering} onClick={() => changeMode('login')}>Iniciar sesión</button>
          <button className={`auth-tab ${registering ? 'active' : ''}`} type="button" role="tab"
            aria-selected={registering} onClick={() => changeMode('register')}>Crear cuenta</button>
        </div>

        {registering ? (
          <form onSubmit={submitRegister} key="register">
            <label className="form-field">Correo electrónico
              <input name="email" type="email" autoComplete="email" maxLength={160} placeholder="tu@correo.com" required />
            </label>
            <label className="form-field">Contraseña
              <input name="password" type="password" autoComplete="new-password" required />
            </label>
            <label className="form-field">Confirmar contraseña
              <input name="confirmPassword" type="password" autoComplete="new-password" required />
            </label>
            <button className="button button-primary auth-submit" type="submit" disabled={busy}>Crear cuenta</button>
            <p className="auth-note">Las cuentas nuevas se crean con permisos de usuario.</p>
          </form>
        ) : (
          <form onSubmit={submitLogin} key="login">
            <label className="form-field">Correo electrónico
              <input name="email" type="email" autoComplete="username" maxLength={160} placeholder="tu@correo.com"
                required value={loginEmail} onChange={(event) => setLoginEmail(event.target.value)} />
            </label>
            <label className="form-field">Contraseña
              <input name="password" type="password" autoComplete="current-password" required />
            </label>
            <button className="button button-primary auth-submit" type="submit" disabled={busy}>Iniciar sesión</button>
          </form>
        )}

        {message && <div className="auth-message" role="alert">{message}</div>}
      </div>
      <p className="auth-footer">EstadoCámaras <span>•</span> Monitoreo IP / MAC</p>
    </section>
  )
}
