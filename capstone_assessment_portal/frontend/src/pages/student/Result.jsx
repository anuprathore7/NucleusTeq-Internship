import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

import { getResultByAttemptAPI } from "../../api/result.api";

import Button from "../../components/common/Button";
import Badge from "../../components/common/Badge";
import Spinner from "../../components/common/Spinner";
import Alert from "../../components/common/Alert";

import { formatDateTime } from "../../utils/helpers";
import { ROUTES } from "../../utils/constants";

/**
 * Circular score indicator.
 * Shows percentage as a progress ring.
 */
const ScoreRing = ({ percentage, passed }) => {
  const radius = 54;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (percentage / 100) * circumference;

  return (
    <div className="relative w-36 h-36 flex items-center justify-center">
      <svg
        className="absolute inset-0 -rotate-90"
        width="144"
        height="144"
        viewBox="0 0 144 144"
      >
        {/** Background ring */}
        <circle
          cx="72"
          cy="72"
          r={radius}
          fill="none"
          stroke="#E2E8F0"
          strokeWidth="10"
        />
        {/** Progress ring */}
        <circle
          cx="72"
          cy="72"
          r={radius}
          fill="none"
          stroke={passed ? "#16A34A" : "#DC2626"}
          strokeWidth="10"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          style={{ transition: "stroke-dashoffset 1s ease" }}
        />
      </svg>

      {/** Center text */}
      <div className="text-center">
        <p
          className={`text-2xl font-bold ${passed ? "text-green-600" : "text-red-600"}`}
        >
          {percentage}%
        </p>
        <p className="text-xs text-slate-400 font-medium mt-0.5">
          {passed ? "Passed" : "Failed"}
        </p>
      </div>
    </div>
  );
};

/**
 * Single answer breakdown row — shows correct/wrong.
 */
