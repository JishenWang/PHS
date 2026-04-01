export default [
  {
    path: '/doctor',
    redirect: '/doctor/accept'
  },
  {
    path: '/doctor/accept',
    name: 'DoctorAccept',
    component: () => import('@/views/doctor/accept/index.vue'),
    meta: { title: '接诊列表' }
  },
  {
    path: '/doctor/pet',
    name: 'DoctorPet',
    component: () => import('@/views/doctor/pet/index.vue'),
    meta: { title: '宠物档案' }
  },
  {
    path: '/doctor/record',
    name: 'DoctorRecord',
    component: () => import('@/views/doctor/record/index.vue'),
    meta: { title: '病历记录' }
  },
  {
    path: '/doctor/prescription',
    name: 'DoctorPrescription',
    component: () => import('@/views/doctor/prescription/index.vue'),
    meta: { title: '处方开具' }
  },
  {
    path: '/doctor/consult',
    name: 'DoctorConsult',
    component: () => import('@/views/doctor/consult/index.vue'),
    meta: { title: '咨询回复' }
  }
]