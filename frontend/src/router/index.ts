import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '首页' }
    },
    // 公告浏览
    {
      path: '/announcements',
      name: 'announcements',
      component: () => import('../views/announcements/AnnouncementsView.vue'),
      meta: { title: '社区公告' }
    },
    {
      path: '/announcements/:id',
      name: 'announcement-detail',
      component: () => import('../views/announcements/AnnouncementDetailView.vue'),
      meta: { title: '公告详情' }
    },
    // 个人中心
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/profile/ProfileView.vue'),
      meta: { title: '个人中心', requiresAuth: true },
      redirect: '/profile/settings',
      children: [
        {
          path: 'settings',
          name: 'settings',
          component: () => import('../views/profile/SettingsView.vue'),
          meta: { title: '个人设置', requiresAuth: true }
        },
        {
          path: 'my-posts',
          name: 'my-posts',
          component: () => import('../views/profile/MyPostsView.vue'),
          meta: { title: '我的发布', requiresAuth: true }
        }
      ]
    },
    // 寻物启事
    {
      path: '/lost-items',
      name: 'lost-items',
      component: () => import('../views/lost-items/LostItemsView.vue'),
      meta: { title: '寻物启事' }
    },
    {
      path: '/lost-items/create',
      name: 'create-lost-item',
      component: () => import('../views/lost-items/EditLostItemView.vue'),
      meta: { title: '发布寻物启事', requiresAuth: true }
    },
    {
      path: '/lost-items/edit/:id',
      name: 'edit-lost-item',
      component: () => import('../views/lost-items/EditLostItemView.vue'),
      meta: { title: '编辑寻物启事', requiresAuth: true }
    },
    {
      path: '/lost-items/:id',
      name: 'lost-item-detail',
      component: () => import('../views/lost-items/LostItemDetailView.vue'),
      meta: { title: '寻物详情' }
    },
    // 失物招领
    {
      path: '/found-items',
      name: 'found-items',
      component: () => import('../views/found-items/FoundItemsView.vue'),
      meta: { title: '失物招领' }
    },
    {
      path: '/found-items/create',
      name: 'create-found-item',
      component: () => import('../views/found-items/EditFoundItemView.vue'),
      meta: { title: '发布失物招领', requiresAuth: true }
    },
    {
      path: '/found-items/edit/:id',
      name: 'edit-found-item',
      component: () => import('../views/found-items/EditFoundItemView.vue'),
      meta: { title: '编辑失物招领', requiresAuth: true }
    },
    {
      path: '/found-items/:id',
      name: 'found-item-detail',
      component: () => import('../views/found-items/FoundItemDetailView.vue'),
      meta: { title: '招领详情' }
    },
    // 邻里论坛
    {
      path: '/forum',
      name: 'forum',
      component: () => import('../views/forum/ForumView.vue'),
      meta: { title: '邻里论坛' }
    },
    {
      path: '/forum/create',
      name: 'create-post',
      component: () => import('../views/forum/CreatePostView.vue'),
      meta: { title: '发布帖子', requiresAuth: true }
    },
    {
      path: '/forum/:id',
      name: 'post-detail',
      component: () => import('../views/forum/PostDetailView.vue'),
      meta: { title: '帖子详情' }
    },
    // 登录/注册
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/auth/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/auth/RegisterView.vue'),
      meta: { title: '注册' }
    },
    // 管理员登录
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../views/auth/AdminLoginView.vue'),
      meta: { title: '管理员登录' }
    },
    {
      path: '/admin/forgot-password',
      name: 'admin-forgot-password',
      component: () => import('../views/auth/AdminForgotPasswordView.vue'),
      meta: { title: '管理员密码找回' }
    },
    // 管理员页面
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/admin/AdminView.vue'),
      meta: { title: '管理中心', requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'announcements',
          name: 'admin-announcements',
          component: () => import('../views/admin/AnnouncementsManageView.vue'),
          meta: { title: '公告管理', requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'reports',
          name: 'admin-reports',
          component: () => import('../views/admin/ReportsManageView.vue'),
          meta: { title: '举报管理', requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'claims',
          name: 'admin-claims',
          component: () => import('../views/admin/ClaimsManageView.vue'),
          meta: { title: '认领管理', requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'residents',
          name: 'admin-residents',
          component: () => import('../views/admin/ResidentsManageView.vue'),
          meta: { title: '居民管理', requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/UsersManageView.vue'),
          meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true, requiresSysAdmin: true }
        }
      ]
    },
    // 404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: { title: '页面未找到' }
    }
  ]
})

// 路由守卫，处理鉴权和页面标题
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '住宅小区互助寻物系统'}`

  // 导入用户状态管理
  const userStore = useUserStore()

  // 记录路由守卫开始
  console.log(`[Router Guard] Navigating to: ${to.path}`)

  // 检查是否允许绕过管理员权限验证（开发环境下）
  const isDev = import.meta.env.DEV
  const allowAdminAccess = import.meta.env.VITE_ALLOW_ADMIN_ACCESS === 'true'
  const bypassAdminCheck = isDev && allowAdminAccess

  if (bypassAdminCheck && (to.meta.requiresAdmin || to.meta.requiresSysAdmin)) {
    console.log('[Router Guard] Development mode: bypassing admin permission check')
    next()
    return
  }

  // 如果当前有用户信息，直接通过
  if (userStore.isAuthenticated) {
    console.log('[Router Guard] User is already authenticated')

    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      console.log('[Router Guard] Access denied: Admin privileges required')
      next({ name: 'home' })
      return
    }

    // 检查是否需要系统管理员权限
    if (to.meta.requiresSysAdmin && !userStore.isSysAdmin) {
      console.log('[Router Guard] Access denied: SysAdmin privileges required')
      next({ name: 'admin' })
      return
    }

    // 权限符合，放行
    next()
    return
  }

  // 无需认证的页面，直接通过
  if (!to.meta.requiresAuth) {
    console.log('[Router Guard] No authentication required for this route')
    next()
    return
  }

  // 有 token 但没有用户数据，尝试获取用户信息
  if (userStore.token && !userStore.user) {
    console.log('[Router Guard] Has token but no user data, attempting to fetch user data')
    try {
      const result = await userStore.fetchCurrentUser()

      if (result?.success) {
        console.log('[Router Guard] Successfully retrieved user data')

        // 再次检查管理员/系统管理员权限
        if (to.meta.requiresAdmin && !userStore.isAdmin) {
          console.log('[Router Guard] Access denied: Admin privileges required')
          next({ name: 'home' })
          return
        }

        if (to.meta.requiresSysAdmin && !userStore.isSysAdmin) {
          console.log('[Router Guard] Access denied: SysAdmin privileges required')
          next({ name: 'admin' })
          return
        }

        // 权限符合，放行
        next()
        return
      } else if (result?.pending) {
        console.log('[Router Guard] User data fetch in progress, continuing navigation')
        next()
        return
      }

      // 获取用户数据失败，清除 token 并重定向到登录页
      console.log('[Router Guard] Failed to fetch user data, redirecting to login')
      userStore.logout()
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    } catch (error) {
      console.error('[Router Guard] Error retrieving user data:', error)
      userStore.logout()
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 未认证且需要认证的页面，重定向到登录
  console.log('[Router Guard] Authentication required, redirecting to login')
  next({ name: 'login', query: { redirect: to.fullPath } })
})

export default router
