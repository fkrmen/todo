import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin',
    name: 'AdminPage',
    component: () => import('../views/admin/AdminPage.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/',
    component: () => import('../views/MainLayout.vue'),
    redirect: '/todos',
    children: [
      {
        path: 'todos',
        name: 'TodoList',
        component: () => import('../views/TodoList.vue'),
        meta: { title: '待办事项' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    next('/todos')
    return
  }

  if (to.path === '/login' && token) {
    next(role === 'ADMIN' ? '/admin' : '/todos')
    return
  }

  document.title = to.meta.title || 'Todo 待办'
  next()
})

export default router
