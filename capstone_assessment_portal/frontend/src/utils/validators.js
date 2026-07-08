/**
 * 
 * validators.js
 * 
 *  Centralized frontend validation.
 *  Reusable
 *  Easy to maintain
 *  Same validation everywhere
 */


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