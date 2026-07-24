import { useState, useEffect, useMemo } from "react"
import { useNavigate } from "react-router-dom"

import {
  getCategoriesAPI,
  createCategoryAPI,
  updateCategoryAPI,
  deleteCategoryAPI
} from "../../api/category.api"

import { getQuizzesByCategoryAPI } from "../../api/quiz.api"
import { getQuestionsByQuizAPI }   from "../../api/question.api"

import PageHeader from "../../components/common/PageHeader"
import Button     from "../../components/common/Button"
import Input      from "../../components/common/Input"
import Alert      from "../../components/common/Alert"
import Modal      from "../../components/common/Modal"
import EmptyState from "../../components/common/EmptyState"
import Spinner    from "../../components/common/Spinner"

import {
  validateCategoryName,
  validateCategoryDescription
} from "../../utils/validators"

/**
 * Fetches quiz count and total question count for a given category.
 * Returns { quizCount, questionCount }.
 */
const fetchCategoryStats = async (categoryId) => {
  try {
    const quizData  = await getQuizzesByCategoryAPI(categoryId)
    const quizzes   = quizData.quizzes || []
    let questionCount = 0

    await Promise.all(
      quizzes.map(async (quiz) => {
        try {
          const qData = await getQuestionsByQuizAPI(quiz.id)
          questionCount += qData.questions?.length || 0
        } catch {
          /* skip failed quiz */
        }
      })
    )

    return { quizCount: quizzes.length, questionCount }
  } catch {
    return { quizCount: 0, questionCount: 0 }
  }
}

/**
 * Category card — shows name, description, quiz count, question count.
 * "View Quizzes" navigates to the Quizzes page pre-filtered to this category.
 */
const CategoryCard = ({ category, stats, onEdit, onDelete, onViewQuizzes }) => (
  <div className="bg-white rounded-xl border border-slate-200 p-5 flex flex-col gap-4 hover:shadow-md transition-shadow">

    {/* Icon + name */}
    <div className="flex items-start justify-between gap-3">

      <div className="flex items-start gap-3 flex-1 min-w-0">
        <div className="w-10 h-10 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
          <svg className="w-5 h-5 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        </div>
        <div className="min-w-0">
          <h3 className="text-sm font-semibold text-slate-900 truncate">{category.name}</h3>
          <p className="text-xs text-slate-500 mt-1 line-clamp-2 leading-relaxed">
            {category.description}
          </p>
        </div>
      </div>

    </div>

    {/* Stats */}
    <div className="flex items-center gap-4 border-t border-slate-100 pt-3">
      <div className="flex items-center gap-1.5">
        <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        <span className="text-xs text-slate-500">
          <span className="font-semibold text-slate-700">{stats?.quizCount ?? "—"}</span> Quizzes
        </span>
      </div>
      <div className="flex items-center gap-1.5">
        <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span className="text-xs text-slate-500">
          <span className="font-semibold text-slate-700">{stats?.questionCount ?? "—"}</span> Questions
        </span>
      </div>
    </div>

    {/* View Quizzes — takes admin straight to this category's quizzes */}
    <Button
      variant="secondary"
      size="sm"
      fullWidth
      onClick={() => onViewQuizzes(category)}
    >
      View Quizzes
    </Button>

    {/* Edit / Delete */}
    <div className="flex items-center gap-2">
      <Button
        variant="secondary"
        size="sm"
        fullWidth
        onClick={() => onEdit(category)}
      >
        Edit
      </Button>
      <Button
        variant="danger"
        size="sm"
        fullWidth
        onClick={() => onDelete(category)}
      >
        Delete
      </Button>
    </div>

  </div>
)

/**
 * Category form used in both create and edit modals.
 * Validates name and description before submitting.
 */
const CategoryForm = ({ initial, onSubmit, onCancel, loading, error }) => {

  const [name, setName]               = useState(initial?.name        || "")
  const [description, setDescription] = useState(initial?.description || "")
  const [errors, setErrors]           = useState({ name: "", description: "" })

  const validate = () => {
    const nameErr = validateCategoryName(name)
    const descErr = validateCategoryDescription(description)
    setErrors({ name: nameErr, description: descErr })
    return !nameErr && !descErr
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!validate()) return
    onSubmit({ name: name.trim(), description: description.trim() })
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">

      {error && <Alert type="error" message={error} />}

      <Input
        label="Category Name"
        placeholder="e.g. Python Programming"
        value={name}
        onChange={(e) => {
          setName(e.target.value)
          if (errors.name) setErrors((p) => ({ ...p, name: "" }))
        }}
        error={errors.name}
        hint="Must contain letters. No repetitive text like '111' or 'aaa'."
        required
        maxLength={100}
      />

      <Input
        label="Description"
        placeholder="Briefly describe what this category covers"
        value={description}
        onChange={(e) => {
          setDescription(e.target.value)
          if (errors.description) setErrors((p) => ({ ...p, description: "" }))
        }}
        error={errors.description}
        hint="Must be meaningful text with at least one letter."
        required
        maxLength={500}
      />

      <div className="flex justify-end gap-3 pt-2">
        <Button variant="secondary" type="button" onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          {initial ? "Save Changes" : "Create Category"}
        </Button>
      </div>

    </form>
  )
}

