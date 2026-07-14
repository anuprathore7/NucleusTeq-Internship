/**
 * Register Page
 *
 * 1. User enters username, email and password.
 * 2. Frontend validates inputs.
 * 3. Backend creates account.
 * 4. Success message shown.
 * 5. Redirect to login page.
 *
 */

import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"

import { registerAPI } from "../../api/auth.api"

import {
  validateUsername,
  validateEmail,
  validatePassword,
  validateConfirmPassword,
} from "../../utils/validators"

import { getErrorMessage } from "../../utils/helpers"

import Input from "../../components/common/Input"
import Button from "../../components/common/Button"
import Alert from "../../components/common/Alert"

const Register = () => {

  const navigate = useNavigate()

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  })

  const [errors, setErrors] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  })

  const [apiError, setApiError] = useState("")
  const [successMessage, setSuccessMessage] = useState("")
  const [loading, setLoading] = useState(false)

  
  /* Update */
  

  const handleChange = (field) => (event) => {

    const value = event.target.value

    setForm((prev) => ({
      ...prev,
      [field]: value,
    }))

    if (errors[field]) {
      setErrors((prev) => ({
        ...prev,
        [field]: "",
      }))
    }

    if (apiError) {
      setApiError("")
    }
  }

  
  /* Validation */
  

  const validateField = (field) => {

    let error = ""

    switch (field) {

      case "username":
        error = validateUsername(form.username)
        break

      case "email":
        error = validateEmail(form.email)
        break

      case "password":
        error = validatePassword(form.password)
        break

      case "confirmPassword":
        error = validateConfirmPassword(
          form.password,
          form.confirmPassword
        )
        break

      default:
        break
    }

    setErrors((prev) => ({
      ...prev,
      [field]: error,
    }))
  }

  
  /* Validation */
  

  const validateForm = () => {

    const newErrors = {

      username: validateUsername(form.username),

      email: validateEmail(form.email),

      password: validatePassword(form.password),

      confirmPassword: validateConfirmPassword(
        form.password,
        form.confirmPassword
      ),
    }

    setErrors(newErrors)

    return Object.values(newErrors).every(
      (error) => error === ""
    )
  }

  
  /* Submit */
  

  const handleSubmit = async (event) => {

    event.preventDefault()

    setApiError("")
    setSuccessMessage("")

    if (!validateForm()) {
      return
    }

    try {

      setLoading(true)

      await registerAPI(
        form.username,
        form.email,
        form.password
      )

      setSuccessMessage(
        "Registration successful. Redirecting to login..."
      )

      setTimeout(() => {
        navigate("/login")
      }, 2000)

    } catch (error) {

      setApiError(
        getErrorMessage(
          error,
          "Registration failed."
        )
      )

    } finally {

      setLoading(false)

    }
  }

  return (

    <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4 py-10">

      <div className="w-full max-w-md">

        <div className="bg-white rounded-2xl shadow-lg border border-slate-200 p-8">

          {/* Header */}

          <div className="text-center mb-8">

            <h1 className="text-3xl font-bold text-slate-800">
              Create Account
            </h1>

            <p className="mt-2 text-slate-500">
              Register to start taking quizzes.
            </p>

          </div>

          {/* Success */}

          <Alert
            type="success"
            message={successMessage}
          />

          {/* Error */}

          <Alert
            type="error"
            message={apiError}
          />

          {/* Form */}

          <form
            onSubmit={handleSubmit}
            className="space-y-5 mt-5"
            noValidate
          >

            <Input
              label="Username"
              name="username"
              value={form.username}
              placeholder="Enter username"
              onChange={handleChange("username")}
              onBlur={() => validateField("username")}
              error={errors.username}
              required
              autoComplete="username"
              maxLength={30}
            />

            <Input
              label="Email Address"
              name="email"
              type="email"
              value={form.email}
              placeholder="Enter email"
              onChange={handleChange("email")}
              onBlur={() => validateField("email")}
              error={errors.email}
              required
              autoComplete="email"
            />

            <Input
              label="Password"
              name="password"
              type="password"
              value={form.password}
              placeholder="Create password"
              onChange={handleChange("password")}
              onBlur={() => validateField("password")}
              error={errors.password}
              required
              autoComplete="new-password"
            />

            <Input
              label="Confirm Password"
              name="confirmPassword"
              type="password"
              value={form.confirmPassword}
              placeholder="Confirm password"
              onChange={handleChange("confirmPassword")}
              onBlur={() => validateField("confirmPassword")}
              error={errors.confirmPassword}
              required
              autoComplete="new-password"
            />

            <Button
              type="submit"
              loading={loading}
              fullWidth
              size="lg"
            >
              Create Account
            </Button>

          </form>

          {/* Footer */}

          <div className="mt-8 text-center text-sm">

            <span className="text-slate-500">
              Already have an account?
            </span>

            <Link
              to="/login"
              className="ml-2 font-medium text-primary-600 hover:text-primary-700"
            >
              Sign In
            </Link>

          </div>

        </div>

      </div>

    </div>

  )
}

export default Register