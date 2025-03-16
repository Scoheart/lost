import { defineStore } from 'pinia'
import apiClient from '@/utils/api'
import { ElMessage } from 'element-plus'
import { handleApiError } from '@/utils/apiHelpers'

// 内部辅助函数替代 urlHelpers
function addSearchParams(url: string, params: Record<string, any>): string {
  const urlObj = new URL(url, window.location.origin);

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      urlObj.searchParams.append(key, String(value));
    }
  });

  // 返回相对路径
  return urlObj.pathname + urlObj.search;
}

// 认领申请接口
export interface ClaimApplication {
  id: number
  foundItemId: number
  foundItemTitle: string
  foundItemImage: string | null
  applicantId: number
  applicantName: string
  applicantContact: string | null
  ownerId: number
  ownerName: string
  ownerContact?: string | null
  description: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
  updatedAt: string
  processedAt: string | null
}

// 分页数据接口
export interface ClaimsPagination {
  applications: ClaimApplication[]
  currentPage: number
  pageSize: number
  totalPages: number
  totalItems: number
}

// Store 状态接口
export interface ClaimsState {
  myApplications: {
    list: ClaimApplication[]
    loading: boolean
    currentPage: number
    pageSize: number
    totalItems: number
    totalPages: number
    filter: string
  }
  processingApplications: {
    list: ClaimApplication[]
    loading: boolean
    currentPage: number
    pageSize: number
    totalItems: number
    totalPages: number
    filter: string
  }
  currentApplication: ClaimApplication | null
  loading: boolean
  error: string | null
}

