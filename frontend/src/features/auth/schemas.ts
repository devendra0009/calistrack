import { z } from 'zod'

export const loginSchema = z.object({
  email: z.email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
})

export const registerSchema = z.object({
  displayName: z.string().min(1, 'Display name is required').max(100),
  email: z.email('Enter a valid email'),
  password: z.string().min(6, 'Password must be at least 6 characters').max(128),
})

export type LoginValues = z.infer<typeof loginSchema>
export type RegisterValues = z.infer<typeof registerSchema>
