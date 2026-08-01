import { useMutation } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type { ChatRequest, ChatResponse } from '@/shared/api/types'

export function sendChatMessage(body: ChatRequest) {
  return api.post<ChatResponse>('/api/v1/chat', body)
}

export function useChatMutation() {
  return useMutation({
    mutationFn: (message: string) => sendChatMessage({ message }),
  })
}
