import { useActionState, startTransition } from 'react'
import { Link, useNavigate } from 'react-router'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { PageShell } from '@/shared/ui/PageShell'
import { login } from '@/features/auth/api'
import { loginSchema } from '@/features/auth/schemas'

type FormState = { error?: string; fieldErrors?: Record<string, string> }

export function LoginPage() {
  const navigate = useNavigate()

  const [state, formAction, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      const parsed = loginSchema.safeParse({
        email: formData.get('email'),
        password: formData.get('password'),
      })

      if (!parsed.success) {
        const fieldErrors: Record<string, string> = {}
        for (const issue of parsed.error.issues) {
          const key = String(issue.path[0] ?? 'form')
          if (!fieldErrors[key]) fieldErrors[key] = issue.message
        }
        return { fieldErrors }
      }

      try {
        await login(parsed.data)
        startTransition(() => navigate('/setup', { replace: true }))
        return {}
      } catch (err) {
        const message =
          err instanceof ApiError ? err.message : 'Unable to sign in'
        return { error: message }
      }
    },
    {},
  )

  return (
    <PageShell
      title="Welcome back"
      subtitle="Sign in to continue your calisthenics path."
    >
      <form action={formAction} className="max-w-md space-y-4 rounded-2xl border border-stone-200 bg-white/90 p-6 shadow-sm">
        <Input
          label="Email"
          name="email"
          type="email"
          autoComplete="email"
          required
          error={state.fieldErrors?.email}
        />
        <Input
          label="Password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          error={state.fieldErrors?.password}
        />
        {state.error ? (
          <p className="text-sm text-red-600" role="alert">
            {state.error}
          </p>
        ) : null}
        <Button type="submit" loading={pending} className="w-full">
          Sign in
        </Button>
        <p className="text-center text-sm text-stone-600">
          New here?{' '}
          <Link className="font-semibold text-emerald-800 hover:underline" to="/register">
            Create an account
          </Link>
        </p>
      </form>
    </PageShell>
  )
}
