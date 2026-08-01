import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'ghost'
  loading?: boolean
  children?: ReactNode
}

export function Button({
  className,
  variant = 'primary',
  loading,
  disabled,
  children,
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(
        'inline-flex cursor-pointer items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50',
        variant === 'primary' &&
          'bg-emerald-700 text-white hover:bg-emerald-800',
        variant === 'secondary' &&
          'border border-stone-300 bg-stone-50 text-stone-900 hover:bg-stone-100',
        variant === 'ghost' && 'text-stone-700 hover:bg-stone-100',
        className,
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? 'Please wait…' : children}
    </button>
  )
}
