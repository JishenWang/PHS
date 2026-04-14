export default [
  {
    path: '/desk',
    component: () => import('@/layout/DeskLayout.vue'),
    redirect: '/desk/customer',
    meta: { title: '前台收银端' },
    children: [
      {
        path: 'customer',
        name: 'DeskCustomer',
        component: () => import('@/views/desk/customer/index.vue'),
        meta: { title: '客户查询' }
      },
      {
        path: 'register',
        name: 'DeskRegister',
        component: () => import('@/views/desk/register/index.vue'),
        meta: { title: '挂号管理' }
      },
      {
        path: 'charge',
        name: 'DeskCharge',
        component: () => import('@/views/desk/charge/index.vue'),
        meta: { title: '收费管理' }
      }
    ]
  }
]
