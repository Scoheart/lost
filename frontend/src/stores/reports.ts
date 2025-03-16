import { defineStore } from 'pinia'
import api from '../utils/api'

/**
 * 举报类型枚举
 */
export enum ReportType {
  LOST_ITEM = 'LOST_ITEM',
  FOUND_ITEM = 'FOUND_ITEM',
  COMMENT = 'COMMENT',
}

/**
 * 举报状态枚举
 */
export enum ReportStatus {
  PENDING = 'PENDING',
  RESOLVED = 'RESOLVED',
  REJECTED = 'REJECTED',
}

/**
 * 处理方式枚举
 */
export enum ActionType {
  NONE = 'NONE',
  CONTENT_DELETE = 'CONTENT_DELETE',
  USER_WARNING = 'USER_WARNING',
  USER_BAN = 'USER_BAN',
  USER_LOCK = 'USER_LOCK',
}

/**
 * 举报对象接口
 */
export interface Report {
  id: number
  reportType: ReportType
  reportedItemId: number
  reportedItemTitle: string
  reporterId: number
  reporterUsername: string
  reportedUserId: number
  reportedUsername: string
  reason: string
  status: ReportStatus
  resolutionNotes?: string
  resolvedByAdminId?: number
  resolvedByAdminUsername?: string
  createdAt: string
  resolvedAt?: string
}

export const useReportsStore = defineStore('reports', {
  state: () => ({
    reports: [] as Report[],
    currentReport: null as Report | null,
    loading: false,
    totalItems: 0,
    currentPage: 1,
    pageSize: 10,
    pendingCount: 0,
    resolvedCount: 0,
    rejectedCount: 0,
    error: null as string | null,
  }),

  getters: {
    /**
     * 获取待处理举报数量
     */
    getPendingReportsCount: (state) => state.pendingCount,
  },

  actions: {
    /**
     * 获取举报列表
     */
    async fetchReports(params = {}) {
      this.loading = true
      this.error = null

      try {
        const response = await api.get('/admin/reports', { params })

        if (response.data.success) {
          this.reports = response.data.data.reports
          this.totalItems = response.data.data.totalItems
          this.currentPage = response.data.data.currentPage
          this.pageSize = response.data.data.pageSize
          this.pendingCount = response.data.data.pendingReportsCount
          this.calculateCounts()
          return { success: true, data: response.data.data }
        } else {
          this.error = response.data.message || '获取举报列表失败'
          return { success: false, message: this.error }
        }
      } catch (error) {
        console.error('Failed to fetch reports:', error)
        this.error = error.message || '获取举报列表失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取举报详情
     */
    async fetchReportById(reportId) {
      this.loading = true
      this.error = null

      try {
        const response = await api.get(`/admin/reports/${reportId}`)

        if (response.data.success) {
          this.currentReport = response.data.data
          return { success: true, data: response.data.data }
        } else {
          this.error = response.data.message || '获取举报详情失败'
          return { success: false, message: this.error }
        }
      } catch (error) {
        console.error('Failed to fetch report details:', error)
        this.error = error.message || '获取举报详情失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 处理举报
     */
    async resolveReport(reportId, resolution) {
      this.loading = true
      this.error = null

      try {
        const response = await api.put(`/admin/reports/${reportId}/resolve`, resolution)

        if (response.data.success) {
          // 更新当前举报
          this.currentReport = response.data.data

          // 更新列表中对应的举报
          const index = this.reports.findIndex(report => report.id === reportId)
          if (index !== -1) {
            this.reports[index] = response.data.data
          }

          this.calculateCounts()
          return { success: true, data: response.data.data }
        } else {
          this.error = response.data.message || '处理举报失败'
          return { success: false, message: this.error }
        }
      } catch (error) {
        console.error('Failed to resolve report:', error)
        this.error = error.message || '处理举报失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取待处理举报数量
     */
    async fetchReportsCount() {
      try {
        const response = await api.get('/admin/reports/count')

        if (response.data.success) {
          this.pendingCount = response.data.data.pendingCount
          if (response.data.data.resolvedCount) {
            this.resolvedCount = response.data.data.resolvedCount
          }
          if (response.data.data.rejectedCount) {
            this.rejectedCount = response.data.data.rejectedCount
          }
          return { success: true, data: response.data.data }
        } else {
          return { success: false, message: response.data.message }
        }
      } catch (error) {
        console.error('Failed to fetch reports count:', error)
        return { success: false, message: error.message }
      }
    },

    /**
     * 获取用户提交的举报历史
     */
    async fetchUserReports() {
      this.loading = true
      try {
        const response = await api.get('/reports/my')

        if (response.data.success) {
          return {
            success: true,
            data: response.data.data
          }
        } else {
          return {
            success: false,
            message: response.data.message || '获取用户举报列表失败'
          }
        }
      } catch (error) {
        console.error('Failed to fetch user reports:', error)
        return {
          success: false,
          message: error.message || '获取用户举报列表失败'
        }
      } finally {
        this.loading = false
      }
    },

    /**
     * 计算已处理和已驳回的举报数量
     */
    calculateCounts() {
      const resolved = this.reports.filter(report => report.status === ReportStatus.RESOLVED).length
      const rejected = this.reports.filter(report => report.status === ReportStatus.REJECTED).length

      this.resolvedCount = resolved
      this.rejectedCount = rejected
    },

    /**
     * 重置状态
     */
    reset() {
      this.reports = []
      this.currentReport = null
      this.loading = false
      this.totalItems = 0
      this.currentPage = 1
      this.pageSize = 10
      this.pendingCount = 0
      this.resolvedCount = 0
      this.rejectedCount = 0
      this.error = null
    }
  }
})
