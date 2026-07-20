import { createBrowserRouter, Navigate } from 'react-router'
import {
  PublicOnlyLayout,
  RejectAdminLayout,
  RequireAdminLayout,
  RequireAuthLayout,
  RequireGoalLayout,
  RequirePlacedLayout,
  SetupIndexRedirect,
} from '@/app/guards'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RegisterPage } from '@/features/auth/pages/RegisterPage'
import { GoalPage } from '@/features/onboarding/pages/GoalPage'
import { QuestionsPage } from '@/features/onboarding/pages/QuestionsPage'
import { PlacementResultPage } from '@/features/onboarding/pages/PlacementResultPage'
import { HomePage } from '@/features/home/pages/HomePage'
import { WorkoutSessionPage } from '@/features/home/pages/WorkoutSessionPage'
import { ProfilePage } from '@/features/profile/pages/ProfilePage'
import { AdminLayout } from '@/features/admin/layout/AdminLayout'
import { AdminHomePage } from '@/features/admin/pages/AdminHomePage'
import { AdminExercisesPage } from '@/features/admin/pages/AdminExercisesPage'
import { AdminNodesPage } from '@/features/admin/pages/AdminNodesPage'
import { AdminWorkoutsPage } from '@/features/admin/pages/AdminWorkoutsPage'
import { AdminWorkoutDetailPage } from '@/features/admin/pages/AdminWorkoutDetailPage'
import { AdminPathPage } from '@/features/admin/pages/AdminPathPage'
import { AdminPathQuestionsPage } from '@/features/admin/pages/AdminPathQuestionsPage'

// Vite injects BASE_URL from `base` (e.g. `/calistrack/` on GitHub Pages)
const basename = import.meta.env.BASE_URL.replace(/\/$/, '') || undefined

export const router = createBrowserRouter(
  [
  {
    path: '/',
    element: <Navigate to="/setup" replace />,
  },
  {
    element: <PublicOnlyLayout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },
  {
    element: <RequireAuthLayout />,
    children: [
      {
        element: <RequireAdminLayout />,
        children: [
          {
            path: '/admin',
            element: <AdminLayout />,
            children: [
              { index: true, element: <AdminHomePage /> },
              { path: 'exercises', element: <AdminExercisesPage /> },
              { path: 'nodes', element: <AdminNodesPage /> },
              { path: 'workouts', element: <AdminWorkoutsPage /> },
              { path: 'workouts/:workoutId', element: <AdminWorkoutDetailPage /> },
              { path: 'path', element: <AdminPathPage /> },
              { path: 'path-questions', element: <AdminPathQuestionsPage /> },
            ],
          },
        ],
      },
      {
        element: <RejectAdminLayout />,
        children: [
          { path: '/setup', element: <SetupIndexRedirect /> },
          { path: '/setup/goal', element: <GoalPage /> },
          {
            element: <RequireGoalLayout />,
            children: [
              { path: '/setup/questions', element: <QuestionsPage /> },
            ],
          },
          { path: '/setup/result', element: <PlacementResultPage /> },
          {
            element: <RequirePlacedLayout />,
            children: [
              { path: '/home', element: <HomePage /> },
              { path: '/sessions/:sessionId', element: <WorkoutSessionPage /> },
              { path: '/profile', element: <ProfilePage /> },
            ],
          },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
  ],
  { basename },
)
