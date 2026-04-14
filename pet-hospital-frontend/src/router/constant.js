// ==================== 常量路由（所有人可访问）====================
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    hidden: true,
    meta: { title: '登录' }
  },
  {
    path: '/403',
    name: '403',
    component: () => import('../views/403/index.vue'),
    hidden: true,
    meta: { title: '无权限' }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('../views/404/index.vue'),
    hidden: true,
    meta: { title: '页面不存在' }
  }
]

// ==================== 异步路由（需要登录和权限）====================
export const asyncRoutes = [
  // 管理员路由
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { title: '管理端', roles: ['admin'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'dashboard' }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('../views/admin/user/index.vue'),
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'doctor',
        name: 'AdminDoctor',
        component: () => import('../views/admin/doctor/index.vue'),
        meta: { title: '医生管理', icon: 'doctor' }
      },
      {
        path: 'pet',
        name: 'AdminPet',
        component: () => import('../views/admin/pet/index.vue'),
        meta: { title: '宠物管理', icon: 'pet' }
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('../views/admin/system/index.vue'),
        meta: { title: '系统配置', icon: 'setting' }
      }
    ]
  },
  
  // 客户自助端路由
  {
    path: '/owner',
    name: 'Owner',
    component: () => import('../layout/OwnerLayout.vue'),
    meta: { title: '客户自助端', roles: ['owner'] },
    children: [
      {
        path: 'pet',
        name: 'OwnerPet',
        component: () => import('../views/owner/pet/index.vue'),
        meta: { title: '我的宠物', icon: 'pet' }
      },
      {
        path: 'health',
        name: 'OwnerHealth',
        component: () => import('../views/owner/health/index.vue'),
        meta: { title: '健康记录', icon: 'health' }
      },
      {
        path: 'reserve',
        name: 'OwnerReserve',
        component: () => import('../views/owner/reserve/index.vue'),
        meta: { title: '预约申请', icon: 'reserve' }
      },
      {
        path: 'consult',
        name: 'OwnerConsult',
        component: () => import('../views/owner/consult/index.vue'),
        meta: { title: '在线咨询', icon: 'consult' }
      }
    ]
  },
  
  // 前台收银端路由
  {
    path: '/desk',
    name: 'Desk',
    component: () => import('../layout/DeskLayout.vue'),
    meta: { title: '前台收银端', roles: ['desk'] },
    children: [
      {
        path: 'register',
        name: 'DeskRegister',
        component: () => import('../views/desk/register/index.vue'),
        meta: { title: '挂号管理', icon: 'register' }
      },
      {
        path: 'charge',
        name: 'DeskCharge',
        component: () => import('../views/desk/charge/index.vue'),
        meta: { title: '收费管理', icon: 'charge' }
      },
      {
        path: 'customer',
        name: 'DeskCustomer',
        component: () => import('../views/desk/customer/index.vue'),
        meta: { title: '客户管理', icon: 'customer' }
      }
    ]
  },
  
  // 医生端路由
  {
    path: '/doctor',
    name: 'Doctor',
    component: () => import('../layout/DoctorLayout.vue'),
    meta: { title: '医生端', roles: ['doctor'] },
    children: [
      {
        path: 'accept',
        name: 'DoctorAccept',
        component: () => import('../views/doctor/accept/index.vue'),
        meta: { title: '接诊管理', icon: 'accept' }
      },
      {
        path: 'record',
        name: 'DoctorRecord',
        component: () => import('../views/doctor/record/index.vue'),
        meta: { title: '病历管理', icon: 'record' }
      },
      {
        path: 'prescription',
        name: 'DoctorPrescription',
        component: () => import('../views/doctor/prescription/index.vue'),
        meta: { title: '处方管理', icon: 'prescription' }
      },
      {
        path: 'consult',
        name: 'DoctorConsult',
        component: () => import('../views/doctor/consult/index.vue'),
        meta: { title: '在线咨询', icon: 'consult' }
      },
      {
        path: 'pet',
        name: 'DoctorPet',
        component: () => import('../views/doctor/pet/index.vue'),
        meta: { title: '宠物档案', icon: 'pet' }
      }
    ]
  },
  
  // 404路由必须放在最后
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    hidden: true
  }
]