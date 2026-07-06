"""
All routes will sit under /assessment/v1/
v1 = version 1, so future breaking changes go to /assessment/v2/
without breaking existing clients
"""

URL_PREFIX = "/assessment"
API_VERSION = "/v1"

"""
Each module gets its own sub-path
Final URL example: /assessment/v1/auth/register
"""
AUTH_PREFIX = "/auth"
CATEGORY_PREFIX = "/categories"  
QUIZ_PREFIX = "/quizzes"  
QUESTION_PREFIX = "/questions"  