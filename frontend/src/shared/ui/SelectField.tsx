import type { SelectHTMLAttributes } from 'react'
import { cn } from '@/shared/lib/cn'

type SelectFieldProps = SelectHTMLAttributes<HTMLSelectElement> & {
  label: string
  error?: string
  options: ReadonlyArray<{ value: string; label: string } | string>
}

export function SelectField({
  label,
  error,
  options,
  className,
  id,
  ...props
}: SelectFieldProps) {
  const selectId = id ?? props.name ?? label.toLowerCase().replace(/\s+/g, '-')
  return (
    <label className="flex flex-col gap-1.5 text-sm">
      <span className="font-medium text-stone-800">{label}</span>
      <select
        id={selectId}
        className={cn(
          'rounded-lg border border-stone-300 bg-white px-3 py-2 text-stone-900 outline-none ring-emerald-600/30 focus:ring-2',
          error && 'border-red-500',
          className,
        )}
        {...props}
      >
        {options.map((opt) => {
          const value = typeof opt === 'string' ? opt : opt.value
          const text = typeof opt === 'string' ? opt : opt.label
          return (
            <option key={value} value={value}>
              {text}
            </option>
          )
        })}
      </select>
      {error ? <span className="text-xs text-red-600">{error}</span> : null}
    </label>
  )
}
