import { useState, useEffect, useMemo } from "react"
import { useNavigate }                  from "react-router-dom"

import { getMyResultsAPI } from "../../api/result.api"

import PageHeader  from "../../components/common/PageHeader"
import Table       from "../../components/common/Table"
import Pagination  from "../../components/common/Pagination"
import Badge       from "../../components/common/Badge"
import Button      from "../../components/common/Button"
import Spinner     from "../../components/common/Spinner"
import EmptyState  from "../../components/common/EmptyState"

import { formatDateTime } from "../../utils/helpers"

/* Rows shown per page on the results history table */
const PAGE_SIZE = 10

const StudentHistory = () => {

  const navigate = useNavigate()

  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage]       = useState(1)

  useEffect(() => {

    const loadResults = async () => {
      try {
        const data = await getMyResultsAPI()
        setResults(data.results || [])
      } catch {
        /* silently fail */
      } finally {
        setLoading(false)
      }
    }

    loadResults()

  }, [])

  const paginated = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE
    return results.slice(start, start + PAGE_SIZE)
  }, [results, page])

  const columns = [
    {
      key:   "quiz_title",
      label: "Quiz Name"
    },
    {
      key:    "score",
      label:  "Score",
      render: (value, row) => `${value} / ${row.total_marks}`
    },
    {
      key:    "percentage",
      label:  "Percentage",
      render: (value) => `${value}%`
    },
    {
      key:    "passed",
      label:  "Status",
      render: (value) => (
        <Badge
          label={value ? "Passed" : "Failed"}
          variant={value ? "success" : "error"}
        />
      )
    },
    {
      key:    "submitted_at",
      label:  "Date",
      render: (value) => formatDateTime(value)
    },
    {
      key:    "attempt_id",
      label:  "Details",
      render: (value) => (
        <button
          onClick={() => navigate(`/student/result/${value}`)}
          className="text-sm text-primary-600 hover:text-primary-700 font-medium transition-colors hover:underline underline-offset-2"
        >
          View
        </button>
      )
    }
  ]

  if (loading) return <Spinner fullPage />

  return (
    <div>

      <PageHeader
        title="My Results"
        subtitle="History of all your quiz attempts"
        action={
          <Button
            variant="secondary"
            onClick={() => navigate("/student/categories")}
          >
            Browse Categories
          </Button>
        }
      />

      {results.length === 0 ? (
        <EmptyState
          title="No results yet"
          description="Complete a quiz to see your results here."
          action={
            <Button onClick={() => navigate("/student/categories")}>
              Browse Categories
            </Button>
          }
        />
      ) : (
        <>
          <Table
            columns={columns}
            rows={paginated}
            emptyMessage="No results found"
          />
          <Pagination
            currentPage={page}
            totalItems={results.length}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </>
      )}

    </div>
  )
}

export default StudentHistory