/**
 * Delete confirmation modal body.
 */
const DeleteConfirm = ({ category, onConfirm, onCancel, loading, error }) => (
  <div className="flex flex-col gap-4">
    {error && <Alert type="error" message={error} />}
    <p className="text-sm text-slate-600">
      Are you sure you want to delete{" "}
      <span className="font-semibold text-slate-900">"{category?.name}"</span>?
      This will also remove every quiz and question linked to it.
    </p>
    <div className="flex justify-end gap-3">
      <Button variant="secondary" onClick={onCancel} disabled={loading}>Cancel</Button>
      <Button variant="danger" onClick={onConfirm} loading={loading}>Delete</Button>
    </div>
  </div>
)

const AdminCategories = () => {

  const navigate = useNavigate()

  const [categories, setCategories] = useState([])
  const [statsMap, setStatsMap]     = useState({})
  const [loading, setLoading]       = useState(true)
  const [search, setSearch]         = useState("")
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [formLoading, setFormLoading]   = useState(false)
  const [formError, setFormError]       = useState("")

  const loadCategories = async () => {
    try {
      setLoading(true)
      const data = await getCategoriesAPI()
      const cats = data.categories || []
      setCategories(cats)

      /* Load stats for all categories in parallel */
      const statsEntries = await Promise.all(
        cats.map(async (cat) => {
          const stats = await fetchCategoryStats(cat.id)
          return [cat.id, stats]
        })
      )
      setStatsMap(Object.fromEntries(statsEntries))

    } catch {
      /* silently fail */
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadCategories() }, [])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return categories
    return categories.filter(
      (c) =>
        c.name.toLowerCase().includes(q) ||
        c.description.toLowerCase().includes(q)
    )
  }, [search, categories])

  const handleCreate = async (payload) => {
    try {
      setFormLoading(true)
      setFormError("")
      await createCategoryAPI(payload)
      setCreateOpen(false)
      loadCategories()
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to create category")
    } finally {
      setFormLoading(false)
    }
  }

  const handleEdit = async (payload) => {
    try {
      setFormLoading(true)
      setFormError("")
      await updateCategoryAPI(editTarget.id, payload)
      setEditTarget(null)
      loadCategories()
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to update category")
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      setFormLoading(true)
      setFormError("")
      await deleteCategoryAPI(deleteTarget.id)
      setDeleteTarget(null)
      loadCategories()
    } catch (err) {
      setFormError(err?.response?.data?.detail || "Failed to delete category")
    } finally {
      setFormLoading(false)
    }
  }

  /* Sends admin to the Quizzes page, pre-filtered to this category */
  const handleViewQuizzes = (category) => {
    navigate(`/admin/quizzes?category=${category.id}`, {
      state: { categoryName: category.name }
    })
  }

  return (
    <div>

      <PageHeader
        title="Categories"
        subtitle={`${categories.length} categories total`}
        action={
          <Button onClick={() => { setFormError(""); setCreateOpen(true) }}>
            + New Category
          </Button>
        }
      />

      {/* Search */}
      <div className="mb-6 max-w-sm">
        <Input
          placeholder="Search by name or description..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Cards grid */}
      {loading ? (
        <div className="flex justify-center py-16">
          <Spinner size="lg" />
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState
          title={search ? "No categories found" : "No categories yet"}
          description={
            search
              ? `No results for "${search}"`
              : "Create your first category to get started."
          }
          action={
            !search && (
              <Button onClick={() => { setFormError(""); setCreateOpen(true) }}>
                + New Category
              </Button>
            )
          }
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filtered.map((cat) => (
            <CategoryCard
              key={cat.id}
              category={cat}
              stats={statsMap[cat.id]}
              onEdit={(c) => { setFormError(""); setEditTarget(c) }}
              onDelete={(c) => { setFormError(""); setDeleteTarget(c) }}
              onViewQuizzes={handleViewQuizzes}
            />
          ))}
        </div>
      )}

      {/* Create Modal */}
      <Modal
        open={createOpen}
        title="Create Category"
        onClose={() => setCreateOpen(false)}
      >
        <CategoryForm
          onSubmit={handleCreate}
          onCancel={() => setCreateOpen(false)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

      {/* Edit Modal */}
      <Modal
        open={!!editTarget}
        title="Edit Category"
        onClose={() => setEditTarget(null)}
      >
        <CategoryForm
          initial={editTarget}
          onSubmit={handleEdit}
          onCancel={() => setEditTarget(null)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

      {/* Delete Modal */}
      <Modal
        open={!!deleteTarget}
        title="Delete Category"
        onClose={() => setDeleteTarget(null)}
      >
        <DeleteConfirm
          category={deleteTarget}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={formLoading}
          error={formError}
        />
      </Modal>

    </div>
  )
}

export default AdminCategories