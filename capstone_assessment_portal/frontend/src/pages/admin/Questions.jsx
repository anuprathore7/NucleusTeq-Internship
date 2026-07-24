import { useState, useEffect, useMemo } from "react"
import { useNavigate, useLocation, useSearchParams } from "react-router-dom"

import { getQuizzesAPI }                      from "../../api/quiz.api"
import {
  getQuestionsByQuizAPI,
  createQuestionAPI,
  updateQuestionAPI,
  deleteQuestionAPI
} from "../../api/question.api"

import PageHeader  from "../../components/common/PageHeader"
import Table       from "../../components/common/Table"
import Pagination  from "../../components/common/Pagination"
import Button      from "../../components/common/Button"
import Input       from "../../components/common/Input"
import Alert       from "../../components/common/Alert"
import Modal       from "../../components/common/Modal"
import Badge       from "../../components/common/Badge"
import EmptyState  from "../../components/common/EmptyState"
import Select      from "../../components/common/Select"

import {
  validateQuestionText,
  validateOption
} from "../../utils/validators"

/* Rows shown per page on the questions table */
const PAGE_SIZE = 10

const TYPES = [
  { value: "mcq",        label: "Multiple Choice (MCQ)" },
  { value: "true_false", label: "True / False" }
]

const DIFFICULTIES = [
  { value: "easy",   label: "Easy" },
  { value: "medium", label: "Medium" },
  { value: "hard",   label: "Hard" }
]

const difficultyVariant = { easy: "success", medium: "warning", hard: "error" }

/* Checks whether a question with the same text already exists in this quiz,
   case-insensitive and trimmed, excluding the question currently being edited */
const isDuplicateQuestionText = (text, existingQuestions, excludeId) => {
  const cleaned = text.trim().toLowerCase()
  return existingQuestions.some((q) =>
    q.id !== excludeId && q.question_text.trim().toLowerCase() === cleaned
  )
}

/* Shared question form for create and edit. Validates all fields before submitting. */
const QuestionForm = ({ initial, quizId, existingQuestions, onSubmit, onCancel, loading, error }) => {

  const [form, setForm] = useState({
    question_text:  initial?.question_text  || "",
    question_type:  initial?.question_type  || "mcq",
    options:        initial?.options        || ["", "", "", ""],
    correct_answer: initial?.correct_answer  || "",
    difficulty:     initial?.difficulty      || "easy",
    marks:          initial?.marks != null ? String(initial.marks) : "1"
  })

  const [errors, setErrors] = useState({})

  const isTrueFalse = form.question_type === "true_false"

  const updateField = (field) => (e) => {
    const value = e.target.value

    if (field === "question_type") {
      setForm((p) => ({
        ...p,
        question_type:  value,
        options:        value === "true_false" ? ["True", "False"] : ["", "", "", ""],
        correct_answer: ""
      }))
      setErrors({})
      return
    }

    setForm((p) => ({ ...p, [field]: value }))
    if (errors[field]) setErrors((p) => ({ ...p, [field]: "" }))
  }

  const updateOption = (index) => (e) => {
    const updated = [...form.options]
    updated[index] = e.target.value
    setForm((p) => ({ ...p, options: updated, correct_answer: "" }))
    if (errors[`opt_${index}`]) setErrors((p) => ({ ...p, [`opt_${index}`]: "" }))
  }

  const validate = () => {
    const e = {}

    const textErr = validateQuestionText(form.question_text)
    if (textErr) {
      e.question_text = textErr
    } else if (isDuplicateQuestionText(form.question_text, existingQuestions, initial?.id)) {
      e.question_text = "A question with this exact text already exists in this quiz"
    }

    if (!isTrueFalse) {
      form.options.forEach((opt, i) => {
        const optErr = validateOption(opt, i)
        if (optErr) e[`opt_${i}`] = optErr
      })
    }

    if (!form.correct_answer) {
      e.correct_answer = "Please select the correct answer"
    } else if (!form.options.includes(form.correct_answer)) {
      e.correct_answer = "Correct answer must match one of the options"
    }

    const marksNum = Number(form.marks)
    if (!form.marks || isNaN(marksNum) || marksNum < 1) {
      e.marks = "Marks must be at least 1"
    }
    if (marksNum > 10) {
      e.marks = "Marks cannot exceed 10"
    }

    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!validate()) return
    onSubmit({
      quiz_id:        quizId,
      question_text:  form.question_text.trim(),
      question_type:  form.question_type,
      options:        form.options,
      correct_answer: form.correct_answer,
      difficulty:     form.difficulty,
      marks:          Number(form.marks)
    })
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4 max-h-[75vh] overflow-y-auto pr-1">

      {error && <Alert type="error" message={error} />}

      <Input
        label="Question Text"
        placeholder="Enter a clear and meaningful question"
        value={form.question_text}
        onChange={updateField("question_text")}
        error={errors.question_text}
        hint="Must be meaningful and unique within this quiz."
        required
        maxLength={1000}
      />

      <Select
        label="Question Type"
        value={form.question_type}
        onChange={updateField("question_type")}
        options={TYPES}
        disabled={!!initial}
        hint={initial ? "Question type cannot be changed after creation." : ""}
        placeholder=""
        required
      />

      <div className="flex flex-col gap-2">
        <label className="text-sm font-medium text-slate-700">
          Options <span className="text-red-500">*</span>
        </label>
        {form.options.map((opt, i) => (
          <Input
            key={i}
            placeholder={`Option ${i + 1}`}
            value={opt}
            onChange={updateOption(i)}
            disabled={isTrueFalse}
            error={errors[`opt_${i}`]}
          />
        ))}
        {!isTrueFalse && (
          <p className="text-xs text-slate-400">Each option must be meaningful text.</p>
        )}
      </div>

      <Select
        label="Correct Answer"
        value={form.correct_answer}
        onChange={updateField("correct_answer")}
        options={form.options.filter((o) => o.trim()).map((opt) => ({ value: opt, label: opt }))}
        error={errors.correct_answer}
        placeholder="Select the correct answer"
        required
      />

      <div className="grid grid-cols-2 gap-4">

        <Select
          label="Difficulty"
          value={form.difficulty}
          onChange={updateField("difficulty")}
          options={DIFFICULTIES}
          placeholder=""
        />

        <Input
          label="Marks"
          type="number"
          placeholder="1"
          value={form.marks}
          onChange={updateField("marks")}
          error={errors.marks}
          hint="Between 1 and 10"
          required
        />

      </div>

      <div className="flex justify-end gap-3 pt-2">
        <Button variant="secondary" type="button" onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          {initial ? "Save Changes" : "Add Question"}
        </Button>
      </div>

    </form>
  )
}