const AnswerRow = ({ item, index }) => {
  const isCorrect = item.is_correct;

  return (
    <div
      className={`
      rounded-xl border p-4 flex flex-col gap-3
      ${isCorrect ? "border-green-200 bg-green-50" : "border-red-200 bg-red-50"}
    `}
    >
      {/** Question */}
      <div className="flex items-start gap-3">
        <span
          className={`
          w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold shrink-0 mt-0.5
          ${isCorrect ? "bg-green-500 text-white" : "bg-red-500 text-white"}
        `}
        >
          {isCorrect ? "✓" : "✗"}
        </span>
        <p className="text-sm font-medium text-slate-800 leading-relaxed">
          {item.question_text}
        </p>
      </div>

      {/** Answer comparison */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pl-9">
        <div
          className={`rounded-lg p-3 ${isCorrect ? "bg-green-100" : "bg-red-100"}`}
        >
          <p className="text-xs font-semibold text-slate-500 mb-1">
            Your Answer
          </p>
          <p
            className={`text-sm font-medium ${isCorrect ? "text-green-700" : "text-red-700"}`}
          >
            {item.selected_answer || (
              <span className="italic text-slate-400">No answer</span>
            )}
          </p>
        </div>

        {!isCorrect && (
          <div className="rounded-lg p-3 bg-green-100">
            <p className="text-xs font-semibold text-slate-500 mb-1">
              Correct Answer
            </p>
            <p className="text-sm font-medium text-green-700">
              {item.correct_answer}
            </p>
          </div>
        )}
      </div>

      {/** Marks */}
      <div className="pl-9">
        <Badge
          label={`${item.marks_obtained} / ${item.marks_possible} marks`}
          variant={isCorrect ? "success" : "error"}
        />
      </div>
    </div>
  );
};

const StudentResult = () => {
  const { attemptId } = useParams();
  const navigate = useNavigate();

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadResult = async () => {
      try {
        const data = await getResultByAttemptAPI(attemptId);
        setResult(data);
      } catch (err) {
        setError(
          err?.response?.data?.detail ||
            "Failed to load result. Please try again.",
        );
      } finally {
        setLoading(false);
      }
    };

    loadResult();
  }, [attemptId]);

  if (loading) return <Spinner fullPage />;

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
        <div className="max-w-md w-full">
          <Alert type="error" message={error} />
          <div className="mt-4 text-center">
            <Button onClick={() => navigate(ROUTES.STUDENT_DASHBOARD)}>
              Back to Dashboard
            </Button>
          </div>
        </div>
      </div>
    );
  }

  if (!result) return null;

  const totalCorrect =
    result.answer_breakdown?.filter((a) => a.is_correct).length || 0;

  return (
    <div className="min-h-screen bg-slate-50 py-8 px-4">
      <div className="max-w-2xl mx-auto flex flex-col gap-6">
        {/** Result summary card */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
          {/** Pass / Fail banner */}
          <div
            className={`
            rounded-xl px-4 py-3 text-center mb-6 font-semibold text-sm
            ${
              result.passed
                ? "bg-green-50 text-green-700 border border-green-200"
                : "bg-red-50 text-red-700 border border-red-200"
            }
          `}
          >
            {result.passed
              ? "Congratulations! You passed this quiz."
              : "You did not pass this time. Keep practicing!"}
          </div>

          {/** Score ring + stats */}
          <div className="flex flex-col sm:flex-row items-center gap-8">
            <div className="flex-shrink-0">
              <ScoreRing
                percentage={result.percentage}
                passed={result.passed}
              />
            </div>

            <div className="grid grid-cols-2 gap-4 flex-1 w-full">
              <div className="bg-slate-50 rounded-xl p-4 text-center border border-slate-100">
                <p className="text-xs text-slate-400 mb-1">Score</p>
                <p className="text-xl font-bold text-slate-900">
                  {result.score} / {result.total_marks}
                </p>
              </div>

              <div className="bg-slate-50 rounded-xl p-4 text-center border border-slate-100">
                <p className="text-xs text-slate-400 mb-1">Correct</p>
                <p className="text-xl font-bold text-slate-900">
                  {totalCorrect} / {result.answer_breakdown?.length || 0}
                </p>
              </div>

              <div className="bg-slate-50 rounded-xl p-4 text-center border border-slate-100">
                <p className="text-xs text-slate-400 mb-1">Pass Mark</p>
                <p className="text-xl font-bold text-slate-900">
                  {result.pass_percentage}%
                </p>
              </div>

              <div className="bg-slate-50 rounded-xl p-4 text-center border border-slate-100">
                <p className="text-xs text-slate-400 mb-1">Submitted</p>
                <p className="text-xs font-medium text-slate-600 mt-1">
                  {formatDateTime(result.submitted_at)}
                </p>
              </div>
            </div>
          </div>

          {/** Quiz title */}
          <div className="mt-6 pt-5 border-t border-slate-100">
            <p className="text-xs text-slate-400 mb-1">Quiz</p>
            <p className="text-sm font-semibold text-slate-900">
              {result.quiz_title}
            </p>
          </div>
        </div>

        {/** Answer breakdown */}
        {result.answer_breakdown && result.answer_breakdown.length > 0 && (
          <div>
            <h2 className="text-base font-semibold text-slate-800 mb-4">
              Answer Breakdown
            </h2>

            <div className="flex flex-col gap-3">
              {result.answer_breakdown.map((item, index) => (
                <AnswerRow key={item.question_id} item={item} index={index} />
              ))}
            </div>
          </div>
        )}

        {/** Action buttons */}
        <div className="flex flex-col sm:flex-row gap-3">
          <Button
            fullWidth
            variant="secondary"
            onClick={() => navigate(ROUTES.STUDENT_DASHBOARD)}
          >
            Back to Dashboard
          </Button>
          <Button fullWidth onClick={() => navigate("/student/categories")}>
            Browse More Quizzes
          </Button>
        </div>
      </div>
    </div>
  );
};

export default StudentResult;
