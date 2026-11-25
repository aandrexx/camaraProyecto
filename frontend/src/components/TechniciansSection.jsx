import { useCallback, useEffect, useState } from 'react'
import { tecnicosApi } from '../api/client.js'
import { matches, plural } from '../utils/format.js'
import Modal, { ModalHeading } from './Modal.jsx'

export default function TechniciansSection({ onActiveCountChange }) {
  const [technicians, setTechnicians] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [editing, setEditing] = useState(null) // null = cerrado, {} = nuevo, técnico = editar
  const [busyId, setBusyId] = useState(null)

  const load = useCallback(async () => {
    setError('')
    setLoading(true)
    try {
      const data = await tecnicosApi.listar()
      if (!Array.isArray(data)) throw new Error('La API devolvió un formato inesperado al cargar técnicos.')
      setTechnicians(data)
    } catch (err) {
      setError(`No se pudieron cargar los técnicos: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    // oxlint-disable-next-line react/set-state-in-effect
    load()
  }, [load])

  useEffect(() => {
    onActiveCountChange(technicians.filter((technician) => technician.activo).length)
  }, [technicians, onActiveCountChange])

  async function changeStatus(technician) {
    const activating = !technician.activo
    const confirmation = activating
      ? `¿Reactivar a ${technician.nombre}? Volverá a recibir notificaciones.`
      : `¿Desactivar a ${technician.nombre}? Dejará de recibir notificaciones.`
    if (!window.confirm(confirmation)) return

    setBusyId(technician.id)
    setError('')
    try {
      if (activating) await tecnicosApi.reactivar(technician.id)
      else await tecnicosApi.desactivar(technician.id)
      await load()
    } catch (err) {
      setError(`No se pudo ${activating ? 'reactivar' : 'desactivar'} el técnico: ${err.message}`)
    } finally {
      setBusyId(null)
    }
  }

  const filtered = technicians.filter((technician) =>
    matches(query, [technician.nombre, technician.telefono])
    && (!statusFilter || String(technician.activo) === statusFilter))
  const hasFilter = query.trim().length > 0 || statusFilter.length > 0

  return (
    <section className="camera-section technicians-section" id="technicians-section">
      <div className="section-heading">
        <div>
          <h2>Técnicos</h2>
          <p>Administra los contactos que reciben las notificaciones de WhatsApp.</p>
        </div>
        <div className="section-actions">
          <label className="search-box">
            <span aria-hidden="true">⌕</span>
            <input type="search" placeholder="Buscar por nombre o teléfono" aria-label="Buscar técnicos"
              value={query} onChange={(event) => setQuery(event.target.value)} />
          </label>
          <label className="filter-box">
            <span className="sr-only">Filtrar técnicos por estado</span>
            <select aria-label="Filtrar técnicos por estado" value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}>
              <option value="">Todos</option>
              <option value="true">Activos</option>
              <option value="false">Inactivos</option>
            </select>
          </label>
          <button className="button button-secondary" id="refresh-technicians-button" type="button"
            aria-label="Actualizar técnicos" onClick={load} disabled={loading}>↻</button>
          <button className="button button-primary" id="add-technician-button" type="button"
            onClick={() => setEditing({})}>＋ Nuevo técnico</button>
        </div>
      </div>

      {error && <div className="error-banner technicians-error" role="alert">{error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>NOMBRE</th>
              <th>TELÉFONO</th>
              <th>ESTADO</th>
              <th><span className="sr-only">Acciones</span></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((technician) => (
              <tr key={technician.id}>
                <td><div className="camera-name"><strong>{technician.nombre}</strong></div></td>
                <td><span className="ip-address">{technician.telefono}</span></td>
                <td>
                  <span className={`status-badge ${technician.activo ? 'technician-active' : 'technician-inactive'}`}>
                    {technician.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td>
                  <div className="row-actions">
                    <button className="row-action" type="button" title="Editar técnico"
                      aria-label={`Editar ${technician.nombre}`} onClick={() => setEditing(technician)}>✎</button>
                    {technician.activo ? (
                      <button className="row-action row-action-danger" type="button" title="Desactivar técnico"
                        aria-label={`Desactivar ${technician.nombre}`} disabled={busyId === technician.id}
                        onClick={() => changeStatus(technician)}>×</button>
                    ) : (
                      <button className="button button-restore" type="button" disabled={busyId === technician.id}
                        onClick={() => changeStatus(technician)}>Reactivar</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && filtered.length === 0 && (
          <div className="empty-state">
            <div className="empty-illustration">♙</div>
            <h3>{hasFilter ? 'No se encontraron técnicos' : 'Todavía no hay técnicos'}</h3>
            <p>
              {hasFilter
                ? 'Prueba con otro nombre, teléfono o filtro.'
                : 'Agrega técnicos para que reciban las notificaciones de WhatsApp.'}
            </p>
            {!hasFilter && (
              <button className="button button-primary" type="button" onClick={() => setEditing({})}>＋ Agregar técnico</button>
            )}
          </div>
        )}
      </div>
      <footer className="table-footer">
        <span>{plural(filtered.length, 'técnico', 'técnicos')}</span>
        <span>El teléfono debe incluir código de país, solo dígitos.</span>
      </footer>

      <Modal open={editing !== null} onClose={() => setEditing(null)} className="compact-modal">
        {editing && (
          <TechnicianForm
            key={editing.id ?? 'new'}
            technician={editing}
            onClose={() => setEditing(null)}
            onSaved={() => {
              setEditing(null)
              load()
            }}
          />
        )}
      </Modal>
    </section>
  )
}

function TechnicianForm({ technician, onClose, onSaved }) {
  const editing = technician.id != null
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setError('')
    const form = new FormData(event.currentTarget)
    const payload = {
      nombre: String(form.get('nombre')).trim(),
      telefono: String(form.get('telefono')).trim(),
    }
    setSaving(true)
    try {
      if (editing) await tecnicosApi.actualizar(technician.id, payload)
      else await tecnicosApi.crear(payload)
      onSaved()
    } catch (err) {
      setError(`No se pudo guardar el técnico: ${err.message}`)
      setSaving(false)
    }
  }

  return (
    <form className="modal-form" onSubmit={submit}>
      <ModalHeading eyebrow="NOTIFICACIONES" title={editing ? 'Editar técnico' : 'Nuevo técnico'} onClose={onClose} />
      <p className="modal-description">El contacto recibirá avisos de WhatsApp sobre el estado de las cámaras.</p>
      <label className="form-field"><span>Nombre <span className="required">*</span></span>
        <input name="nombre" type="text" maxLength={100} autoComplete="name" placeholder="Ej. Ana Pérez" required
          defaultValue={technician.nombre ?? ''} />
      </label>
      <label className="form-field"><span>Teléfono con código de país <span className="required">*</span></span>
        <input name="telefono" type="tel" inputMode="numeric" minLength={8} maxLength={15} pattern="[0-9]{8,15}"
          title="Ingresa de 8 a 15 dígitos, incluido el código de país, sin + ni espacios"
          placeholder="5215512345678" required defaultValue={technician.telefono ?? ''} />
        <small>De 8 a 15 dígitos. Incluye el código de país; no uses + ni espacios.</small>
      </label>
      {error && <div className="form-error" role="alert">{error}</div>}
      <div className="modal-actions">
        <button className="button button-secondary" type="button" onClick={onClose}>Cancelar</button>
        <button className="button button-primary" type="submit" disabled={saving}>
          {editing ? 'Guardar cambios' : 'Guardar técnico'}
        </button>
      </div>
    </form>
  )
}
