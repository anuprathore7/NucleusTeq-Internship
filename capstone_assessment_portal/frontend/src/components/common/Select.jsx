import { ChevronDown } from "lucide-react"

/* Reusable styled dropdown, matches Input component's visual language */
const Select = ({
  label,
  value,
  onChange,
  options,
  error = "",
  hint = "",
  disabled = false,
  required = false,
  placeholder = "Select an option"
}) => {
  return (
    <div className="flex flex-col gap-1.5 w-full">
      {label && (
        <label className="text-sm font-medium text-slate-700">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}

      <div className="relative">
        <select
          value={value}
          onChange={onChange}
          disabled={disabled}
          className={`
            w-full appearance-none rounded-lg border px-4 py-3 pr-10
            text-sm bg-white transition-all duration-200 outline-none
            ${error
              ? "border-red-500 focus:ring-2 focus:ring-red-200"
              : "border-slate-300 focus:ring-2 focus:ring-primary-200 focus:border-primary-500"
            }
            ${disabled ? "bg-slate-100 cursor-not-allowed text-slate-400" : "text-slate-800"}
          `}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>

        <ChevronDown
          size={16}
          className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none"
        />
      </div>

      {error ? (
        <p className="text-xs text-red-600">{error}</p>
      ) : (
        hint && <p className="text-xs text-slate-500">{hint}</p>
      )}
    </div>
  )
}

export default Select