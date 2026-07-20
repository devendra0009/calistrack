import { useEffect, useState } from 'react'
import { cn } from '@/shared/lib/cn'
import { readStoredSession } from '@/shared/lib/session-storage'

type UserAvatarProps = {
  displayName: string
  avatarUrl?: string | null
  size?: 'md' | 'lg'
  className?: string
}

function initialsFromName(displayName: string): string {
  const parts = displayName.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[1][0]).toUpperCase()
}

function needsAuthFetch(url: string): boolean {
  return url.includes('/api/v1/media/local/')
}

export function UserAvatar({
  displayName,
  avatarUrl,
  size = 'lg',
  className,
}: UserAvatarProps) {
  const [resolvedUrl, setResolvedUrl] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)
  const initials = initialsFromName(displayName)

  useEffect(() => {
    setFailed(false)
    setResolvedUrl(null)

    if (!avatarUrl) return

    if (!needsAuthFetch(avatarUrl)) {
      setResolvedUrl(avatarUrl)
      return
    }

    const { idToken } = readStoredSession()
    let objectUrl: string | null = null
    let cancelled = false

    ;(async () => {
      try {
        const res = await fetch(avatarUrl, {
          headers: idToken ? { Authorization: `Bearer ${idToken}` } : {},
        })
        if (!res.ok) throw new Error('avatar fetch failed')
        const blob = await res.blob()
        if (cancelled) return
        objectUrl = URL.createObjectURL(blob)
        setResolvedUrl(objectUrl)
      } catch {
        if (!cancelled) setFailed(true)
      }
    })()

    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [avatarUrl])

  const sizeClass = size === 'lg' ? 'h-28 w-28 text-2xl' : 'h-10 w-10 text-sm'
  const showImage = Boolean(resolvedUrl) && !failed

  return (
    <div
      className={cn(
        'flex shrink-0 items-center justify-center overflow-hidden rounded-full border border-stone-200 bg-emerald-800 font-semibold text-white',
        sizeClass,
        className,
      )}
      aria-label={displayName}
    >
      {showImage ? (
        <img
          src={resolvedUrl!}
          alt={displayName}
          className="h-full w-full object-cover"
          onError={() => setFailed(true)}
        />
      ) : (
        <span aria-hidden>{initials}</span>
      )}
    </div>
  )
}