/* Delete confirm */
const DeleteConfirm = ({ onConfirm, onCancel, loading, error }) => (
  <div className="flex flex-col gap-4">
    {error && <Alert type="error" message={error} />}
    <p className="text-sm text-slate-600">
      Are you sure you want to delete this question? This action cannot be undone.
    </p>
    <div className="flex justify-end gap-3">
      <Button variant="secondary" onClick={onCancel} disabled={loading}>Cancel</Button>
      <Button variant="danger" onClick={onConfirm} loading={loading}>Delete</Button>
    </div>
  </div>
)

const AdminQuestions = () => {

  const navigate        = useNavigate()
  const location          = useLocation()
  const [searchParams]    = useSearchParams()

  /* When arriving from Quizzes -> "Questions", this pre-selects the quiz below */
  const quizIdFromUrl  = searchParams.get("quiz")
  const quizTitleFromUrl = location.state?.quizTitle

  const [quizzes, setQuizzes]               = useState([])
  const [selectedQuizId, setSelectedQuizId] = useState(quizIdFromUrl || "")
  const [questions, setQuestions]           = useState([])
  const [loading, setLoading]               = useState(false)
  const [quizLoading, setQuizLoading]       = useState(true)
  const [search, setSearch]                 = useState("")
  const [page, setPage]                     = useState(1)
  const [createOpen, setCreateOpen]         = useState(false)
  const [editTarget, setEditTarget]         = useState(null)
  const [deleteTarget, setDeleteTarget]     = useState(null)
  const [formLoading, setFormLoading]       = useState(false)
  const [formError, setFormError]           = useState("")

  useEffect(() => {
    const loadQuizzes = async () => {
      try {
        const data = await getQuizzesAPI()
        setQuizzes(data.quizzes || [])
      } catch {
        /* silently fail */
      } finally {
        setQuizLoading(false)
      }
    }
    loadQuizzes()
  }, [])

  const loadQuestions = async (quizId) => {
    try {
      setLoading(true)
      const data = await getQuestionsByQuizAPI(quizId)
      setQuestions(data.questions || [])
    } catch {
      setQuestions([])
    } finally {
      setLoading(false)
    }
  }

  /* If we arrived with a quiz already chosen (from the Quizzes page),
     load its questions immediately — no manual selection needed */
  useEffect(() => {
    if (quizIdFromUrl) {
      loadQuestions(quizIdFromUrl)
    }
  }, [quizIdFromUrl])

  const handleQuizChange = (e) => {
    const id = e.target.value
    setSelectedQuizId(id)
    setSearch("")
    setQuestions([])
    if (id) loadQuestions(id)
  }

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return questions
    return questions.filter((q_) =>
      q_.question_text.toLowerCase().includes(q)
    )
  }, [search, questions])

  /* Reset to page 1 whenever the search term or selected quiz changes */
  useEffect(() => { setPage(1) }, [search, selectedQuizId])

  const paginated = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE
    return filtered.slice(start, start + PAGE_SIZE)
  }, [filtered, page])

  const handleCreate = async (payload) => {
    try {
      setFormLoading(true)
      setFormError("")
      await createQuestionAPI(payload)
      setCreateOpen(false)
      loadQuestions(selectedQuizId)
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to create question")
    } finally {
      setFormLoading(false)
    }
  }

  const handleEdit = async (payload) => {
    try {
      setFormLoading(true)
      setFormError("")
      /* Remove quiz_id and question_type from update payload */
      const { quiz_id, question_type, ...updatePayload } = payload
      await updateQuestionAPI(editTarget.id, updatePayload)
      setEditTarget(null)
      loadQuestions(selectedQuizId)
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to update question")
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      setFormLoading(true)
      setFormError("")
      await deleteQuestionAPI(deleteTarget.id)
      setDeleteTarget(null)
      loadQuestions(selectedQuizId)
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to delete question")
    } finally {
      setFormLoading(false)
    }
  }

  const columns = [
    {
      key:    "question_text",
      label:  "Question",
      render: (value) => (
        <span className="text-slate-800 text-sm line-clamp-2 max-w-xs">{value}</span>
      )
    },
    {
      key:    "question_type",
      label:  "Type",
      render: (value) => (
        <Badge
          label={value === "mcq" ? "MCQ" : "True / False"}
          variant="info"
        />
      )
    },
    {
      key:    "difficulty",
      label:  "Difficulty",
      render: (value) => (
        <Badge
          label={value.charAt(0).toUpperCase() + value.slice(1)}
          variant={difficultyVariant[value] || "default"}
        />
      )
    },
    {
      key:   "marks",
      label: "Marks"
    },
    {
      key:    "actions",
      label:  "Actions",
      render: (_, row) => (
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => { setFormError(""); setEditTarget(row) }}
          >
            Edit
          </Button>
          <Button
            variant="danger"
            size="sm"
            onClick={() => { setFormError(""); setDeleteTarget(row) }}
          >
            Delete
          </Button>
        </div>
      )
    }
  ]

  return (
    <div>

      <PageHeader
        title={quizTitleFromUrl ? `${quizTitleFromUrl} — Questions` : "Questions"}
        subtitle="Manage questions per quiz"
        action={
          <div className="flex items-center gap-2">
            {quizIdFromUrl && (
              <Button
                variant="secondary"
                size="sm"
                onClick={() => navigate("/admin/quizzes")}
              >
                Back to Quizzes
              </Button>
            )}
            {selectedQuizId && (
              <Button onClick={() => { setFormError(""); setCreateOpen(true) }}>
                + Add Question
              </Button>
            )}
          </div>
        }
      />

      <div className="mb-6 max-w-sm">
        <Select
          label="Select Quiz"
          value={selectedQuizId}
          onChange={handleQuizChange}
          options={quizzes.map((q) => ({ value: q.id, label: q.title }))}
          disabled={quizLoading}
          placeholder="— Select a quiz to view questions —"
        />
      </div>

      {selectedQuizId && (
        <div className="mb-4 max-w-sm">
          <Input
            placeholder="Search questions..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      )}

      {!selectedQuizId ? (
        <EmptyState
          title="No quiz selected"
          description="Select a quiz above to view and manage its questions."
        />
      ) : filtered.length === 0 && search ? (
        <EmptyState
          title="No questions found"
          description={`No questions match "${search}"`}
        />
      ) : (
        <>
          <Table
            columns={columns}
            rows={paginated}
            loading={loading}
            emptyMessage="No questions yet. Add one to get started."
          />
          <Pagination
            currentPage={page}
            totalItems={filtered.length}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </>
      )}

      <Modal open={createOpen} title="Add Question" onClose={() => setCreateOpen(false)}>
        <QuestionForm
          quizId={selectedQuizId}
          existingQuestions={questions}
          onSubmit={handleCreate}
          onCancel={() => setCreateOpen(false)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

      <Modal open={!!editTarget} title="Edit Question" onClose={() => setEditTarget(null)}>
        <QuestionForm
          initial={editTarget}
          quizId={selectedQuizId}
          existingQuestions={questions}
          onSubmit={handleEdit}
          onCancel={() => setEditTarget(null)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

      <Modal open={!!deleteTarget} title="Delete Question" onClose={() => setDeleteTarget(null)}>
        <DeleteConfirm
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

    </div>
  )
}

export default AdminQuestions