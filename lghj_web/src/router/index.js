import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import Login from '../views/Login.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue')
      },
      {
        path: 'trade',
        name: 'TradeManage',
        component: () => import('../views/admin/TradeManage.vue')
      },
      {
        path: 'blog',
        name: 'BlogManage',
        component: () => import('../views/admin/BlogManage.vue')
      },
      {
        path: 'comment',
        name: 'CommentManage',
        component: () => import('../views/admin/CommentManage.vue')
      },
      {
        path: 'stock',
        name: 'StockManage',
        component: () => import('../views/admin/StockManage.vue')
      }
    ]
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'market',
        name: 'Market',
        component: () => import('../views/Market.vue')
      },
      {
        path: 'prediction',
        name: 'Prediction',
        component: () => import('../views/Prediction.vue')
      },
      {
        path: 'watchlist',
        name: 'Watchlist',
        component: () => import('../views/Watchlist.vue')
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('../views/Community.vue')
      },
      {
        path: 'news',
        name: 'News',
        component: () => import('../views/News.vue')
      },
      {
        path: 'simulation',
        name: 'Simulation',
        component: () => import('../views/Simulation.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
import { useUserStore } from '../stores/user'

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAdmin && userStore.userInfo.userType !== 3) {
    next('/login')
  } else {
    next()
  }
})

export default router
