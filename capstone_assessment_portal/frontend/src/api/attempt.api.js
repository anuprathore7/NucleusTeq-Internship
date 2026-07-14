import axiosInstance from "./axios"
import { ENDPOINTS } from "../utils/constants"

export const startAttemptAPI = async (quizId) => {
  const response = await axiosInstance.post(ENDPOINTS.START_ATTEMPT, {
    quiz_id: quizId
  })
  return response.data
}

export const getMyAttemptsAPI = async () => {
  const response = await axiosInstance.get(ENDPOINTS.MY_ATTEMPTS)
  return response.data
}

export const getAttemptByIdAPI = async (attemptId) => {
  const response = await axiosInstance.get(ENDPOINTS.ATTEMPT_BY_ID(attemptId))
  return response.data
}

export const saveAnswerAPI = async (attemptId, questionId, selectedAnswer) => {
  const response = await axiosInstance.post(ENDPOINTS.SAVE_ANSWER(attemptId), {
    question_id: questionId,
    selected_answer: selectedAnswer
  })
  return response.data
}

export const submitAttemptAPI = async (attemptId) => {
  const response = await axiosInstance.post(ENDPOINTS.SUBMIT_ATTEMPT(attemptId))
  return response.data
}