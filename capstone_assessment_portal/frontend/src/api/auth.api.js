/**
 * Auth API calls.
 * Only talks to /auth/* endpoints.
 * Returns raw response data — no business logic here.
 */

import axiosInstance from "./axios"
import { ENDPOINTS } from "../utils/constants"
import { encryptPassword } from "../utils/rsa";

export const loginAPI = async (email, password) => {

  const encryptedPassword =
    await encryptPassword(password)

  const response = await axiosInstance.post(
    ENDPOINTS.LOGIN,
    {
      email,
      password: encryptedPassword
    }
  )

  return response.data

}

export const registerAPI = async (
  username,
  email,
  password
) => {

  const encryptedPassword =
    await encryptPassword(password)

  const response = await axiosInstance.post(
    ENDPOINTS.REGISTER,
    {
      username,
      email,
      password: encryptedPassword
    }
  )

  return response.data

}

export const getMeAPI = async () => {
  const response = await axiosInstance.get(ENDPOINTS.ME)
  return response.data
}

export const refreshTokenAPI = async (refreshToken) => {
  const response = await axiosInstance.post(ENDPOINTS.REFRESH, {
    refresh_token: refreshToken
  })
  return response.data
}