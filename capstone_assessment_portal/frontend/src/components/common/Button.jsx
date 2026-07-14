/**
 *
 * Reusable Button Component
 * 
 *
 * Props
 * -----
 * variant   : primary | secondary | danger | ghost
 * size      : sm | md | lg
 * loading   : boolean
 * disabled  : boolean
 * fullWidth : boolean
 * leftIcon  : ReactNode
 * rightIcon : ReactNode
 *
 * 
 */

const variantClasses = {
  primary:
    "bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500",

  secondary:
    "bg-white border border-slate-300 text-slate-700 hover:bg-slate-50 focus:ring-slate-400",

  danger:
    "bg-red-600 text-white hover:bg-red-700 focus:ring-red-500",

  ghost:
    "bg-transparent text-slate-700 hover:bg-slate-100 focus:ring-slate-300",
}

const sizeClasses = {
  sm: "px-3 py-2 text-sm",
  md: "px-4 py-2.5 text-sm",
  lg: "px-5 py-3 text-base",
}

const Button = ({
  children,
  type = "button",

  variant = "primary",
  size = "md",

  loading = false,
  disabled = false,

  fullWidth = false,

  leftIcon,
  rightIcon,

  onClick,
  className = "",
}) => {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      onClick={onClick}
      className={`
        inline-flex
        items-center
        justify-center
        gap-2

        rounded-lg
        font-medium

        transition-all
        duration-200

        focus:outline-none
        focus:ring-2
        focus:ring-offset-2

        disabled:opacity-60
        disabled:cursor-not-allowed

        ${variantClasses[variant]}
        ${sizeClasses[size]}
        ${fullWidth ? "w-full" : ""}
        ${className}
      `}
    >
      {/* Loading Spinner */}

      {loading && (
        <span
          className="
            h-4
            w-4
            animate-spin
            rounded-full
            border-2
            border-white/40
            border-t-white
          "
        />
      )}

      {/* Left Icon */}

      {!loading && leftIcon}

      {/* Button Text */}

      <span>
        {loading ? "Please wait..." : children}
      </span>

      {/* Right Icon */}

      {!loading && rightIcon}
    </button>
  )
}

export default Button