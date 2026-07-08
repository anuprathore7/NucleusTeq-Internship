/**
 * StudentRoute
 *
 * Allows access only to authenticated student users.
 * Admin users are redirected to the admin dashboard.
 * Guests are redirected to the login page.
 */

import { Navigate } from "react-router-dom"
import ProtectedRoute from "./ProtectedRoute"
import { useAuth } from "../context/AuthContext"
import { ROUTES, ROLES } from "../utils/constants"

const StudentRoute = ({ children }) => {
  const { user } = useAuth()

  return (
    <ProtectedRoute>
      {user.role === ROLES.STUDENT ? (
        children
      ) : (
        <Navigate to={ROUTES.ADMIN_DASHBOARD} replace />
      )}
    </ProtectedRoute>
  )
}

export default StudentRoute