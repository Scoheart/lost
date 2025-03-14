import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

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
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/UsersManageView.vue'),
          meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true, requiresSysAdmin: true }
        }
      ]
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
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '住宅小区互助寻物系统'}`

  // 鉴权处理 (实际开发时会检查token/用户角色)
  if (to.meta.requiresAuth) {
    // 这里未来会添加实际的鉴权逻辑
    // const isAuthenticated = store.getters.isAuthenticated
    const isAuthenticated = true // 临时硬编码，实际开发需要与用户状态关联

    if (!isAuthenticated) {
      next({ name: 'login', query: { redirect: to.fullPath } })
    } else if (to.meta.requiresAdmin) {
      // 检查是否是管理员
      // const isAdmin = store.getters.isAdmin
      const isAdmin = true // 临时硬编码，实际开发需要与用户状态关联

      if (!isAdmin) {
        next({ name: 'home' })
      } else if (to.meta.requiresSysAdmin) {
        // 检查是否是系统管理员
        // const isSysAdmin = store.getters.isSysAdmin
        const isSysAdmin = true // 临时硬编码，实际开发需要与用户状态关联

        if (!isSysAdmin) {
          next({ name: 'admin' })
        } else {
          next()
        }
      } else {
        next()
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
