import { useState, useEffect, useRef, useCallback } from "react"
import { useParams, useNavigate }                    from "react-router-dom"

import {
  getAttemptByIdAPI,
  saveAnswerAPI,
  submitAttemptAPI
} from "../../api/attempt.api"

import Spinner from "../../components/common/Spinner"
import Button  from "../../components/common/Button"
import Alert   from "../../components/common/Alert"

/**
 * Format seconds into MM:SS string.
 */
const formatCountdown = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`
}

/**
 * Calculate remaining seconds from attempt data.
 *
 * Both started_at (from backend) and Date.now() are in UTC.
 * We parse started_at as UTC explicitly to avoid timezone issues.
 */
const calculateRemainingSeconds = (startedAt, timeLimitMinutes) => {
  const startMs     = new Date(startedAt).getTime()
  const nowMs       = Date.now()
  const timeLimitMs = timeLimitMinutes * 60 * 1000
  const elapsedMs   = nowMs - startMs
  const remainingMs = timeLimitMs - elapsedMs

  /** If remaining is negative the attempt is already expired */
  return Math.max(0, Math.floor(remainingMs / 1000))
}

/**
 * Timer bar component.
 * Shows colored countdown bar at the top of the screen.
 */
const TimerBar = ({ secondsLeft, totalSeconds }) => {

  const percent  = totalSeconds > 0 ? Math.max(0, (secondsLeft / totalSeconds) * 100) : 0
  const isRed    = secondsLeft <= 60
  const isYellow = secondsLeft <= 120 && !isRed

  return (
    <div className="flex items-center gap-4 px-6 py-3 bg-white border-b border-slate-200">

      <svg
        className={`w-5 h-5 shrink-0 ${isRed ? "text-red-500" : isYellow ? "text-amber-500" : "text-slate-500"}`}
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>

      <span className={`
        text-sm font-bold tabular-nums w-14 shrink-0
        ${isRed ? "text-red-600" : isYellow ? "text-amber-600" : "text-slate-700"}
      `}>
        {formatCountdown(secondsLeft)}
      </span>

      <div className="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden">
        <div
          className={`
            h-full rounded-full transition-all duration-1000
            ${isRed ? "bg-red-500" : isYellow ? "bg-amber-500" : "bg-primary-500"}
          `}
          style={{ width: `${percent}%` }}
        />
      </div>

      {isRed && (
        <span className="text-xs font-semibold text-red-500 shrink-0 animate-pulse">
          Time running out!
        </span>
      )}

    </div>
  )
}

/**
 * Quiz header showing title and current question progress.
 */
const QuizHeader = ({ title, current, total }) => (
  <div className="px-6 py-4 bg-white border-b border-slate-200 flex items-center justify-between">

    <div>
      <p className="text-xs text-slate-400 font-medium uppercase tracking-wide mb-0.5">
        Quiz
      </p>
      <h1 className="text-base font-bold text-slate-900 line-clamp-1">{title}</h1>
    </div>

    <div className="text-right shrink-0 ml-4">
      <p className="text-xs text-slate-400 font-medium uppercase tracking-wide mb-0.5">
        Progress
      </p>
      <p className="text-base font-bold text-slate-900">
        {current} <span className="text-slate-400 font-normal text-sm">of</span> {total}
      </p>
    </div>

  </div>
)

/**
 * Single question with clickable option buttons.
 * No correct answer is shown — options are just text.
 */
const QuestionView = ({ question, selectedAnswer, onSelect }) => (
  <div className="flex flex-col gap-6">

    {/** Question text box */}
    <div className="bg-white rounded-xl p-6 border border-slate-200 shadow-sm">
      <div className="flex items-center gap-2 mb-4">
        <span className="text-xs font-semibold text-primary-600 uppercase tracking-wide">
          Question
        </span>
        <span className="text-xs text-slate-400">
          • {question.marks} mark{question.marks !== 1 ? "s" : ""}
        </span>
        <span className="text-xs text-slate-400 capitalize">
          • {question.difficulty}
        </span>
      </div>
      <p className="text-base font-medium text-slate-900 leading-relaxed">
        {question.question_text}
      </p>
    </div>

    {/** Answer options */}
    <div className="flex flex-col gap-3">

      <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide px-1">
        Choose your answer
      </p>

      {question.options.map((option, index) => {
        const isSelected = selectedAnswer === option
        const letter     = String.fromCharCode(65 + index)

        return (
          <button
            key={index}
            onClick={() => onSelect(option)}
            className={`
              w-full flex items-center gap-4 p-4 rounded-xl border-2
              text-left transition-all duration-150 cursor-pointer
              ${isSelected
                ? "border-primary-500 bg-primary-50 shadow-sm"
                : "border-slate-200 bg-white hover:border-primary-300 hover:bg-primary-50/30"
              }
            `}
          >
            {/** Letter badge */}
            <span className={`
              w-8 h-8 rounded-lg flex items-center justify-center
              text-xs font-bold shrink-0
              ${isSelected
                ? "bg-primary-600 text-white"
                : "bg-slate-100 text-slate-600"
              }
            `}>
              {letter}
            </span>

            {/** Option text */}
            <span className={`
              text-sm font-medium leading-relaxed flex-1
              ${isSelected ? "text-primary-700" : "text-slate-700"}
            `}>
              {option}
            </span>

            {/** Check icon when selected */}
            {isSelected && (
              <svg
                className="w-5 h-5 text-primary-500 shrink-0"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                  clipRule="evenodd"
                />
              </svg>
            )}

          </button>
        )
      })}

    </div>

  </div>
)

/**
 * Bottom navigation panel with question dots and Prev/Next/Submit.
 */
const NavigationPanel = ({
  total,
  current,
  answers,
  questions,
  onPrev,
  onNext,
  onSubmit,
  onJump,
  submitting
}) => {

  const isLast   = current === total - 1
  const answered = questions.filter((q) => !!answers[q.question_id]).length

  return (
    <div className="px-6 py-4 bg-white border-t border-slate-200 flex flex-col gap-4">

      {/** Question number dots */}
      <div className="flex items-center gap-1.5 flex-wrap">
        {questions.map((q, i) => {
          const isAnswered = !!answers[q.question_id]
          const isCurrent  = i === current

          return (
            <button
              key={i}
              onClick={() => onJump(i)}
              title={`Q${i + 1} — ${isAnswered ? "Answered" : "Not answered"}`}
              className={`
                w-8 h-8 rounded-lg text-xs font-bold transition-all
                ${isCurrent
                  ? "bg-primary-600 text-white ring-2 ring-primary-300 ring-offset-1"
                  : isAnswered
                    ? "bg-primary-100 text-primary-700 hover:bg-primary-200"
                    : "bg-slate-100 text-slate-500 hover:bg-slate-200"
                }
              `}
            >
              {i + 1}
            </button>
          )
        })}
      </div>

      {/** Footer row */}
      <div className="flex items-center justify-between">

        <p className="text-xs text-slate-500">
          <span className="font-semibold text-slate-700">{answered}</span>
          {" "}/{" "}{total} answered
        </p>

        <div className="flex items-center gap-3">

          {current > 0 && (
            <Button
              variant="secondary"
              size="sm"
              onClick={onPrev}
              disabled={submitting}
            >
              Previous
            </Button>
          )}

          {isLast ? (
            <Button
              variant="primary"
              size="sm"
              loading={submitting}
              onClick={onSubmit}
            >
              Submit Quiz
            </Button>
          ) : (
            <Button
              variant="primary"
              size="sm"
              onClick={onNext}
              disabled={submitting}
            >
              Next
            </Button>
          )}

        </div>

      </div>

    </div>
  )
}

/**
 * QuizAttempt — main page for taking a quiz.
 *
 * Flow:
 * 1. Load attempt from backend
 * 2. Restore any previously saved answers
 * 3. Calculate remaining time correctly from started_at
 * 4. Start countdown — auto submit when timer hits 0
 * 5. Student clicks options — answer saved to backend silently
 * 6. Student clicks Submit — manual submit, redirect to result
 */
const QuizAttempt = () => {

  const { attemptId } = useParams()
  const navigate      = useNavigate()

  const [attempt, setAttempt]         = useState(null)
  const [loading, setLoading]         = useState(true)
  const [error, setError]             = useState("")
  const [currentIndex, setCurrentIndex] = useState(0)
  const [answers, setAnswers]         = useState({})
  const [secondsLeft, setSecondsLeft] = useState(null)
  const [totalSeconds, setTotalSeconds] = useState(0)
  const [submitting, setSubmitting]   = useState(false)
  const [savingAnswer, setSavingAnswer] = useState(false)
  const [submitError, setSubmitError] = useState("")

  const timerRef       = useRef(null)
  const hasSubmittedRef = useRef(false)

  /** Load attempt from backend */
  useEffect(() => {

    const loadAttempt = async () => {
      try {
        const data = await getAttemptByIdAPI(attemptId)

        /**
         * If attempt is already submitted, redirect straight to result.
         * This handles the case where student refreshes after submitting.
         */
        if (data.status === "submitted") {
          navigate(`/student/result/${attemptId}`, { replace: true })
          return
        }

        setAttempt(data)

        /** Restore previously saved answers into local state */
        const savedMap = {}
        if (data.answers && data.answers.length > 0) {
          data.answers.forEach((a) => {
            savedMap[a.question_id] = a.selected_answer
          })
        }
        setAnswers(savedMap)

        /**
         * Calculate remaining time.
         * started_at is UTC ISO string from backend.
         * We compute how many seconds have elapsed since the attempt started,
         * then subtract from the total time limit.
         *
         * Example:
         * started_at  = "2026-07-10T10:00:00Z"
         * time_limit  = 30 minutes = 1800 seconds
         * now         = "2026-07-10T10:05:00Z"
         * elapsed     = 5 minutes = 300 seconds
         * remaining   = 1800 - 300 = 1500 seconds
         */
        const remaining = calculateRemainingSeconds(
          data.started_at,
          data.time_limit
        )

        const total = data.time_limit * 60

        setTotalSeconds(total)
        setSecondsLeft(remaining)

      } catch (err) {
        setError("Failed to load quiz. Please refresh the page.")
      } finally {
        setLoading(false)
      }
    }

    loadAttempt()

  }, [attemptId, navigate])

  /**
   * Auto submit handler.
   * Called when timer hits 0.
   * Uses ref to prevent calling twice.
   */
  const handleAutoSubmit = useCallback(async () => {
    if (hasSubmittedRef.current) return
    hasSubmittedRef.current = true

    try {
      await submitAttemptAPI(attemptId)
    } catch {
      /** Even if submit fails, redirect to result page */
    } finally {
      navigate(`/student/result/${attemptId}`, { replace: true })
    }
  }, [attemptId, navigate])

  /**
   * Start countdown timer only after attempt is loaded and
   * secondsLeft is set. Prevents timer from starting before
   * we know how much time is left.
   */
  useEffect(() => {

    /** Wait until secondsLeft is calculated */
    if (secondsLeft === null || !attempt) return

    /** If already expired when page loaded, auto submit immediately */
    if (secondsLeft <= 0) {
      handleAutoSubmit()
      return
    }

    timerRef.current = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timerRef.current)
          handleAutoSubmit()
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }

  }, [secondsLeft, attempt, handleAutoSubmit])

  /**
   * Save answer when student selects an option.
   * Updates local state immediately for instant UI response.
   * Sends to backend silently in background.
   */
  const handleSelectAnswer = async (questionId, selectedAnswer) => {

    /** Instant local update */
    setAnswers((prev) => ({ ...prev, [questionId]: selectedAnswer }))

    /** Background save */
    try {
      setSavingAnswer(true)
      await saveAnswerAPI(attemptId, questionId, selectedAnswer)
    } catch {
      /** Silently fail — answer is still in local state */
    } finally {
      setSavingAnswer(false)
    }
  }

  /**
   * Manual submit when student clicks Submit Quiz button.
   */
  const handleSubmit = async () => {

    if (hasSubmittedRef.current) return
    hasSubmittedRef.current = true

    try {
      setSubmitting(true)
      setSubmitError("")

      if (timerRef.current) clearInterval(timerRef.current)

      await submitAttemptAPI(attemptId)
      navigate(`/student/result/${attemptId}`, { replace: true })

    } catch (err) {
      hasSubmittedRef.current = false
      setSubmitError(
        err?.response?.data?.detail || "Failed to submit. Please try again."
      )
      setSubmitting(false)
    }
  }

  /** Loading state */
  if (loading) return <Spinner fullPage />

  /** Error state */
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
        <div className="max-w-md w-full flex flex-col gap-4">
          <Alert type="error" message={error} />
          <Button
            variant="secondary"
            onClick={() => navigate("/student/dashboard")}
          >
            Back to Dashboard
          </Button>
        </div>
      </div>
    )
  }

  if (!attempt) return null

  const questions  = attempt.questions || []
  const currentQ   = questions[currentIndex]

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">

      {/** Sticky top — timer + quiz header */}
      <div className="sticky top-0 z-20 shadow-sm">

        {secondsLeft !== null && (
          <TimerBar
            secondsLeft={secondsLeft}
            totalSeconds={totalSeconds}
          />
        )}

        <QuizHeader
          title={attempt.snapshot?.title || "Quiz"}
          current={currentIndex + 1}
          total={questions.length}
        />

      </div>

      {/** Question area */}
      <div className="flex-1 flex justify-center px-4 py-8">
        <div className="w-full max-w-2xl flex flex-col gap-4">

          {currentQ ? (
            <QuestionView
              question={currentQ}
              selectedAnswer={answers[currentQ.question_id] || ""}
              onSelect={(option) =>
                handleSelectAnswer(currentQ.question_id, option)
              }
            />
          ) : (
            <p className="text-center text-slate-400 text-sm py-16">
              No questions found in this quiz.
            </p>
          )}

          {/** Auto-save indicator */}
          {savingAnswer && (
            <p className="text-xs text-slate-400 text-center animate-pulse">
              Saving your answer...
            </p>
          )}

          {/** Submit error */}
          {submitError && (
            <Alert type="error" message={submitError} />
          )}

        </div>
      </div>

      {/** Sticky bottom navigation */}
      <div className="sticky bottom-0 z-20 shadow-[0_-2px_8px_rgba(0,0,0,0.06)]">
        <NavigationPanel
          total={questions.length}
          current={currentIndex}
          answers={answers}
          questions={questions}
          onPrev={() => setCurrentIndex((i) => Math.max(0, i - 1))}
          onNext={() => setCurrentIndex((i) => Math.min(questions.length - 1, i + 1))}
          onJump={(i) => setCurrentIndex(i)}
          onSubmit={handleSubmit}
          submitting={submitting}
        />
      </div>

    </div>
  )
}

export default QuizAttempt