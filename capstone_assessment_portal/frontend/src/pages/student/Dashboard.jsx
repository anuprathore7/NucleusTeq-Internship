
import { useState, useEffect } from "react"
import { useNavigate }         from "react-router-dom"

import { getMyResultsAPI }  from "../../api/result.api"
import { getMyAttemptsAPI } from "../../api/attempt.api"

import PageHeader from "../../components/common/PageHeader"
import StatCard   from "../../components/common/StatCard"
import Table      from "../../components/common/Table"
import Badge      from "../../components/common/Badge"
import Button     from "../../components/common/Button"

import { formatDateTime } from "../../utils/helpers"
import { ROUTES }         from "../../utils/constants"

const AttemptIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
  </svg>
)

const PassIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
)

const FailIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
)

const ScoreIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
  </svg>
)

const recentColumns = [
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
    label:  "Status",
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

const StudentDashboard = () => {

  const navigate = useNavigate()

  const [stats, setStats] = useState({
    totalAttempts: 0,
    passed:        0,
    failed:        0,
    avgScore:      0
  })

  const [recentResults, setRecentResults] = useState([])
  const [loading, setLoading]             = useState(true)

  useEffect(() => {

    const fetchData = async () => {
      try {
        const resultsData = await getMyResultsAPI()
        const results     = resultsData.results || []

        const passed  = results.filter((r) => r.passed).length
        const failed  = results.length - passed

        const avgScore = results.length > 0
          ? Math.round(results.reduce((sum, r) => sum + r.percentage, 0) / results.length)
          : 0

        setStats({ totalAttempts: results.length, passed, failed, avgScore })
        setRecentResults(results.slice(0, 4))

      } catch {
        /** silently fail */
      } finally {
        setLoading(false)
      }
    }

    fetchData()

  }, [])

  return (
    <div className="flex flex-col gap-8">

      <PageHeader
        title="My Dashboard"
        subtitle="Track your quiz performance"
        action={
          <Button onClick={() => navigate("/student/categories")}>
            Browse Categories
          </Button>
        }
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">

        <StatCard
          label="Total Attempts"
          value={loading ? "—" : stats.totalAttempts}
          icon={<AttemptIcon />}
          color="blue"
        />

        <StatCard
          label="Passed"
          value={loading ? "—" : stats.passed}
          icon={<PassIcon />}
          color="green"
        />

        <StatCard
          label="Failed"
          value={loading ? "—" : stats.failed}
          icon={<FailIcon />}
          color="red"
        />

        <StatCard
          label="Average Score"
          value={loading ? "—" : `${stats.avgScore}%`}
          icon={<ScoreIcon />}
          color="purple"
        />

      </div>

      <div>

        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-semibold text-slate-800">Recent Results</h2>
          {recentResults.length > 0 && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate(ROUTES.STUDENT_HISTORY)}
            >
              View All
            </Button>
          )}
        </div>

        <Table
          columns={recentColumns}
          rows={recentResults}
          loading={loading}
          emptyMessage="No quizzes completed yet. Browse categories to start!"
        />

      </div>

    </div>
  )
}

export default StudentDashboard