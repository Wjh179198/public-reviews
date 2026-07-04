import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  // ========== 用户端 ==========
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    children: [
      {
        path: '',
        redirect: '/shops',
      },
      {
        path: 'shops',
        name: 'ShopList',
        component: () => import('@/views/shop/ShopList.vue'),
      },
      {
        path: 'shops/:id',
        name: 'ShopDetail',
        component: () => import('@/views/shop/ShopDetail.vue'),
      },
      {
        path: 'shops/:id/write-review',
        name: 'WriteReview',
        component: () => import('@/views/shop/WriteReview.vue'),
      },
      {
        path: 'blog',
        name: 'BlogFeed',
        component: () => import('@/views/blog/BlogFeed.vue'),
      },
      {
        path: 'blog/publish',
        name: 'PublishBlog',
        component: () => import('@/views/blog/PublishBlog.vue'),
      },
      {
        path: 'register-shop',
        name: 'RegisterShop',
        component: () => import('@/views/merchant/RegisterShop.vue'),
      },
      {
        path: 'shop-manage',
        name: 'ShopManage',
        component: () => import('@/views/merchant/ShopManage.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
      },
      {
        path: 'profile/edit',
        name: 'EditProfile',
        component: () => import('@/views/user/EditProfile.vue'),
      },
      {
        path: 'profile/recharge',
        name: 'Recharge',
        component: () => import('@/views/user/Recharge.vue'),
      },
      {
        path: 'profile/orders',
        name: 'MyOrders',
        component: () => import('@/views/user/MyOrders.vue'),
      },
      {
        path: 'user/:id',
        name: 'UserDetail',
        component: () => import('@/views/user/UserDetail.vue'),
      },
      {
        path: 'search',
        name: 'SearchResult',
        component: () => import('@/views/search/SearchResult.vue'),
      },
    ],
  },

  // ========== 管理端 ==========
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { guest: true },
  },
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    meta: { admin: true },
    children: [
      {
        path: '',
        redirect: '/admin/users',
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/AdminProfile.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')

  // 管理端路由守卫
  if (to.meta.admin || to.path.startsWith('/admin')) {
    if (to.path === '/admin/login') {
      next()
      return
    }
    if (!adminToken) {
      next('/admin/login')
      return
    }
    next()
    return
  }

  // 用户端路由守卫
  if (to.meta.guest) {
    // 已登录用户访问登录/注册页，重定向到首页
    if (token) {
      next('/shops')
      return
    }
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
