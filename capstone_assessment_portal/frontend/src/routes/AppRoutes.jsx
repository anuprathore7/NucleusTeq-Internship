/**
 * AppRoutes
 *
 * Defines all application routes.
 * Public routes are accessible only to guests.
 * Admin routes require admin authentication.
 * Student routes require student authentication.
 */

import { Routes, Route, Navigate } from "react-router-dom"

import Login from "../pages/auth/Login"
import Register from "../pages/auth/Register"

import Spinner from "../components/common/Spinner"

import { useAuth } from "../context/AuthContext"

import PublicRoute from "./PublicRoute"
import AdminRoute from "./AdminRoute"
import StudentRoute from "./StudentRoute"

import { ROUTES } from "../utils/constants"

const AdminDashboard = () => (
  <div className="min-h-screen flex items-center justify-center bg-slate-50">
    <p className="text-slate-500 text-sm">
      Admin Dashboard — Coming next
    </p>
  </div>
)

const StudentDashboard = () => (
  <div className="min-h-screen flex items-center justify-center bg-slate-50">
    <p className="text-slate-500 text-sm">
      Student Dashboard — Coming next
    </p>
  </div>
)

const AppRoutes = () => {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return <Spinner fullPage />
  }

  return (
    <Routes>

      <Route
        path="/"
        element={
          <Navigate
            to={user ? ROUTES.LOGIN : ROUTES.LOGIN}
            replace
          />
        }
      />

      <Route
        path={ROUTES.LOGIN}
        element={
          <PublicRoute>
            <Login />
          </PublicRoute>
        }
      />

      <Route
        path={ROUTES.REGISTER}
        element={
          <PublicRoute>
            <Register />
          </PublicRoute>
        }
      />

      <Route
        path={ROUTES.ADMIN_DASHBOARD}
        element={
          <AdminRoute>
            <AdminDashboard />
          </AdminRoute>
        }
      />

      <Route
        path={ROUTES.STUDENT_DASHBOARD}
        element={
          <StudentRoute>
            <StudentDashboard />
          </StudentRoute>
        }
      />

      <Route
        path="*"
        element={<Navigate to={ROUTES.LOGIN} replace />}
      />

    </Routes>
  )
}

export default AppRoutes