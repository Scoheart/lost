// stores/lostItems.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'
import { getUpdateEndpoint, handleApiError, processCommentsResponse } from '@/utils/apiHelpers'

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
  status: 'pending'
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
  userAvatar?: string
  itemId: number
  itemType: 'lost' | 'found'
  createdAt: string
}

export interface CommentPagination {
  currentPage: number
  pageSize: number
  totalItems: number
  totalPages: number
}

export interface LostItemsState {
  items: LostItem[]
  currentItem: LostItem | null
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

export const useLostItemsStore = defineStore('lostItems', {
  state: (): LostItemsState => ({
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
    foundItems: (state) => state.items.filter(item => item.status === 'found'),
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

    // GET /lost-items - 查询寻物启事列表
    async fetchLostItems({ page = 1, pageSize = 10, status = '', category = '', query = '' } = {}) {
      try {
        const queryParams = new URLSearchParams()
        if (page) queryParams.append('page', page.toString())
        if (pageSize) queryParams.append('pageSize', pageSize.toString())
        if (status) queryParams.append('status', status)
        if (category) queryParams.append('category', category)
        if (query) queryParams.append('query', query)

        const queryString = queryParams.toString()
        const url = queryString ? `/lost-items?${queryString}` : '/lost-items'

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
          message: '获取寻物启事列表成功',
          data: this.items
        }
      } catch (error) {
        console.error('获取寻物启事列表失败:', error)
        this.items = []
        return {
          success: false,
          message: '获取寻物启事列表失败',
          error
        }
      }
    },

    // GET /lost-items/{id} - 查询单个寻物启事
    async fetchLostItemById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get(`/lost-items/${id}`)

        // 确保我们有有效的响应数据
        if (response.data) {
          // 支持多种响应格式
          if (response.data.data && (typeof response.data.data === 'object')) {
            this.currentItem = response.data.data;
          } else {
            this.currentItem = response.data;
          }

          return { success: true, data: this.currentItem };
        } else {
          this.error = '获取寻物启事详情失败，数据格式有误';
          return { success: false, message: this.error };
        }
      } catch (error: any) {
        console.error('Failed to fetch lost item:', error);
        this.error = error.response?.data?.message || '获取寻物启事详情失败';
        this.currentItem = null; // 确保清除当前物品
        return { success: false, message: this.error };
      } finally {
        this.loading = false;
      }
    },

    // GET /lost-items/my-posts - 查询当前用户的寻物启事
    async fetchMyLostItems({ page = 1, pageSize = 10, status = '' } = {}) {
      try {
        const queryParams = new URLSearchParams()
        if (page) queryParams.append('page', page.toString())
        if (pageSize) queryParams.append('pageSize', pageSize.toString())
        if (status) queryParams.append('status', status)

        const queryString = queryParams.toString()
        const url = queryString ? `/lost-items/my-posts?${queryString}` : '/lost-items/my-posts'

        console.log(`[LostItemsStore] Fetching user's lost items from: ${url}`)
        const response = await apiClient.get(url)
        console.log('[LostItemsStore] Response:', response.data)

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
          message: '获取我的寻物启事列表成功',
          items: items,
          total: totalItems,
          currentPage: currentPage,
          pageSize: currentPageSize
        }
      } catch (error) {
        console.error('获取我的寻物启事列表失败:', error)
        this.items = []
        return {
          success: false,
          message: '获取我的寻物启事列表失败',
          error,
          items: [],
          total: 0
        }
      }
    },

    async fetchComments(itemId: number, page = 1, size = 10) {
      try {
        const response = await apiClient.get('/comments', {
          params: {
            itemId,
            itemType: 'lost',
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

    // POST /lost-items - 发布寻物启事
    async createLostItem(
      itemData: Omit<LostItem, 'id' | 'userId' | 'username' | 'status' | 'createdAt' | 'updatedAt'>,
    ) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post('/lost-items', itemData)
        const newItem = response.data.item || response.data.data || response.data

        // 更新列表（可选，取决于后端返回格式）
        if (newItem) {
          this.items.unshift(newItem)
        }

        return {
          success: true,
          id: newItem.id || response.data.id,
          data: newItem
        }
      } catch (error: any) {
        this.error = error.response?.data?.message || '发布寻物启事失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // PUT /lost-items/{id} - 更新寻物启事
    async updateLostItem(id: number, itemData: Partial<LostItem>) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        // 使用专门的状态更新端点
        let endpoint = `/lost-items/${id}`;

        // 如果仅更新状态，使用专门的状态更新端点
        if (itemData && Object.keys(itemData).length === 1 && 'status' in itemData) {
          endpoint = `/lost-items/${id}/status`;
          console.log('使用状态更新API端点:', endpoint);
        }

        const response = await apiClient.put(endpoint, itemData)
        const updatedItem = response.data.item || response.data.data || response.data

        // 更新当前查看的物品（如果是同一个）
        if (this.currentItem?.id === id) {
          this.currentItem = updatedItem || { ...this.currentItem, ...itemData }
        }

        // 更新列表中的物品
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = updatedItem || { ...this.items[itemIndex], ...itemData }
        }

        return { success: true, data: updatedItem }
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新寻物启事失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // DELETE /lost-items/{id} - 删除寻物启事
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

    async addComment(itemId: number, content: string) {
      try {
        const response = await apiClient.post('/comments', {
          itemId,
          itemType: 'lost',
          content
        })

        console.log('Add comment API response:', response.data);

        // 评论添加成功后，重新获取评论列表
        if (response.data && response.data.success) {
          return {
            success: true,
            message: '评论添加成功',
            data: response.data.data
          };
        } else {
          return {
            success: false,
            message: response.data?.message || '评论添加失败'
          };
        }
      } catch (error: any) {
        console.error('Failed to add comment:', error);
        return {
          success: false,
          message: error.response?.data?.message || '评论添加失败'
        };
      }
    },

    /**
     * 将寻物启事标记为"已找到"(现在直接删除)
     */
    async markAsFound(id: number) {
      if (!this.token) return { success: false, message: '未授权操作' }

      try {
        console.log(`将寻物启事 #${id} 标记为已找到并删除`)

        // 找到要删除的物品的索引
        const itemIndex = this.items.findIndex(item => item.id === id)
        const itemExists = itemIndex !== -1

        // 现在直接删除该物品
        const endpoint = `/lost-items/${id}`
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
            message: '物品已标记为已找到并从列表中删除',
            data: response.data.data
          }
        } else {
          return { success: false, message: response.data.message }
        }
      } catch (error: any) {
        console.error(`删除寻物启事 #${id} 失败:`, error)
        return {
          success: false,
          message: error.response?.data?.message || '操作失败，请稍后再试'
        }
      }
    },

    /**
     * 关闭寻物启事(现在直接删除)
     */
    async closeItem(id: number) {
      if (!this.token) return { success: false, message: '未授权操作' }

      try {
        console.log(`关闭并删除寻物启事 #${id}`)

        // 找到要删除的物品的索引
        const itemIndex = this.items.findIndex(item => item.id === id)
        const itemExists = itemIndex !== -1

        // 现在直接删除该物品
        const endpoint = `/lost-items/${id}`
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
            message: '寻物启事已关闭并从列表中删除',
            data: response.data.data
          }
        } else {
          return { success: false, message: response.data.message }
        }
      } catch (error: any) {
        console.error(`关闭寻物启事 #${id} 失败:`, error)
        return {
          success: false,
          message: error.response?.data?.message || '操作失败，请稍后再试'
        }
      }
    },
  },
})
