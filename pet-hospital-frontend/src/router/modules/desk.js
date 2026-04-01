export default [
  {
    path: '/desk',
    redirect: '/desk/customer'
  },
  {
    path: '/desk/customer',
    name: 'DeskCustomer',
    component: () => import('@/views/desk/customer/index.vue'),
    meta: { title: '客户查询' }
  },
  {
    path: '/desk/register',
    name: 'DeskRegister',
    component: () => import('@/views/desk/register/index.vue'),
    meta: { title: '挂号管理' }
  },
  {
    path: '/desk/charge',
    name: 'DeskCharge',
    component: () => import('@/views/desk/charge/index.vue'),
    meta: { title: '收费管理' }
  }
]