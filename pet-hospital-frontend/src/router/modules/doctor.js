/**
 * 医生端路由配置
 * 适配团队公共路由结构（无嵌套 layout）
 */

export default [
  {
    path: '/doctor/accept',
    name: 'DoctorAccept',
    component: () => import('@/views/doctor/accept/index.vue'),
    meta: {
      title: '接诊列表',
      icon: 'User',
      role: 'DOCTOR',
      permission: 'accept:view'
    }
  },
  {
    path: '/doctor/pet',
    name: 'DoctorPet',
    component: () => import('@/views/doctor/pet/index.vue'),
    meta: {
      title: '宠物档案',
      icon: 'FirstAidKit',
      role: 'DOCTOR',
      permission: 'pet:view'
    }
  },
  {
    path: '/doctor/record',
    name: 'DoctorRecord',
    component: () => import('@/views/doctor/record/index.vue'),
    meta: {
      title: '病历记录',
      icon: 'Document',
      role: 'DOCTOR',
      permission: 'record:view'
    }
  },
  {
    path: '/doctor/prescription',
    name: 'DoctorPrescription',
    component: () => import('@/views/doctor/prescription/index.vue'),
    meta: {
      title: '处方开具',
      icon: 'Tickets',
      role: 'DOCTOR',
      permission: 'prescription:view'
    }
  },
  {
    path: '/doctor/consult',
    name: 'DoctorConsult',
    component: () => import('@/views/doctor/consult/index.vue'),
    meta: {
      title: '咨询回复',
      icon: 'ChatDotRound',
      role: 'DOCTOR',
      permission: 'consult:view'
    }
  },
  // ========== 新增：个人中心 ==========
  {
    path: '/doctor/profile',
    name: 'DoctorProfile',
    component: () => import('@/views/doctor/profile/index.vue'),
    meta: {
      title: '个人中心',
      icon: 'User',
      role: 'DOCTOR',
      permission: 'profile:view',
      hidden: true  // 不在侧边栏显示
    }
  },
  // ========== 新增：系统设置 ==========
  {
    path: '/doctor/settings',
    name: 'DoctorSettings',
    component: () => import('@/views/doctor/settings/index.vue'),
    meta: {
      title: '系统设置',
      icon: 'Setting',
      role: 'DOCTOR',
      permission: 'settings:view',
      hidden: true  // 不在侧边栏显示
    }
  }
]