import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";

const Input = ({
  label,
  type = "text",
  placeholder = "",
  value,
  onChange,
  error = "",
  hint = "",
  disabled = false,
  required = false,
  name,
  id,
  autoComplete,
  onBlur,
  maxLength,
}) => {
  const inputId = id || name || label?.toLowerCase().replace(/\s+/g, "-");

  const [showPassword, setShowPassword] = useState(false);

  const isPassword = type === "password";

  const inputType = isPassword && showPassword ? "text" : type;

  const isValid = value && value.trim() && !error;

  return (
    <div className="flex flex-col gap-1.5 w-full">
      {/* ---------------- Label ---------------- */}

      {label && (
        <label htmlFor={inputId} className="text-sm font-medium text-slate-700">
          {label}

          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}

      {/* Input Wrapper */}

      <div className="relative">
        <input
          id={inputId}
          name={name}
          type={inputType}
          value={value}
          placeholder={placeholder}
          onChange={onChange}
          onBlur={onBlur}
          disabled={disabled}
          autoComplete={autoComplete}
          maxLength={maxLength}
          aria-invalid={!!error}
          aria-describedby={`${inputId}-message`}
          className={`
            w-full
            rounded-lg
            border
            px-4
            py-3
            text-sm
            bg-white
            transition-all
            duration-200
            outline-none

            placeholder:text-slate-400

            ${
              error
                ? "border-red-500 focus:ring-2 focus:ring-red-200"
                : isValid
                  ? "border-green-500 focus:ring-2 focus:ring-green-200"
                  : "border-slate-300 focus:ring-2 focus:ring-primary-200 focus:border-primary-500"
            }

            ${disabled ? "bg-slate-100 cursor-not-allowed text-slate-400" : ""}

            ${isPassword ? "pr-12" : ""}
          `}
        />

        {/* Password Toggle */}

        {isPassword && (
          <button
            type="button"
            onClick={() => setShowPassword((prev) => !prev)}
            className="
              absolute
              right-3
              top-1/2
              -translate-y-1/2
              text-slate-500
              hover:text-slate-700
            "
          >
            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>

      {/* Character Count */}

      {maxLength && value && (
        <p className="text-xs text-slate-400 text-right">
          {value.length}/{maxLength}
        </p>
      )}

      {/* Error */}

      {error ? (
        <p id={`${inputId}-message`} className="text-xs text-red-600">
          {error}
        </p>
      ) : (
        hint && (
          <p id={`${inputId}-message`} className="text-xs text-slate-500">
            {hint}
          </p>
        )
      )}
    </div>
  );
};

export default Input;
