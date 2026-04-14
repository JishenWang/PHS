import request from '@/utils/request'

/**
 * 医生端API接口封装
 * 按业务模块分类：接诊管理、宠物档案、病历管理、处方管理、在线咨询、个人统计
 */

// ==================== 接诊管理模块 ====================
export const acceptModule = {
  // 获取待接诊列表
  getWaitAcceptList: (params) => request.get('/doctor/accept/list', { params }),
  
  // 获取宠物详情
  getPetDetail: (petId) => request.get(`/doctor/pet/${petId}`),
  
  // 更新接诊状态
  updateAcceptStatus: (data) => request.post('/doctor/accept/status', data),
  
  // 获取待接诊数量（用于顶部导航栏红点）
  getWaitAcceptCount: () => request.get('/doctor/accept/count')
}

// ==================== 病历管理模块 ====================
export const recordModule = {
  // 创建病历
  createMedicalRecord: (data) => request.post('/doctor/record', data),
  
  // 更新病历
  updateMedicalRecord: (data) => request.put('/doctor/record', data),
  
  // 获取病历列表
  getMedicalRecordList: (params) => request.get('/doctor/record/list', { params }),
  
  // 获取病历详情
  getMedicalRecordDetail: (recordId) => request.get(`/doctor/record/${recordId}`),
  
  // 导出处历
  exportMedicalRecord: (params) => request.get('/doctor/record/export', { 
    params, 
    responseType: 'blob' 
  })
}

// ==================== 处方管理模块 ====================
export const prescriptionModule = {
  // 获取药品列表
  getDrugList: (params) => request.get('/doctor/prescription/drugs', { params }),
  
  // 获取服务项目列表
  getServiceList: (params) => request.get('/doctor/prescription/services', { params }),
  
  // 创建处方
  createPrescription: (data) => request.post('/doctor/prescription', data),
  
  // 提交处方
  submitPrescription: (prescriptionId) => request.post(`/doctor/prescription/${prescriptionId}/submit`),
  
  // 获取处方列表
  getPrescriptionList: (params) => request.get('/doctor/prescription/list', { params }),
  
  // 获取处方详情
  getPrescriptionDetail: (prescriptionId) => request.get(`/doctor/prescription/${prescriptionId}`),
  
  // 打印处方
  printPrescription: (prescriptionId) => request.get(`/doctor/prescription/${prescriptionId}/print`, {
    responseType: 'blob'
  })
}

// ==================== 在线咨询模块 ====================
export const consultModule = {
  // 获取未读咨询数量
  getUnreadConsultCount: () => request.get('/doctor/consult/unread-count'),
  
  // 获取咨询列表
  getConsultList: (params) => request.get('/doctor/consult/list', { params }),
  
  // 获取咨询详情
  getConsultDetail: (consultId) => request.get(`/doctor/consult/${consultId}`),
  
  // 回复咨询
  replyConsult: (data) => request.post('/doctor/consult/reply', data)
}

// ==================== 医生个人模块 ====================
export const doctorModule = {
  // 获取医生信息
  getDoctorInfo: () => request.get('/doctor/info'),
  
  // 更新医生信息
  updateDoctorInfo: (data) => request.put('/doctor/info', data),
  
  // 更新接诊状态
  updateDoctorStatus: (data) => request.put('/doctor/status', data),
  
  // 获取认证状态
  getAuthStatus: () => request.get('/doctor/auth-status'),
  
  // 获取个人统计
  getDoctorStatistics: (params) => request.get('/doctor/statistics', { params }),
  
  // 获取接诊统计（图表数据）
  getAcceptStatistics: (params) => request.get('/doctor/statistics/accept', { params })
}

// 统一导出
export default {
  accept: acceptModule,
  record: recordModule,
  prescription: prescriptionModule,
  consult: consultModule,
  doctor: doctorModule
}