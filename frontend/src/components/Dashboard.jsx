import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { camarasApi } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'
import { formatTime } from '../utils/format.js'
import Sidebar from './Sidebar.jsx'
import StatsGrid from './StatsGrid.jsx'
import CameraTable from './CameraTable.jsx'
import RetiredCameras from './RetiredCameras.jsx'
import TechniciansSection from './TechniciansSection.jsx'
import CameraFormModal from './CameraFormModal.jsx'
import HistoryModal from './HistoryModal.jsx'

const REFRESH_MS = 60_000

export default function Dashboard() {
  const { session, isAdmin, logout } = useAuth()
  const [cameras, setCameras] = useState([])
  const [retired, setRetired] = useState([])
  const [loaded, setLoaded] = useState(false)
  const [loading, setLoading] = useState(false)
  const [connected, setConnected] = useState(null)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState(null)
  const [technicianCount, setTechnicianCount] = useState(0)
  const [editing, setEditing] = useState(null) // null = cerrado, {} = nueva, cámara = editar
  const [historyCamera, setHistoryCamera] = useState(null)
  const [retiredOpen, setRetiredOpen] = useState(false)
  const retiredRef = useRef(null)

  const refresh = useCallback(async () => {
    setError('')
    setLoading(true)
    try {
      const [activas, retiradas] = await Promise.all([camarasApi.listar(), camarasApi.listarRetiradas()])
      if (!Array.isArray(activas) || !Array.isArray(retiradas)) {
        throw new Error('La API devolvió un formato inesperado al cargar cámaras.')
      }
      setCameras(activas)
      setRetired(retiradas)
      setLoaded(true)
      setLastUpdated(new Date())
      setConnected(true)
    } catch (err) {
      setConnected(false)
      setError(`No se pudo actualizar el panel: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    // Carga inicial y luego cada minuto (el servidor verifica las cámaras cada minuto).
    // oxlint-disable-next-line react/set-state-in-effect
    refresh()
    const timer = setInterval(refresh, REFRESH_MS)
    return () => clearInterval(timer)
  }, [refresh])

  const duplicateIps = useMemo(() => {
    const counts = new Map()
    cameras.forEach((camera) => {
      const ip = String(camera.ip ?? '').trim()
      if (ip) counts.set(ip, (counts.get(ip) ?? 0) + 1)
    })
    return new Set([...counts].filter(([, count]) => count > 1).map(([ip]) => ip))
  }, [cameras])

  async function retireCamera(camera) {
    if (!window.confirm(`¿Retirar la cámara "${camera.nombre}"? Dejará de aparecer y de monitorearse, pero su historial se conservará.`)) {
      return
    }
    setError('')
    try {
      await camarasApi.retirar(camera.id)
      await refresh()
    } catch (err) {
      setError(`No se pudo retirar la cámara: ${err.message}`)
    }
  }

  async function restoreCamera(camera) {
    setError('')
    try {
      await camarasApi.restaurar(camera.id)
      await refresh()
    } catch (err) {
      setError(`No se pudo restaurar la cámara: ${err.message}`)
    }
  }

  function showRetired() {
    setRetiredOpen(true)
    retiredRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div className="app-shell">
      <Sidebar
        cameraCount={cameras.length}
        retiredCount={retired.length}
        technicianCount={isAdmin ? technicianCount : null}
        connected={connected}
        onShowRetired={showRetired}
      />

      <main className="main-content" id="dashboard">
        <header className="topbar">
          <div className="breadcrumb">Monitoreo <span>/</span> <strong>Panel general</strong></div>
          <div className="topbar-actions">
            <span className="session-identity">{session.email} · {session.rol}</span>
            <span className="last-updated">
              {lastUpdated ? `Actualizado ${formatTime(lastUpdated)}` : 'Sin actualizar'}
            </span>
            <button className="button button-secondary" id="refresh-button" type="button"
              onClick={refresh} disabled={loading}>
              <span className="refresh-icon">↻</span> Actualizar
            </button>
            {isAdmin && (
              <button className="button button-primary" type="button" onClick={() => setEditing({})}>＋ Nueva cámara</button>
            )}
            <button className="button button-secondary" type="button" onClick={() => logout()}>Salir</button>
          </div>
        </header>

        <section className="page-heading">
          <div>
            <p className="eyebrow">VISTA GENERAL</p>
            <h1>Monitoreo de cámaras</h1>
            <p className="page-description">Estado de conexión e identificación de dispositivos en tu red.</p>
          </div>
        </section>

        {error && <div className="error-banner" role="alert">{error}</div>}

        <StatsGrid cameras={loaded ? cameras : null} duplicateIpCount={duplicateIps.size} />

        <CameraTable
          cameras={cameras}
          loaded={loaded}
          failed={!loaded && connected === false}
          duplicateIps={duplicateIps}
          isAdmin={isAdmin}
          onAdd={() => setEditing({})}
          onEdit={setEditing}
          onDelete={retireCamera}
          onHistory={setHistoryCamera}
        />

        <RetiredCameras
          ref={retiredRef}
          cameras={retired}
          open={retiredOpen}
          onToggle={setRetiredOpen}
          isAdmin={isAdmin}
          onRestore={restoreCamera}
          onHistory={setHistoryCamera}
        />

        {isAdmin && <TechniciansSection onActiveCountChange={setTechnicianCount} />}

        <footer className="page-footer">EstadoCámaras <span>•</span> Panel de monitoreo de red</footer>
      </main>

      <CameraFormModal
        camera={editing}
        onClose={() => setEditing(null)}
        onSaved={() => {
          setEditing(null)
          refresh()
        }}
      />
      <HistoryModal camera={historyCamera} onClose={() => setHistoryCamera(null)} />
    </div>
  )
}
