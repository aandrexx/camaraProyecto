import { useState } from 'react'
import { formatDate, plural } from '../utils/format.js'
import { CameraName, IpAddress } from './CameraTable.jsx'

export default function RetiredCameras({ ref, cameras, open, onToggle, isAdmin, onRestore, onHistory }) {
  const [restoringId, setRestoringId] = useState(null)
  const count = cameras.length

  async function restore(camera) {
    setRestoringId(camera.id)
    try {
      await onRestore(camera)
    } finally {
      setRestoringId(null)
    }
  }

  return (
    <details
      ref={ref}
      id="retired-section"
      className={`camera-section retired-section ${count > 0 ? 'has-retired-cameras' : ''}`.trim()}
      open={open}
      onToggle={(event) => onToggle(event.currentTarget.open)}
    >
      <summary className="retired-summary">
        <span><strong>Cámaras retiradas</strong><small>Ya no se monitorean. Su historial se conserva.</small></span>
        <span className="retired-count">{count}</span>
      </summary>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>CÁMARA</th>
              <th>DIRECCIÓN IP</th>
              <th>ÚLTIMA REVISIÓN</th>
              <th><span className="sr-only">Acciones</span></th>
            </tr>
          </thead>
          <tbody>
            {cameras.map((camera) => (
              <tr key={camera.id}>
                <td><CameraName camera={camera} /></td>
                <td><IpAddress camera={camera} /></td>
                <td><span className="timestamp">{formatDate(camera.ultimaVerificacion)}</span></td>
                <td>
                  <div className="row-actions">
                    <button className="row-action" type="button" title="Ver historial" aria-label="Ver historial"
                      onClick={() => onHistory(camera)}>◷</button>
                    {isAdmin && (
                      <button className="button button-restore" type="button"
                        disabled={restoringId === camera.id} onClick={() => restore(camera)}>Restaurar</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {count === 0 && (
          <div className="empty-state">
            <div className="empty-illustration">↶</div>
            <h3>No hay cámaras retiradas</h3>
            <p>Las cámaras que retires aparecerán aquí y podrás restaurarlas.</p>
          </div>
        )}
      </div>
      <footer className="table-footer">
        <span>{plural(count, 'cámara', 'cámaras')}</span>
        <span>Restaurar una cámara la vuelve a incluir en el monitoreo.</span>
      </footer>
    </details>
  )
}
