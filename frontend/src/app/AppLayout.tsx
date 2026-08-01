import { Outlet } from 'react-router'
import { AppNav } from '@/app/AppNav'
import { AiCoachChatWidget } from '@/features/chat/components/AiCoachChatWidget'

/** Shared chrome for placed-user routes — navbar stays the same across pages. */
export function AppLayout() {
  return (
    <div className="bg-app min-h-dvh">
      <AppNav />
      <Outlet />
      <AiCoachChatWidget />
    </div>
  )
}
