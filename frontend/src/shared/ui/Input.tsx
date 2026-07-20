import type { InputHTMLAttributes } from 'react'
import { cn } from '@/shared/lib/cn'

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string
  error?: string
}

export function Input({ label, error, className, id, ...props }: InputProps) {
  const inputId = id ?? props.name ?? label.toLowerCase().replace(/\s+/g, '-')
  return (
    <label className="flex flex-col gap-1.5 text-sm">
      <span className="font-medium text-stone-800">{label}</span>
      <input
        id={inputId}
        className={cn(
          'rounded-lg border border-stone-300 bg-white px-3 py-2 text-stone-900 outline-none ring-emerald-600/30 focus:ring-2',
          error && 'border-red-500',
          className,
        )}
        {...props}
      />
      {error ? <span className="text-xs text-red-600">{error}</span> : null}
    </label>
  )
}
