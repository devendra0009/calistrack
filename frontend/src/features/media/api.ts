import { api } from '@/shared/api/client'
import { ApiError } from '@/shared/api/errors'
import type {
  MediaResponse,
  MediaType,
  MediaVisibility,
  ResourceType,
  UploadRequestResponse,
} from '@/features/media/types'

const IMAGE_MIME_TYPES = new Set([
  'image/jpeg',
  'image/png',
  'image/webp',
  'image/gif',
])

const VIDEO_MIME_TYPES = new Set([
  'video/mp4',
  'video/webm',
  'video/quicktime',
])

const MAX_AVATAR_BYTES = 5 * 1024 * 1024
const MAX_EXERCISE_IMAGE_BYTES = 10 * 1024 * 1024
const MAX_EXERCISE_VIDEO_BYTES = 50 * 1024 * 1024

/**
 * Direct-to-storage upload:
 * 1) authorize → 2) PUT/POST file to provider → 3) complete + verify
 */
export async function uploadMediaFile(
  file: File,
  options: {
    mediaType: MediaType
    resourceType?: ResourceType
    visibility?: MediaVisibility
  },
): Promise<MediaResponse> {
  const authorization = await api.post<UploadRequestResponse>(
    '/api/v1/media/upload-request',
    {
      originalFilename: file.name,
      mimeType: file.type || 'application/octet-stream',
      fileSizeBytes: file.size,
      resourceType: options.resourceType ?? 'IMAGE',
      mediaType: options.mediaType,
      visibility: options.visibility ?? 'PUBLIC',
    },
  )

  await transferToProvider(authorization, file)

  return api.post<MediaResponse>('/api/v1/media/complete', {
    mediaId: authorization.mediaId,
  })
}

function mediaPublicUrl(media: MediaResponse): string {
  const url = media.secureUrl ?? media.downloadUrl
  if (!url) {
    throw new ApiError(502, 'Upload completed but no media URL was returned')
  }
  return url
}

export async function uploadAvatarAndSetProfile(file: File): Promise<string> {
  assertAvatarFile(file)

  const media = await uploadMediaFile(file, {
    mediaType: 'AVATAR',
    resourceType: 'IMAGE',
    visibility: 'PUBLIC',
  })

  const avatarUrl = mediaPublicUrl(media)
  await api.patch('/api/v1/me', { avatarUrl })
  return avatarUrl
}

/** Upload an exercise thumbnail image; returns the public URL to store on the exercise. */
export async function uploadExerciseThumbnail(file: File): Promise<string> {
  assertExerciseImage(file)
  const media = await uploadMediaFile(file, {
    mediaType: 'EXERCISE_THUMBNAIL',
    resourceType: 'IMAGE',
    visibility: 'PUBLIC',
  })
  return mediaPublicUrl(media)
}

/** Upload an exercise demo video (or image); returns the public URL to store on the exercise. */
export async function uploadExerciseDemo(file: File): Promise<string> {
  const isVideo = VIDEO_MIME_TYPES.has(file.type)
  const isImage = IMAGE_MIME_TYPES.has(file.type)
  if (!isVideo && !isImage) {
    throw new ApiError(
      400,
      'Demo must be an MP4, WebM, MOV, JPEG, PNG, WebP, or GIF',
    )
  }
  if (isVideo && file.size > MAX_EXERCISE_VIDEO_BYTES) {
    throw new ApiError(400, 'Demo video must be 50 MB or smaller')
  }
  if (isImage && file.size > MAX_EXERCISE_IMAGE_BYTES) {
    throw new ApiError(400, 'Demo image must be 10 MB or smaller')
  }

  const media = await uploadMediaFile(file, {
    mediaType: 'EXERCISE_DEMO',
    resourceType: isVideo ? 'VIDEO' : 'IMAGE',
    visibility: 'PUBLIC',
  })
  return mediaPublicUrl(media)
}

function assertAvatarFile(file: File): void {
  if (!IMAGE_MIME_TYPES.has(file.type)) {
    throw new ApiError(400, 'Profile photo must be a JPEG, PNG, WebP, or GIF')
  }
  if (file.size > MAX_AVATAR_BYTES) {
    throw new ApiError(400, 'Profile photo must be 5 MB or smaller')
  }
}

function assertExerciseImage(file: File): void {
  if (!IMAGE_MIME_TYPES.has(file.type)) {
    throw new ApiError(400, 'Thumbnail must be a JPEG, PNG, WebP, or GIF')
  }
  if (file.size > MAX_EXERCISE_IMAGE_BYTES) {
    throw new ApiError(400, 'Thumbnail must be 10 MB or smaller')
  }
}

async function transferToProvider(
  authorization: UploadRequestResponse,
  file: File,
): Promise<void> {
  const method = (authorization.httpMethod || 'PUT').toUpperCase()

  if (method === 'PUT') {
    const headers = new Headers(authorization.headers ?? {})
    if (!headers.has('Content-Type') && file.type) {
      headers.set('Content-Type', file.type)
    }

    const res = await fetch(authorization.uploadUrl, {
      method: 'PUT',
      headers,
      body: file,
    })

    if (!res.ok) {
      throw new ApiError(res.status, `Direct upload failed (${res.status})`)
    }
    return
  }

  // Cloudinary-style signed form POST (multipart to the provider, never to our API)
  const form = new FormData()
  for (const [key, value] of Object.entries(authorization.formFields ?? {})) {
    form.append(key, value)
  }
  form.append('file', file)

  const res = await fetch(authorization.uploadUrl, {
    method: 'POST',
    body: form,
  })

  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new ApiError(
      res.status,
      text || `Direct upload failed (${res.status})`,
    )
  }
}
