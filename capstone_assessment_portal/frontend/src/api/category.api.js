import axiosInstance from "./axios"
import { ENDPOINTS } from "../utils/constants"

export const getCategoriesAPI = async () => {
  const response = await axiosInstance.get(ENDPOINTS.CATEGORIES)
  return response.data
}

export const getCategoryByIdAPI = async (id) => {
  const response = await axiosInstance.get(ENDPOINTS.CATEGORY_BY_ID(id))
  return response.data
}

export const createCategoryAPI = async (data) => {
  const response = await axiosInstance.post(ENDPOINTS.CATEGORIES, data)
  return response.data
}

export const updateCategoryAPI = async (id, data) => {
  const response = await axiosInstance.put(ENDPOINTS.CATEGORY_BY_ID(id), data)
  return response.data
}

export const deleteCategoryAPI = async (id) => {
  const response = await axiosInstance.delete(ENDPOINTS.CATEGORY_BY_ID(id))
  return response.data
}