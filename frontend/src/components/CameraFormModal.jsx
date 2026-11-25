import { useState } from 'react'
import { camarasApi } from '../api/client.js'
import Modal, { ModalHeading } from './Modal.jsx'

const IP_PATTERN = '((25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])'
const MAC_PATTERN = '([0-9A-Fa-f]{2}[:\\-]){5}[0-9A-Fa-f]{2}'

// camera: null = cerrado, {} = nueva cámara, objeto con id = edición
export default function CameraFormModal({ camera, onClose, onSaved }) {
  return (
    <Modal open={camera !== null} onClose={onClose}>
      {camera && <CameraForm key={camera.id ?? 'new'} camera={camera} onClose={onClose} onSaved={onSaved} />}
    </Modal>
  )
}

function CameraForm({ camera, onClose, onSaved }) {
  const editing = camera.id != null
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setError('')
    const form = new FormData(event.currentTarget)
    const text = (name) => String(form.get(name) ?? '').trim()
    const payload = {
      nombre: text('nombre'),
      ip: text('ip'),
      puerto: text('puerto') ? Number(text('puerto')) : null,
      ubicacion: text('ubicacion') || null,
      macEsperada: text('macEsperada') || null,
    }
    setSaving(true)
    try {
      if (editing) await camarasApi.actualizar(camera.id, payload)
      else await camarasApi.crear(payload)
      onSaved()
    } catch (err) {
      setError(`No se pudo guardar la cámara: ${err.message}`)
      setSaving(false)
    }
  }

  return (
    <form className="modal-form" onSubmit={submit}>
      <ModalHeading eyebrow="CONFIGURACIÓN" title={editing ? 'Editar cámara' : 'Nueva cámara'} onClose={onClose} />
      <p className="modal-description">Completa los datos de conexión y la MAC autorizada del dispositivo.</p>
      <div className="form-grid">
        <label className="form-field full-width"><span>Nombre de la cámara <span className="required">*</span></span>
          <input name="nombre" type="text" maxLength={100} placeholder="Ej. Entrada principal" required
            defaultValue={camera.nombre ?? ''} />
        </label>
        <label className="form-field"><span>Dirección IP <span className="required">*</span></span>
          <input name="ip" type="text" inputMode="decimal" maxLength={15} pattern={IP_PATTERN}
            title="Ingresa una dirección IPv4 válida, por ejemplo 192.168.1.20"
            placeholder="192.168.1.20" required defaultValue={camera.ip ?? ''} />
        </label>
        <label className="form-field">Puerto TCP
          <input name="puerto" type="number" min={1} max={65535} placeholder="80" defaultValue={camera.puerto ?? ''} />
        </label>
        <label className="form-field full-width">Ubicación
          <input name="ubicacion" type="text" maxLength={150} placeholder="Ej. Pasillo norte"
            defaultValue={camera.ubicacion ?? ''} />
        </label>
        <label className="form-field full-width">MAC esperada
          <input name="macEsperada" type="text" maxLength={17} pattern={MAC_PATTERN}
            title="Usa una MAC válida, por ejemplo 00:1A:2B:3C:4D:5E"
            placeholder="00:1A:2B:3C:4D:5E" defaultValue={camera.macEsperada ?? ''} />
          <small>Opcional. Acepta formatos con dos puntos o guiones.</small>
        </label>
      </div>
      {error && <div className="form-error" role="alert">{error}</div>}
      <div className="modal-actions">
        <button className="button button-secondary" type="button" onClick={onClose}>Cancelar</button>
        <button className="button button-primary" type="submit" disabled={saving}>
          {editing ? 'Guardar cambios' : 'Guardar cámara'}
        </button>
      </div>
    </form>
  )
}
