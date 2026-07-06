"""
Central place for all API response messages and error details.
"""

# ─── Token Messages ─────────────────────────────────────────────────────────────

REFRESH_TOKEN_EXPIRED = "Refresh token has expired, please login again"
INVALID_REFRESH_TOKEN = "Invalid refresh token"

# ─── Auth Messages ─────────────────────────────────────────────────────────────

USER_ALREADY_EXISTS = "User with this email or username already exists"
INVALID_CREDENTIALS = "Invalid email or password"
USER_NOT_FOUND = "User not found"
TOKEN_EXPIRED = "Token has expired, please login again"
INVALID_TOKEN = "Could not validate token"
INSUFFICIENT_PERMISSIONS = "You do not have permission to perform this action"

# ─── Category Messages ──────────────────────────────────────────────────────────

CATEGORY_NOT_FOUND = "Category not found"
CATEGORY_ALREADY_EXISTS = "Category with this name already exists"
CATEGORY_INVALID_ID = "Invalid category ID format"

# ─── Quiz Messages ──────────────────────────────────────────────────────────────

QUIZ_NOT_FOUND = "Quiz not found"
QUIZ_ALREADY_EXISTS = "Quiz with this title already exists in this category"
QUIZ_INVALID_ID = "Invalid quiz ID format"
QUIZ_CATEGORY_NOT_FOUND = "Category not found. Please provide a valid category ID"

# ─── Question Messages ──────────────────────────────────────────────────────────

QUESTION_NOT_FOUND = "Question not found"
QUESTION_ALREADY_EXISTS = "Question with this text already exists in this quiz"
QUESTION_INVALID_ID = "Invalid question ID format"
QUESTION_QUIZ_NOT_FOUND = "Quiz not found. Please provide a valid quiz ID"
QUESTION_INVALID_OPTIONS = "MCQ questions must have exactly 4 options"
QUESTION_INVALID_TRUE_FALSE = "True/False questions must have exactly 2 options: 'True' and 'False'"
QUESTION_INVALID_CORRECT_ANSWER = "Correct answer must match one of the provided options"

# ─── Success Messages ─────────────────────────────────────────────────────────

CATEGORY_DELETED = "Category deleted successfully"
QUIZ_DELETED = "Quiz deleted successfully"
QUESTION_DELETED = "Question deleted successfully"

# ─── Delete Dependency Messages ───────────────────────────────────────────────

CATEGORY_HAS_QUIZZES = "Cannot delete category. Please delete all linked quizzes first."
QUIZ_HAS_QUESTIONS = "Cannot delete quiz. Please delete all linked questions first."

# ── Attempt Messages ──────────────────────────────────────────────────────────

ATTEMPT_NOT_FOUND = "Attempt not found"
ATTEMPT_INVALID_ID = "Invalid attempt ID format"
ATTEMPT_QUIZ_NOT_FOUND = "Quiz not found. Please provide a valid quiz ID"
ATTEMPT_ALREADY_SUBMITTED = "This attempt has already been submitted"
ATTEMPT_NOT_IN_PROGRESS = "This attempt is not in progress"
ATTEMPT_UNAUTHORIZED = "You are not authorized to access this attempt"
ATTEMPT_INVALID_QUESTION = "One or more question IDs do not belong to this quiz"
ATTEMPT_STARTED = "Quiz attempt started successfully"
ATTEMPT_SUBMITTED = "Quiz submitted successfully"
ATTEMPT_MAX_REACHED = "You have reached the maximum number of attempts for this quiz"
ATTEMPT_ANSWER_SAVED = "Answer saved successfully"
ATTEMPT_AUTO_SUBMITTED = "Time expired. Quiz has been auto submitted"
ATTEMPT_EXPIRED = "Quiz time has expired"