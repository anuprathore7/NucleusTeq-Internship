/**
 * Spinner — loading indicator.
 * fullPage → centered in full screen
 */

const Spinner = ({ fullPage = false, size = "md" }) => {
  const sizeMap = {
    sm: "w-5 h-5 border-2",
    md: "w-8 h-8 border-[3px]",
    lg: "w-12 h-12 border-4"
  }

  const spin = (
    <div
      className={`
        ${sizeMap[size]}
        rounded-full
        border-slate-200
        border-t-primary-600
        animate-spin
      `}
    />
  )

  if (fullPage) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        {spin}
      </div>
    )
  }

  return spin
}

export default Spinner