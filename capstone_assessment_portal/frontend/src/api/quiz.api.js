import axiosInstance from "./axios"
import { ENDPOINTS } from "../utils/constants"

export const getQuizzesAPI = async () => {
  const response = await axiosInstance.get(ENDPOINTS.QUIZZES)
  return response.data
}

export const getQuizByIdAPI = async (id) => {
  const response = await axiosInstance.get(ENDPOINTS.QUIZ_BY_ID(id))
  return response.data
}

export const getQuizzesByCategoryAPI = async (categoryId) => {
  const response = await axiosInstance.get(ENDPOINTS.QUIZZES_BY_CATEGORY(categoryId))
  return response.data
}

export const createQuizAPI = async (data) => {
  const response = await axiosInstance.post(ENDPOINTS.QUIZZES, data)
  return response.data
}

export const updateQuizAPI = async (id, data) => {
  const response = await axiosInstance.put(ENDPOINTS.QUIZ_BY_ID(id), data)
  return response.data
}

export const deleteQuizAPI = async (id) => {
  const response = await axiosInstance.delete(ENDPOINTS.QUIZ_BY_ID(id))
  return response.data
}