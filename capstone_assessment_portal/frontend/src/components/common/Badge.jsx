/**
 * Badge
 *
 * Small colored label for status, role, difficulty etc.
 * Used in tables and cards.
 */

const variants = {
  success: "bg-green-100 text-green-700",
  error:   "bg-red-100 text-red-700",
  warning: "bg-amber-100 text-amber-700",
  info:    "bg-blue-100 text-blue-700",
  default: "bg-slate-100 text-slate-700",
  purple:  "bg-purple-100 text-purple-700"
}

const Badge = ({ label, variant = "default" }) => {
  return (
    <span className={`
      inline-flex items-center px-2.5 py-0.5
      text-xs font-medium rounded-full
      ${variants[variant]}
    `}>
      {label}
    </span>
  )
}

export default Badge