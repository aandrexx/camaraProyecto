import Brand from './Brand.jsx'

export default function Sidebar({ cameraCount, retiredCount, technicianCount, connected, onShowRetired }) {
  const connection = connected === null
    ? { className: '', label: 'Conectando...' }
    : connected
      ? { className: 'online', label: 'API conectada' }
      : { className: 'offline', label: 'Sin conexión con la API' }

  return (
    <aside className="sidebar">
      <Brand />
      <div className="sidebar-label">ESPACIO DE TRABAJO</div>
      <div className="workspace-card">
        <span className="workspace-dot"></span>
        <span><strong>Operaciones</strong><small>Red de videovigilancia</small></span>
      </div>

      <div className="sidebar-label navigation-label">MONITOREO</div>
      <a className="nav-link active" href="#dashboard"><span className="nav-icon">▦</span> Panel general</a>
      <a className="nav-link" href="#cameras">
        <span className="nav-icon">◉</span> Cámaras <span className="nav-count">{cameraCount}</span>
      </a>
      <a className="nav-link" href="#retired-section" onClick={(event) => {
        event.preventDefault()
        onShowRetired()
      }}>
        <span className="nav-icon">↶</span> Retiradas <span className="nav-count">{retiredCount}</span>
      </a>
      {technicianCount !== null && (
        <a className="nav-link" href="#technicians-section">
          <span className="nav-icon">♙</span> Técnicos <span className="nav-count">{technicianCount}</span>
        </a>
      )}

      <div className="sidebar-bottom">
        <div className={`connection-indicator ${connection.className}`.trim()}>
          <span className="connection-dot"></span><span>{connection.label}</span>
        </div>
        <div className="sidebar-footnote">EstadoCámaras <span>•</span> Monitoreo IP / MAC</div>
      </div>
    </aside>
  )
}
