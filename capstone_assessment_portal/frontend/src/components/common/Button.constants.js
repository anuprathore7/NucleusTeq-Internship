/**
 * Style maps for the Button component.
 * Pulled out into their own file per code review — keeps Button.jsx
 * focused on structure/behavior, not styling constants.
 */

export const BUTTON_VARIANT_CLASSES = {
  primary:
    "bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500",

  secondary:
    "bg-white border border-slate-300 text-slate-700 hover:bg-slate-50 focus:ring-slate-400",

  danger:
    "bg-red-600 text-white hover:bg-red-700 focus:ring-red-500",

  ghost:
    "bg-transparent text-slate-700 hover:bg-slate-100 focus:ring-slate-300",
}

export const BUTTON_SIZE_CLASSES = {
  sm: "px-3 py-2 text-sm",
  md: "px-4 py-2.5 text-sm",
  lg: "px-5 py-3 text-base",
}