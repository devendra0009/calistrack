/** Parses stretch exercise descriptions into form steps + muscle/benefit line. */

export type StretchGuide = {
  steps: string[]
  /** Raw text after TARGETS: (muscles + what it helps). */
  targets: string | null
  /** Muscle list before the em dash / hyphen benefit clause. */
  muscles: string | null
  /** Benefit clause after the dash, if present. */
  benefit: string | null
}

const TARGETS_RE = /^TARGETS:\s*/i

export function parseStretchGuide(description: string | null | undefined): StretchGuide {
  if (!description?.trim()) {
    return { steps: [], targets: null, muscles: null, benefit: null }
  }

  const lines = description
    .split(/\n+/)
    .map((line) => line.trim())
    .filter(Boolean)

  const targetIdx = lines.findIndex((line) => TARGETS_RE.test(line))
  const stepLines =
    targetIdx >= 0 ? lines.slice(0, targetIdx) : lines
  const targetsRaw =
    targetIdx >= 0
      ? lines[targetIdx].replace(TARGETS_RE, '').trim()
      : null

  const steps = stepLines.map((line) =>
    line.replace(/^\s*\d+[.)]\s*/, '').trim(),
  )

  let muscles: string | null = null
  let benefit: string | null = null
  if (targetsRaw) {
    const split = targetsRaw.split(/\s+[—–-]\s+/)
    if (split.length >= 2) {
      muscles = split[0].trim()
      benefit = split.slice(1).join(' — ').trim()
    } else {
      muscles = targetsRaw
    }
  }

  return { steps, targets: targetsRaw, muscles, benefit }
}

export function formatHoldClock(totalSeconds: number): string {
  const safe = Math.max(0, Math.floor(totalSeconds))
  const m = Math.floor(safe / 60)
  const s = safe % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export function holdLabel(line: {
  targetHoldSeconds: number | null
  targetSets: number | null
  targetReps: number | null
}): string {
  if (line.targetHoldSeconds != null) {
    if (line.targetSets != null && line.targetSets > 1) {
      return `${line.targetSets} × ${line.targetHoldSeconds}s`
    }
    return `${line.targetHoldSeconds}s`
  }
  if (line.targetReps != null) {
    return line.targetSets != null
      ? `${line.targetSets} × ${line.targetReps}`
      : `${line.targetReps} reps`
  }
  return 'As comfortable'
}
