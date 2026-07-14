import { useState, useEffect, useMemo } from "react"

import { getCategoriesAPI }                                         from "../../api/category.api"
import { getQuizzesAPI, createQuizAPI, updateQuizAPI, deleteQuizAPI } from "../../api/quiz.api"

import PageHeader from "../../components/common/PageHeader"
import Table      from "../../components/common/Table"
import Button     from "../../components/common/Button"
import Input      from "../../components/common/Input"
import Alert      from "../../components/common/Alert"
import Modal      from "../../components/common/Modal"
import Badge      from "../../components/common/Badge"
import EmptyState from "../../components/common/EmptyState"

import {
  validateQuizTitle,
  validateQuizDescription,
  validateTimeLimit,
  validatePassPercentage
} from "../../utils/validators"

import { formatDateTime } from "../../utils/helpers"

/**
 * Quiz form used for both create and edit.
 */
const QuizForm = ({ initial, categories, onSubmit, onCancel, loading, error }) => {

 const [form, setForm] = useState({
  title:           initial?.title           || "",
  description:     initial?.description     || "",
  category_id:     initial?.category_id     || "",
  time_limit:      initial?.time_limit != null ? String(initial.time_limit) : "",
  pass_percentage: initial?.pass_percentage != null ? String(initial.pass_percentage) : ""
})

  const [errors, setErrors] = useState({})

  const update = (field) => (e) => {
    setForm((p) => ({ ...p, [field]: e.target.value }))
    if (errors[field]) setErrors((p) => ({ ...p, [field]: "" }))
  }

  const validate = () => {
    const e = {
      title:           validateQuizTitle(form.title),
      description:     validateQuizDescription(form.description),
      category_id:     !form.category_id ? "Please select a category" : "",
      time_limit:      validateTimeLimit(form.time_limit),
      pass_percentage: validatePassPercentage(form.pass_percentage)
    }
    setErrors(e)
    return Object.values(e).every((v) => !v)
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!validate()) return
    onSubmit({
      title:           form.title.trim(),
      description:     form.description.trim(),
      category_id:     form.category_id,
      time_limit:      Number(form.time_limit),
      pass_percentage: Number(form.pass_percentage)
    })
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4 max-h-[70vh] overflow-y-auto pr-1">

      {error && <Alert type="error" message={error} />}

      <Input
        label="Quiz Title"
        placeholder="e.g. Python Basics Test"
        value={form.title}
        onChange={update("title")}
        error={errors.title}
        hint="Must be meaningful text with at least one letter."
        required
        maxLength={200}
      />

      <Input
        label="Description"
        placeholder="Describe what this quiz covers"
        value={form.description}
        onChange={update("description")}
        error={errors.description}
        hint="Must be meaningful. Repetitive text like '1111' is not allowed."
        required
        maxLength={1000}
      />

      {/** Category select */}
      <div className="flex flex-col gap-1.5">
        <label className="text-sm font-medium text-slate-700">
          Category <span className="text-red-500">*</span>
        </label>
        <select
          value={form.category_id}
          onChange={update("category_id")}
          className={`
            w-full px-4 py-2.5 text-sm rounded-lg border bg-white
            text-slate-900 outline-none transition duration-200
            focus:ring-2 focus:border-transparent
            ${errors.category_id
              ? "border-red-400 focus:ring-red-400"
              : "border-slate-200 focus:ring-primary-500"
            }
          `}
        >
          <option value="">Select a category</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        {errors.category_id && (
          <p className="text-xs text-red-500">{errors.category_id}</p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Input
          label="Time Limit (min)"
          type="number"
          placeholder="30"
          value={form.time_limit}
          onChange={update("time_limit")}
          error={errors.time_limit}
          required
        />
        <Input
          label="Pass Percentage (%)"
          type="number"
          placeholder="60"
          value={form.pass_percentage}
          onChange={update("pass_percentage")}
          error={errors.pass_percentage}
          required
        />
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <Button variant="secondary" type="button" onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          {initial ? "Save Changes" : "Create Quiz"}
        </Button>
      </div>

    </form>
  )
}

/**
 * Delete confirm modal body.
 */
const DeleteConfirm = ({ quiz, onConfirm, onCancel, loading, error }) => (
  <div className="flex flex-col gap-4">
    {error && <Alert type="error" message={error} />}
    <p className="text-sm text-slate-600">
      Delete{" "}
      <span className="font-semibold text-slate-900">"{quiz?.title}"</span>?
      This will fail if questions are still linked to it.
    </p>
    <div className="flex justify-end gap-3">
      <Button variant="secondary" onClick={onCancel} disabled={loading}>Cancel</Button>
      <Button variant="danger" onClick={onConfirm} loading={loading}>Delete</Button>
    </div>
  </div>
)

const AdminQuizzes = () => {

  const [quizzes, setQuizzes]       = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading]       = useState(true)
  const [search, setSearch]         = useState("")
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [formLoading, setFormLoading]   = useState(false)
  const [formError, setFormError]       = useState("")

  const loadData = async () => {
    try {
      setLoading(true)
      const [quizData, catData] = await Promise.all([
        getQuizzesAPI(),
        getCategoriesAPI()
      ])
      setQuizzes(quizData.quizzes       || [])
      setCategories(catData.categories  || [])
    } catch {
      /** silently fail */
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return quizzes
    return quizzes.filter((quiz) =>
      quiz.title.toLowerCase().includes(q)
    )
  }, [search, quizzes])

  const handleCreate = async (payload) => {
    try {
      setFormLoading(true)
      setFormError("")
      await createQuizAPI(payload)
      setCreateOpen(false)
      loadData()
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to create quiz")
    } finally {
      setFormLoading(false)
    }
  }

  const handleEdit = async (payload) => {
    try {
      setFormLoading(true)
      setFormError("")
      await updateQuizAPI(editTarget.id, payload)
      setEditTarget(null)
      loadData()
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to update quiz")
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      setFormLoading(true)
      setFormError("")
      await deleteQuizAPI(deleteTarget.id)
      setDeleteTarget(null)
      loadData()
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to delete quiz")
    } finally {
      setFormLoading(false)
    }
  }

  const columns = [
    {
      key:    "title",
      label:  "Quiz Title",
      render: (value) => (
        <span className="font-medium text-slate-900">{value}</span>
      )
    },
    {
      key:    "time_limit",
      label:  "Duration",
      render: (value) => `${value} min`
    },
    {
      key:    "pass_percentage",
      label:  "Pass %",
      render: (value) => (
        <Badge label={`${value}%`} variant="info" />
      )
    },
    {
      key:    "created_at",
      label:  "Created",
      render: (value) => formatDateTime(value)
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
        title="Quizzes"
        subtitle={`${quizzes.length} quizzes total`}
        action={
          <Button onClick={() => { setFormError(""); setCreateOpen(true) }}>
            + New Quiz
          </Button>
        }
      />

      <div className="mb-4 max-w-sm">
        <Input
          placeholder="Search quizzes..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {!loading && filtered.length === 0 && search ? (
        <EmptyState title="No results" description={`No quizzes match "${search}"`} />
      ) : (
        <Table
          columns={columns}
          rows={filtered}
          loading={loading}
          emptyMessage="No quizzes yet. Create one to get started."
        />
      )}

      <Modal open={createOpen} title="Create Quiz" onClose={() => setCreateOpen(false)}>
        <QuizForm
          categories={categories}
          onSubmit={handleCreate}
          onCancel={() => setCreateOpen(false)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

      <Modal open={!!editTarget} title="Edit Quiz" onClose={() => setEditTarget(null)}>
        <QuizForm
          initial={editTarget}
          categories={categories}
          onSubmit={handleEdit}
          onCancel={() => setEditTarget(null)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

      <Modal open={!!deleteTarget} title="Delete Quiz" onClose={() => setDeleteTarget(null)}>
        <DeleteConfirm
          quiz={deleteTarget}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

    </div>
  )
}

export default AdminQuizzes