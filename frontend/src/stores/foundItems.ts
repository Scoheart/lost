// stores/foundItems.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'
import { getUpdateEndpoint, handleApiError, processCommentsResponse } from '@/utils/apiHelpers'
import { getFoundStatusChangeMessage } from '@/utils/statusHelpers'
import type { Comment, CommentPagination } from '@/stores/lostItems'
import type { Ref } from 'vue'

// 内部辅助函数替代 urlHelpers 缺失的功能
function addSearchParams(url: string, params: Record<string, any>): string {
  const urlObj = new URL(url, window.location.origin);

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      urlObj.searchParams.append(key, String(value));
    }
  });

  return urlObj.pathname + urlObj.search;
}

// 定义失物招领评论类型
export interface FoundItemComment {
  id: number;
  content: string;
  userId: number;
  username: string;
  createdAt: string;
  updatedAt: string;
}

export interface FoundItem {
  id: number
  title: string
  description: string
  foundDate: string
  foundLocation: string
  storageLocation?: string
  category: string
  images: string[]
  contactInfo: string
  claimRequirements?: string
  status: 'pending' | 'processing' | 'claimed'
  userId: number
  username: string
  createdAt: string
  updatedAt: string
  // 认领信息
  claimInfo?: {
    userId: number
    username: string
    contactInfo?: string
    applyTime: string
  }
}

