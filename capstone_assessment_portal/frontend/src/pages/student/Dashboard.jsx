/**
 * Student Dashboard
 *
 * Shows student's personal stats and recent quiz results.
 * Stats: total attempts, passed, failed, average score.
 */

import { useState, useEffect } from "react"

import { getMyResultsAPI } from "../../api/result.api"
import { getMyAttemptsAPI } from "../../api/attempt.api"

import PageHeader from "../../components/common/PageHeader"
import StatCard from "../../components/common/StatCard"
import Table from "../../components/common/Table"
import Badge from "../../components/common/Badge"
import Button from "../../components/common/Button"
import { formatDateTime } from "../../utils/helpers"
import { useNavigate } from "react-router-dom"
import { ROUTES } from "../../utils/constants"

/* Icons */

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

/* Recent results table columns */

const resultColumns = [
  {
    key: "quiz_title",
    label: "Quiz Name"
  },
  {
    key: "score",
    label: "Score",
    render: (value, row) => `${value} / ${row.total_marks}`
  },
  {
    key: "percentage",
    label: "Percentage",
    render: (value) => `${value}%`
  },
  {
    key: "passed",
    label: "Status",
    render: (value) => (
      <Badge
        label={value ? "Passed" : "Failed"}
        variant={value ? "success" : "error"}
      />
    )
  },
  {
    key: "submitted_at",
    label: "Date",
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
          ? Math.round(
              results.reduce((sum, r) => sum + r.percentage, 0) / results.length
            )
          : 0

        setStats({
          totalAttempts: results.length,
          passed,
          failed,
          avgScore
        })

        /* Show only 5 most recent */
        setRecentResults(results.slice(0, 5))

      } catch (error) {
        console.error("Student dashboard fetch error:", error)
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [])

  return (
    <div>

      <PageHeader
        title="My Dashboard"
        subtitle="Track your quiz performance"
        action={
          <Button
            onClick={() => navigate(ROUTES.STUDENT_QUIZZES)}
          >
            Browse Quizzes
          </Button>
        }
      />

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-8">

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

      {/* Recent Results */}
      <div>

        <div className="flex items-center justify-between mb-4">

          <h2 className="text-base font-semibold text-slate-800">
            Recent Results
          </h2>

          {recentResults.length > 0 && (
            <button
              onClick={() => navigate(ROUTES.STUDENT_HISTORY)}
              className="text-sm text-primary-600 hover:text-primary-700 font-medium transition-colors"
            >
              View all
            </button>
          )}

        </div>

        <Table
          columns={resultColumns}
          rows={recentResults}
          loading={loading}
          emptyMessage="You have not completed any quizzes yet. Start one now!"
        />

      </div>

    </div>
  )
}

export default StudentDashboard