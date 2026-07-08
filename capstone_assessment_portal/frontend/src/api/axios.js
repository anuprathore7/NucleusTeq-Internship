/**
 * Custom Axios instance.
 *
 * Configure base URL, headers and interceptors once.
 * Every API file imports this — no duplication.
 *
 * 1.adds JWT token to every request automatically
 * 2.on 401, tries to refresh token silently
 *  if refresh fails → logout and go to login
 */

import axios from "axios"
import { API_BASE_URL } from "../utils/constants"
import {
  getAccessToken,
  getRefreshToken,
  saveAccessToken,
  clearStorage
} from "../utils/storage"

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json"
  }
})

// ── Request Interceptor ───────────────────────────────────────────────────────
// Runs before EVERY outgoing request
// Adds Authorization header automatically
axiosInstance.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── Response Interceptor 
// Runs after EVERY response
// Handles expired access token silently
axiosInstance.interceptors.response.use(
  // success — return response as is
  (response) => response,

  // error — handle 401
  async (error) => {
    const originalRequest = error.config

    // 401 means token expired
    // _retry flag prevents infinite loop
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = getRefreshToken()

        // call refresh to get new access token
        const response = await axios.post(
          `${API_BASE_URL}/auth/refresh`,
          { refresh_token: refreshToken }
        )

        const newToken = response.data.access_token
        saveAccessToken(newToken)

        // retry original request with new token
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return axiosInstance(originalRequest)

      } catch {
        // refresh failed → force logout
        clearStorage()
        window.location.href = "/login"
      }
    }

    return Promise.reject(error)
  }
)

export default axiosInstance