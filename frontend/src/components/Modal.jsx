import { useEffect, useRef } from 'react'

// Envoltorio de <dialog> nativo: abre con showModal() y cierra con Esc,
// con un clic fuera del contenido o con el botón ×.
export default function Modal({ open, onClose, className = '', children }) {
  const ref = useRef(null)

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      className={`modal ${className}`.trim()}
      onClose={onClose}
      onClick={(event) => {
        if (event.target === ref.current) onClose()
      }}
    >
      {open && children}
    </dialog>
  )
}

export function ModalHeading({ eyebrow, title, onClose }) {
  return (
    <div className="modal-heading">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
      </div>
      <button className="icon-button close-dialog" type="button" aria-label="Cerrar" onClick={onClose}>×</button>
    </div>
  )
}
