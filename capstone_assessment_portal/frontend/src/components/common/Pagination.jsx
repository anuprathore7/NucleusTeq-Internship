import { ChevronLeft, ChevronRight } from "lucide-react"

/* Builds a compact page-number list with ellipsis for large page counts */
const buildPageList = (currentPage, totalPages) => {
  const pages = []

  if (totalPages <= 7) {
    for (let i = 1; i <= totalPages; i++) pages.push(i)
    return pages
  }

  pages.push(1)

  if (currentPage > 3) pages.push("...")

  const start = Math.max(2, currentPage - 1)
  const end = Math.min(totalPages - 1, currentPage + 1)
  for (let i = start; i <= end; i++) pages.push(i)

  if (currentPage < totalPages - 2) pages.push("...")

  pages.push(totalPages)

  return pages
}

/* Reusable pagination bar — shows item range, prev/next, and page numbers */
const Pagination = ({ currentPage, totalItems, pageSize, onPageChange }) => {

  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize))

  if (totalItems === 0) return null

  const startItem = (currentPage - 1) * pageSize + 1
  const endItem = Math.min(currentPage * pageSize, totalItems)

  const goTo = (page) => {
    if (page < 1 || page > totalPages || page === currentPage) return
    onPageChange(page)
  }

  const pageList = buildPageList(currentPage, totalPages)

  return (
    <div className="
      flex flex-col sm:flex-row items-center justify-between gap-3
      bg-white rounded-xl border border-slate-200 px-6 py-4 mt-3
    ">

      <p className="text-xs text-slate-500 order-2 sm:order-1">
        Showing <span className="font-semibold text-slate-700">{startItem}-{endItem}</span> of{" "}
        <span className="font-semibold text-slate-700">{totalItems}</span>
      </p>

      <div className="flex items-center gap-1 order-1 sm:order-2">

        <button
          onClick={() => goTo(currentPage - 1)}
          disabled={currentPage === 1}
          className="
            w-8 h-8 flex items-center justify-center rounded-lg
            text-slate-500 hover:bg-slate-100 disabled:opacity-40
            disabled:hover:bg-transparent disabled:cursor-not-allowed transition-colors
          "
        >
          <ChevronLeft size={16} />
        </button>

        {pageList.map((p, i) =>
          p === "..." ? (
            <span key={`ellipsis-${i}`} className="w-8 h-8 flex items-center justify-center text-xs text-slate-400">
              &#8230;
            </span>
          ) : (
            <button
              key={p}
              onClick={() => goTo(p)}
              className={`
                w-8 h-8 flex items-center justify-center rounded-lg text-xs font-semibold transition-colors
                ${p === currentPage
                  ? "bg-primary-600 text-white"
                  : "text-slate-600 hover:bg-slate-100"
                }
              `}
            >
              {p}
            </button>
          )
        )}

        <button
          onClick={() => goTo(currentPage + 1)}
          disabled={currentPage === totalPages}
          className="
            w-8 h-8 flex items-center justify-center rounded-lg
            text-slate-500 hover:bg-slate-100 disabled:opacity-40
            disabled:hover:bg-transparent disabled:cursor-not-allowed transition-colors
          "
        >
          <ChevronRight size={16} />
        </button>

      </div>

    </div>
  )
}

export default Pagination