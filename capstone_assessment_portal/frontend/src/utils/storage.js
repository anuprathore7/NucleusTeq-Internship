/**
 * All localStorage operations in one place.
 *
 * If we switch from localStorage to cookies later,
 * we only change this one file.
 * Nothing else needs to change.
 */

import { STORAGE_KEYS } from "./constants"

// ── Access Token

export const saveAccessToken = (token) => {
  localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token)
}

export const getAccessToken = () => {
  return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
}

export const removeAccessToken = () => {
  localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
}

// ── Refresh Token 

export const saveRefreshToken = (token) => {
  localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, token)
}

export const getRefreshToken = () => {
  return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
}

export const removeRefreshToken = () => {
  localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
}

// ── User 

export const saveUser = (user) => {
  // JSON.stringify converts object to string for storage
  localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user))
}

export const getUser = () => {
  const raw = localStorage.getItem(STORAGE_KEYS.USER)
  // JSON.parse converts string back to object
  return raw ? JSON.parse(raw) : null
}

export const removeUser = () => {
  localStorage.removeItem(STORAGE_KEYS.USER)
}

// ── Clear all on logout 

export const clearStorage = () => {
  removeAccessToken()
  removeRefreshToken()
  removeUser()
}