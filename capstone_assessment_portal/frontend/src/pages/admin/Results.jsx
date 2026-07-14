import { useState, useEffect, useMemo } from "react"

import { getQuizzesAPI }                                        from "../../api/quiz.api"
import { getAllResultsAdminAPI, getResultsByQuizAdminAPI }       from "../../api/result.api"

import PageHeader from "../../components/common/PageHeader"
import Table      from "../../components/common/Table"
import Input      from "../../components/common/Input"
import Badge      from "../../components/common/Badge"
import EmptyState from "../../components/common/EmptyState"

import { formatDateTime } from "../../utils/helpers"

const AdminResults = () => {

  const [quizzes, setQuizzes]         = useState([])
  const [results, setResults]         = useState([])
  const [selectedQuiz, setSelectedQuiz] = useState("")
  const [loading, setLoading]         = useState(true)
  const [search, setSearch]           = useState("")

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
      } catch { /** silently fail */ }
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
      r.quiz_title.toLowerCase().includes(q)
    )
  }, [search, results])

  const columns = [
    {
      key:    "student_id",
      label:  "Student",
      render: (value) => (
        <span className="font-mono text-xs bg-slate-100 text-slate-700 px-2.5 py-1 rounded-md font-medium">
          {value?.slice(-8).toUpperCase()}
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

      {/** Filters */}
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
            placeholder="Search by quiz name..."
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
        <Table
          columns={columns}
          rows={filtered}
          loading={loading}
          emptyMessage="No results available"
        />
      )}

    </div>
  )
}

export default AdminResults