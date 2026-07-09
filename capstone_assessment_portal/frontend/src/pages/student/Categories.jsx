/**
 * Student Categories Page
 *
 * Student can:
 * - Browse all available categories
 * - Search categories by name
 * - Click a category to see its quizzes
 */

import { useState, useEffect, useMemo } from "react"
import { useNavigate } from "react-router-dom"

import { getCategoriesAPI } from "../../api/category.api"

import PageHeader from "../../components/common/PageHeader"
import Input      from "../../components/common/Input"
import EmptyState from "../../components/common/EmptyState"
import Spinner    from "../../components/common/Spinner"

/*  Category Card  */

const CategoryCard = ({ category, onClick }) => (
  <button
    onClick={() => onClick(category)}
    className="
      w-full text-left
      bg-white rounded-xl border border-slate-200
      p-5 hover:border-primary-400 hover:shadow-md
      transition-all duration-200
      group
    "
  >

    {/* Icon */}
    <div className="w-10 h-10 rounded-lg bg-primary-50 flex items-center justify-center mb-4 group-hover:bg-primary-100 transition-colors">
      <svg className="w-5 h-5 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
      </svg>
    </div>

    {/* Name */}
    <h3 className="text-sm font-semibold text-slate-900 mb-1 group-hover:text-primary-600 transition-colors">
      {category.name}
    </h3>

    {/* Description */}
    <p className="text-xs text-slate-500 line-clamp-2 leading-relaxed">
      {category.description}
    </p>

    {/* Arrow */}
    <div className="mt-4 flex items-center gap-1 text-xs text-primary-600 font-medium opacity-0 group-hover:opacity-100 transition-opacity">
      View Quizzes
      <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
      </svg>
    </div>

  </button>
)

/*  Main Component  */

const StudentCategories = () => {

  const navigate = useNavigate()

  const [categories, setCategories] = useState([])
  const [loading, setLoading]       = useState(true)
  const [search, setSearch]         = useState("")

  useEffect(() => {
    const fetch = async () => {
      try {
        const data = await getCategoriesAPI()
        setCategories(data.categories || [])
      } catch {
        /* silently fail */
      } finally {
        setLoading(false)
      }
    }
    fetch()
  }, [])

  /* filter in memory */
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return categories
    return categories.filter(
      (c) =>
        c.name.toLowerCase().includes(q) ||
        c.description.toLowerCase().includes(q)
    )
  }, [search, categories])

  const handleCategoryClick = (category) => {
    navigate(`/student/quizzes?category=${category.id}`, {
      state: { categoryName: category.name }
    })
  }

  if (loading) return <Spinner fullPage />

  return (
    <div>

      <PageHeader
        title="Categories"
        subtitle="Choose a category to start your quiz"
      />

      {/* Search */}
      <div className="mb-6 max-w-sm">
        <Input
          placeholder="Search categories..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Grid */}
      {filtered.length === 0 ? (
        <EmptyState
          title={search ? "No categories found" : "No categories available"}
          description={
            search
              ? `No results for "${search}"`
              : "Categories will appear here once the admin creates them."
          }
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filtered.map((category) => (
            <CategoryCard
              key={category.id}
              category={category}
              onClick={handleCategoryClick}
            />
          ))}
        </div>
      )}

    </div>
  )
}

export default StudentCategories