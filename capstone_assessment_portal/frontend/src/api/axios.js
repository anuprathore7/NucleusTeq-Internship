/**
 * axios.js
 *
 * Shared Axios instance used throughout the application.
 *
 * Features
 * - Automatically attaches access token.
 * - Refreshes expired access token.
 * - Retries failed request once.
 * - Does NOT refresh during login.
 * - Does NOT refresh refresh-token requests.
 * - Prevents infinite retry loops.
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

axiosInstance.interceptors.response.use(

  (response) => response,

  async (error) => {

    const originalRequest = error.config

    const status = error.response?.status

    const requestUrl = originalRequest?.url || ""

    const isLoginRequest =
      requestUrl.includes("/auth/login")

    const isRefreshRequest =
      requestUrl.includes("/auth/refresh")

    if (
      status !== 401 ||
      originalRequest._retry ||
      isLoginRequest ||
      isRefreshRequest
    ) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    const refreshToken = getRefreshToken()

    if (!refreshToken) {
      return Promise.reject(error)
    }

    try {

      const response = await axios.post(
        `${API_BASE_URL}/auth/refresh`,
        {
          refresh_token: refreshToken
        }
      )

      const newAccessToken = response.data.access_token

      saveAccessToken(newAccessToken)

      originalRequest.headers.Authorization =
        `Bearer ${newAccessToken}`

      return axiosInstance(originalRequest)

    } catch (refreshError) {

      clearStorage()

      if (window.location.pathname !== "/login") {
        window.location.href = "/login"
      }

      return Promise.reject(refreshError)
    }

  }

)

export default axiosInstance