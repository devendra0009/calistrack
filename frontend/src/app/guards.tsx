import { Navigate, Outlet } from 'react-router'
import { Spinner } from '@/shared/ui/Spinner'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'
import { useMe } from '@/features/profile/api'
import { useOnboardingStatus } from '@/features/onboarding/api'
import { isAdmin } from '@/features/admin/role'

export function PublicOnlyLayout() {
  const { isReady, isAuthenticated } = useAuthSession()

  if (!isReady) {
    return (
      <div className="min-h-dvh">
        <Spinner label="Starting…" />
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to="/setup" replace />
  }

  return <Outlet />
}

export function RequireAuthLayout() {
  const { isReady, isAuthenticated } = useAuthSession()

  if (!isReady) {
    return (
      <div className="min-h-dvh">
        <Spinner label="Starting…" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}

/** Sends users to the correct setup step, home, or admin. */
export function SetupIndexRedirect() {
  const me = useMe()
  const status = useOnboardingStatus(Boolean(me.data?.goal) && !isAdmin(me.data))

  if (me.isLoading || (me.data?.goal && !isAdmin(me.data) && status.isLoading)) {
    return (
      <div className="min-h-dvh">
        <Spinner label="Loading profile…" />
      </div>
    )
  }

  if (me.isError || !me.data) {
    return <Navigate to="/login" replace />
  }

  if (isAdmin(me.data)) {
    return <Navigate to="/admin" replace />
  }

  if (!me.data.goal) {
    return <Navigate to="/setup/goal" replace />
  }

  if (status.isError) {
    return <Navigate to="/setup/questions" replace />
  }

  if (!status.data?.completed) {
    return <Navigate to="/setup/questions" replace />
  }

  return <Navigate to="/home" replace />
}

/** Admin-only area. Non-admins go back through normal setup routing. */
export function RequireAdminLayout() {
  const me = useMe()

  if (me.isLoading) {
    return (
      <div className="min-h-dvh">
        <Spinner label="Loading profile…" />
      </div>
    )
  }

  if (me.isError || !me.data) {
    return <Navigate to="/login" replace />
  }

  if (!isAdmin(me.data)) {
    return <Navigate to="/setup" replace />
  }

  return <Outlet />
}

/** Keep admins out of the user onboarding / app shell. */
export function RejectAdminLayout() {
  const me = useMe()

  if (me.isLoading) {
    return (
      <div className="min-h-dvh">
        <Spinner label="Loading profile…" />
      </div>
    )
  }

  if (me.data && isAdmin(me.data)) {
    return <Navigate to="/admin" replace />
  }

  return <Outlet />
}

export function RequireGoalLayout() {
  const me = useMe()
  const status = useOnboardingStatus(Boolean(me.data?.goal))

  if (me.isLoading || (me.data?.goal && status.isLoading)) {
    return (
      <div className="min-h-dvh">
        <Spinner label="Loading profile…" />
      </div>
    )
  }

  if (me.data && isAdmin(me.data)) {
    return <Navigate to="/admin" replace />
  }

  if (!me.data?.goal) {
    return <Navigate to="/setup/goal" replace />
  }

  // Already placed — don't re-show questionnaire / result setup flow.
  if (status.data?.completed) {
    return <Navigate to="/home" replace />
  }

  return <Outlet />
}

export function RequirePlacedLayout() {
  const me = useMe()
  const status = useOnboardingStatus(Boolean(me.data) && !isAdmin(me.data))

  if (me.isLoading || (me.data && !isAdmin(me.data) && status.isLoading)) {
    return (
      <div className="min-h-dvh">
        <Spinner label="Loading profile…" />
      </div>
    )
  }

  if (!me.data) {
    return <Navigate to="/login" replace />
  }

  if (isAdmin(me.data)) {
    return <Navigate to="/admin" replace />
  }

  if (!me.data.goal || !status.data?.completed) {
    return <Navigate to="/setup" replace />
  }

  return <Outlet />
}
