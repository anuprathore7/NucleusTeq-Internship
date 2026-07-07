"""
Constants for attempt and result status values.

Keeping these as constants prevents typos like
"In_Progress" vs "in_progress" scattered across files.
Change value in one place — updates everywhere.
"""

# ── Attempt Status ────────────────────────────────────────────────────────────

# Student has started the quiz but not submitted yet
ATTEMPT_STATUS_IN_PROGRESS = "in_progress"

# Student has submitted all answers
ATTEMPT_STATUS_SUBMITTED = "submitted"

# All valid attempt statuses in one list
ATTEMPT_STATUSES = [
    ATTEMPT_STATUS_IN_PROGRESS,
    ATTEMPT_STATUS_SUBMITTED
]

MAX_ATTEMPTS_PER_QUIZ = 2