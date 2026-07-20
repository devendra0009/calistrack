export function Spinner({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="flex min-h-40 flex-col items-center justify-center gap-3 text-stone-600">
      <div className="h-8 w-8 animate-spin rounded-full border-2 border-stone-300 border-t-emerald-700" />
      <p className="text-sm">{label}</p>
    </div>
  )
}
