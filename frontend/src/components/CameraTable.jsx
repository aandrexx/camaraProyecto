import { useState } from 'react'
import { formatDate, matches, plural, statusDetails } from '../utils/format.js'

export function CameraName({ camera }) {
  return (
    <div className="camera-name">
      <strong>{camera.nombre}</strong>
      <span>{camera.ubicacion || 'Ubicación no definida'}</span>
    </div>
  )
}

export function IpAddress({ camera }) {
  return <span className="ip-address">{camera.ip || '—'}{camera.puerto ? `:${camera.puerto}` : ''}</span>
}

function Mac({ value, emptyText }) {
  return value
    ? <span className="mac-address">{value}</span>
    : <span className="mac-address empty">{emptyText}</span>
}

const STATUS_OPTIONS = [
  ['', 'Todos los estados'],
  ['ACTIVA', 'Activas'],
  ['INACTIVA', 'Inactivas'],
  ['DEGRADADA', 'Degradadas'],
  ['ALERTA', 'Alertas'],
  ['DESCONOCIDO', 'Desconocidas'],
]

export default function CameraTable({
  cameras, loaded, failed, duplicateIps, isAdmin, onAdd, onEdit, onDelete, onHistory,
}) {
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  const filtered = cameras.filter((camera) =>
    matches(query, [camera.nombre, camera.ip, camera.ubicacion, camera.macEsperada, camera.ultimaMacDetectada])
    && (!statusFilter || camera.estado === statusFilter))

  const hasSearch = query.trim().length > 0
  const hasFilter = statusFilter.length > 0
  const emptyTitle = hasSearch ? 'No hay resultados'
    : hasFilter ? 'No hay cámaras con ese estado'
      : cameras.length === 0 ? 'Todavía no hay cámaras' : 'No se encontraron cámaras'
  const emptyDescription = hasSearch || hasFilter
    ? 'Prueba con otro nombre, dirección IP o ubicación.'
    : 'Registra una cámara para comenzar a monitorear la red.'

  return (
    <section className="camera-section" id="cameras">
      <div className="section-heading">
        <div>
          <h2>Dispositivos</h2>
          <p>Consulta el estado y la identidad de cada cámara.</p>
        </div>
        <label className="search-box">
          <span aria-hidden="true">⌕</span>
          <input type="search" placeholder="Buscar por nombre, IP o ubicación" aria-label="Buscar cámaras"
            value={query} onChange={(event) => setQuery(event.target.value)} />
        </label>
        <label className="filter-box">
          <span className="sr-only">Filtrar por estado</span>
          <select aria-label="Filtrar cámaras por estado" value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}>
            {STATUS_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </label>
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>CÁMARA</th>
              <th>DIRECCIÓN IP</th>
              <th>ESTADO</th>
              <th>MAC ESPERADA</th>
              <th>MAC DETECTADA</th>
              <th>ÚLTIMA REVISIÓN</th>
              <th><span className="sr-only">Acciones</span></th>
            </tr>
          </thead>
          <tbody>
            {!loaded && (
              <tr>
                <td colSpan={7} className="loading-cell">
                  {failed
                    ? 'No fue posible cargar los dispositivos.'
                    : <><span className="loading-spinner"></span> Cargando dispositivos...</>}
                </td>
              </tr>
            )}
            {filtered.map((camera) => {
              const status = statusDetails(camera.estado)
              return (
                <tr key={camera.id}>
                  <td><CameraName camera={camera} /></td>
                  <td>
                    <IpAddress camera={camera} />
                    {duplicateIps.has(String(camera.ip ?? '').trim()) && <span className="ip-warning">⚠ IP repetida</span>}
                  </td>
                  <td><span className={`status-badge ${status.className}`}>{status.text}</span></td>
                  <td><Mac value={camera.macEsperada} emptyText="No configurada" /></td>
                  <td>
                    <Mac value={camera.ultimaMacDetectada} emptyText="Sin detectar" />
                    {camera.macCoincide === false && <span className="mac-warning">⚠ No coincide</span>}
                  </td>
                  <td><span className="timestamp">{formatDate(camera.ultimaVerificacion)}</span></td>
                  <td>
                    <div className="row-actions">
                      <button className="row-action" type="button" title="Ver historial" aria-label="Ver historial"
                        onClick={() => onHistory(camera)}>◷</button>
                      {isAdmin && (
                        <>
                          <button className="row-action" type="button" title="Editar cámara" aria-label="Editar cámara"
                            onClick={() => onEdit(camera)}>✎</button>
                          <button className="row-action row-action-danger" type="button" title="Retirar cámara"
                            aria-label="Retirar cámara" onClick={() => onDelete(camera)}>×</button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>

        {loaded && filtered.length === 0 && (
          <div className="empty-state">
            <div className="empty-illustration">◉</div>
            <h3>{emptyTitle}</h3>
            <p>{emptyDescription}</p>
            {!hasSearch && !hasFilter && isAdmin && (
              <button className="button button-primary" type="button" onClick={onAdd}>＋ Agregar cámara</button>
            )}
          </div>
        )}
      </div>
      <footer className="table-footer">
        <span>{plural(filtered.length, 'dispositivo', 'dispositivos')}</span>
        <span>La verificación se actualiza automáticamente desde el servidor.</span>
      </footer>
    </section>
  )
}