export const useClaimsStore = defineStore('claims', {
  state: (): ClaimsState => ({
    myApplications: {
      list: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalItems: 0,
      totalPages: 0,
      filter: ''
    },
    processingApplications: {
      list: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalItems: 0,
      totalPages: 0,
      filter: ''
    },
    currentApplication: null,
    loading: false,
    error: null
  }),

  actions: {
    /**
     * 获取用户提交的认领申请
     * @param params 查询参数
     */
    async fetchMyApplications(params?: { page?: number; status?: string; size?: number }) {
      this.myApplications.loading = true;
      this.error = null;

      try {
        const page = params?.page || this.myApplications.currentPage;
        const status = params?.status || '';
        const size = params?.size || this.myApplications.pageSize;

        // 构建URL和查询参数
        const url = addSearchParams('/claims/my-applications', {
          page,
          size,
          status
        });

        const response = await apiClient.get(url);
        const responseData = response.data;

        if (responseData.success) {
          const paginationData = responseData.data;
          this.myApplications = {
            list: paginationData.applications,
            loading: false,
            currentPage: paginationData.currentPage,
            pageSize: paginationData.pageSize,
            totalItems: paginationData.totalItems,
            totalPages: paginationData.totalPages,
            filter: status
          };
          return paginationData;
        } else {
          throw new Error(responseData.message || '获取认领申请失败');
        }
      } catch (error) {
        const errorMessage = '获取认领申请失败，请稍后重试';
        handleApiError(error, errorMessage);
        this.error = errorMessage;
        throw error;
      } finally {
        this.myApplications.loading = false;
      }
    },

    /**
     * 获取需要用户处理的认领申请
     * @param params 查询参数
     */
    async fetchProcessingApplications(params?: { page?: number; status?: string; size?: number }) {
      this.processingApplications.loading = true;
      this.error = null;

      try {
        const page = params?.page || this.processingApplications.currentPage;
        const status = params?.status || '';
        const size = params?.size || this.processingApplications.pageSize;

        // 构建URL和查询参数
        const url = addSearchParams('/claims/for-processing', {
          page,
          size,
          status
        });

        const response = await apiClient.get(url);
        const responseData = response.data;

        if (responseData.success) {
          const paginationData = responseData.data;
          this.processingApplications = {
            list: paginationData.applications,
            loading: false,
            currentPage: paginationData.currentPage,
            pageSize: paginationData.pageSize,
            totalItems: paginationData.totalItems,
            totalPages: paginationData.totalPages,
            filter: status
          };
          return paginationData;
        } else {
          throw new Error(responseData.message || '获取待处理认领申请失败');
        }
      } catch (error) {
        const errorMessage = '获取待处理认领申请失败，请稍后重试';
        handleApiError(error, errorMessage);
        this.error = errorMessage;
        throw error;
      } finally {
        this.processingApplications.loading = false;
      }
    },

    /**
     * 获取认领申请详情
     * @param id 认领申请ID
     */
    async fetchApplicationById(id: number) {
      this.loading = true;
      this.error = null;

      try {
        const response = await apiClient.get(`/claims/${id}`);
        const responseData = response.data;

        if (responseData.success) {
          this.currentApplication = responseData.data;
          return responseData.data;
        } else {
          throw new Error(responseData.message || '获取认领申请详情失败');
        }
      } catch (error) {
        const errorMessage = '获取认领申请详情失败，请稍后重试';
        handleApiError(error, errorMessage);
        this.error = errorMessage;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 获取指定失物招领的认领申请
     * @param foundItemId 失物招领ID
     * @param params 查询参数
     */
    async fetchApplicationsByFoundItem(
      foundItemId: number,
      params?: { page?: number; size?: number }
    ) {
      this.loading = true;
      this.error = null;

      try {
        const page = params?.page || 1;
        const size = params?.size || 10;

        // 构建URL和查询参数
        const url = addSearchParams(`/claims/by-found-item/${foundItemId}`, {
          page,
          size
        });

        const response = await apiClient.get(url);
        const responseData = response.data;

        if (responseData.success) {
          return responseData.data;
        } else {
          throw new Error(responseData.message || '获取物品认领申请失败');
        }
      } catch (error) {
        const errorMessage = '获取物品认领申请失败，请稍后重试';
        handleApiError(error, errorMessage);
        this.error = errorMessage;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 提交认领申请
     * @param foundItemId 失物招领ID
     * @param claimData 认领申请数据
     */
    async applyForClaim(foundItemId: number, claimData: { description: string }) {
      this.loading = true;
      this.error = null;

      try {
        const response = await apiClient.post(`/claims/apply/${foundItemId}`, claimData);
        const responseData = response.data;

        if (responseData.success) {
          ElMessage.success('认领申请提交成功，请等待审核');
          return responseData.data;
        } else {
          throw new Error(responseData.message || '提交认领申请失败');
        }
      } catch (error) {
        // 使用后端返回的具体错误信息，这样用户能更清楚地知道为什么不能重复申请
        const errorResult = handleApiError(error, '提交认领申请失败，请稍后重试');
        this.error = errorResult.message;
        ElMessage.error(errorResult.message);
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 批准认领申请
     * @param applicationId 认领申请ID
     */
    async approveApplication(applicationId: number) {
      this.loading = true;
      this.error = null;

      try {
        const response = await apiClient.post(`/claims/approve/${applicationId}`);
        const responseData = response.data;

        if (responseData.success) {
          ElMessage.success('已批准认领申请');
          return responseData.data;
        } else {
          throw new Error(responseData.message || '批准认领申请失败');
        }
      } catch (error) {
        const errorMessage = '批准认领申请失败，请稍后重试';
        handleApiError(error, errorMessage);
        this.error = errorMessage;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 拒绝认领申请
     * @param applicationId 认领申请ID
     */
    async rejectApplication(applicationId: number) {
      this.loading = true;
      this.error = null;

      try {
        const response = await apiClient.post(`/claims/reject/${applicationId}`);
        const responseData = response.data;

        if (responseData.success) {
          ElMessage.success('已拒绝认领申请');
          return responseData.data;
        } else {
          throw new Error(responseData.message || '拒绝认领申请失败');
        }
      } catch (error) {
        const errorMessage = '拒绝认领申请失败，请稍后重试';
        handleApiError(error, errorMessage);
        this.error = errorMessage;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 重置store状态
     */
    resetState() {
      this.myApplications = {
        list: [],
        loading: false,
        currentPage: 1,
        pageSize: 10,
        totalItems: 0,
        totalPages: 0,
        filter: ''
      };
      this.processingApplications = {
        list: [],
        loading: false,
        currentPage: 1,
        pageSize: 10,
        totalItems: 0,
        totalPages: 0,
        filter: ''
      };
      this.currentApplication = null;
      this.loading = false;
      this.error = null;
    }
  }
});
