import { useState, useEffect, useMemo } from "react"

import { getAllResultsAdminAPI } from "../../api/result.api"

import PageHeader  from "../../components/common/PageHeader"
import Table       from "../../components/common/Table"
import Pagination  from "../../components/common/Pagination"
import Input       from "../../components/common/Input"
import Badge       from "../../components/common/Badge"
import EmptyState  from "../../components/common/EmptyState"
import Modal       from "../../components/common/Modal"
import Spinner     from "../../components/common/Spinner"

import { formatDateTime } from "../../utils/helpers"

/* Rows shown per page on the main students table */
const PAGE_SIZE = 10

/* Build per-student summary from flat results array */
const aggregateStudents = (results) => {
  const map = {}

  results.forEach((r) => {
    if (!map[r.student_id]) {
      map[r.student_id] = {
        student_id:    r.student_id,
        username:      r.username || "Unknown",
        attempts:      [],
        totalAttempts: 0,
        passed:        0,
        failed:        0,
        totalScore:    0
      }
    }
    map[r.student_id].attempts.push(r)
    map[r.student_id].totalAttempts += 1
    map[r.student_id].totalScore    += r.percentage
    if (r.passed) map[r.student_id].passed += 1
    else          map[r.student_id].failed  += 1
  })

  return Object.values(map).map((s) => ({
    ...s,
    passRate: s.totalAttempts > 0
      ? Math.round((s.passed / s.totalAttempts) * 100)
      : 0,
    avgScore: s.totalAttempts > 0
      ? Math.round(s.totalScore / s.totalAttempts)
      : 0
  }))
}

/* Detail modal — shows all attempts for one student with attempt_id */
const StudentDetailModal = ({ student, open, onClose }) => {

  const attemptColumns = [
    {
      key:    "attempt_id",
      label:  "Attempt ID",
      render: (value) => (
        <span className="font-mono text-xs bg-slate-100 text-slate-600 px-2 py-1 rounded">
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
      label:  "%",
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
      label:  "Date",
      render: (value) => formatDateTime(value)
    }
  ]

  return (
    <Modal
      open={open}
      title={student ? `Student — ${student.username}` : ""}
      onClose={onClose}
      size="lg"
    >
      {student && (
        <div className="flex flex-col gap-5">

          {/* Summary stats */}
          <div className="grid grid-cols-3 gap-3">
            <div className="bg-slate-50 rounded-xl p-4 text-center border border-slate-200">
              <p className="text-xs text-slate-500 mb-1">Attempts</p>
              <p className="text-2xl font-bold text-slate-900">{student.totalAttempts}</p>
            </div>
            <div className="bg-slate-50 rounded-xl p-4 text-center border border-slate-200">
              <p className="text-xs text-slate-500 mb-1">Pass Rate</p>
              <p className={`text-2xl font-bold ${student.passRate >= 50 ? "text-green-600" : "text-red-500"}`}>
                {student.passRate}%
              </p>
            </div>
            <div className="bg-slate-50 rounded-xl p-4 text-center border border-slate-200">
              <p className="text-xs text-slate-500 mb-1">Avg Score</p>
              <p className="text-2xl font-bold text-primary-600">{student.avgScore}%</p>
            </div>
          </div>

          {/* Passed / Failed breakdown */}
          <div className="flex items-center gap-4">
            <Badge label={`${student.passed} Passed`} variant="success" />
            <Badge label={`${student.failed} Failed`} variant="error" />
          </div>

          {/* Full attempts table — scrolls within the modal, no pagination needed here since it's a detail view */}
          <div>
            <p className="text-sm font-semibold text-slate-800 mb-3">All Attempts</p>
            <div className="overflow-x-auto">
              <Table
                columns={attemptColumns}
                rows={student.attempts}
                emptyMessage="No attempts recorded"
              />
            </div>
          </div>

        </div>
      )}
    </Modal>
  )
}

const AdminStudents = () => {

  const [students, setStudents] = useState([])
  const [loading, setLoading]   = useState(true)
  const [search, setSearch]     = useState("")
  const [page, setPage]         = useState(1)
  const [selected, setSelected] = useState(null)

  const loadStudents = async () => {
    try {
      setLoading(true)
      const data    = await getAllResultsAdminAPI()
      const results = data.results || []
      setStudents(aggregateStudents(results))
    } catch {
      /* silently fail */
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadStudents() }, [])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return students
    return students.filter((s) =>
      s.username.toLowerCase().includes(q)
    )
  }, [search, students])

  /* Reset to page 1 whenever the search term changes */
  useEffect(() => { setPage(1) }, [search])

  const paginated = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE
    return filtered.slice(start, start + PAGE_SIZE)
  }, [filtered, page])

  const columns = [
    {
      key:    "username",
      label:  "Student",
      render: (value) => (
        <span className="text-sm font-medium text-slate-800">{value}</span>
      )
    },
    {
      key:   "totalAttempts",
      label: "Total Attempts"
    },
    {
      key:   "passed",
      label: "Passed"
    },
    {
      key:   "failed",
      label: "Failed"
    },
    {
      key:    "passRate",
      label:  "Pass Rate",
      render: (value) => (
        <span className={`text-sm font-semibold ${value >= 50 ? "text-green-600" : "text-red-500"}`}>
          {value}%
        </span>
      )
    },
    {
      key:    "avgScore",
      label:  "Avg Score",
      render: (value) => (
        <span className="text-sm font-medium text-slate-700">{value}%</span>
      )
    },
    {
      key:    "actions",
      label:  "Detail",
      render: (_, row) => (
        <button
          onClick={() => setSelected(row)}
          className="text-sm text-primary-600 hover:text-primary-700 font-medium transition-colors underline-offset-2 hover:underline"
        >
          View
        </button>
      )
    }
  ]

  if (loading) {
    return (
      <div className="flex justify-center py-24">
        <Spinner size="lg" />
      </div>
    )
  }

  return (
    <div>

      <PageHeader
        title="Students"
        subtitle="Individual student performance and attempt history"
      />

      <div className="mb-4 max-w-sm">
        <Input
          placeholder="Search by student name..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {filtered.length === 0 && search ? (
        <EmptyState
          title="No students found"
          description={`No students match "${search}"`}
        />
      ) : (
        <>
          <Table
            columns={columns}
            rows={paginated}
            emptyMessage="No student data available yet"
          />
          <Pagination
            currentPage={page}
            totalItems={filtered.length}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </>
      )}

      <StudentDetailModal
        open={!!selected}
        student={selected}
        onClose={() => setSelected(null)}
      />

    </div>
  )
}

export default AdminStudents