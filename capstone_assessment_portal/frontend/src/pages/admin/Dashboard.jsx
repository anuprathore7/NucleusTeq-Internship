import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"

import { getCategoriesAPI }     from "../../api/category.api"
import { getQuizzesAPI }        from "../../api/quiz.api"
import { getAllResultsAdminAPI } from "../../api/result.api"

import PageHeader from "../../components/common/PageHeader"
import StatCard   from "../../components/common/StatCard"
import Table      from "../../components/common/Table"
import Badge      from "../../components/common/Badge"
import Button     from "../../components/common/Button"

import { formatDateTime } from "../../utils/helpers"
import { ROUTES }         from "../../utils/constants"

const CategoryIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
  </svg>
)

const QuizIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
  </svg>
)

const StudentIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
  </svg>
)

/** Recent submissions table — clean columns, no student id */
const submissionColumns = [
  {
    key:   "quiz_title",
    label: "Quiz"
  },
  {
    key:    "percentage",
    label:  "Score",
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

const AdminDashboard = () => {

  const navigate = useNavigate()

  const [stats, setStats]             = useState({ categories: 0, quizzes: 0, uniqueStudents: 0 })
  const [recentResults, setRecentResults] = useState([])
  const [loading, setLoading]             = useState(true)

  useEffect(() => {

    const fetchData = async () => {
      try {
        const [catData, quizData, resultsData] = await Promise.all([
          getCategoriesAPI(),
          getQuizzesAPI(),
          getAllResultsAdminAPI()
        ])

        const results        = resultsData.results || []
        const uniqueStudents = new Set(results.map((r) => r.student_id)).size

        setStats({
          categories:    catData.total  || 0,
          quizzes:       quizData.total || 0,
          uniqueStudents
        })

        /** Show max 4 recent submissions */
        setRecentResults(results.slice(0, 4))

      } catch (err) {
        console.error("Dashboard error:", err)
      } finally {
        setLoading(false)
      }
    }

    fetchData()

  }, [])

  return (
    <div className="flex flex-col gap-8">

      <PageHeader
        title="Dashboard"
        subtitle="System overview and recent activity"
      />

      {/** Three stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">

        <StatCard
          label="Total Categories"
          value={loading ? "—" : stats.categories}
          icon={<CategoryIcon />}
          color="blue"
        />

        <StatCard
          label="Total Quizzes"
          value={loading ? "—" : stats.quizzes}
          icon={<QuizIcon />}
          color="purple"
        />

        <StatCard
          label="Students Attempted"
          value={loading ? "—" : stats.uniqueStudents}
          icon={<StudentIcon />}
          color="green"
        />

      </div>

      {/** Recent submissions — max 4 rows, View All button */}
      <div>

        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-semibold text-slate-800">
            Recent Submissions
          </h2>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(ROUTES.ADMIN_RESULTS)}
          >
            View All
          </Button>
        </div>

        <Table
          columns={submissionColumns}
          rows={recentResults}
          loading={loading}
          emptyMessage="No submissions yet"
        />

      </div>

    </div>
  )
}

export default AdminDashboard