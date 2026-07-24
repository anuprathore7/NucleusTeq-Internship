import { useState, useEffect, useMemo } from "react"
import { useNavigate, useLocation, useSearchParams } from "react-router-dom"
import { ListChecks } from "lucide-react"

import { getCategoriesAPI }                                                                from "../../api/category.api"
import { getQuizzesAPI, getQuizzesByCategoryAPI, createQuizAPI, updateQuizAPI, deleteQuizAPI } from "../../api/quiz.api"

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
  validateQuizTitle,
  validateQuizDescription,
  validateTimeLimit,
  validatePassPercentage
} from "../../utils/validators"

import { formatDateTime } from "../../utils/helpers"

/* Rows shown per page across the quizzes table */
const PAGE_SIZE = 10

/* Checks whether a quiz with the same title already exists in the same
   category, case-insensitive and trimmed, excluding the quiz being edited */
const isDuplicateQuizTitle = (title, categoryId, existingQuizzes, excludeId) => {
  const cleaned = title.trim().toLowerCase()
  return existingQuizzes.some((q) =>
    q.id !== excludeId &&
    q.category_id === categoryId &&
    q.title.trim().toLowerCase() === cleaned
  )
}

/* Quiz form used for both create and edit */
const QuizForm = ({ initial, categories, existingQuizzes, onSubmit, onCancel, loading, error }) => {

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

    if (!e.title && !e.category_id) {
      if (isDuplicateQuizTitle(form.title, form.category_id, existingQuizzes, initial?.id)) {
        e.title = "A quiz with this title already exists in this category"
      }
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
        hint="Must be meaningful and unique within the selected category."
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

      <Select
        label="Category"
        value={form.category_id}
        onChange={update("category_id")}
        options={categories.map((c) => ({ value: c.id, label: c.name }))}
        error={errors.category_id}
        placeholder="Select a category"
        required
      />

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

/* Delete confirmation modal body */
const DeleteConfirm = ({ quiz, onConfirm, onCancel, loading, error }) => (
  <div className="flex flex-col gap-4">
    {error && <Alert type="error" message={error} />}
    <p className="text-sm text-slate-600">
      Delete{" "}
      <span className="font-semibold text-slate-900">"{quiz?.title}"</span>?
      This will also remove every question linked to it.
    </p>
    <div className="flex justify-end gap-3">
      <Button variant="secondary" onClick={onCancel} disabled={loading}>Cancel</Button>
      <Button variant="danger" onClick={onConfirm} loading={loading}>Delete</Button>
    </div>
  </div>
)

const AdminQuizzes = () => {

  const navigate        = useNavigate()
  const location         = useLocation()
  const [searchParams]   = useSearchParams()

  const categoryIdFilter   = searchParams.get("category")
  const categoryNameFilter = location.state?.categoryName

  const [quizzes, setQuizzes]       = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading]       = useState(true)
  const [search, setSearch]         = useState("")
  const [page, setPage]             = useState(1)
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [formLoading, setFormLoading]   = useState(false)
  const [formError, setFormError]       = useState("")

  const loadData = async () => {
    try {
      setLoading(true)
      const [quizData, catData] = await Promise.all([
        categoryIdFilter ? getQuizzesByCategoryAPI(categoryIdFilter) : getQuizzesAPI(),
        getCategoriesAPI()
      ])
      setQuizzes(quizData.quizzes       || [])
      setCategories(catData.categories  || [])
    } catch {
      /* silently fail */
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [categoryIdFilter])

  const categoryMap = useMemo(() => {
    const map = {}
    categories.forEach((c) => { map[c.id] = c.name })
    return map
  }, [categories])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return quizzes
    return quizzes.filter((quiz) =>
      quiz.title.toLowerCase().includes(q)
    )
  }, [search, quizzes])

  useEffect(() => { setPage(1) }, [search, categoryIdFilter])

  const paginated = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE
    return filtered.slice(start, start + PAGE_SIZE)
  }, [filtered, page])

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

  const handleViewQuestions = (quiz) => {
    navigate(`/admin/questions?quiz=${quiz.id}`, {
      state: { quizTitle: quiz.title }
    })
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
      key:    "category_id",
      label:  "Category",
      render: (value) => (
        <span className="text-slate-600 text-sm">{categoryMap[value] || "—"}</span>
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
        <div className="flex items-center gap-3">

          {/* Distinct styling on purpose — this is the most-used action on this
              page, so it should stand out from the plain Edit/Delete pair */}
          <button
            onClick={() => handleViewQuestions(row)}
            className="
              flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg
              text-sm font-medium text-primary-600 bg-primary-50
              hover:bg-primary-100 hover:text-primary-700
              transition-colors
            "
          >
            <ListChecks size={15} />
            Questions
          </button>

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
        title={categoryNameFilter ? `${categoryNameFilter} — Quizzes` : "Quizzes"}
        subtitle={`${quizzes.length} quiz${quizzes.length !== 1 ? "es" : ""} total`}
        action={
          <div className="flex items-center gap-2">
            {categoryIdFilter && (
              <Button
                variant="secondary"
                size="sm"
                onClick={() => navigate("/admin/categories")}
              >
                Back to Categories
              </Button>
            )}
            <Button onClick={() => { setFormError(""); setCreateOpen(true) }}>
              + New Quiz
            </Button>
          </div>
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
        <>
          <Table
            columns={columns}
            rows={paginated}
            loading={loading}
            emptyMessage="No quizzes yet. Create one to get started."
          />
          <Pagination
            currentPage={page}
            totalItems={filtered.length}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </>
      )}

      <Modal open={createOpen} title="Create Quiz" onClose={() => setCreateOpen(false)}>
        <QuizForm
          categories={categories}
          existingQuizzes={quizzes}
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
          existingQuizzes={quizzes}
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