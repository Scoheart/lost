// stores/forum.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'

export interface ForumPost {
  id: number
  title: string
  content: string
  userId: number
  username: string
  likeCount: number
  commentCount: number
  isTop: boolean
  status: 'active' | 'locked' | 'deleted'
  createdAt: string
  updatedAt: string
}

export interface ForumComment {
  id: number
  content: string
  postId: number
  userId: number
  username: string
  likeCount: number
  createdAt: string
}

export interface ForumState {
  posts: ForumPost[]
  currentPost: ForumPost | null
  comments: ForumComment[]
  loading: boolean
  error: string | null
  filters: {
    keyword: string | null
    userId: number | null
  }
  pagination: {
    page: number
    pageSize: number
    total: number
  }
}

export const useForumStore = defineStore('forum', {
  state: (): ForumState => ({
    posts: [],
    currentPost: null,
    comments: [],
    loading: false,
    error: null,
    filters: {
      keyword: null,
      userId: null
    },
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    filteredPosts: (state) => {
      let result = [...state.posts]

      if (state.filters.keyword) {
        const keyword = state.filters.keyword.toLowerCase()
        result = result.filter(post =>
          post.title.toLowerCase().includes(keyword) ||
          post.content.toLowerCase().includes(keyword)
        )
      }

      if (state.filters.userId) {
        result = result.filter(post => post.userId === state.filters.userId)
      }

      return result
    },

    paginatedPosts: (state) => {
      const start = (state.pagination.page - 1) * state.pagination.pageSize
      const end = start + state.pagination.pageSize
      return state.posts.slice(start, end)
    },

    topPosts: (state) => {
      return state.posts.filter(post => post.isTop && post.status === 'active')
    }
  },

  actions: {
    setFilters(filters: Partial<ForumState['filters']>) {
      this.filters = { ...this.filters, ...filters }
    },

    async fetchPosts() {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get('/forum/posts', {
          params: {
            page: this.pagination.page,
            pageSize: this.pagination.pageSize,
            ...this.filters
          }
        })

        this.posts = response.data.items
        this.pagination.total = response.data.total
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取论坛帖子列表失败'
      } finally {
        this.loading = false
      }
    },

    async fetchPostById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get(`/forum/posts/${id}`)
        this.currentPost = response.data

        // 同时获取评论
        await this.fetchComments(id)
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取帖子详情失败'
      } finally {
        this.loading = false
      }
    },

    async fetchComments(postId: number) {
      try {
        const response = await apiClient.get(`/forum/posts/${postId}/comments`)
        this.comments = response.data
      } catch (error: any) {
        console.error('获取评论失败', error)
        // 评论获取失败不影响主流程，只记录错误
      }
    },

    async createPost(postData: { title: string, content: string }) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post('/forum/posts', postData)

        // 更新列表（可选，取决于后端返回格式）
        if (response.data.post) {
          this.posts.unshift(response.data.post)
        }

        return { success: true, id: response.data.id }
      } catch (error: any) {
        this.error = error.response?.data?.message || '发布帖子失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async updatePost(id: number, postData: Partial<ForumPost>) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        const response = await apiClient.put(`/forum/posts/${id}`, postData)

        // 更新当前查看的帖子（如果是同一个）
        if (this.currentPost?.id === id) {
          this.currentPost = response.data.post || { ...this.currentPost, ...postData }
        }

        // 更新列表中的帖子
        const postIndex = this.posts.findIndex(post => post.id === id)
        if (postIndex !== -1) {
          this.posts[postIndex] = response.data.post || { ...this.posts[postIndex], ...postData }
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新帖子失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async deletePost(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        await apiClient.delete(`/forum/posts/${id}`)

        // 从列表中移除
        this.posts = this.posts.filter(post => post.id !== id)

        // 清除当前帖子（如果是同一个）
        if (this.currentPost?.id === id) {
          this.currentPost = null
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '删除帖子失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async addComment(comment: { content: string, postId: number }) {
      try {
        const response = await apiClient.post(`/forum/posts/${comment.postId}/comments`, {
          content: comment.content
        })

        this.comments.unshift(response.data)

        // 更新评论计数
        if (this.currentPost && this.currentPost.id === comment.postId) {
          this.currentPost.commentCount++
        }

        return { success: true }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '发表评论失败'
        }
      }
    },

    async likePost(id: number) {
      try {
        const response = await apiClient.post(`/forum/posts/${id}/like`)

        // 更新点赞计数
        if (this.currentPost && this.currentPost.id === id) {
          this.currentPost.likeCount = response.data.likeCount
        }

        // 更新列表中的帖子
        const postIndex = this.posts.findIndex(post => post.id === id)
        if (postIndex !== -1) {
          this.posts[postIndex].likeCount = response.data.likeCount
        }

        return { success: true }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '点赞失败'
        }
      }
    },

    async reportPost(id: number, reason: string) {
      try {
        await apiClient.post(`/forum/posts/${id}/report`, { reason })
        return { success: true, message: '举报已提交，感谢您的反馈' }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '举报提交失败'
        }
      }
    }
  }
})
