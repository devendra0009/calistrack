/**
 * Mild client-side image compression before Cloudinary upload.
 * Shrinks large edges a little and re-encodes at high quality so on-image
 * text stays readable — not an aggressive “crush everything” pass.
 */

export type CompressImageOptions = {
  /** Longest side in px. Default 1600 — enough for how-to diagrams with text. */
  maxEdge?: number
  /** JPEG/WebP quality 0–1. Default 0.84 keeps text crisp. */
  quality?: number
  /** Skip if file is already this small (bytes). Default 150KB. */
  minBytesToCompress?: number
}

const DEFAULTS: Required<CompressImageOptions> = {
  maxEdge: 1600,
  quality: 0.84,
  minBytesToCompress: 150 * 1024,
}

function prefersWebp(): boolean {
  try {
    const c = document.createElement('canvas')
    return c.toDataURL('image/webp').startsWith('data:image/webp')
  } catch {
    return false
  }
}

function replaceExtension(filename: string, ext: string): string {
  const base = filename.replace(/\.[^.]+$/, '') || filename
  return `${base}.${ext}`
}

/**
 * Returns a smaller File when compression helps; otherwise the original.
 * GIFs are left alone (animation / palette). Videos are not handled here.
 */
export async function compressImageForUpload(
  file: File,
  options: CompressImageOptions = {},
): Promise<File> {
  if (!file.type.startsWith('image/')) return file
  if (file.type === 'image/gif') return file

  const { maxEdge, quality, minBytesToCompress } = { ...DEFAULTS, ...options }
  if (file.size < minBytesToCompress) return file

  let bitmap: ImageBitmap
  try {
    bitmap = await createImageBitmap(file)
  } catch {
    return file
  }

  const { width, height } = bitmap
  if (width < 1 || height < 1) {
    bitmap.close()
    return file
  }

  const scale = Math.min(1, maxEdge / Math.max(width, height))
  const targetW = Math.max(1, Math.round(width * scale))
  const targetH = Math.max(1, Math.round(height * scale))

  // Already small enough on disk and not oversized — skip re-encode noise
  if (scale === 1 && file.size < minBytesToCompress * 2) {
    bitmap.close()
    return file
  }

  const canvas = document.createElement('canvas')
  canvas.width = targetW
  canvas.height = targetH
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    bitmap.close()
    return file
  }

  ctx.imageSmoothingEnabled = true
  ctx.imageSmoothingQuality = 'high'
  ctx.drawImage(bitmap, 0, 0, targetW, targetH)
  bitmap.close()

  const useWebp = prefersWebp()
  const mimeType = useWebp ? 'image/webp' : 'image/jpeg'
  const ext = useWebp ? 'webp' : 'jpg'

  const blob = await new Promise<Blob | null>((resolve) => {
    canvas.toBlob((b) => resolve(b), mimeType, quality)
  })

  if (!blob || blob.size === 0) return file
  // Only swap if we actually saved space (keep a small margin for encoder variance)
  if (blob.size >= file.size * 0.98) return file

  return new File([blob], replaceExtension(file.name, ext), {
    type: mimeType,
    lastModified: Date.now(),
  })
}

/** Presets tuned per media use-case. */
export const IMAGE_COMPRESS_PRESETS = {
  /** How-to / demo stills — mild shrink, text-friendly quality. */
  exercise: { maxEdge: 1600, quality: 0.84, minBytesToCompress: 150 * 1024 },
  /** Profile photos — smaller edge is fine. */
  avatar: { maxEdge: 800, quality: 0.82, minBytesToCompress: 80 * 1024 },
} as const satisfies Record<string, CompressImageOptions>
