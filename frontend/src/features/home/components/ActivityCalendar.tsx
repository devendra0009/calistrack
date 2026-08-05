import { useMemo } from 'react'
import { cn } from '@/shared/lib/cn'
import { Spinner } from '@/shared/ui/Spinner'
import { activityRangeForDays, useActivityCalendar } from '@/features/home/activityApi'

type ActivityCalendarProps = {
  /** Inclusive day window ending today. Default 7; later wire a 30/60 dropdown. */
  dayCount?: number
}

type DayCell = {
  date: string
  label: string
  weekday: string
  active: boolean
  count: number
  isToday: boolean
}

function buildDayCells(
  from: string,
  to: string,
  activeByDate: Map<string, number>,
): DayCell[] {
  const cells: DayCell[] = []
  const cursor = parseLocalYmd(from)
  const end = parseLocalYmd(to)
  const today = to

  const weekdayFmt = new Intl.DateTimeFormat(undefined, { weekday: 'short' })
  const dayFmt = new Intl.DateTimeFormat(undefined, { day: 'numeric' })

  while (cursor <= end) {
    const ymd = formatLocalYmd(cursor)
    const count = activeByDate.get(ymd) ?? 0
    cells.push({
      date: ymd,
      label: dayFmt.format(cursor),
      weekday: weekdayFmt.format(cursor),
      active: count > 0,
      count,
      isToday: ymd === today,
    })
    cursor.setDate(cursor.getDate() + 1)
  }
  return cells
}

function parseLocalYmd(ymd: string): Date {
  const [y, m, d] = ymd.split('-').map(Number)
  return new Date(y, m - 1, d)
}

function formatLocalYmd(date: Date): string {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function ActivityCalendar({ dayCount = 7 }: ActivityCalendarProps) {
  const query = useActivityCalendar(dayCount)
  const { from, to } = activityRangeForDays(dayCount)

  const cells = useMemo(() => {
    const activeByDate = new Map<string, number>()
    for (const day of query.data?.days ?? []) {
      activeByDate.set(day.date, day.count)
    }
    return buildDayCells(from, to, activeByDate)
  }, [from, to, query.data?.days])

  const activeCount = cells.filter((c) => c.active).length

  return (
    <section className="rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm sm:p-6">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 className="text-sm font-semibold uppercase tracking-wide text-stone-500">
            Activity Calendar
          </h2>
          <p className="mt-1 text-sm text-stone-600">
            Let&apos;s do some stretch or workout to keep your activity level
            high.
          </p>
        </div>
        {!query.isLoading && !query.isError ? (
          <p className="shrink-0 text-xs font-medium tabular-nums text-stone-500">
            {activeCount}/{dayCount} days active
          </p>
        ) : null}
      </div>

      {query.isLoading ? (
        <div className="mt-5">
          <Spinner label="Loading activity…" />
        </div>
      ) : query.isError ? (
        <p className="mt-5 text-sm text-stone-600">
          Could not load your activity. Try refreshing the page.
        </p>
      ) : (
        <ol
          className={cn(
            'mt-5 grid gap-2',
            dayCount <= 7
              ? 'grid-cols-7'
              : 'grid-cols-7 sm:grid-cols-10',
          )}
          aria-label={`Last ${dayCount} days of activity`}
        >
          {cells.map((cell) => (
            <li key={cell.date} className="min-w-0">
              <div
                className={cn(
                  'flex flex-col items-center gap-1.5 rounded-xl px-1 py-2',
                  cell.isToday && 'ring-1 ring-stone-300 ring-offset-1 ring-offset-stone-50',
                )}
                title={
                  cell.active
                    ? `${cell.date}: ${cell.count} completed`
                    : `${cell.date}: no activity`
                }
              >
                <span className="text-[10px] font-semibold uppercase tracking-wide text-stone-400">
                  {cell.weekday}
                </span>
                <span
                  className={cn(
                    'flex size-9 items-center justify-center rounded-full text-sm font-semibold tabular-nums transition-colors sm:size-10',
                    cell.active
                      ? 'bg-emerald-600 text-white'
                      : 'bg-stone-200/80 text-stone-500',
                  )}
                  aria-label={
                    cell.active
                      ? `${cell.weekday} ${cell.label}, activity done`
                      : `${cell.weekday} ${cell.label}, no activity`
                  }
                >
                  {cell.label}
                </span>
              </div>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
