import type { MeResponse } from '@/shared/api/types'

export function isAdmin(me: Pick<MeResponse, 'role'> | null | undefined): boolean {
  return me?.role === 'ADMIN'
}

/** Post-login destination based on role and onboarding. */
export function postAuthPath(me: MeResponse, onboardingCompleted: boolean | undefined): string {
  if (isAdmin(me)) return '/admin'
  if (!me.goal) return '/setup/goal'
  if (!onboardingCompleted) return '/setup/questions'
  return '/home'
}
