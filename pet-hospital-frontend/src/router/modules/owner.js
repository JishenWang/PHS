export default [
  {
    path: '/owner',
    redirect: '/owner/pet'
  },
  {
    path: '/owner/pet',
    name: 'OwnerPet',
    component: () => import('@/views/owner/pet/index.vue'),
    meta: { title: '我的宠物' }
  },
  {
    path: '/owner/health',
    name: 'OwnerHealth',
    component: () => import('@/views/owner/health/index.vue'),
    meta: { title: '健康记录' }
  },
  {
    path: '/owner/reserve',
    name: 'OwnerReserve',
    component: () => import('@/views/owner/reserve/index.vue'),
    meta: { title: '预约申请' }
  },
  {
    path: '/owner/consult',
    name: 'OwnerConsult',
    component: () => import('@/views/owner/consult/index.vue'),
    meta: { title: '在线咨询' }
  }
]