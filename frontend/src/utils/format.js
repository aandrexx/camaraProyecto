export function formatDate(value) {
  if (!value) return 'Sin registro'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'Sin registro'
  return new Intl.DateTimeFormat('es-MX', { dateStyle: 'short', timeStyle: 'short' }).format(date)
}

export function formatTime(date) {
  return new Intl.DateTimeFormat('es-MX', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  }).format(date)
}

const STATUS = {
  ACTIVA: { text: 'Activa', className: 'status-active' },
  INACTIVA: { text: 'Inactiva', className: 'status-inactive' },
  DEGRADADA: { text: 'Degradada', className: 'status-degraded' },
  ALERTA: { text: 'Alerta', className: 'status-alert' },
}

export function statusDetails(status) {
  return STATUS[status] ?? { text: 'Desconocida', className: 'status-unknown' }
}

export function plural(count, singular, pluralForm) {
  return `${count} ${count === 1 ? singular : pluralForm}`
}

export function matches(query, values) {
  const q = query.trim().toLocaleLowerCase('es')
  return values.some((value) => String(value ?? '').toLocaleLowerCase('es').includes(q))
}
