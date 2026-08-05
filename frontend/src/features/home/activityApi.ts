import { useQuery } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type { ActivityCalendarResponse } from '@/shared/api/types'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'

export const activityQueryKeyRoot = ['activity'] as const

export function activityCalendarQueryKey(from: string, to: string, timezone: string) {
  return [...activityQueryKeyRoot, 'calendar', from, to, timezone] as const
}

function localYmd(date: Date): string {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/** Inclusive local-date window ending today, length = dayCount (e.g. 7, 30, 60). */
export function activityRangeForDays(dayCount: number, now = new Date()) {
  const safeCount = Math.max(1, Math.min(dayCount, 90))
  const to = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const from = new Date(to)
  from.setDate(from.getDate() - (safeCount - 1))
  return { from: localYmd(from), to: localYmd(to) }
}

export function browserTimezone(): string {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'
  } catch {
    return 'UTC'
  }
}

export function fetchActivityCalendar(from: string, to: string, timezone: string) {
  const params = new URLSearchParams({ from, to, timezone })
  return api.get<ActivityCalendarResponse>(`/api/v1/activity?${params}`)
}

/**
 * Activity days for the last `dayCount` local days (default 7).
 * Pass 30 / 60 later when a range dropdown is added.
 */
export function useActivityCalendar(dayCount = 7, enabled = true) {
  const { isAuthenticated, isReady } = useAuthSession()
  const { from, to } = activityRangeForDays(dayCount)
  const timezone = browserTimezone()

  return useQuery({
    queryKey: activityCalendarQueryKey(from, to, timezone),
    queryFn: () => fetchActivityCalendar(from, to, timezone),
    enabled: enabled && isReady && isAuthenticated,
    retry: false,
    staleTime: 60_000,
  })
}
