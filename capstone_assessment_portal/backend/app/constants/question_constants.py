"""
Constants for question types and difficulty levels.
"""

# ─── Question Types ─────────────────────────────────────────────────────────────

# Multiple Choice Question — has 4 options, one correct answer
MCQ = "mcq"

# True or False question — only two options: True and False
TRUE_FALSE = "true_false"

# All valid question types in one list
# Used for validation in schemas
QUESTION_TYPES = [MCQ, TRUE_FALSE]

# ─── Difficulty Levels ───────────────────────────────────────────────────────────

EASY = "easy"
MEDIUM = "medium"
HARD = "hard"

# All valid difficulty levels
DIFFICULTY_LEVELS = [EASY, MEDIUM, HARD]

# ─── True/False Options ──────────────────────────────────────────────────────────

# These are the ONLY valid options for a True/False question
# Stored as strings so they work consistently in MongoDB
TRUE_FALSE_OPTIONS = ["True", "False"]

# ─── MCQ Rules ───────────────────────────────────────────────────────────────────

# Every MCQ must have exactly this many options
MCQ_OPTIONS_COUNT = 4