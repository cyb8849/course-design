import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/customer/Layout.vue'),
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/customer/Home.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('../views/customer/Products.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/customer/ProductDetail.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../views/customer/Cart.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'addresses',
        name: 'Addresses',
        component: () => import('../views/customer/Addresses.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/customer/Orders.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('../views/customer/OrderDetail.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'payment',
        name: 'Payment',
        component: () => import('../views/customer/Payment.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/customer/Profile.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'traceability',
        name: 'Traceability',
        component: () => import('../views/customer/Traceability.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'traceability/:productId',
        name: 'TraceabilityWithId',
        component: () => import('../views/customer/Traceability.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      },
      {
        path: 'logistics',
        name: 'LogisticsTracking',
        component: () => import('../views/customer/LogisticsTracking.vue'),
        meta: { requiresAuth: true, roles: ['CUSTOMER'] }
      }
    ]
  },
  {
    path: '/farmer',
    name: 'Farmer',
    component: () => import('../views/farmer/Layout.vue'),
    meta: { requiresAuth: true, roles: ['FARMER'] },
    children: [
      {
        path: 'home',
        name: 'FarmerHome',
        component: () => import('../views/farmer/Home.vue')
      },
      {
        path: 'products',
        name: 'FarmerProducts',
        component: () => import('../views/farmer/Products.vue')
      },
      {
        path: 'orders',
        name: 'FarmerOrders',
        component: () => import('../views/farmer/Orders.vue')
      },
      {
        path: 'order/:id',
        name: 'FarmerOrderDetail',
        component: () => import('../views/farmer/OrderDetail.vue')
      },
      {
        path: 'statistics',
        name: 'FarmerStatistics',
        component: () => import('../views/farmer/Statistics.vue')
      },
      {
        path: 'traceability',
        name: 'FarmerTraceability',
        component: () => import('../views/farmer/Traceability.vue')
      },
      {
        path: 'ai',
        name: 'FarmerAI',
        component: () => import('../views/farmer/AI.vue')
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('../views/admin/Home.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue')
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/Products.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('../views/admin/Categories.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/Orders.vue')
      },
      {
        path: 'orders/:id',
        name: 'AdminOrderDetail',
        component: () => import('../views/admin/OrderDetail.vue')
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('../views/admin/Statistics.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let user = null
  if (userStr) {
    user = JSON.parse(userStr)
  }

  // 不需要认证的页面
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  // 需要认证的页面
  if (!token || !user) {
    next({ name: 'Login' })
    return
  }

  // 角色权限检查
  if (to.meta.roles && !to.meta.roles.includes(user.role)) {
    // 根据角色重定向到对应首页
    if (user.role === 'FARMER') {
      next({ name: 'FarmerHome' })
    } else if (user.role === 'ADMIN') {
      next({ name: 'AdminHome' })
    } else {
      next({ name: 'Home' })
    }
    return
  }

  next()
})

export default router