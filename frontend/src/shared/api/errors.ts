export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export function parseErrorMessage(body: unknown, fallback: string): string {
  if (!body || typeof body !== 'object') return fallback

  // RFC 7807 Problem Details
  if ('detail' in body) {
    const detail = (body as { detail: unknown }).detail
    if (typeof detail === 'string' && detail.length > 0) return detail
  }

  // Legacy { error: "..." }
  if ('error' in body) {
    const error = (body as { error: unknown }).error
    if (typeof error === 'string' && error.length > 0) return error
  }

  return fallback
}

