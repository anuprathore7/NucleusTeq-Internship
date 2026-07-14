import { useState, useEffect, useMemo } from "react"
import { useNavigate, useLocation, useSearchParams } from "react-router-dom"

import { getCategoriesAPI }                        from "../../api/category.api"
import { getQuizzesAPI, getQuizzesByCategoryAPI }   from "../../api/quiz.api"
import { getMyAttemptsAPI, startAttemptAPI }        from "../../api/attempt.api"

import PageHeader from "../../components/common/PageHeader"
import Input      from "../../components/common/Input"
import Button     from "../../components/common/Button"
import Badge      from "../../components/common/Badge"
import EmptyState from "../../components/common/EmptyState"
import Spinner    from "../../components/common/Spinner"
import Alert      from "../../components/common/Alert"

const MAX_ATTEMPTS = 2

/* Checks whether an in_progress attempt has passed its own deadline */
const isAttemptExpired = (attempt) => {
  const deadline = new Date(attempt.started_at).getTime() + attempt.time_limit * 60000
  return Date.now() > deadline
}

/* Single quiz card with category badge, attempt count, and resume state */
const QuizCard = ({ quiz, categoryName, attemptInfo, onAction, actingOn }) => {

  const { count, resumable } = attemptInfo
  const attemptsLeft = MAX_ATTEMPTS - count
  const isMaxReached = count >= MAX_ATTEMPTS && !resumable

  let buttonLabel = "Start Quiz"
  if (resumable) buttonLabel = "Resume Quiz"
  else if (isMaxReached) buttonLabel = "Attempts Exhausted"

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 flex flex-col gap-4 hover:shadow-md transition-shadow">

      <div>
        {/* Category badge sits above the title so it reads as a label, not a stat */}
        {categoryName && (
          <Badge label={categoryName} variant="purple" />
        )}
        <h3 className="text-sm font-semibold text-slate-900 mt-2 mb-1">{quiz.title}</h3>
        <p className="text-xs text-slate-500 line-clamp-2 leading-relaxed">
          {quiz.description}
        </p>
      </div>

      <div className="flex flex-wrap gap-2">
        <Badge label={`${quiz.time_limit} min`}         variant="info" />
        <Badge label={`Pass: ${quiz.pass_percentage}%`} variant="default" />
        {resumable && <Badge label="In Progress" variant="warning" />}
      </div>

      <div className="flex items-center justify-between text-xs border-t border-slate-100 pt-3">
        <span className="text-slate-500">
          Attempts:{" "}
          <span className={`font-semibold ${isMaxReached ? "text-red-500" : "text-slate-700"}`}>
            {count} / {MAX_ATTEMPTS}
          </span>
        </span>
        {isMaxReached ? (
          <Badge label="Max reached" variant="error" />
        ) : resumable ? (
          <Badge label="Resume available" variant="warning" />
        ) : (
          <Badge label={`${attemptsLeft} left`} variant="success" />
        )}
      </div>

      <Button
        fullWidth
        size="sm"
        disabled={isMaxReached}
        loading={actingOn === quiz.id}
        onClick={() => onAction(quiz.id)}
        variant={isMaxReached ? "secondary" : resumable ? "secondary" : "primary"}
      >
        {buttonLabel}
      </Button>

    </div>
  )
}

const StudentQuizList = () => {

  const navigate       = useNavigate()
  const location        = useLocation()
  const [searchParams]  = useSearchParams()

  const categoryId   = searchParams.get("category")
  const categoryName = location.state?.categoryName

  const [quizzes, setQuizzes]         = useState([])
  const [categoryMap, setCategoryMap] = useState({})
  const [attemptInfoMap, setAttemptInfoMap] = useState({})
  const [loading, setLoading]         = useState(true)
  const [search, setSearch]           = useState("")
  const [actingOn, setActingOn]       = useState(null)
  const [error, setError]             = useState("")

  useEffect(() => {

    const fetchData = async () => {
      try {
        setLoading(true)

        const [quizData, attemptData, catData] = await Promise.all([
          categoryId ? getQuizzesByCategoryAPI(categoryId) : getQuizzesAPI(),
          getMyAttemptsAPI(),
          getCategoriesAPI()
        ])

        const quizList = quizData.quizzes || []
        setQuizzes(quizList)

        /* Build a map: category_id -> category name, so every card can show its category label */
        const categories = catData.categories || []
        const catMap = {}
        categories.forEach((c) => { catMap[c.id] = c.name })
        setCategoryMap(catMap)

        /* Build a map: quiz_id -> { count, resumable } */
        const attempts = attemptData.attempts || []
        const map = {}

        attempts.forEach((a) => {
          if (!map[a.quiz_id]) {
            map[a.quiz_id] = { count: 0, resumable: false }
          }
          map[a.quiz_id].count += 1

          if (a.status === "in_progress" && !isAttemptExpired(a)) {
            map[a.quiz_id].resumable = true
          }
        })

        setAttemptInfoMap(map)

      } catch {
        /* silently fail */
      } finally {
        setLoading(false)
      }
    }

    fetchData()

  }, [categoryId])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return quizzes
    return quizzes.filter((q_) => q_.title.toLowerCase().includes(q))
  }, [search, quizzes])

  const handleAction = async (quizId) => {
    try {
      setError("")
      setActingOn(quizId)
      const attempt = await startAttemptAPI(quizId)
      navigate(`/student/attempt/${attempt.id}`)
    } catch (err) {
      setError(err?.response?.data?.detail || "Failed to start quiz. Please try again.")
    } finally {
      setActingOn(null)
    }
  }

  if (loading) return <Spinner fullPage />

  return (
    <div>

      <PageHeader
        title={categoryName ? `${categoryName} — Quizzes` : "All Quizzes"}
        subtitle={`${quizzes.length} quiz${quizzes.length !== 1 ? "es" : ""} available`}
        action={
          categoryId && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => navigate("/student/categories")}
            >
              Back to Categories
            </Button>
          )
        }
      />

      {error && (
        <div className="mb-4">
          <Alert type="error" message={error} />
        </div>
      )}

      <div className="mb-6 max-w-sm">
        <Input
          placeholder="Search quizzes..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title={search ? "No quizzes found" : "No quizzes available"}
          description={
            search
              ? `No quizzes match "${search}"`
              : "Quizzes will appear once the admin adds them."
          }
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((quiz) => (
            <QuizCard
              key={quiz.id}
              quiz={quiz}
              categoryName={categoryMap[quiz.category_id]}
              attemptInfo={attemptInfoMap[quiz.id] || { count: 0, resumable: false }}
              onAction={handleAction}
              actingOn={actingOn}
            />
          ))}
        </div>
      )}

    </div>
  )
}

export default StudentQuizList