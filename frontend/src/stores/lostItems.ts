// stores/lostItems.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'

export interface LostItem {
  id: number
  title: string
  description: string
  lostDate: string
  lostLocation: string
  category: string
  images: string[]
  reward?: number
  contactInfo: string
  status: 'pending' | 'found' | 'closed'
  userId: number
  username: string
  createdAt: string
  updatedAt: string
}

export interface Comment {
  id: number
  content: string
  userId: number
  username: string
  itemId: number
  itemType: 'lost' | 'found'
  createdAt: string
}

export interface LostItemsState {
  items: LostItem[]
  currentItem: LostItem | null
  comments: Comment[]
  loading: boolean
  error: string | null
  filters: {
    category: string | null
    status: string | null
    keyword: string | null
  }
  pagination: {
    page: number
    pageSize: number
    total: number
  }
}

export const useLostItemsStore = defineStore('lostItems', {
  state: (): LostItemsState => ({
    items: [],
    currentItem: null,
    comments: [],
    loading: false,
    error: null,
    filters: {
      category: null,
      status: null,
      keyword: null,
    },
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0,
    },
  }),

  getters: {
    filteredItems: (state) => {
      let result = [...state.items]

      if (state.filters.category) {
        result = result.filter((item) => item.category === state.filters.category)
      }

      if (state.filters.status) {
        result = result.filter((item) => item.status === state.filters.status)
      }

      if (state.filters.keyword) {
        const keyword = state.filters.keyword.toLowerCase()
        result = result.filter(
          (item) =>
            item.title.toLowerCase().includes(keyword) ||
            item.description.toLowerCase().includes(keyword) ||
            item.lostLocation.toLowerCase().includes(keyword),
        )
      }

      return result
    },

    paginatedItems: (state) => {
      const start = (state.pagination.page - 1) * state.pagination.pageSize
      const end = start + state.pagination.pageSize
      return state.items.slice(start, end)
    },
  },

  actions: {
    setFilters(filters: Partial<LostItemsState['filters']>) {
      this.filters = { ...this.filters, ...filters }
    },

    async fetchLostItems() {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get('/lost-items', {
          params: {
            page: this.pagination.page,
            pageSize: this.pagination.pageSize,
            ...this.filters
          }
        })

        this.items = response.data.items
        this.pagination.total = response.data.total
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取寻物启事列表失败'
      } finally {
        this.loading = false
      }
    },

    async fetchLostItemById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get(`/lost-items/${id}`)
        this.currentItem = response.data

        // 同时获取评论
        await this.fetchComments(id)
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取寻物启事详情失败'
      } finally {
        this.loading = false
      }
    },

    async fetchComments(itemId: number) {
      try {
        const response = await apiClient.get(`/lost-items/${itemId}/comments`)
        this.comments = response.data
      } catch (error: any) {
        console.error('获取评论失败', error)
        // 评论获取失败不影响主流程，只记录错误
      }
    },

    async createLostItem(
      itemData: Omit<LostItem, 'id' | 'userId' | 'username' | 'status' | 'createdAt' | 'updatedAt'>,
    ) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post('/lost-items', itemData)
        // 更新列表（可选，取决于后端返回格式）
        if (response.data.item) {
          this.items.unshift(response.data.item)
        }
        return { success: true, id: response.data.id }
      } catch (error: any) {
        this.error = error.response?.data?.message || '发布寻物启事失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async updateLostItem(id: number, itemData: Partial<LostItem>) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        const response = await apiClient.put(`/lost-items/${id}`, itemData)

        // 更新当前查看的物品（如果是同一个）
        if (this.currentItem?.id === id) {
          this.currentItem = response.data.item || { ...this.currentItem, ...itemData }
        }

        // 更新列表中的物品
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = response.data.item || { ...this.items[itemIndex], ...itemData }
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新寻物启事失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async deleteLostItem(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        await apiClient.delete(`/lost-items/${id}`)

        // 从列表中移除
        this.items = this.items.filter(item => item.id !== id)

        // 清除当前物品（如果是同一个）
        if (this.currentItem?.id === id) {
          this.currentItem = null
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '删除寻物启事失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async addComment(comment: { content: string; itemId: number }) {
      try {
        const response = await apiClient.post(`/lost-items/${comment.itemId}/comments`, {
          content: comment.content
        })

        this.comments.unshift(response.data)
        return { success: true }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '发表评论失败'
        }
      }
    }
  },
})
