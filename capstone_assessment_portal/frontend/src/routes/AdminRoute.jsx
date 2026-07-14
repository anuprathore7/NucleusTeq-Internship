/**
 * AdminRoute
 *
 * Allows access only to authenticated admin users.
 * Students are redirected to their dashboard.
 * Guests are redirected to the login page.
 */

import { Navigate } from "react-router-dom"
import ProtectedRoute from "./ProtectedRoute"
import { useAuth } from "../context/AuthContext"
import { ROUTES, ROLES } from "../utils/constants"

const AdminRoute = ({ children }) => {
  const { user } = useAuth()

  return (
    <ProtectedRoute>
      {user.role === ROLES.ADMIN ? (
        children
      ) : (
        <Navigate to={ROUTES.STUDENT_DASHBOARD} replace />
      )}
    </ProtectedRoute>
  )
}

export default AdminRoute