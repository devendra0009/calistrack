/**
 * Short spoken cues for stretch session actions (Web Speech API),
 * then a whistle the instant the spoken phrase ends.
 */

export type StretchCue =
  | 'start'
  | 'stop'
  | 'pause'
  | 'resume'
  | 'rest now'
  | 'done'
  | 'next'
  | 'finish'

/** Drop your whistle here: `frontend/public/sounds/whistle.mp3` */
const WHISTLE_SRC = '/sounds/whistle.mp3'

let cueGeneration = 0
let audioCtx: AudioContext | null = null
let whistleAudio: HTMLAudioElement | null = null
let whistleFileAvailable: boolean | null = null
let pendingPoll: ReturnType<typeof setInterval> | null = null
let pendingFallback: ReturnType<typeof setTimeout> | null = null

function getAudioContext(): AudioContext | null {
  if (typeof window === 'undefined') return null
  const Ctx =
    window.AudioContext ||
    (window as unknown as { webkitAudioContext?: typeof AudioContext })
      .webkitAudioContext
  if (!Ctx) return null
  if (!audioCtx) audioCtx = new Ctx()
  return audioCtx
}

/** Short referee-style whistle when no MP3 is present. */
function playSynthesizedWhistle(): void {
  const ctx = getAudioContext()
  if (!ctx) return

  void ctx.resume().catch(() => {})

  const now = ctx.currentTime
  const gain = ctx.createGain()
  gain.gain.setValueAtTime(0.0001, now)
  gain.gain.exponentialRampToValueAtTime(0.22, now + 0.02)
  gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.28)
  gain.connect(ctx.destination)

  const osc = ctx.createOscillator()
  osc.type = 'sine'
  osc.frequency.setValueAtTime(1800, now)
  osc.frequency.linearRampToValueAtTime(2400, now + 0.12)
  osc.frequency.linearRampToValueAtTime(1900, now + 0.28)
  osc.connect(gain)
  osc.start(now)
  osc.stop(now + 0.3)
}

function getWhistleAudio(): HTMLAudioElement {
  if (!whistleAudio) {
    whistleAudio = new Audio(WHISTLE_SRC)
    whistleAudio.preload = 'auto'
    try {
      whistleAudio.load()
    } catch {
      // ignore
    }
  }
  return whistleAudio
}

/** Prefetch so whistle starts with no decode lag when the word ends. */
export function preloadStretchSounds(): void {
  if (typeof window === 'undefined') return
  getWhistleAudio()
}

async function playWhistle(): Promise<void> {
  if (typeof window === 'undefined') return

  if (whistleFileAvailable === false) {
    playSynthesizedWhistle()
    return
  }

  try {
    const audio = getWhistleAudio()
    audio.pause()
    audio.currentTime = 0
    await audio.play()
    whistleFileAvailable = true
  } catch {
    whistleFileAvailable = false
    playSynthesizedWhistle()
  }
}

function clearPending(): void {
  if (pendingPoll != null) {
    clearInterval(pendingPoll)
    pendingPoll = null
  }
  if (pendingFallback != null) {
    clearTimeout(pendingFallback)
    pendingFallback = null
  }
}

/**
 * Speaks the cue, then whistles as soon as the phrase finishes speaking
 * (no overlap). Polls `speaking` because Chrome often delays `onend`.
 */
export function speakCue(phrase: StretchCue | string): void {
  if (typeof window === 'undefined') return

  const text = phrase.trim()
  if (!text) return

  const gen = ++cueGeneration
  let fired = false
  clearPending()

  const fireWhistleOnce = () => {
    if (fired || gen !== cueGeneration) return
    fired = true
    clearPending()
    void playWhistle()
  }

  const synth = window.speechSynthesis
  if (!synth) {
    fireWhistleOnce()
    return
  }

  try {
    synth.cancel()
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.rate = 1.1
    utterance.pitch = 1
    utterance.volume = 1
    utterance.onend = fireWhistleOnce
    utterance.onerror = fireWhistleOnce
    synth.speak(utterance)

    // Detect real end of audio (Chrome’s onend is often ~0.5–1s late).
    let heardSpeaking = false
    pendingPoll = setInterval(() => {
      if (gen !== cueGeneration) {
        clearPending()
        return
      }
      if (synth.speaking) {
        heardSpeaking = true
        return
      }
      if (heardSpeaking) {
        fireWhistleOnce()
      }
    }, 32)

    // Safety if speaking never flips true (rare engine glitches).
    pendingFallback = setTimeout(fireWhistleOnce, 2000)
  } catch {
    fireWhistleOnce()
  }
}
