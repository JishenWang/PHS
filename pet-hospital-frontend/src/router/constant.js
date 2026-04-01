// 常量路由：不需要权限，所有用户都能访问
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404/index.vue'),
    meta: { title: '404', hidden: true }
  },
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/403/index.vue'),
    meta: { title: '403', hidden: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]