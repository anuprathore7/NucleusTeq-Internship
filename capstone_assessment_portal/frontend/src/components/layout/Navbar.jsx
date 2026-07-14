/**
 * Navbar
 *
 * Top navigation bar shared across admin and student layouts.
 * Shows current page title, user info and logout button.
 */

import { useAuth } from "../../context/AuthContext"
import Button from "../common/Button"

const Navbar = ({ title }) => {
  const { user, logout } = useAuth()

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-8 flex items-center justify-between shrink-0">

      {/* Page Title */}
      <h2 className="text-base font-semibold text-slate-800">{title}</h2>

      {/* Right side */}
      <div className="flex items-center gap-4">

        {/* User info */}
        <div className="text-right hidden sm:block">
          <p className="text-sm font-medium text-slate-900">{user?.username}</p>
          <p className="text-xs text-slate-400 capitalize">{user?.role}</p>
        </div>

        {/* Avatar */}
        <div className="w-9 h-9 rounded-full bg-primary-600 flex items-center justify-center text-white text-sm font-semibold uppercase shrink-0">
          {user?.username?.charAt(0)}
        </div>

        {/* Logout */}
        <Button variant="secondary" size="sm" onClick={logout}>
          Sign Out
        </Button>

      </div>

    </header>
  )
}

export default Navbar