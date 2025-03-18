import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/utils/api'
import type { AxiosResponse } from 'axios'
import { useUserStore } from './user'

export interface Comment {
  id: number
  content: string
  userId: number
  username: string
  userAvatar?: string
  itemId: number
  itemType: string
  createdAt: string
  updatedAt?: string
}

interface CommentPagination {
  currentPage: number
  pageSize: number
  totalItems: number
  totalPages: number
}

interface CreateCommentRequest {
  content: string
  itemId: number
  itemType: string
}

interface CreateItemCommentRequest {
  content: string
  itemId: number
  itemType: string
}

interface CreatePostCommentRequest {
  content: string
  postId: number
}

export const useCommentsStore = defineStore('comments', () => {
  // 状态
  const comments = ref<Comment[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref<CommentPagination>({
    currentPage: 0,
    pageSize: 10,
    totalItems: 0,
    totalPages: 0
  })

  // 获取用户信息
  const userStore = useUserStore()

  /**
   * 获取评论列表 - 统一方法，会根据itemType自动选择合适的API
   */
  async function fetchComments(itemId: number, itemType: string, page = 1, size = 10) {
    loading.value = true
    error.value = null

    try {
      let response;

      if (itemType === 'post') {
        // 使用帖子评论API
        response = await apiClient.get<any, AxiosResponse<{ data: any, message?: string, success: boolean }>>(
          `/post-comments?postId=${itemId}&page=${page}&size=${size}`
        );
      } else {
        // 使用物品评论API（失物或寻物）
        response = await apiClient.get<any, AxiosResponse<{ data: any, message?: string, success: boolean }>>(
          `/item-comments?itemId=${itemId}&itemType=${itemType}&page=${page}&size=${size}`
        );
      }

      if (!response.data.success) {
        throw new Error(response.data.message || '获取评论失败');
      }

      const responseData = response.data.data;
      comments.value = responseData.comments || [];
      pagination.value = {
        currentPage: responseData.currentPage,
        pageSize: responseData.pageSize,
        totalItems: responseData.totalItems,
        totalPages: responseData.totalPages
      }

      return {
        content: comments.value,
        currentPage: pagination.value.currentPage,
        pageSize: pagination.value.pageSize,
        totalItems: pagination.value.totalItems,
        totalPages: pagination.value.totalPages
      }
    } catch (err: any) {
      console.error('获取评论失败:', err)
      error.value = err.response?.data?.message || '获取评论失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加评论 - 统一方法，会根据itemType自动选择合适的API
   */
  async function addComment(commentData: CreateCommentRequest) {
    loading.value = true
    error.value = null

    try {
      let response;

      if (commentData.itemType === 'post') {
        // 使用帖子评论API
        const postCommentRequest: CreatePostCommentRequest = {
          content: commentData.content,
          postId: commentData.itemId
        };

        response = await apiClient.post<any, AxiosResponse<{ data: any, message: string, success: boolean }>>(
          '/post-comments',
          postCommentRequest
        );
      } else {
        // 使用物品评论API（失物或寻物）
        const itemCommentRequest: CreateItemCommentRequest = {
          content: commentData.content,
          itemId: commentData.itemId,
          itemType: commentData.itemType
        };

        response = await apiClient.post<any, AxiosResponse<{ data: any, message: string, success: boolean }>>(
          '/item-comments',
          itemCommentRequest
        );
      }

      if (response.data.success) {
        return response.data
      } else {
        throw new Error(response.data.message || '添加评论失败')
      }
    } catch (err: any) {
      console.error('添加评论失败:', err)
      error.value = err.response?.data?.message || '添加评论失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除评论 - 统一方法，会根据itemType自动选择合适的API
   */
  async function deleteComment(commentId: number, itemType: string) {
    loading.value = true
    error.value = null

    try {
      let endpoint = itemType === 'post'
        ? `/post-comments/${commentId}`  // 帖子评论
        : `/item-comments/${commentId}`; // 物品评论

      const response = await apiClient.delete<any, AxiosResponse<{ message: string, success: boolean }>>(endpoint);

      if (response.data.success) {
        // 从本地状态中移除评论
        comments.value = comments.value.filter(comment => comment.id !== commentId)
        return response.data
      } else {
        throw new Error(response.data.message || '删除评论失败')
      }
    } catch (err: any) {
      console.error('删除评论失败:', err)
      error.value = err.response?.data?.message || '删除评论失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 重置状态
  function resetState() {
    comments.value = []
    error.value = null
    pagination.value = {
      currentPage: 0,
      pageSize: 10,
      totalItems: 0,
      totalPages: 0
    }
  }

  return {
    // 状态
    comments,
    loading,
    error,
    pagination,

    // 方法
    fetchComments,
    addComment,
    deleteComment,
    resetState
  }
})
