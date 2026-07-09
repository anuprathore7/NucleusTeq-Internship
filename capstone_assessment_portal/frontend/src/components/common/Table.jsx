/**
 * Table
 *
 * Reusable table component.
 * Accepts columns definition and rows data.
 * Used across admin pages for categories, quizzes, questions, results.
 *
 * columns: [{ key, label, render }]
 * rows:    array of data objects
 */

const Table = ({ columns, rows, loading, emptyMessage = "No data found" }) => {

  if (loading) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-12 text-center">
        <div className="w-8 h-8 border-[3px] border-slate-200 border-t-primary-600 rounded-full animate-spin mx-auto" />
        <p className="text-sm text-slate-400 mt-3">Loading...</p>
      </div>
    )
  }

  if (!rows || rows.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-12 text-center">
        <p className="text-sm text-slate-400">{emptyMessage}</p>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">

          <thead>
            <tr className="border-b border-slate-200 bg-slate-50">
              {columns.map((col) => (
                <th
                  key={col.key}
                  className="text-left px-6 py-3.5 text-xs font-semibold text-slate-500 uppercase tracking-wide"
                >
                  {col.label}
                </th>
              ))}
            </tr>
          </thead>

          <tbody className="divide-y divide-slate-100">
            {rows.map((row, rowIndex) => (
              <tr
                key={rowIndex}
                className="hover:bg-slate-50 transition-colors"
              >
                {columns.map((col) => (
                  <td
                    key={col.key}
                    className="px-6 py-4 text-slate-700"
                  >
                    {/* Pass index as third argument to render */}
                    {col.render
                      ? col.render(row[col.key], row, rowIndex)
                      : row[col.key] ?? "—"
                    }
                  </td>
                ))}
              </tr>
            ))}
          </tbody>

        </table>
      </div>
    </div>
  )
}

export default Table