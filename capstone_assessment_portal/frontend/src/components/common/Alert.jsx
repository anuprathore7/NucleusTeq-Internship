/**
 *
 * Alert Component
 * 
 *
 * Reusable alert component for displaying:
 *
 * - Error
 * - Success
 * - Warning
 * - Info
 */

const styles = {
  error: {
    wrapper:
      "border border-red-200 bg-red-50 text-red-700",
    title: "Error"
  },

  success: {
    wrapper:
      "border border-green-200 bg-green-50 text-green-700",
    title: "Success"
  },

  warning: {
    wrapper:
      "border border-yellow-200 bg-yellow-50 text-yellow-700",
    title: "Warning"
  },

  info: {
    wrapper:
      "border border-blue-200 bg-blue-50 text-blue-700",
    title: "Information"
  }
}

const Alert = ({
  type = "error",
  message,
  className = ""
}) => {

  if (!message) return null

  const config = styles[type]

  return (
    <div
      role="alert"
      className={`
        rounded-lg
        px-4
        py-3
        text-sm
        ${config.wrapper}
        ${className}
      `}
    >
      <p className="font-semibold mb-1">
        {config.title}
      </p>

      <p className="leading-6">
        {message}
      </p>
    </div>
  )
}

export default Alert