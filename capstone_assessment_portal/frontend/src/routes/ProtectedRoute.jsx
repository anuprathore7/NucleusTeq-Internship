/**
 * ProtectedRoute
 *
 * Prevents unauthenticated users from accessing protected pages.
 * While authentication is being restored, a loading spinner is shown.
 * If the user is not logged in, they are redirected to the login page.
 */

import { Navigate } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import { ROUTES } from "../utils/constants"
import Spinner from "../components/common/Spinner"

const ProtectedRoute = ({ children }) => {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return <Spinner fullPage />
  }

  if (!user) {
    return <Navigate to={ROUTES.LOGIN} replace />
  }

  return children
}

export default ProtectedRoute