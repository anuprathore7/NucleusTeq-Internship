import { Routes, Route, Navigate } from "react-router-dom"

import { useAuth }   from "../context/AuthContext"
import { ROUTES }    from "../utils/constants"

import Spinner       from "../components/common/Spinner"
import PublicRoute   from "./PublicRoute"
import AdminRoute    from "./AdminRoute"
import StudentRoute  from "./StudentRoute"

import AdminLayout   from "../components/layout/AdminLayout"
import StudentLayout from "../components/layout/StudentLayout"

import Login    from "../pages/auth/Login"
import Register from "../pages/auth/Register"

import AdminDashboard  from "../pages/admin/Dashboard"
import AdminCategories from "../pages/admin/Categories"
import AdminQuizzes    from "../pages/admin/Quizzes"
import AdminQuestions  from "../pages/admin/Questions"
import AdminStudents   from "../pages/admin/Students"
import AdminResults    from "../pages/admin/Results"

import StudentDashboard  from "../pages/student/Dashboard"
import StudentCategories from "../pages/student/Categories"
import StudentQuizList   from "../pages/student/QuizList"
import QuizAttempt       from "../pages/student/QuizAttempt"
import StudentResult     from "../pages/student/Result"
import StudentHistory    from "../pages/student/History"

const AppRoutes = () => {

  const { isLoading } = useAuth()

  if (isLoading) return <Spinner fullPage />

  return (
    <Routes>

      <Route path="/" element={<Navigate to={ROUTES.LOGIN} replace />} />

      <Route path={ROUTES.LOGIN}    element={<PublicRoute><Login /></PublicRoute>} />
      <Route path={ROUTES.REGISTER} element={<PublicRoute><Register /></PublicRoute>} />

      {/** Admin routes inside layout */}
      <Route path="/admin" element={<AdminRoute><AdminLayout /></AdminRoute>}>
        <Route path="dashboard"  element={<AdminDashboard />} />
        <Route path="categories" element={<AdminCategories />} />
        <Route path="quizzes"    element={<AdminQuizzes />} />
        <Route path="questions"  element={<AdminQuestions />} />
        <Route path="students"   element={<AdminStudents />} />
        <Route path="results"    element={<AdminResults />} />
      </Route>

      {/** Student routes inside layout */}
      <Route path="/student" element={<StudentRoute><StudentLayout /></StudentRoute>}>
        <Route path="dashboard"  element={<StudentDashboard />} />
        <Route path="categories" element={<StudentCategories />} />
        <Route path="quizzes"    element={<StudentQuizList />} />
        <Route path="history"    element={<StudentHistory />} />
      </Route>

      {/**
       * Quiz attempt and result are OUTSIDE the student layout
       * because they use a full-screen design without the sidebar.
       */}
      <Route
        path="/student/attempt/:attemptId"
        element={<StudentRoute><QuizAttempt /></StudentRoute>}
      />
      <Route
        path="/student/result/:attemptId"
        element={<StudentRoute><StudentResult /></StudentRoute>}
      />

      <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />

    </Routes>
  )
}

export default AppRoutes