import { useEffect } from "react"

const Modal = ({ open, title, onClose, children, size = "md" }) => {

  useEffect(() => {
    if (!open) return
    const onKey = (e) => { if (e.key === "Escape") onClose() }
    window.addEventListener("keydown", onKey)
    return () => window.removeEventListener("keydown", onKey)
  }, [open, onClose])

  if (!open) return null

  const widthClass = size === "lg" ? "max-w-3xl" : "max-w-lg"

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center px-4 py-8">

      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <div className={`
        relative z-10 w-full ${widthClass} max-h-[85vh]
        bg-white rounded-2xl shadow-xl border border-slate-200
        flex flex-col overflow-hidden
      `}>

        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 shrink-0">
          <h2 className="text-base font-semibold text-slate-900">{title}</h2>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="px-6 py-5 overflow-y-auto">
          {children}
        </div>

      </div>

    </div>
  )
}

export default Modal