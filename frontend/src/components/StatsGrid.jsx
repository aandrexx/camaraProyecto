function StatCard({ label, icon, iconClass, value, caption, dotClass }) {
  return (
    <article className="stat-card">
      <div className="stat-top">
        <span className="stat-label">{label}</span>
        <span className={`stat-icon ${iconClass}`}>{icon}</span>
      </div>
      <div className="stat-value">{value ?? '—'}</div>
      <div className="stat-caption">
        {dotClass && <span className={`caption-dot ${dotClass}`}></span>} {caption}
      </div>
    </article>
  )
}

// cameras === null mientras no se haya cargado nada todavía.
export default function StatsGrid({ cameras, duplicateIpCount }) {
  const count = (predicate) => (cameras ? cameras.filter(predicate).length : null)

  return (
    <section className="stats-grid" aria-label="Resumen de cámaras">
      <StatCard label="Total de cámaras" icon="▦" iconClass="icon-neutral"
        value={cameras?.length} caption="Registradas para monitoreo" />
      <StatCard label="Activas" icon="✓" iconClass="icon-green" dotClass="green-dot"
        value={count((c) => c.estado === 'ACTIVA')} caption="Conectadas ahora" />
      <StatCard label="Inactivas" icon="!" iconClass="icon-red" dotClass="red-dot"
        value={count((c) => c.estado === 'INACTIVA')} caption="Sin conexión" />
      <StatCard label="Alertas de MAC" icon="⌁" iconClass="icon-amber" dotClass="amber-dot"
        value={count((c) => c.macCoincide === false)} caption="Identidad no coincide" />
      <StatCard label="IPs duplicadas" icon="↔" iconClass="icon-amber" dotClass="amber-dot"
        value={cameras ? duplicateIpCount : null} caption="Repetidas entre registros" />
    </section>
  )
}
