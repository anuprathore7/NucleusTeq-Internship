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

import { BUTTON_VARIANT_CLASSES, BUTTON_SIZE_CLASSES } from "./Button.constants"

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

        ${BUTTON_VARIANT_CLASSES[variant]}
        ${BUTTON_SIZE_CLASSES[size]}
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