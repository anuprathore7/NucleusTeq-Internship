/**
 * PublicRoute
 *
 * Allows access only to users who are not authenticated.
 * Logged-in users are redirected to their respective dashboard.
 */

import { Navigate } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import { ROUTES, ROLES } from "../utils/constants"
import Spinner from "../components/common/Spinner"

const PublicRoute = ({ children }) => {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return <Spinner fullPage />
  }

  if (!user) {
    return children
  }

  return (
    <Navigate
      to={
        user.role === ROLES.ADMIN
          ? ROUTES.ADMIN_DASHBOARD
          : ROUTES.STUDENT_DASHBOARD
      }
      replace
    />
  )
}

export default PublicRoute