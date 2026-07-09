/**
 * Student Quiz List Page
 *
 * Student can:
 * - Browse quizzes (optionally filtered by category)
 * - Search quizzes by title
 * - Click a quiz to start an attempt
 */

import { useState, useEffect, useMemo } from "react"
import { useNavigate, useLocation, useSearchParams } from "react-router-dom"

import { getQuizzesAPI, getQuizzesByCategoryAPI } from "../../api/quiz.api"
import { startAttemptAPI } from "../../api/attempt.api"

import PageHeader from "../../components/common/PageHeader"
import Input      from "../../components/common/Input"
import Button     from "../../components/common/Button"
import Badge      from "../../components/common/Badge"
import EmptyState from "../../components/common/EmptyState"
import Spinner    from "../../components/common/Spinner"
import Alert      from "../../components/common/Alert"

/*  Quiz Card  */

const QuizCard = ({ quiz, onStart, starting }) => (
  <div className="bg-white rounded-xl border border-slate-200 p-5 flex flex-col gap-4 hover:shadow-md transition-shadow">

    {/* Title */}
    <div>
      <h3 className="text-sm font-semibold text-slate-900 mb-1">{quiz.title}</h3>
      <p className="text-xs text-slate-500 line-clamp-2 leading-relaxed">
        {quiz.description}
      </p>
    </div>

    {/* Meta info */}
    <div className="flex flex-wrap gap-2">
      <Badge label={`${quiz.time_limit} min`} variant="info" />
      <Badge label={`Pass: ${quiz.pass_percentage}%`} variant="default" />
    </div>

    {/* Start button */}
    <Button
      fullWidth
      size="sm"
      loading={starting === quiz.id}
      onClick={() => onStart(quiz.id)}
    >
      Start Quiz
    </Button>

  </div>
)

/*  Main Component  */

const StudentQuizList = () => {

  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()

  const categoryId   = searchParams.get("category")
  const categoryName = location.state?.categoryName

  const [quizzes, setQuizzes] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch]   = useState("")
  const [starting, setStarting] = useState(null)
  const [error, setError]     = useState("")

  useEffect(() => {
    const fetch = async () => {
      try {
        setLoading(true)
        const data = categoryId
          ? await getQuizzesByCategoryAPI(categoryId)
          : await getQuizzesAPI()
        setQuizzes(data.quizzes || [])
      } catch {
        /* silently fail */
      } finally {
        setLoading(false)
      }
    }
    fetch()
  }, [categoryId])

  /* filter in memory */
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return quizzes
    return quizzes.filter((q_) => q_.title.toLowerCase().includes(q))
  }, [search, quizzes])

  const handleStart = async (quizId) => {
    try {
      setError("")
      setStarting(quizId)
      const attempt = await startAttemptAPI(quizId)
      navigate(`/student/attempt/${attempt.id}`)
    } catch (err) {
      setError(err?.response?.data?.detail || "Failed to start quiz. Please try again.")
    } finally {
      setStarting(null)
    }
  }

  if (loading) return <Spinner fullPage />

  return (
    <div>

      <PageHeader
        title={categoryName ? `${categoryName} Quizzes` : "All Quizzes"}
        subtitle={`${quizzes.length} quiz${quizzes.length !== 1 ? "es" : ""} available`}
        action={
          categoryId && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => navigate("/student/quizzes")}
            >
              View All Quizzes
            </Button>
          )
        }
      />

      {/* Error */}
      {error && (
        <div className="mb-4">
          <Alert type="error" message={error} />
        </div>
      )}

      {/* Search */}
      <div className="mb-6 max-w-sm">
        <Input
          placeholder="Search quizzes..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Grid */}
      {filtered.length === 0 ? (
        <EmptyState
          title={search ? "No quizzes found" : "No quizzes available"}
          description={
            search
              ? `No quizzes match "${search}"`
              : "Quizzes will appear here once the admin adds them."
          }
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((quiz) => (
            <QuizCard
              key={quiz.id}
              quiz={quiz}
              onStart={handleStart}
              starting={starting}
            />
          ))}
        </div>
      )}

    </div>
  )
}

export default StudentQuizList