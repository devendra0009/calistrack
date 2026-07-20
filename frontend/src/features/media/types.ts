export type ResourceType = 'IMAGE' | 'VIDEO' | 'RAW' | 'AUTO'
export type MediaType =
  | 'AVATAR'
  | 'EXERCISE_DEMO'
  | 'EXERCISE_THUMBNAIL'
  | 'ASSESSMENT_VIDEO'
  | 'GENERAL'
export type MediaVisibility = 'PRIVATE' | 'PUBLIC' | 'UNLISTED'
export type UploadStatus =
  | 'PENDING'
  | 'UPLOADED'
  | 'VERIFIED'
  | 'FAILED'
  | 'DELETED'
export type StorageProviderType = 'CLOUDINARY' | 'AWS_S3' | 'LOCAL'

export interface UploadRequestBody {
  originalFilename: string
  mimeType: string
  fileSizeBytes: number
  resourceType: ResourceType
  mediaType: MediaType
  visibility?: MediaVisibility
}

export interface UploadRequestResponse {
  mediaId: string
  provider: StorageProviderType
  uploadUrl: string
  httpMethod: string
  headers: Record<string, string>
  formFields: Record<string, string>
  publicId: string
  bucketName: string
}

export interface MediaResponse {
  id: string
  ownerUserId: string
  provider: StorageProviderType
  bucketName: string | null
  publicId: string
  originalFilename: string | null
  mimeType: string
  extension: string | null
  fileSizeBytes: number | null
  width: number | null
  height: number | null
  durationSeconds: number | null
  resourceType: ResourceType
  mediaType: MediaType
  secureUrl: string | null
  thumbnailUrl: string | null
  checksum: string | null
  uploadStatus: UploadStatus
  visibility: MediaVisibility
  downloadUrl: string | null
  providerMetadata: Record<string, unknown> | null
  createdAt: string
  updatedAt: string
}
