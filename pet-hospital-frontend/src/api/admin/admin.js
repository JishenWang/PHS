import request from '@/utils/request'

// ==================== 认证相关 ====================
export function login(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

// ==================== 仪表盘 ====================
export function getDashboardData() {
  return request({
    url: '/admin/dashboard',
    method: 'get'
  })
}

export function getTrendData(period) {
  return request({
    url: '/admin/dashboard/trend',
    method: 'get',
    params: { period }
  })
}

export function getPetTypeDistribution() {
  return request({
    url: '/admin/dashboard/pet-distribution',
    method: 'get'
  })
}

export function getRecentActivities() {
  return request({
    url: '/admin/dashboard/activities',
    method: 'get'
  })
}

export function getDeptDistribution() {
  return request({
    url: '/admin/dashboard/dept-distribution',
    method: 'get'
  })
}

export function getAppointmentStatusDistribution() {
  return request({
    url: '/admin/dashboard/appointment-status',
    method: 'get'
  })
}

export function getTodayAppointments() {
  return request({
    url: '/admin/dashboard/today-appointments',
    method: 'get'
  })
}

export function getNotices() {
  return request({
    url: '/admin/dashboard/notices',
    method: 'get'
  })
}

// ==================== 用户管理（不分页）====================
export function getUserList(keyword) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params: { keyword }
  })
}

export function addUser(data) {
  return request({
    url: '/admin/user/add',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/admin/user/update',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: '/admin/user/delete/' + id,
    method: 'delete'
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: '/admin/user/status',
    method: 'put',
    params: { id, status }
  })
}

export function exportUsers(params) {
  return request({
    url: '/admin/user/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// ==================== 医生管理（不分页）====================
export function getDoctorList(params) {
  return request({
    url: '/admin/doctor/list',
    method: 'get',
    params  // { name, department, title }
  })
}

export function addDoctor(data) {
  return request({
    url: '/admin/doctor/add',
    method: 'post',
    data
  })
}

export function updateDoctor(data) {
  return request({
    url: '/admin/doctor/update',
    method: 'put',
    data
  })
}

export function deleteDoctor(id) {
  return request({
    url: '/admin/doctor/delete/' + id,
    method: 'delete'
  })
}

export function updateDoctorStatus(id, status) {
  return request({
    url: '/admin/doctor/status',
    method: 'put',
    params: { id, status }
  })
}

export function exportDoctors(params) {
  return request({
    url: '/admin/doctor/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// ==================== 宠物管理（不分页）====================
export function getPetList(keyword) {
  return request({
    url: '/admin/pet/list',
    method: 'get',
    params: { keyword }
  })
}

export function addPet(data) {
  return request({
    url: '/admin/pet/add',
    method: 'post',
    data
  })
}

export function updatePet(data) {
  return request({
    url: '/admin/pet/update',
    method: 'put',
    data
  })
}

export function deletePet(id) {
  return request({
    url: '/admin/pet/delete/' + id,
    method: 'delete'
  })
}

export function exportPets(params) {
  return request({
    url: '/admin/pet/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// ==================== 系统配置 ====================
export function getBasicConfig() {
  return request({
    url: '/admin/system/config/basic',
    method: 'get'
  })
}

export function saveBasicConfig(data) {
  return request({
    url: '/admin/system/config/basic',
    method: 'put',
    data
  })
}

export function getBusinessConfig() {
  return request({
    url: '/admin/system/config/business',
    method: 'get'
  })
}

export function saveBusinessConfig(data) {
  return request({
    url: '/admin/system/config/business',
    method: 'put',
    data
  })
}

export function getNotificationConfig() {
  return request({
    url: '/admin/system/config/notification',
    method: 'get'
  })
}

export function saveNotificationConfig(data) {
  return request({
    url: '/admin/system/config/notification',
    method: 'put',
    data
  })
}

export function backupData() {
  return request({
    url: '/admin/system/backup',
    method: 'post'
  })
}

export function restoreData(data) {
  return request({
    url: '/admin/system/restore',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function clearCache() {
  return request({
    url: '/admin/system/cache',
    method: 'delete'
  })
}

export function resetSystem() {
  return request({
    url: '/admin/system/reset',
    method: 'post'
  })
}

export function getOperationLogs(params) {
  return request({
    url: '/admin/system/logs',
    method: 'get',
    params
  })
}

// ==================== 药品管理 ====================
export function getMedicineList(keyword) {
  return request({
    url: '/admin/medicine/list',
    method: 'get',
    params: { keyword }
  })
}

export function getMedicineDetail(id) {
  return request({
    url: '/admin/medicine/detail/' + id,
    method: 'get'
  })
}

export function addMedicine(data) {
  return request({
    url: '/admin/medicine',
    method: 'post',
    data
  })
}

export function updateMedicine(id, data) {
  return request({
    url: '/admin/medicine/' + id,
    method: 'put',
    data
  })
}

export function deleteMedicine(id) {
  return request({
    url: '/admin/medicine/' + id,
    method: 'delete'
  })
}

export function updateMedicineStatus(id, status) {
  return request({
    url: '/admin/medicine/status',
    method: 'put',
    params: { id, status }
  })
}

// ==================== 系统设置 ====================
export function getAdminVersion() {
  return request({
    url: '/admin/version',
    method: 'get'
  })
}

export function submitAdminFeedback(data) {
  return request({
    url: '/admin/feedback',
    method: 'post',
    data
  })
}
