const ownerRoutes = {
  path: '/owner',
  name: 'Owner',
  component: () => import('@/layout/OwnerLayout.vue'),
  meta: { title: '客户自助端', roles: ['owner'] },
  children: [
    {
      path: 'pet',
      name: 'OwnerPet',
      component: () => import('@/views/owner/pet/index.vue'),
      meta: { title: '我的宠物', icon: 'Pets', keepAlive: true }
    },
    {
      path: 'pet/add',
      name: 'OwnerPetAdd',
      component: () => import('@/views/owner/pet/add.vue'),
      meta: { title: '添加宠物', hidden: true }
    },
    {
      path: 'pet/:id',
      name: 'OwnerPetDetail',
      component: () => import('@/views/owner/pet/detail.vue'),
      meta: { title: '宠物详情', hidden: true }
    },
    {
      path: 'health',
      name: 'OwnerHealth',
      component: () => import('@/views/owner/health/index.vue'),
      meta: { title: '健康记录', icon: 'Notebook', keepAlive: true }
    },
    {
      path: 'reserve',
      name: 'OwnerReserve',
      component: () => import('@/views/owner/reserve/index.vue'),
      meta: { title: '预约申请', icon: 'Calendar', keepAlive: true }
    },
    {
      path: 'reserve/:id',
      name: 'OwnerReserveDetail',
      component: () => import('@/views/owner/reserve/detail.vue'),
      meta: { title: '预约详情', hidden: true }
    },
    {
      path: 'reserve/create',
      name: 'OwnerReserveCreate',
      component: () => import('@/views/owner/reserve/create.vue'),
      meta: { title: '创建预约', hidden: true }
    },
    {
      path: 'consult',
      name: 'OwnerConsult',
      component: () => import('@/views/owner/consult/index.vue'),
      meta: { title: '在线咨询', icon: 'ChatDotRound', keepAlive: true }
    },
    {
      path: 'consult/:id',
      name: 'OwnerConsultDetail',
      component: () => import('@/views/owner/consult/detail.vue'),
      meta: { title: '咨询详情', hidden: true }
    },
    {
      path: 'profile',
      name: 'OwnerProfile',
      component: () => import('@/views/owner/profile/index.vue'),
      meta: { title: '个人中心', hidden: true }
    }
  ]
}

export default ownerRoutes