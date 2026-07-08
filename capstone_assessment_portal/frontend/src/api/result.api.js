import axiosInstance from "./axios"
import { ENDPOINTS } from "../utils/constants"

export const getMyResultsAPI = async () => {
  const response = await axiosInstance.get(ENDPOINTS.MY_RESULTS)
  return response.data
}

export const getResultByAttemptAPI = async (attemptId) => {
  const response = await axiosInstance.get(ENDPOINTS.RESULT_BY_ATTEMPT(attemptId))
  return response.data
}

export const getAllResultsAdminAPI = async () => {
  const response = await axiosInstance.get(ENDPOINTS.ADMIN_ALL_RESULTS)
  return response.data
}

export const getResultsByQuizAdminAPI = async (quizId) => {
  const response = await axiosInstance.get(ENDPOINTS.ADMIN_RESULTS_BY_QUIZ(quizId))
  return response.data
}