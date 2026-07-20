import { api } from '@/shared/api/client'
import type { CatalogGoal } from '@/shared/api/types'

export async function listGoals(): Promise<CatalogGoal[]> {
  return api.get<CatalogGoal[]>('/api/v1/nodes')
}
