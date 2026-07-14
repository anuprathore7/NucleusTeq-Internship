/**
 * Login Page
 *
 * 1. User enters email and password.
 * 2. Frontend validates inputs.
 * 3. Backend authenticates user.
 * 4. AuthContext stores tokens.
 * 5. Redirect based on role.
 *
 */

import { useState } from "react";
import { Link } from "react-router-dom";

import { loginAPI } from "../../api/auth.api";
import { useAuth } from "../../context/AuthContext";

import {
  validateLoginEmail,
  validateLoginPassword,
} from "../../utils/validators";

import { getErrorMessage } from "../../utils/helpers";

import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";

const Login = () => {
  const { login } = useAuth();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [errors, setErrors] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);

  const [apiError, setApiError] = useState("");

  /* Update field */

  const handleChange = (field) => (e) => {
    const value = e.target.value;

    setForm((prev) => ({
      ...prev,
      [field]: value,
    }));

    // Clear field error while typing
    if (errors[field]) {
      setErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
    }

    // Clear backend error
    if (apiError) {
      setApiError("");
    }
  };

  /* Validate one field */

  const validateField = (field) => {
    let error = "";

    switch (field) {
      case "email":
        error = validateLoginEmail(form.email);
        break;

      case "password":
        error = validateLoginPassword(form.password);
        break;

      default:
        break;
    }

    setErrors((prev) => ({
      ...prev,
      [field]: error,
    }));
  };

  /* Validate form*/

  const validateForm = () => {
    const newErrors = {
      email: validateLoginEmail(form.email),
      password: validateLoginPassword(form.password),
    };

    setErrors(newErrors);

    return Object.values(newErrors).every((error) => error === "");
  };

  /* Submit */

  const handleSubmit = async (event) => {
    event.preventDefault();

    setApiError("");

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);

      const response = await loginAPI(form.email, form.password);

      login(response);
    } catch (error) {
      console.log("Status:", error?.response?.status);
      console.log("Response:", error?.response?.data);

      if (error?.response?.status === 401) {
        setApiError("Invalid email or password.");
      } else {
        setApiError(
          getErrorMessage(error, "Something went wrong. Please try again."),
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-lg border border-slate-200 p-8">
          {/* Header*/}

          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-slate-800">
              Assessment Portal
            </h1>

            <p className="mt-2 text-slate-500">Sign in to continue</p>
          </div>

          {/* API Error */}

          <Alert type="error" message={apiError} />

          {/* Form */}

          <form onSubmit={handleSubmit} className="space-y-5 mt-5" noValidate>
            <Input
              label="Email Address"
              name="email"
              type="email"
              placeholder="Enter your email"
              value={form.email}
              onChange={handleChange("email")}
              onBlur={() => validateField("email")}
              error={errors.email}
              autoComplete="email"
              required
            />

            <Input
              label="Password"
              name="password"
              type="password"
              placeholder="Enter your password"
              value={form.password}
              onChange={handleChange("password")}
              onBlur={() => validateField("password")}
              error={errors.password}
              autoComplete="current-password"
              required
            />

            <Button type="submit" loading={loading} fullWidth size="lg">
              Sign In
            </Button>
          </form>

          {/* Footer*/}

          <div className="mt-8 text-center text-sm">
            <span className="text-slate-500">Don't have an account?</span>

            <Link
              to="/register"
              className="ml-2 font-medium text-primary-600 hover:text-primary-700"
            >
              Create account
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
