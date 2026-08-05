import { useActionState, startTransition, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { PageShell } from '@/shared/ui/PageShell'
import { toast } from '@/shared/ui/notify'
import { login } from '@/features/auth/api'
import { loginSchema } from '@/features/auth/schemas'

type FormState = { fieldErrors?: Record<string, string> }

export function LoginPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const [state, formAction, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      const values = {
        email: String(formData.get('email') ?? ''),
        password: String(formData.get('password') ?? ''),
      }
      const parsed = loginSchema.safeParse(values)

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
        // Defer so the toast isn't dropped by the form-action transition
        window.setTimeout(() => {
          toast.error(message, { title: 'Couldn’t sign in' })
        }, 0)
        return {}
      }
    },
    {},
  )

  return (
    <PageShell
      title="Welcome back"
      subtitle="Sign in to continue your calisthenics path."
    >
      <form
        action={formAction}
        className="max-w-md space-y-4 rounded-2xl border border-stone-200 bg-stone-50/90 p-6 shadow-sm"
      >
        <Input
          label="Email"
          name="email"
          type="email"
          autoComplete="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={state.fieldErrors?.email}
        />
        <Input
          label="Password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={state.fieldErrors?.password}
        />
        <Button type="submit" loading={pending} className="w-full">
          Sign in
        </Button>
        <p className="text-center text-sm text-stone-600">
          New here?{' '}
          <Link className="font-semibold text-emerald-900 hover:underline" to="/register">
            Create an account
          </Link>
        </p>
      </form>
    </PageShell>
  )
}
