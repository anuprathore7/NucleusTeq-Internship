/**
 * StatCard
 *
 * Displays a single statistic with a label, value and optional icon.
 * Used in both admin and student dashboards.
 */

const StatCard = ({ label, value, icon, color = "blue" }) => {

  const colorMap = {
    blue:   "bg-blue-50 text-blue-600",
    green:  "bg-green-50 text-green-600",
    purple: "bg-purple-50 text-purple-600",
    orange: "bg-orange-50 text-orange-600",
    red:    "bg-red-50 text-red-600"
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-6 flex items-center gap-4">

      {/* Icon */}
      {icon && (
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${colorMap[color]}`}>
          {icon}
        </div>
      )}

      {/* Content */}
      <div>
        <p className="text-sm text-slate-500 font-medium">{label}</p>
        <p className="text-2xl font-bold text-slate-900 mt-0.5">{value}</p>
      </div>

    </div>
  )
}

export default StatCard