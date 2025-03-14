// stores/announcements.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'

export interface Announcement {
  id: number
  title: string
  content: string
  isSticky: boolean
  isActive: boolean
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
    stickyAnnouncements: (state) => {
      return state.announcements.filter(item => item.isSticky && item.isActive)
    },

    regularAnnouncements: (state) => {
      return state.announcements.filter(item => !item.isSticky && item.isActive)
    },

    paginatedAnnouncements: (state) => {
      const start = (state.pagination.page - 1) * state.pagination.pageSize
      const end = start + state.pagination.pageSize
      return state.announcements.slice(start, end)
    }
  },

  actions: {
    async fetchAnnouncements() {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get('/announcements', {
          params: {
            page: this.pagination.page,
            pageSize: this.pagination.pageSize
          }
        })

        this.announcements = response.data.items
        this.pagination.total = response.data.total
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取公告列表失败'
      } finally {
        this.loading = false
      }
    },

    async fetchAnnouncementById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get(`/announcements/${id}`)
        this.currentAnnouncement = response.data
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取公告详情失败'
      } finally {
        this.loading = false
      }
    },

    // 以下方法仅管理员可用
    async createAnnouncement(announcementData: Partial<Announcement>) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post('/announcements', announcementData)

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

    async updateAnnouncement(id: number, announcementData: Partial<Announcement>) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        const response = await apiClient.put(`/announcements/${id}`, announcementData)

        // 更新当前查看的公告（如果是同一个）
        if (this.currentAnnouncement?.id === id) {
          this.currentAnnouncement = response.data.announcement || {
            ...this.currentAnnouncement,
            ...announcementData
          }
        }

        // 更新列表中的公告
        const announcementIndex = this.announcements.findIndex(a => a.id === id)
        if (announcementIndex !== -1) {
          this.announcements[announcementIndex] = response.data.announcement || {
            ...this.announcements[announcementIndex],
            ...announcementData
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

    async deleteAnnouncement(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        await apiClient.delete(`/announcements/${id}`)

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
