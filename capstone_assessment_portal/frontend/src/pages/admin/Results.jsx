import { useState, useEffect, useMemo } from "react"

import { getQuizzesAPI }                                   from "../../api/quiz.api"
import { getAllResultsAdminAPI, getResultsByQuizAdminAPI }  from "../../api/result.api"

import PageHeader  from "../../components/common/PageHeader"
import Table       from "../../components/common/Table"
import Pagination  from "../../components/common/Pagination"
import Input       from "../../components/common/Input"
import Badge       from "../../components/common/Badge"
import EmptyState  from "../../components/common/EmptyState"

import { formatDateTime } from "../../utils/helpers"

/* Rows shown per page across the results table */
const PAGE_SIZE = 10

const AdminResults = () => {

  const [quizzes, setQuizzes]         = useState([])
  const [results, setResults]         = useState([])
  const [selectedQuiz, setSelectedQuiz] = useState("")
  const [loading, setLoading]         = useState(true)
  const [search, setSearch]           = useState("")
  const [page, setPage]               = useState(1)

  const loadAll = async () => {
    try {
      setLoading(true)
      const data = await getAllResultsAdminAPI()
      setResults(data.results || [])
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  const loadByQuiz = async (quizId) => {
    try {
      setLoading(true)
      const data = await getResultsByQuizAdminAPI(quizId)
      setResults(data.results || [])
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const init = async () => {
      try {
        const data = await getQuizzesAPI()
        setQuizzes(data.quizzes || [])
      } catch { /* silently fail */ }
      loadAll()
    }
    init()
  }, [])

  const handleQuizChange = (e) => {
    const id = e.target.value
    setSelectedQuiz(id)
    setSearch("")
    if (id) loadByQuiz(id)
    else    loadAll()
  }

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return results
    return results.filter((r) =>
      r.quiz_title.toLowerCase().includes(q) ||
      (r.username || "").toLowerCase().includes(q)
    )
  }, [search, results])

  /* Reset to page 1 whenever search or the quiz filter changes */
  useEffect(() => { setPage(1) }, [search, selectedQuiz])

  const paginated = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE
    return filtered.slice(start, start + PAGE_SIZE)
  }, [filtered, page])

  const columns = [
    {
      key:    "username",
      label:  "Student",
      render: (value) => (
        <span className="text-sm font-medium text-slate-800">
          {value || "Unknown"}
        </span>
      )
    },
    {
      key:   "quiz_title",
      label: "Quiz"
    },
    {
      key:    "score",
      label:  "Score",
      render: (value, row) => `${value} / ${row.total_marks}`
    },
    {
      key:    "percentage",
      label:  "Percentage",
      render: (value) => `${value}%`
    },
    {
      key:    "passed",
      label:  "Result",
      render: (value) => (
        <Badge
          label={value ? "Passed" : "Failed"}
          variant={value ? "success" : "error"}
        />
      )
    },
    {
      key:    "submitted_at",
      label:  "Submitted At",
      render: (value) => formatDateTime(value)
    }
  ]

  return (
    <div>

      <PageHeader
        title="Results"
        subtitle="All student quiz submissions"
      />

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">

        <div className="w-full sm:max-w-xs">
          <select
            value={selectedQuiz}
            onChange={handleQuizChange}
            className="w-full px-4 py-2.5 text-sm rounded-lg border border-slate-200 bg-white outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition"
          >
            <option value="">All Quizzes</option>
            {quizzes.map((q) => (
              <option key={q.id} value={q.id}>{q.title}</option>
            ))}
          </select>
        </div>

        <div className="w-full sm:max-w-xs">
          <Input
            placeholder="Search by student or quiz..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

      </div>

      {!loading && filtered.length === 0 && search ? (
        <EmptyState
          title="No results found"
          description={`No results match "${search}"`}
        />
      ) : (
        <>
          <Table
            columns={columns}
            rows={paginated}
            loading={loading}
            emptyMessage="No results available"
          />
          <Pagination
            currentPage={page}
            totalItems={filtered.length}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </>
      )}

    </div>
  )
}

export default AdminResults