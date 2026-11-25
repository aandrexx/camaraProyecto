export default function Brand({ className = '' }) {
  return (
    <a className={`brand ${className}`.trim()} href="/" aria-label="EstadoCámaras inicio">
      <span className="brand-mark">EC</span>
      <span className="brand-copy">
        <strong>EstadoCámaras</strong>
        <small>Centro de monitoreo</small>
      </span>
    </a>
  )
}
