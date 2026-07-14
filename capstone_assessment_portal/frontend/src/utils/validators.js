/**
 * 
 * validators.js
 * 
 *  Centralized frontend validation.
 *  Reusable
 *  Easy to maintain
 *  Same validation everywhere
 */

/* Login Email Validation */

export const validateLoginEmail = (email) => {

  const value = email.trim().toLowerCase()

  if (!value) {
    return "Email is required"
  }

  const emailRegex =
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  if (!emailRegex.test(value)) {
    return "Please enter a valid email address"
  }

  return ""
}


/* Login Password Validation */

export const validateLoginPassword = (password) => {

  if (!password) {
    return "Password is required"
  }

  return ""
}


/*Email Validation */

export const validateEmail = (email) => {

  const value = email.trim().toLowerCase()

  if (!value) {
    return "Email is required"
  }

  if (value.length > 254) {
    return "Email is too long"
  }

  const emailRegex =
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  if (!emailRegex.test(value)) {
    return "Please enter a valid email address"
  }

  /* Block purely numeric local-parts like "11@gmail.com" — require at least one letter */
  const localPart = value.split("@")[0]
  if (!/[a-zA-Z]/.test(localPart)) {
    return "Email must include a name, not just numbers"
  }

  return ""
}


/*Username Validation */

export const validateUsername = (username) => {

  const value = username.trim()

  if (!value) {
    return "Username is required"
  }

  if (value.length < 3) {
    return "Username must be at least 3 characters"
  }

  if (value.length > 30) {
    return "Username cannot exceed 30 characters"
  }

  if (!/^[A-Za-z0-9_]+$/.test(value)) {
    return "Only letters, numbers and underscores are allowed"
  }

  if (value.startsWith("_")) {
    return "Username cannot start with an underscore"
  }

  if (value.endsWith("_")) {
    return "Username cannot end with an underscore"
  }

  if (value.includes("__")) {
    return "Username cannot contain consecutive underscores"
  }

  if (/^\d+$/.test(value)) {
    return "Username cannot contain only numbers"
  }

  // Reject aaaaaaaa / oooooooo / 11111111
  if (/^(.)\1+$/.test(value)) {
    return "Username cannot contain only one repeated character"
  }

  return ""
}


/*Password Validation */

