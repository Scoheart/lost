import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/utils/api'
import type { AxiosResponse } from 'axios'
import { useUserStore } from './user'

interface Post {
  id: number
  title: string
  content: string
  userId: number
  username: string
  userAvatar?: string
  createdAt: string
  updatedAt: string
  commentCount: number
}

interface CreatePostRequest {
  title: string
  content: string
}

interface UpdatePostRequest {
  title: string
  content: string
}

interface PostsResponse {
  items: Post[]
  page: number
  pageSize: number
  totalPages: number
  total: number
}

export const usePostsStore = defineStore('posts', () => {
  // 状态
  const posts = ref<Post[]>([])
  const currentPost = ref<Post | null>(null)
  const userPosts = ref<Post[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const pageSize = ref(10)

  // 计算属性
  const userStore = useUserStore()
  const isPostOwner = computed(() => {
    return currentPost.value && userStore.user && currentPost.value.userId === userStore.user.id
  })

  // 方法
  // 获取所有帖子
  async function fetchPosts(page: number = 0, size: number = 10) {
    loading.value = true
    error.value = null

    try {
      const response = await apiClient.get<any, AxiosResponse<{ data: PostsResponse }>>(`/posts?page=${page}&size=${size}`)

      posts.value = response.data.data.items
      totalElements.value = response.data.data.total
      totalPages.value = response.data.data.totalPages
      currentPage.value = response.data.data.page
      pageSize.value = response.data.data.pageSize

      return response.data.data
    } catch (err: any) {
      console.error('获取帖子列表失败:', err)
      error.value = err.response?.data?.message || '获取帖子列表失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 获取帖子详情
  async function fetchPostById(id: number) {
    loading.value = true
    error.value = null
    currentPost.value = null

    try {
      const response = await apiClient.get<any, AxiosResponse<{ data: Post }>>(`/posts/${id}`)
      currentPost.value = response.data.data
      return currentPost.value
    } catch (err: any) {
      console.error('获取帖子详情失败:', err)
      error.value = err.response?.data?.message || '获取帖子详情失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 创建帖子
  async function createPost(postData: CreatePostRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await apiClient.post<any, AxiosResponse<{ data: Post }>>('/posts', postData)
      return response.data.data
    } catch (err: any) {
      console.error('创建帖子失败:', err)
      error.value = err.response?.data?.message || '创建帖子失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 更新帖子
  async function updatePost(id: number, postData: UpdatePostRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await apiClient.put<any, AxiosResponse<{ data: Post }>>(`/posts/${id}`, postData)
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value = response.data.data
      }
      return response.data.data
    } catch (err: any) {
      console.error('更新帖子失败:', err)
      error.value = err.response?.data?.message || '更新帖子失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 删除帖子
  async function deletePost(id: number) {
    loading.value = true
    error.value = null

    try {
      const response = await apiClient.delete<any, AxiosResponse<{ message: string }>>(`/posts/${id}`)
      // 删除成功后，从列表中移除
      posts.value = posts.value.filter(post => post.id !== id)
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value = null
      }
      return response.data.message
    } catch (err: any) {
      console.error('删除帖子失败:', err)
      error.value = err.response?.data?.message || '删除帖子失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 获取用户帖子
  async function fetchUserPosts(userId: number, page: number = 0, size: number = 10) {
    loading.value = true
    error.value = null

    try {
      const response = await apiClient.get<any, AxiosResponse<{ data: PostsResponse }>>(`/posts/user/${userId}?page=${page}&size=${size}`)
      userPosts.value = response.data.data.items
      totalElements.value = response.data.data.total
      totalPages.value = response.data.data.totalPages
      currentPage.value = response.data.data.page
      pageSize.value = response.data.data.pageSize
      return response.data.data
    } catch (err: any) {
      console.error('获取用户帖子失败:', err)
      error.value = err.response?.data?.message || '获取用户帖子失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 获取当前用户帖子
  async function fetchMyPosts(page: number = 0, size: number = 10) {
    loading.value = true
    error.value = null

    try {
      const response = await apiClient.get<any, AxiosResponse<{ data: PostsResponse }>>(`/posts/me?page=${page}&size=${size}`)
      userPosts.value = response.data.data.items
      totalElements.value = response.data.data.total
      totalPages.value = response.data.data.totalPages
      currentPage.value = response.data.data.page
      pageSize.value = response.data.data.pageSize
      return response.data.data
    } catch (err: any) {
      console.error('获取我的帖子失败:', err)
      error.value = err.response?.data?.message || '获取我的帖子失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 搜索帖子
  async function searchPosts(keyword: string, page: number = 0, size: number = 10) {
    loading.value = true
    error.value = null

    try {
      const response = await apiClient.get<any, AxiosResponse<{ data: PostsResponse }>>(
        `/posts/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`
      )
      posts.value = response.data.data.items
      totalElements.value = response.data.data.total
      totalPages.value = response.data.data.totalPages
      currentPage.value = response.data.data.page
      pageSize.value = response.data.data.pageSize

      return response.data.data
    } catch (err: any) {
      console.error('搜索帖子失败:', err)
      error.value = err.response?.data?.message || '搜索帖子失败'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 重置状态
  function resetState() {
    posts.value = []
    currentPost.value = null
    userPosts.value = []
    error.value = null
    totalElements.value = 0
    totalPages.value = 0
    currentPage.value = 0
  }

  return {
    // 状态
    posts,
    currentPost,
    userPosts,
    loading,
    error,
    totalElements,
    totalPages,
    currentPage,
    pageSize,

    // 计算属性
    isPostOwner,

    // 方法
    fetchPosts,
    fetchPostById,
    createPost,
    updatePost,
    deletePost,
    fetchUserPosts,
    fetchMyPosts,
    searchPosts,
    resetState
  }
})
