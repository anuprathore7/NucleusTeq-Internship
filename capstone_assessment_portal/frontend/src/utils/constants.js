/**
 * All application constants in one place.
 * Never hardcode strings, URLs, or keys anywhere else.
 * Change here → updates everywhere automatically.
 */

// ── Backend API base URL
export const API_BASE_URL = "http://localhost:8000/assessment/v1"

// ── User roles 
// Must match backend roles.py exactly
export const ROLES = {
  ADMIN:   "admin",
  STUDENT: "student"
}

// ── Local storage keys 
// Used in storage.js — never write these strings anywhere else
export const STORAGE_KEYS = {
  ACCESS_TOKEN:  "ap_access_token",
  REFRESH_TOKEN: "ap_refresh_token",
  USER:          "ap_user"
}

// ── Frontend route paths 
// Used in AppRoutes.jsx and navigation links
export const ROUTES = {
  // public
  LOGIN:    "/login",
  REGISTER: "/register",

  // admin
  ADMIN_DASHBOARD:  "/admin/dashboard",
  ADMIN_CATEGORIES: "/admin/categories",
  ADMIN_QUIZZES:    "/admin/quizzes",
  ADMIN_QUESTIONS:  "/admin/questions",
  ADMIN_RESULTS:    "/admin/results",

  // student
  STUDENT_DASHBOARD: "/student/dashboard",
  STUDENT_QUIZZES:   "/student/quizzes",
  STUDENT_ATTEMPT:   "/student/attempt/:attemptId",
  STUDENT_RESULT:    "/student/result/:attemptId",
  STUDENT_HISTORY:   "/student/history"
}

// ── API endpoint paths 
// Used in api/ files — never write endpoint strings directly in api files
export const ENDPOINTS = {
  // auth
  LOGIN:    "/auth/login",
  REGISTER: "/auth/register",
  ME:       "/auth/me",
  REFRESH:  "/auth/refresh",

  // categories
  CATEGORIES:          "/categories",
  CATEGORY_BY_ID:      (id) => `/categories/${id}`,

  // quizzes
  QUIZZES:             "/quizzes",
  QUIZ_BY_ID:          (id) => `/quizzes/${id}`,
  QUIZZES_BY_CATEGORY: (categoryId) => `/quizzes/category/${categoryId}`,

  // questions
  QUESTIONS:           "/questions",
  QUESTIONS_BY_QUIZ:   (quizId) => `/questions/quiz/${quizId}`,
  QUESTION_BY_ID:      (id) => `/questions/${id}`,

  // attempts
  START_ATTEMPT:  "/attempts/start",
  MY_ATTEMPTS:    "/attempts/my",
  ATTEMPT_BY_ID:  (id) => `/attempts/${id}`,
  SAVE_ANSWER:    (id) => `/attempts/${id}/answer`,
  SUBMIT_ATTEMPT: (id) => `/attempts/${id}/submit`,

  // results
  MY_RESULTS:            "/results/my",
  RESULT_BY_ATTEMPT:     (attemptId) => `/results/${attemptId}`,
  ADMIN_ALL_RESULTS:     "/results/admin/all",
  ADMIN_RESULTS_BY_QUIZ: (quizId)    => `/results/admin/quiz/${quizId}`
}