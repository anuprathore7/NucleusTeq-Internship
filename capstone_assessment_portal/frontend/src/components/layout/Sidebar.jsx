/**
 * Sidebar
 *
 * Reusable sidebar for admin and student layouts.
 * Accepts title, subtitle and navigation items with optional icons.
 */

import { NavLink } from "react-router-dom"

const Sidebar = ({ title, subtitle, items }) => {
  return (
    <aside className="w-64 bg-slate-900 text-white flex flex-col shrink-0">

      {/* Brand */}
      <div className="px-6 py-5 border-b border-slate-800">
        <h1 className="text-base font-bold text-white">{title}</h1>
        {subtitle && (
          <p className="text-xs text-slate-400 mt-0.5">{subtitle}</p>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 overflow-y-auto">
        {items.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end
            className={({ isActive }) => `
              flex items-center gap-3
              px-4 py-2.5
              rounded-lg mb-1
              text-sm font-medium
              transition-colors duration-150
              ${isActive
                ? "bg-primary-600 text-white"
                : "text-slate-400 hover:bg-slate-800 hover:text-white"
              }
            `}
          >
            {item.icon && (
              <span className="shrink-0">{item.icon}</span>
            )}
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="px-6 py-4 border-t border-slate-800">
        <p className="text-xs text-slate-500">Assessment Portal v1.0</p>
      </div>

    </aside>
  )
}

export default Sidebar