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