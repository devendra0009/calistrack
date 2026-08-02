import {
  useEffect,
  useId,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from 'react'
import { MessageCircle, SendHorizontal, X } from 'lucide-react'
import { toast } from 'sonner'
import { useChatMutation } from '@/features/chat/api'
import { ApiError } from '@/shared/api/errors'
import { cn } from '@/shared/lib/cn'

type ChatRole = 'user' | 'assistant'

type ChatMessage = {
  id: string
  role: ChatRole
  content: string
}

const SUGGESTIONS = [
  'How can I improve my pull-ups?',
  'What is a good beginner weekly plan?',
  'How should I recover between skill days?',
] as const

const WELCOME =
  "Hi — I'm the CaliTrack AI Coach. Ask me about calisthenics, form, progressions, nutrition, or recovery."

function newId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

export function AiCoachChatWidget() {
  const titleId = useId()
  const [open, setOpen] = useState(false)
  const [input, setInput] = useState('')
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const listRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const chatMutation = useChatMutation()

  const resetConversation = () => {
    setMessages([])
    setInput('')
    chatMutation.reset()
  }

  const close = () => {
    setOpen(false)
    resetConversation()
  }

  const openPanel = () => {
    setOpen(true)
    setMessages([{ id: newId(), role: 'assistant', content: WELCOME }])
  }

  useEffect(() => {
    if (!open) return
    const frame = requestAnimationFrame(() => inputRef.current?.focus())
    return () => cancelAnimationFrame(frame)
  }, [open])

  useEffect(() => {
    if (!open) return
    const el = listRef.current
    if (!el) return
    el.scrollTop = el.scrollHeight
  }, [open, messages, chatMutation.isPending])

  useEffect(() => {
    if (!open) return
    const onKeyDown = (event: globalThis.KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
        setMessages([])
        setInput('')
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [open])

  const send = async (raw: string) => {
    const message = raw.trim()
    if (!message || chatMutation.isPending) return

    const userMessage: ChatMessage = {
      id: newId(),
      role: 'user',
      content: message,
    }
    setMessages((prev) => [...prev, userMessage])
    setInput('')

    try {
      const result = await chatMutation.mutateAsync(message)
      setMessages((prev) => [
        ...prev,
        {
          id: newId(),
          role: 'assistant',
          content:
            result.response.trim() || 'No response received. Please try again.',
        },
      ])
    } catch (err) {
      const detail =
        err instanceof ApiError ? err.message : 'Could not reach the AI coach'
      toast.error(detail)
      setMessages((prev) => [
        ...prev,
        {
          id: newId(),
          role: 'assistant',
          content: detail,
        },
      ])
    }
  }

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    void send(input)
  }

  const onKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void send(input)
    }
  }

  const showSuggestions =
    messages.length <= 1 && !chatMutation.isPending && input.trim().length === 0

  return (
    <div className="pointer-events-none fixed right-3 bottom-[calc(4.75rem+env(safe-area-inset-bottom))] z-40 flex flex-col items-end gap-3 md:right-6 md:bottom-6">
      {open ? (
        <section
          role="dialog"
          aria-modal="false"
          aria-labelledby={titleId}
          className="pointer-events-auto flex h-[min(58dvh,28rem)] w-[min(calc(100vw-1.5rem),24rem)] flex-col overflow-hidden rounded-2xl border border-stone-200 bg-stone-50 shadow-xl shadow-black/20 md:h-[min(70dvh,34rem)]"
        >
          <header className="flex items-center justify-between gap-3 border-b border-emerald-900/20 bg-emerald-800 px-4 py-3 text-white">
            <div className="min-w-0">
              <p
                id={titleId}
                className="truncate text-sm font-semibold tracking-tight"
              >
                CaliTrack AI Coach
              </p>
              <p className="truncate text-xs text-white/70">
                Form, skills, recovery & nutrition
              </p>
            </div>
            <button
              type="button"
              onClick={close}
              className="inline-flex size-8 shrink-0 cursor-pointer items-center justify-center rounded-lg text-white/90 transition hover:bg-white/10 hover:text-white"
              aria-label="Close chat"
            >
              <X className="size-4" strokeWidth={2.25} />
            </button>
          </header>

          <div
            ref={listRef}
            className="flex flex-1 flex-col gap-3 overflow-y-auto bg-stone-50 px-3 py-3"
          >
            {messages.map((message) => (
              <div
                key={message.id}
                className={cn(
                  'flex',
                  message.role === 'user' ? 'justify-end' : 'justify-start',
                )}
              >
                <div
                  className={cn(
                    'max-w-[85%] rounded-2xl px-3.5 py-2.5 text-sm leading-relaxed whitespace-pre-wrap',
                    message.role === 'user'
                      ? 'rounded-br-md bg-emerald-700 text-white'
                      : 'rounded-bl-md border border-stone-200 bg-stone-50 text-stone-800 shadow-sm',
                  )}
                >
                  {message.content}
                </div>
              </div>
            ))}

            {showSuggestions ? (
              <div className="flex flex-col gap-2 pt-1">
                <p className="px-1 text-[11px] font-semibold uppercase tracking-wide text-stone-500">
                  Try asking
                </p>
                {SUGGESTIONS.map((suggestion) => (
                  <button
                    key={suggestion}
                    type="button"
                    onClick={() => void send(suggestion)}
                    className="cursor-pointer rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-left text-sm text-emerald-900 transition hover:border-emerald-300 hover:bg-emerald-100"
                  >
                    {suggestion}
                  </button>
                ))}
              </div>
            ) : null}

            {chatMutation.isPending ? (
              <div className="flex justify-start">
                <div className="rounded-2xl rounded-bl-md border border-stone-200 bg-stone-50 px-3.5 py-3 shadow-sm">
                  <span className="inline-flex items-center gap-1.5 text-sm text-stone-500">
                    <span className="size-1.5 animate-pulse rounded-full bg-emerald-600" />
                    <span
                      className="size-1.5 animate-pulse rounded-full bg-emerald-600"
                      style={{ animationDelay: '150ms' }}
                    />
                    <span
                      className="size-1.5 animate-pulse rounded-full bg-emerald-600"
                      style={{ animationDelay: '300ms' }}
                    />
                    <span className="sr-only">Thinking</span>
                  </span>
                </div>
              </div>
            ) : null}
          </div>

          <form
            onSubmit={onSubmit}
            className="border-t border-stone-200 bg-stone-50 p-3"
          >
            <div className="flex items-end gap-2 rounded-xl border border-stone-200 bg-stone-50 focus-within:border-emerald-400 focus-within:ring-2 focus-within:ring-emerald-600/25">
              <textarea
                ref={inputRef}
                rows={1}
                value={input}
                onChange={(event) => setInput(event.target.value)}
                onKeyDown={onKeyDown}
                placeholder="Ask about workouts, form, recovery…"
                maxLength={4000}
                disabled={chatMutation.isPending}
                className="max-h-28 min-h-11 flex-1 resize-none bg-transparent px-3 py-2.5 text-sm text-stone-900 outline-none placeholder:text-stone-400 disabled:opacity-60"
              />
              <button
                type="submit"
                disabled={chatMutation.isPending || input.trim().length === 0}
                className="mb-1.5 mr-1.5 inline-flex size-9 shrink-0 cursor-pointer items-center justify-center rounded-lg bg-emerald-700 text-white transition hover:bg-emerald-800 disabled:cursor-not-allowed disabled:opacity-40"
                aria-label="Send message"
              >
                <SendHorizontal className="size-4" strokeWidth={2.25} />
              </button>
            </div>
            <p className="mt-2 px-0.5 text-[11px] text-stone-400">
              Enter to send · Esc to close · Chat clears when closed
            </p>
          </form>
        </section>
      ) : null}

      <button
        type="button"
        onClick={() => (open ? close() : openPanel())}
        className={cn(
          'pointer-events-auto inline-flex size-14 cursor-pointer items-center justify-center rounded-full shadow-lg transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-500 focus-visible:ring-offset-2 focus-visible:ring-offset-stone-50',
          open
            ? 'bg-stone-900 text-stone-50 shadow-black/25 hover:bg-stone-800'
            : 'bg-emerald-700 text-white shadow-black/30 hover:bg-emerald-800',
        )}
        aria-expanded={open}
        aria-label={open ? 'Close AI coach' : 'Open AI coach'}
      >
        {open ? (
          <X className="size-6" strokeWidth={2.25} />
        ) : (
          <MessageCircle className="size-6" strokeWidth={2.25} />
        )}
      </button>
    </div>
  )
}
