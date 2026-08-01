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

/** Upload an assessment proof video; returns the public URL to store on the assessment. */
export async function uploadAssessmentVideo(file: File): Promise<string> {
  if (!VIDEO_MIME_TYPES.has(file.type)) {
    throw new ApiError(400, 'Assessment video must be MP4, WebM, or MOV')
  }
  if (file.size > MAX_EXERCISE_VIDEO_BYTES) {
    throw new ApiError(400, 'Assessment video must be 50 MB or smaller')
  }

  const media = await uploadMediaFile(file, {
    mediaType: 'ASSESSMENT_VIDEO',
    resourceType: 'VIDEO',
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
      throw new ApiError(
        res.status,
        await readProviderError(res, `Direct upload failed (${res.status})`),
      )
    }
    return
  }

  // Cloudinary signed form POST — chunk videos / large files (Windows camera clips fail as one shot)
  const shouldChunk =
    authorization.provider === 'CLOUDINARY' &&
    (file.size > CLOUDINARY_CHUNK_THRESHOLD || file.type.startsWith('video/'))

  if (shouldChunk) {
    await uploadCloudinaryChunked(authorization, file)
    return
  }

  const form = new FormData()
  for (const [key, value] of Object.entries(authorization.formFields ?? {})) {
    if (value != null && value !== '') {
      form.append(key, value)
    }
  }
  form.append('file', file, file.name)

  let res: Response
  try {
    res = await fetch(authorization.uploadUrl, {
      method: 'POST',
      body: form,
    })
  } catch (err) {
    throw new ApiError(
      0,
      err instanceof Error
        ? `Upload network error: ${err.message}`
        : 'Upload network error — check connection / Brave shields',
    )
  }

  if (!res.ok) {
    throw new ApiError(
      res.status,
      await readProviderError(res, `Direct upload failed (${res.status})`),
    )
  }
}

/** Cloudinary requires chunks ≥ 5MB (except the last). Use 6MB. */
const CLOUDINARY_CHUNK_SIZE = 6 * 1024 * 1024
/** Chunk videos / large files — single POSTs often fail on Windows camera recordings. */
const CLOUDINARY_CHUNK_THRESHOLD = 5 * 1024 * 1024

async function uploadCloudinaryChunked(
  authorization: UploadRequestResponse,
  file: File,
): Promise<void> {
  const uploadId =
    typeof crypto !== 'undefined' && 'randomUUID' in crypto
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(36).slice(2)}`

  let start = 0
  while (start < file.size) {
    const end = Math.min(start + CLOUDINARY_CHUNK_SIZE, file.size)
    const blob = file.slice(start, end)
    const form = new FormData()
    for (const [key, value] of Object.entries(authorization.formFields ?? {})) {
      if (value != null && value !== '') {
        form.append(key, value)
      }
    }
    form.append('file', blob, file.name)

    let res: Response
    try {
      res = await fetch(authorization.uploadUrl, {
        method: 'POST',
        headers: {
          'X-Unique-Upload-Id': uploadId,
          'Content-Range': `bytes ${start}-${end - 1}/${file.size}`,
        },
        body: form,
      })
    } catch (err) {
      throw new ApiError(
        0,
        err instanceof Error
          ? `Chunked upload network error: ${err.message}`
          : 'Chunked upload network error',
      )
    }

    if (!res.ok) {
      throw new ApiError(
        res.status,
        await readProviderError(
          res,
          `Chunked upload failed at bytes ${start}-${end - 1} (${res.status})`,
        ),
      )
    }

    start = end
  }
}

async function readProviderError(
  res: Response,
  fallback: string,
): Promise<string> {
  const text = await res.text().catch(() => '')
  if (!text) return fallback
  try {
    const json = JSON.parse(text) as {
      error?: { message?: string }
      message?: string
    }
    return json.error?.message || json.message || text || fallback
  } catch {
    return text.length > 280 ? `${text.slice(0, 280)}…` : text
  }
}