export const validatePassword = (password) => {

  if (!password) {
    return "Password is required"
  }

  if (password.length < 8) {
    return "Password must be at least 8 characters"
  }

  if (password.length > 72) {
    return "Password cannot exceed 72 characters"
  }

  if (/\s/.test(password)) {
    return "Password cannot contain spaces"
  }

  if (!/[A-Z]/.test(password)) {
    return "Password must contain at least one uppercase letter"
  }

  if (!/[a-z]/.test(password)) {
    return "Password must contain at least one lowercase letter"
  }

  if (!/\d/.test(password)) {
    return "Password must contain at least one number"
  }

  if (!/[!@#$%^&*(),.?":{}|<>_\-+=/\\[\];'`~]/.test(password)) {
    return "Password must contain at least one special character"
  }

  // Reject AAAAAAAA
  // Reject 11111111
  // Reject @@@@@@@@
  if (/^(.)\1+$/.test(password)) {
    return "Password cannot contain only one repeated character"
  }

  return ""
}


/*Confirm Password */

export const validateConfirmPassword = (
  password,
  confirmPassword
) => {

  if (!confirmPassword) {
    return "Please confirm your password"
  }

  if (password !== confirmPassword) {
    return "Passwords do not match"
  }

  return ""
}


/*Password Strength */

export const getPasswordStrength = (password) => {

  if (!password) {
    return {
      score: 0,
      label: ""
    }
  }

  let score = 0

  if (password.length >= 8) score++

  if (/[A-Z]/.test(password)) score++

  if (/[a-z]/.test(password)) score++

  if (/\d/.test(password)) score++

  if (/[!@#$%^&*(),.?":{}|<>_\-+=/\\[\];'`~]/.test(password)) score++

  switch (score) {

    case 0:
    case 1:
      return {
        score,
        label: "Very Weak"
      }

    case 2:
      return {
        score,
        label: "Weak"
      }

    case 3:
      return {
        score,
        label: "Fair"
      }

    case 4:
      return {
        score,
        label: "Strong"
      }

    case 5:
      return {
        score,
        label: "Very Strong"
      }

    default:
      return {
        score: 0,
        label: ""
      }

  }

}


/*Form Helpers */

export const sanitizeEmail = (email) => {
  return email.trim().toLowerCase()
}

export const sanitizeUsername = (username) => {
  return username.trim()
}
/* Checks if any single character repeats 3 or more times in a row */
const hasConsecutiveRepeats = (value) => {
  return /(.)\1{4,}/.test(value)
}

/* Checks if the text has too little character variety to be meaningful,
   catches things like "ababab", "xyxyxy", "aaaa1111" */
const hasLowVariety = (value) => {
  const cleaned = value.replace(/\s+/g, "")
  if (cleaned.length < 6) return false
  const uniqueChars = new Set(cleaned.toLowerCase()).size
  const ratio = uniqueChars / cleaned.length
  return ratio < 0.35
}

/* Combines both checks — used everywhere isRepetitive was used before */
const isRepetitive = (value) => {
  const cleaned = value.trim()
  if (cleaned.length < 2) return false
  if (/^(.)\1+$/.test(cleaned)) return true
  if (hasConsecutiveRepeats(cleaned)) return true
  if (hasLowVariety(cleaned)) return true
  return false
}
/**
 * Checks if a string is meaningful text.
 * Must contain at least one letter.
 * Rejects strings like "111", "!!!!", "123 456".
 */
const hasMeaningfulContent = (value) => {
  return /[a-zA-Z]/.test(value)
}

/**
 * Category name validation
 */
export const validateCategoryName = (value) => {
  if (!value || !value.trim()) return "Category name is required"
  if (value.trim().length < 3) return "Name must be at least 3 characters"
  if (value.trim().length > 100) return "Name cannot exceed 100 characters"
  if (isRepetitive(value.trim())) return "Please enter a meaningful category name"
  if (!hasMeaningfulContent(value)) return "Name must contain at least one letter"
  return ""
}

/**
 * Category description validation
 */
export const validateCategoryDescription = (value) => {
  if (!value || !value.trim()) return "Description is required"
  if (value.trim().length < 5) return "Description must be at least 5 characters"
  if (value.trim().length > 500) return "Description cannot exceed 500 characters"
  if (isRepetitive(value.trim())) return "Please enter a meaningful description"
  if (!hasMeaningfulContent(value)) return "Description must contain at least one letter"
  return ""
}

/**
 * Quiz title validation
 */
export const validateQuizTitle = (value) => {
  if (!value || !value.trim()) return "Quiz title is required"
  if (value.trim().length < 3) return "Title must be at least 3 characters"
  if (value.trim().length > 200) return "Title cannot exceed 200 characters"
  if (isRepetitive(value.trim())) return "Please enter a meaningful quiz title"
  if (!hasMeaningfulContent(value)) return "Title must contain at least one letter"
  return ""
}

/**
 * Quiz description validation
 */
export const validateQuizDescription = (value) => {
  if (!value || !value.trim()) return "Description is required"
  if (value.trim().length < 5) return "Description must be at least 5 characters"
  if (value.trim().length > 1000) return "Description cannot exceed 1000 characters"
  if (isRepetitive(value.trim())) return "Please enter a meaningful description"
  if (!hasMeaningfulContent(value)) return "Description must contain at least one letter"
  return ""
}

/**
 * Quiz time limit validation
 */
export const validateTimeLimit = (value) => {
  if (!value && value !== 0) return "Time limit is required"
  const num = Number(value)
  if (isNaN(num)) return "Time limit must be a number"
  if (num <= 0) return "Time limit must be greater than 0"
  if (num > 300) return "Time limit cannot exceed 300 minutes"
  return ""
}

/**
 * Quiz pass percentage validation
 */
export const validatePassPercentage = (value) => {
  if (!value && value !== 0) return "Pass percentage is required"
  const num = Number(value)
  if (isNaN(num)) return "Must be a number"
  if (num < 1) return "Must be at least 1%"
  if (num > 100) return "Cannot exceed 100%"
  return ""
}

/**
 * Question text validation
 */
export const validateQuestionText = (value) => {
  if (!value || !value.trim()) return "Question text is required"
  if (value.trim().length < 5) return "Question must be at least 5 characters"
  if (value.trim().length > 1000) return "Question cannot exceed 1000 characters"
  if (isRepetitive(value.trim())) return "Please enter a meaningful question"
  if (!hasMeaningfulContent(value)) return "Question must contain at least one letter"
  return ""
}

/**
 * Question option validation
 */
export const validateOption = (value, index) => {
  if (!value || !value.trim()) return `Option ${index + 1} is required`
  if (value.trim().length < 1) return `Option ${index + 1} is required`
  if (isRepetitive(value.trim()) && value.trim().length > 3)
    return `Option ${index + 1} must be meaningful`
  return ""
}