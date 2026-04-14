import request from '@/utils/request'

// ==================== 宠物管理 ====================

/**
 * 获取宠物列表
 * @param {Object} params - { page, pageSize, keyword }
 */
export function getPetList(params) {
  return request({
    url: '/owner/pet/list',
    method: 'get',
    params
  })
}

/**
 * 获取宠物详情
 * @param {Number} petId - 宠物ID
 */
export function getPetDetail(petId) {
  return request({
    url: `/owner/pet/${petId}`,
    method: 'get'
  })
}

/**
 * 添加宠物
 * @param {Object} data - 宠物信息
 */
export function addPet(data) {
  return request({
    url: '/owner/pet',
    method: 'post',
    data
  })
}

/**
 * 更新宠物信息
 * @param {Number} petId - 宠物ID
 * @param {Object} data - 宠物信息
 */
export function updatePet(petId, data) {
  return request({
    url: `/owner/pet/${petId}`,
    method: 'put',
    data
  })
}

/**
 * 删除宠物
 * @param {Number} petId - 宠物ID
 */
export function deletePet(petId) {
  return request({
    url: `/owner/pet/${petId}`,
    method: 'delete'
  })
}

// ==================== 健康记录（就诊记录） ====================

/**
 * 获取就诊记录列表
 * @param {Object} params - { page, pageSize, petId, type }
 */
export function getHealthRecords(params) {
  return request({
    url: '/owner/health/list',
    method: 'get',
    params
  })
}

/**
 * 获取就诊记录详情
 * @param {Number} recordId - 记录ID
 */
export function getHealthRecordDetail(recordId) {
  return request({
    url: `/owner/health/${recordId}`,
    method: 'get'
  })
}

/**
 * 获取健康统计数据
 */
export function getHealthStatistics() {
  return request({
    url: '/owner/health/statistics',
    method: 'get'
  })
}

// ==================== 自填记录 ====================

/**
 * 获取自填记录列表
 * @param {Object} params - { page, pageSize, petId }
 */
export function getOwnerHealthRecords(params) {
  return request({
    url: '/owner/owner-health/list',
    method: 'get',
    params
  })
}

/**
 * 添加自填记录
 * @param {Object} data - { petId, type, title, content, recordDate }
 */
export function addOwnerHealthRecord(data) {
  return request({
    url: '/owner/owner-health',
    method: 'post',
    data
  })
}

/**
 * 删除自填记录
 * @param {Number} recordId - 记录ID
 */
export function deleteOwnerHealthRecord(recordId) {
  return request({
    url: `/owner/owner-health/${recordId}`,
    method: 'delete'
  })
}

// ==================== 预约管理 ====================

/**
 * 获取预约列表
 * @param {Object} params - { page, pageSize, status }
 */
export function getReserveList(params) {
  return request({
    url: '/owner/reserve/list',
    method: 'get',
    params
  })
}

/**
 * 获取预约详情
 * @param {Number} reserveId - 预约ID
 */
export function getReserveDetail(reserveId) {
  return request({
    url: `/owner/reserve/${reserveId}`,
    method: 'get'
  })
}

/**
 * 创建预约
 * @param {Object} data - { petId, doctorId, serviceType, reserveTime, remark }
 */
export function createReserve(data) {
  return request({
    url: '/owner/reserve',
    method: 'post',
    data
  })
}

/**
 * 取消预约
 * @param {Number} reserveId - 预约ID
 * @param {Object} data - { cancelReason }
 */
export function cancelReserve(reserveId, data) {
  return request({
    url: `/owner/reserve/${reserveId}/cancel`,
    method: 'put',
    data
  })
}

/**
 * 获取可预约的医生列表
 * @param {Object} params - { serviceType, date }
 */
export function getAvailableDoctors(params) {
  return request({
    url: '/owner/reserve/doctors',
    method: 'get',
    params
  })
}

/**
 * 获取可预约的时间段
 * @param {Object} params - { doctorId, date }
 */
export function getAvailableTimeSlots(params) {
  return request({
    url: '/owner/reserve/timeslots',
    method: 'get',
    params
  })
}

// ==================== 在线咨询 ====================

/**
 * 获取咨询列表
 * @param {Object} params - { page, pageSize, status }
 */
export function getConsultList(params) {
  return request({
    url: '/owner/consult/list',
    method: 'get',
    params
  })
}

/**
 * 获取咨询详情
 * @param {Number} consultId - 咨询ID
 */
export function getConsultDetail(consultId) {
  return request({
    url: `/owner/consult/${consultId}`,
    method: 'get'
  })
}

/**
 * 发起咨询
 * @param {Object} data - { petId, doctorId, title, content, images }
 */
export function createConsult(data) {
  return request({
    url: '/owner/consult',
    method: 'post',
    data
  })
}

/**
 * 回复咨询（追加追问）
 * @param {Number} consultId - 咨询ID
 * @param {Object} data - { content, images }
 */
export function replyConsult(consultId, data) {
  return request({
    url: `/owner/consult/${consultId}/reply`,
    method: 'post',
    data
  })
}

/**
 * 评价咨询
 * @param {Number} consultId - 咨询ID
 * @param {Object} data - { rating, comment }
 */
export function rateConsult(consultId, data) {
  return request({
    url: `/owner/consult/${consultId}/rate`,
    method: 'post',
    data
  })
}

// ==================== 订单管理 ====================

/**
 * 获取订单列表
 * @param {Object} params - { page, pageSize, payStatus }
 */
export function getOrderList(params) {
  return request({
    url: '/owner/order/list',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param {Number} orderId - 订单ID
 */
export function getOrderDetail(orderId) {
  return request({
    url: `/owner/order/${orderId}`,
    method: 'get'
  })
}

// ==================== 个人中心 ====================

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return request({
    url: '/owner/profile',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} data - { username, phone, email }
 */
export function updateUserInfo(data) {
  return request({
    url: '/owner/profile',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 */
export function changePassword(data) {
  return request({
    url: '/owner/profile/password',
    method: 'post',
    data
  })
}

/**
 * 绑定手机
 * @param {Object} data - { phone, code }
 */
export function bindPhone(data) {
  return request({
    url: '/owner/profile/phone',
    method: 'post',
    data
  })
}

/**
 * 绑定邮箱
 * @param {Object} data - { email, code }
 */
export function bindEmail(data) {
  return request({
    url: '/owner/profile/email',
    method: 'post',
    data
  })
}

/**
 * 发送验证码
 * @param {Object} data - { phone }
 */
export function sendVerifyCode(data) {
  return request({
    url: '/common/send-code',
    method: 'post',
    data
  })
}