export interface FoundItemsState {
  items: FoundItem[]
  currentItem: FoundItem | null
  comments: Comment[]
  loading: boolean
  error: string | null
  commentPagination: CommentPagination
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

export const useFoundItemsStore = defineStore('foundItems', {
  state: (): FoundItemsState => ({
    items: [],
    currentItem: null,
    comments: [],
    loading: false,
    error: null,
    commentPagination: {
      currentPage: 1,
      pageSize: 10,
      totalItems: 0,
      totalPages: 1
    },
    filters: {
      category: null,
      status: null,
      keyword: null
    },
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    totalComments: (state) => state.commentPagination.totalItems,
    pendingItems: (state) => state.items.filter(item => item.status === 'pending'),
    claimedItems: (state) => state.items.filter(item => item.status === 'claimed'),
    closedItems: (state) => state.items.filter(item => item.status === 'closed'),
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
            item.foundLocation.toLowerCase().includes(keyword),
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
    setFilters(filters: Partial<FoundItemsState['filters']>) {
      this.filters = { ...this.filters, ...filters }
    },

    // GET /found-items - 查询失物招领列表
    async fetchFoundItems({ page = 1, pageSize = 10, status = '', category = '', query = '' } = {}) {
      try {
        const queryParams = new URLSearchParams()
        if (page) queryParams.append('page', page.toString())
        if (pageSize) queryParams.append('pageSize', pageSize.toString())
        if (status) queryParams.append('status', status)
        if (category) queryParams.append('category', category)
        if (query) queryParams.append('query', query)

        const queryString = queryParams.toString()
        const url = queryString ? `/found-items?${queryString}` : '/found-items'

        const response = await apiClient.get(url)

        // 根据API响应格式处理数据
        if (response.data.data?.items) {
          // 新API格式：response.data.data.items包含物品数组
          this.items = response.data.data.items
          this.pagination.total = response.data.data.totalItems || 0
          this.pagination.page = response.data.data.currentPage || page
          this.pagination.pageSize = response.data.data.pageSize || pageSize
        } else if (response.data.items) {
          // 旧格式：response.data.items包含物品数组
          this.items = response.data.items
        } else if (Array.isArray(response.data.data)) {
          // 直接数组格式
          this.items = response.data.data
        } else if (Array.isArray(response.data)) {
          // 最直接的数组格式
          this.items = response.data
        } else {
          // 无法识别的格式，确保items是数组
          console.warn('API响应格式不符合预期，无法获取物品列表', response.data)
          this.items = []
        }

        return {
          success: true,
          message: '获取失物招领列表成功',
          data: this.items
        }
      } catch (error) {
        console.error('获取失物招领列表失败:', error)
        this.items = []
        return {
          success: false,
          message: '获取失物招领列表失败',
          error
        }
      }
    },

    // GET /found-items/{id} - 查询单个失物招领
    async fetchFoundItemById(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      try {
        this.loading = true
        this.error = null

        const response = await apiClient.get(`/found-items/${id}`)
        console.log('Fetched found item details:', response.data)

        if (response.data.success) {
          this.currentItem = response.data.data
          return {
            success: true,
            data: this.currentItem
          }
        } else {
          this.error = response.data.message || '获取物品详情失败'
          return {
            success: false,
            message: this.error
          }
        }
      } catch (error: any) {
        console.error('Failed to fetch found item:', error)
        this.error = error.response?.data?.message || '获取物品详情失败'
        return {
          success: false,
          message: this.error
        }
      } finally {
        this.loading = false
      }
    },

    // GET /found-items/my-posts - 查询当前用户的失物招领
    async fetchMyFoundItems({ page = 1, pageSize = 10, status = '' } = {}) {
      try {
        const queryParams = new URLSearchParams()
        if (page) queryParams.append('page', page.toString())
        if (pageSize) queryParams.append('pageSize', pageSize.toString())
        if (status) queryParams.append('status', status)

        const queryString = queryParams.toString()
        const url = queryString ? `/found-items/my-posts?${queryString}` : '/found-items/my-posts'

        console.log(`[FoundItemsStore] Fetching user's found items from: ${url}`)
        const response = await apiClient.get(url)
        console.log('[FoundItemsStore] Response:', response.data)

        // 根据API响应格式处理数据
        let items = []
        let totalItems = 0
        let currentPage = page
        let currentPageSize = pageSize

        if (response.data.data?.items) {
          // 新API格式：response.data.data.items包含物品数组
          items = response.data.data.items
          totalItems = response.data.data.totalItems || 0
          currentPage = response.data.data.currentPage || page
          currentPageSize = response.data.data.pageSize || pageSize
        } else if (response.data.items) {
          // 旧格式：response.data.items包含物品数组
          items = response.data.items
          totalItems = response.data.total || 0
        } else if (Array.isArray(response.data.data)) {
          // 直接数组格式
          items = response.data.data
        } else if (Array.isArray(response.data)) {
          // 最直接的数组格式
          items = response.data
        } else {
          // 无法识别的格式，确保items是数组
          console.warn('API响应格式不符合预期，无法获取物品列表', response.data)
        }

        this.items = items
        this.pagination.total = totalItems
        this.pagination.page = currentPage
        this.pagination.pageSize = currentPageSize

        return {
          success: true,
          message: '获取我的失物招领列表成功',
          items: items,
          total: totalItems,
          currentPage: currentPage,
          pageSize: currentPageSize
        }
      } catch (error) {
        console.error('获取我的失物招领列表失败:', error)
        this.items = []
        return {
          success: false,
          message: '获取我的失物招领列表失败',
          error,
          items: [],
          total: 0
        }
      }
    },

    async fetchComments(itemId: number, page = 1, size = 10) {
      try {
        const response = await apiClient.get('/item-comments', {
          params: {
            itemId,
            itemType: 'found',
            page,
            size
          }
        })

        console.log('Comments API response:', response.data);

        // 根据API响应示例更新处理逻辑
        if (response.data && response.data.success) {
          // 处理 { success: true, data: { comments: [], ... } } 格式
          const responseData = response.data.data || {};

          // 确保comments是数组，即使是空数组
          this.comments = Array.isArray(responseData.comments) ? responseData.comments : [];

          // 更新分页信息
          this.commentPagination = {
            currentPage: responseData.currentPage || 1,
            pageSize: responseData.pageSize || 10,
            totalItems: responseData.totalItems || 0,
            totalPages: responseData.totalPages || 0
          }

          return { success: true, data: this.comments };
        } else {
          // 处理失败的响应
          console.warn('Comments API returned unsuccessful response:', response.data);
          this.comments = [];
          this.commentPagination = {
            currentPage: 1,
            pageSize: 10,
            totalItems: 0,
            totalPages: 0
          };
          return {
            success: false,
            message: response.data?.message || '获取评论失败'
          };
        }
      } catch (error: any) {
        console.error('Failed to fetch comments:', error);
        // 确保在错误情况下仍有有效数据
        this.comments = [];
        this.commentPagination = {
          currentPage: 1,
          pageSize: 10,
          totalItems: 0,
          totalPages: 0
        };
        return {
          success: false,
          message: error.response?.data?.message || '获取评论失败'
        };
      }
    },

    // POST /found-items - 发布失物招领
    async createFoundItem(
      itemData: Omit<
        FoundItem,
        'id' | 'userId' | 'username' | 'status' | 'createdAt' | 'updatedAt'
      >,
    ) {
      try {
        this.loading = true
        this.error = null
        console.log('Creating found item with data:', itemData)

        const response = await apiClient.post('/found-items', itemData)

        if (response.data.success) {
          const createdItem = response.data.data
          this.items.unshift(createdItem)
          return {
            success: true,
            message: '发布成功',
            data: createdItem
          }
        } else {
          return {
            success: false,
            message: response.data.message || '发布失败，请稍后再试'
          }
        }
      } catch (error: any) {
        console.error('Failed to create found item:', error)
        return {
          success: false,
          message: error.response?.data?.message || '发布失败，请稍后再试'
        }
      } finally {
        this.loading = false
      }
    },

    // PUT /found-items/{id} - 更新失物招领
    async updateFoundItem(id: number, itemData: Partial<FoundItem>) {
      try {
        this.loading = true
        this.error = null
        console.log('Updating found item with data:', itemData)

        const response = await apiClient.put(`/found-items/${id}`, itemData)

        if (response.data.success) {
          const updatedItem = response.data.data

          // 更新当前项
          if (this.currentItem && this.currentItem.id === id) {
            this.currentItem = updatedItem
          }

          // 更新列表中的项
          const index = this.items.findIndex(item => item.id === id)
          if (index !== -1) {
            this.items[index] = updatedItem
          }

          return {
            success: true,
            message: '更新成功',
            data: updatedItem
          }
        } else {
          return {
            success: false,
            message: response.data.message || '更新失败，请稍后再试'
          }
        }
      } catch (error: any) {
        console.error('Failed to update found item:', error)
        return {
          success: false,
          message: error.response?.data?.message || '更新失败，请稍后再试'
        }
      } finally {
        this.loading = false
      }
    },

    // DELETE /found-items/{id} - 删除失物招领
    async deleteFoundItem(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        await apiClient.delete(`/found-items/${id}`)

        // 从列表中移除
        this.items = this.items.filter(item => item.id !== id)

        // 清除当前物品（如果是同一个）
        if (this.currentItem?.id === id) {
          this.currentItem = null
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '删除失物招领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 添加评论
     * @param itemId 失物招领ID
     * @param commentData 评论内容
     * @returns 操作结果
     */
    async addComment(itemId: number, commentData: { content: string, type?: string }) {
      try {
        const response = await apiClient.post('/item-comments', {
          ...commentData,
          itemId,
          itemType: 'found'
        })

        // 获取最新的评论列表
        await this.fetchComments(itemId);

        return {
          success: true,
          message: '评论已添加',
          data: response.data
        }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '添加评论失败'
        }
      }
    },

    /**
     * 删除评论
     * @param commentId 评论ID
     * @returns 操作结果
     */
    async deleteComment(commentId: number) {
      try {
        const response = await apiClient.delete(`/item-comments/${commentId}`);

        if (response.data && response.data.success) {
          return {
            success: true,
            message: '评论删除成功'
          }
        } else {
          return {
            success: false,
            message: response.data?.message || '评论删除失败'
          }
        }
      } catch (error: any) {
        console.error('Failed to delete comment:', error);
        return {
          success: false,
          message: error.response?.data?.message || '评论删除失败'
        }
      }
    },

    async claimItem(id: number, claimData: { description: string }) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post(`/found-items/${id}/claim`, claimData)
        const updatedItem = response.data.item || response.data.data || response.data

        // 更新状态
        if (this.currentItem?.id === id) {
          this.currentItem = updatedItem || {
            ...this.currentItem,
            status: 'claimed'
          }
        }

        // 更新列表中的物品
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = updatedItem || { ...this.items[itemIndex], status: 'claimed' }
        }

        return { success: true, data: updatedItem }
      } catch (error: any) {
        this.error = error.response?.data?.message || '认领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 将物品标记为已认领
     * @param id 失物招领ID
     * @returns 操作结果
     */
    async markAsClaimed(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      console.log(`标记失物招领为已认领 #${id}`)

      try {
        // 使用专用的状态更新端点
        const endpoint = `/found-items/${id}/status`
        const response = await apiClient.put(endpoint, { status: 'claimed' })

        // 更新本地状态
        if (this.currentItem?.id === id) {
          this.currentItem.status = 'claimed'
        }

        // 更新列表中的状态
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex].status = 'claimed'
        }

        return {
          success: true,
          message: getFoundStatusChangeMessage('claimed'),
          data: response.data
        }
      } catch (error: any) {
        return handleApiError(error, '标记物品为已认领失败')
      }
    },

    /**
     * 申请认领失物
     * @param id 失物招领ID
     * @param claimData 认领申请数据，包含申请说明
     * @returns 操作结果
     */
    async applyToClaim(id: number, claimData: { description: string }) {
      this.loading = true
      this.error = null

      try {
        const endpoint = `/found-items/${id}/apply-claim`
        const response = await apiClient.post(endpoint, claimData)
        const updatedItem = response.data.item || response.data.data || response.data

        // 更新状态为"认领中"
        if (this.currentItem?.id === id) {
          this.currentItem = updatedItem || {
            ...this.currentItem,
            status: 'processing'
          }
        }

        // 更新列表中的物品
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = updatedItem || { ...this.items[itemIndex], status: 'processing' }
        }

        return {
          success: true,
          message: '已提交认领申请，请等待物品拥有者确认',
          data: updatedItem
        }
      } catch (error: any) {
        this.error = error.response?.data?.message || '申请认领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新失物招领状态
     * @param id 失物招领ID
     * @param status 新状态
     * @returns 操作结果
     */
    async updateStatus(id: number, status: 'pending' | 'processing' | 'claimed') {
      if (!id) return { success: false, message: '无效的ID' }

      try {
        // 使用专用的状态更新端点
        const endpoint = `/found-items/${id}/status`
        const response = await apiClient.put(endpoint, { status })

        // 更新本地状态
        if (this.currentItem?.id === id) {
          this.currentItem.status = status
        }

        // 更新列表中的状态
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex].status = status
        }

        return {
          success: true,
          message: getFoundStatusChangeMessage(status),
          data: response.data
        }
      } catch (error: any) {
        return handleApiError(error, '更新失物招领状态失败')
      }
    },

    /**
     * 关闭失物招领(现在直接删除)
     */
    async closeItem(id: number) {
      if (!this.token) return { success: false, message: '未授权操作' }

      try {
        console.log(`关闭并删除失物招领 #${id}`)

        // 找到要删除的物品的索引
        const itemIndex = this.items.findIndex(item => item.id === id)
        const itemExists = itemIndex !== -1

        // 现在直接删除该物品
        const endpoint = `/found-items/${id}`
        const response = await apiClient.delete(endpoint)

        if (response.data.success) {
          if (this.currentItem && this.currentItem.id === id) {
            this.currentItem = null
          }

          if (itemExists) {
            // 从数组中删除该物品
            this.items.splice(itemIndex, 1)
          }

          return {
            success: true,
            message: '失物招领已关闭并从列表中删除',
            data: response.data.data
          }
        } else {
          return { success: false, message: response.data.message }
        }
      } catch (error: any) {
        console.error(`关闭失物招领 #${id} 失败:`, error)
        return {
          success: false,
          message: error.response?.data?.message || '操作失败，请稍后再试'
        }
      }
    },
  },
})
