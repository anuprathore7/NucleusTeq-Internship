/**
 * General helper functions used across the app.
 */

// format date to readable string
// Example: "2026-07-04T10:00:00Z" → "Jul 4, 2026"
export const formatDate = (dateString) => {
  if (!dateString) return "—"
  return new Date(dateString).toLocaleDateString("en-US", {
    year:  "numeric",
    month: "short",
    day:   "numeric"
  })
}

// format date with time
// Example: "Jul 4, 2026, 10:00 AM"
export const formatDateTime = (dateString) => {
  if (!dateString) return "—"
  return new Date(dateString).toLocaleString("en-US", {
    year:   "numeric",
    month:  "short",
    day:    "numeric",
    hour:   "2-digit",
    minute: "2-digit"
  })
}

// format seconds into MM:SS for timer
// Example: 1830 seconds → "30:30"
export const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, "0")}:${String(secs).padStart(2, "0")}`
}

// get error message from axios error response
export const getErrorMessage = (error, fallback = "Something went wrong. Please try again.") => {
  return error?.response?.data?.detail || fallback
}