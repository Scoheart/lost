import { defineStore } from 'pinia'
import apiClient from '@/utils/api'

export interface Message {
  id: number
  content: string
  isRead: boolean
  fromUserId: number
  fromUsername: string
  toUserId: number
  relatedItemId?: number
  relatedItemType?: 'lost' | 'found'
  createdAt: string
}

export interface MessagesState {
  messages: Message[]
  unreadCount: number
  loading: boolean
  error: string | null
  pagination: {
    page: number
    pageSize: number
    total: number
  }
}

export const useMessageStore = defineStore('messages', {
  state: (): MessagesState => ({
    messages: [],
    unreadCount: 0,
    loading: false,
    error: null,
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    paginatedMessages: (state) => {
      const start = (state.pagination.page - 1) * state.pagination.pageSize
      const end = start + state.pagination.pageSize
      return state.messages.slice(start, end)
    }
  },

  actions: {
    async fetchMessages() {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get('/messages', {
          params: {
            page: this.pagination.page,
            pageSize: this.pagination.pageSize
          }
        })

        this.messages = response.data.items
        this.pagination.total = response.data.total
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取消息列表失败'
      } finally {
        this.loading = false
      }
    },

    async fetchUnreadCount() {
      try {
        const response = await apiClient.get('/messages/unread-count')
        this.unreadCount = response.data.count
        return response.data.count
      } catch (error: any) {
        console.error('获取未读消息数量失败', error)
        return 0
      }
    },

    async markAsRead(messageId: number) {
      try {
        await apiClient.put(`/messages/${messageId}/read`)

        // 更新本地消息状态
        const messageIndex = this.messages.findIndex(msg => msg.id === messageId)
        if (messageIndex !== -1 && !this.messages[messageIndex].isRead) {
          this.messages[messageIndex].isRead = true
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }

        return { success: true }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '标记消息已读失败'
        }
      }
    },

    async markAllAsRead() {
      try {
        await apiClient.put('/messages/mark-all-read')

        // 更新所有消息为已读
        this.messages.forEach(msg => {
          if (!msg.isRead) {
            msg.isRead = true
          }
        })
        this.unreadCount = 0

        return { success: true }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '标记全部已读失败'
        }
      }
    },

    async deleteMessage(messageId: number) {
      try {
        await apiClient.delete(`/messages/${messageId}`)

        // 更新本地消息列表
        const messageIndex = this.messages.findIndex(msg => msg.id === messageId)
        if (messageIndex !== -1) {
          // 如果删除的是未读消息，减少未读计数
          if (!this.messages[messageIndex].isRead) {
            this.unreadCount = Math.max(0, this.unreadCount - 1)
          }
          this.messages.splice(messageIndex, 1)
        }

        return { success: true }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '删除消息失败'
        }
      }
    },

    async sendMessage(message: {
      toUserId: number,
      content: string,
      relatedItemId?: number,
      relatedItemType?: 'lost' | 'found'
    }) {
      this.loading = true
      this.error = null

      try {
        await apiClient.post('/messages', message)
        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '发送消息失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    }
  }
})
