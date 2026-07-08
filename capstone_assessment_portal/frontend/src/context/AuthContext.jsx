/**
 * AuthContext — global auth state shared across entire app.
 *
 * Like a global variable that any component can read.
 * Without context, you would pass user as prop to every component.
 * With context, any component can just call useAuth() and get user.
 *
 * Provides:
 * user      → logged in user object (null if not logged in)
 * isLoading → true while checking if already logged in on app start
 * login()   → saves tokens, sets user, redirects based on role
 * logout()  → clears storage, resets state, goes to login
 */

import { createContext, useContext, useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import {
  saveAccessToken,
  saveRefreshToken,
  saveUser,
  getAccessToken,
  clearStorage
} from "../utils/storage"
import { getMeAPI } from "../api/auth.api"
import { ROUTES, ROLES } from "../utils/constants"

// create context with null default
const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser]         = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const navigate = useNavigate()

  // on app start — check if user already logged in
  useEffect(() => {
    const initialize = async () => {
      const token = getAccessToken()

      if (token) {
        try {
          // verify token still valid
          const userData = await getMeAPI()
          setUser(userData)
        } catch {
          // token expired or invalid
          clearStorage()
          setUser(null)
        }
      }

      setIsLoading(false)
    }

    initialize()
  }, [])

  const login = (data) => {
    // save everything to localStorage
    saveAccessToken(data.access_token)
    saveRefreshToken(data.refresh_token)
    saveUser(data.user)

    // update state
    setUser(data.user)

    // redirect based on role
    if (data.user.role === ROLES.ADMIN) {
      navigate(ROUTES.ADMIN_DASHBOARD, { replace: true })
    } else {
      navigate(ROUTES.STUDENT_DASHBOARD, { replace: true })
    }
  }

  const logout = () => {
    clearStorage()
    setUser(null)
    navigate(ROUTES.LOGIN, { replace: true })
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// custom hook — shortcut to use auth context
export const useAuth = () => useContext(AuthContext)