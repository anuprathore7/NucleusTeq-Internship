import axiosInstance from "./axios"
import { ENDPOINTS } from "../utils/constants"

export const getQuestionsByQuizAPI = async (quizId) => {
  const response = await axiosInstance.get(ENDPOINTS.QUESTIONS_BY_QUIZ(quizId))
  return response.data
}

export const getQuestionByIdAPI = async (id) => {
  const response = await axiosInstance.get(ENDPOINTS.QUESTION_BY_ID(id))
  return response.data
}

export const createQuestionAPI = async (data) => {
  const response = await axiosInstance.post(ENDPOINTS.QUESTIONS, data)
  return response.data
}

export const updateQuestionAPI = async (id, data) => {
  const response = await axiosInstance.put(ENDPOINTS.QUESTION_BY_ID(id), data)
  return response.data
}

export const deleteQuestionAPI = async (id) => {
  const response = await axiosInstance.delete(ENDPOINTS.QUESTION_BY_ID(id))
  return response.data
}