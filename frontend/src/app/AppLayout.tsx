import { Outlet } from 'react-router'
import { AppNav } from '@/app/AppNav'
import { AiCoachChatWidget } from '@/features/chat/components/AiCoachChatWidget'

/** Shared chrome for placed-user routes — navbar stays the same across pages. */
export function AppLayout() {
  return (
    <div className="bg-app min-h-dvh">
      <AppNav />
      {/* Extra bottom space on mobile clears the fixed tab bar + home indicator */}
      <div className="pb-[calc(4.25rem+env(safe-area-inset-bottom))] md:pb-0">
        <Outlet />
      </div>
      <AiCoachChatWidget />
    </div>
  )
}
