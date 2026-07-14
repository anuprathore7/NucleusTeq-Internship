/**
 * EmptyState
 *
 * Shown when a list or page has no data.
 * Accepts optional action button to guide user.
 */

const EmptyState = ({ title, description, action }) => {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4 text-center">

      {/* Icon */}
      <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
        <svg className="w-8 h-8 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
        </svg>
      </div>

      <h3 className="text-base font-semibold text-slate-800 mb-1">{title}</h3>

      {description && (
        <p className="text-sm text-slate-500 mb-6 max-w-sm">{description}</p>
      )}

      {action && action}

    </div>
  )
}

export default EmptyState