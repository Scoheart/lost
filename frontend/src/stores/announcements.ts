// stores/announcements.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'

export interface Announcement {
  id: number
  title: string
  content: string
  publishDate: string
  adminId: number
  adminName: string
  createdAt: string
  updatedAt: string
}

export interface AnnouncementsState {
  announcements: Announcement[]
  currentAnnouncement: Announcement | null
  loading: boolean
  error: string | null
  pagination: {
    page: number
    pageSize: number
    total: number
  }
}

export const useAnnouncementsStore = defineStore('announcements', {
  state: (): AnnouncementsState => ({
    announcements: [],
    currentAnnouncement: null,
    loading: false,
    error: null,
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    total: (state) => {
      return state.pagination.total;
    },

    paginatedAnnouncements: (state) => {
      const start = (state.pagination.page - 1) * state.pagination.pageSize;
      const end = start + state.pagination.pageSize;
      return state.announcements.slice(start, end);
    }
  },

  actions: {
    // 公开接口：获取已发布公告列表
    async fetchAnnouncements(params?: {
      page?: number;
      pageSize?: number;
      keyword?: string;
    }) {
      this.loading = true
      this.error = null

      try {
        // 如果提供了参数，使用参数，否则使用默认值
        const queryParams = params || {
          page: this.pagination.page,
          pageSize: this.pagination.pageSize
        }

        const response = await apiClient.get('/announcements', {
          params: queryParams
        })

        // 处理后端返回的数据结构
        const responseData = response.data.data;

        // 更新公告列表和分页信息
        this.announcements = responseData.announcements || [];
        this.pagination.page = responseData.currentPage || 1;
        this.pagination.pageSize = responseData.pageSize || 10;
        this.pagination.total = responseData.totalItems || 0;

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取公告列表失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // 公开接口：获取单个公告详情
    async fetchAnnouncementById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get(`/announcements/${id}`)

        // Handle backend response structure
        const responseData = response.data.data || response.data;
        this.currentAnnouncement = responseData.announcement || responseData;

        console.log('Fetched announcement detail:', this.currentAnnouncement);

        return { success: true, data: this.currentAnnouncement }
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取公告详情失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // 管理员接口：获取所有公告
    async fetchAdminAnnouncements(params?: {
      page?: number;
      pageSize?: number;
      keyword?: string;
    }) {
      this.loading = true
      this.error = null

      try {
        // 如果提供了参数，使用参数，否则使用默认值
        const queryParams = params || {
          page: this.pagination.page,
          pageSize: this.pagination.pageSize
        }

        const response = await apiClient.get('/announcements/admin', {
          params: queryParams
        })

        // 处理后端返回的数据结构
        const responseData = response.data.data;

        // 更新公告列表和分页信息
        this.announcements = responseData.announcements || [];
        this.pagination.page = responseData.currentPage || 1;
        this.pagination.pageSize = responseData.pageSize || 10;
        this.pagination.total = responseData.totalItems || 0;

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取管理员公告列表失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // 管理员接口：获取当前管理员创建的公告
    async fetchMyAnnouncements(params?: {
      page?: number;
      pageSize?: number;
      keyword?: string;
    }) {
      this.loading = true
      this.error = null

      try {
        // 如果提供了参数，使用参数，否则使用默认值
        const queryParams = params || {
          page: this.pagination.page,
          pageSize: this.pagination.pageSize
        }

        const response = await apiClient.get('/announcements/admin/mine', {
          params: queryParams
        })

        // 处理后端返回的数据结构
        const responseData = response.data.data;

        // 更新公告列表和分页信息
        this.announcements = responseData.announcements || [];
        this.pagination.page = responseData.currentPage || 1;
        this.pagination.pageSize = responseData.pageSize || 10;
        this.pagination.total = responseData.totalItems || 0;

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取我的公告列表失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // 管理员接口：创建公告
    async createAnnouncement(announcementData: Partial<Announcement>) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post('/announcements/admin', announcementData)

        // 更新列表（可选，取决于后端返回格式）
        if (response.data.announcement) {
          this.announcements.unshift(response.data.announcement)
        }

        return { success: true, id: response.data.id }
      } catch (error: any) {
        this.error = error.response?.data?.message || '发布公告失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // 管理员接口：修改公告
    async updateAnnouncement(announcementData: { id: number } & Partial<Announcement>) {
      if (!announcementData.id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        // 提取id，其余作为数据发送
        const { id, ...data } = announcementData
        const response = await apiClient.put(`/announcements/admin/${id}`, data)

        // 更新当前查看的公告（如果是同一个）
        if (this.currentAnnouncement?.id === id) {
          this.currentAnnouncement = response.data.announcement || {
            ...this.currentAnnouncement,
            ...data
          }
        }

        // 更新列表中的公告
        const announcementIndex = this.announcements.findIndex(a => a.id === id)
        if (announcementIndex !== -1) {
          this.announcements[announcementIndex] = response.data.announcement || {
            ...this.announcements[announcementIndex],
            ...data
          }
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新公告失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // 管理员接口：删除公告
    async deleteAnnouncement(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        await apiClient.delete(`/announcements/admin/${id}`)

        // 从列表中移除
        this.announcements = this.announcements.filter(a => a.id !== id)

        // 清除当前公告（如果是同一个）
        if (this.currentAnnouncement?.id === id) {
          this.currentAnnouncement = null
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '删除公告失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    }
  }
})
