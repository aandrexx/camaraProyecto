import { useEffect, useState } from 'react'
import { camarasApi } from '../api/client.js'
import { formatDate } from '../utils/format.js'
import Modal, { ModalHeading } from './Modal.jsx'

export default function HistoryModal({ camera, onClose }) {
  return (
    <Modal open={camera !== null} onClose={onClose} className="history-modal">
      {camera && <History key={camera.id} camera={camera} onClose={onClose} />}
    </Modal>
  )
}

function History({ camera, onClose }) {
  const [events, setEvents] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    camarasApi.historial(camera.id)
      .then((data) => {
        if (!Array.isArray(data)) throw new Error('La API devolvió un formato inesperado.')
        if (!cancelled) setEvents(data)
      })
      .catch((err) => {
        if (!cancelled) setError(`No se pudo cargar el historial: ${err.message}`)
      })
    return () => {
      cancelled = true
    }
  }, [camera.id])

  return (
    <div className="modal-form">
      <ModalHeading eyebrow="ACTIVIDAD" title={`Historial · ${camera.nombre}`} onClose={onClose} />
      <div className="history-content">
        {error}
        {!error && events === null && <><span className="loading-spinner"></span> Cargando historial...</>}
        {events?.length === 0 && (
          <div className="history-empty">Esta cámara todavía no tiene eventos registrados.</div>
        )}
        {events?.map((event) => {
          const macAlert = event.tipoEvento === 'ALERTA_MAC'
          const title = macAlert
            ? 'Alerta de identidad MAC'
            : `Cambio de estado: ${event.estadoAnterior || 'Sin estado'} → ${event.estadoNuevo || 'Sin estado'}`
          return (
            <article className="history-entry" key={event.id}>
              <span className="history-marker">{macAlert ? '⚠' : '↻'}</span>
              <div>
                <h3>{title}</h3>
                <p>{formatDate(event.fechaCambio)}</p>
                <p className="history-reason">{event.motivo || 'Sin detalle registrado para este evento.'}</p>
              </div>
            </article>
          )
        })}
      </div>
    </div>
  )
}
