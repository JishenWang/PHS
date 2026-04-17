import request from '@/utils/request'
import { useUserStore } from '@/store/user'

// 获取当前医生ID的辅助函数
const getDoctorId = () => {
  const userStore = useUserStore()
  return userStore.userInfo?.doctorId || userStore.userInfo?.id || ''
}

/**
 * 医生端API接口封装
 */

// ==================== 接诊管理模块 ====================
export const acceptModule = {
  // 获取待接诊列表
  getWaitAcceptList: (params) => request.get('/doctor/accept/list', { params }),
  
  // 新增：获取单个挂号单详情（通过ID）
  getRegisterDetail: (registerId) => request.get('/doctor/accept/registerDetail', { params: { registerId } }),
  
  // 获取宠物详情
  getPetDetail: (petId) => request.get('/doctor/pet/detail', { params: { petId } }),
  
  // 更新接诊状态
  updateAcceptStatus: (data) => request.put('/doctor/accept/status', null, { params: data }),
  
  // 获取待接诊数量
  getWaitAcceptCount: (doctorId) => request.get('/doctor/accept/count', { params: { doctorId } })
}

// ==================== 病历管理模块 ====================
// 病历管理模块
export const recordModule = {
  createMedicalRecord: (data) => request.post('/doctor/record', data),
  updateMedicalRecord: (data) => request.put('/doctor/record', data),  // PUT 请求，data 是请求体
  getMedicalRecordList: (params) => request.get('/doctor/record/list', { params }),
  getMedicalRecordDetail: (recordId) => request.get('/doctor/record/detail', { params: { recordId } }),
  exportMedicalRecord: (params) => request.get('/doctor/record/export', { params, responseType: 'blob' })
}

// ==================== 处方管理模块 ====================
export const prescriptionModule = {
  getDrugList: (keyword) => request.get('/doctor/drug/list', { params: { keyword } }),
  getServiceList: (keyword) => request.get('/doctor/service/list', { params: { keyword } }),
  createPrescription: (data) => request.post('/doctor/prescription', data),
  submitPrescription: (prescriptionId) => request.post('/doctor/prescription/submit', null, { params: { prescriptionId } }),
  getPrescriptionList: (params) => request.get('/doctor/prescription/list', { params }),
  getPrescriptionDetail: (prescriptionId) => request.get('/doctor/prescription/detail', { params: { prescriptionId } }),
  printPrescription: (prescriptionId) => request.get('/doctor/prescription/print', { params: { prescriptionId }, responseType: 'blob' })
}

// ==================== 在线咨询模块 ====================
export const consultModule = {
  getUnreadConsultCount: () => {
    const doctorId = getDoctorId()
    return request.get('/doctor/consult/unreadCount', { params: { doctorId } })
  },
  getConsultList: (params) => {
    const doctorId = getDoctorId()
    // 合并参数，确保传递doctorId
    return request.get('/doctor/consult/list', { 
      params: { 
        ...params, 
        doctorId: params?.doctorId || doctorId 
      } 
    })
  },
  getConsultDetail: (consultId) => request.get('/doctor/consult/detail', { params: { consultId } }),
  replyConsult: (data) => request.post('/doctor/consult/reply', data)
}

// ==================== 医生个人模块 ====================
export const doctorModule = {
  getDoctorInfo: () => {
    const doctorId = getDoctorId()
    return request.get('/doctor/info', { params: { doctorId } })
  },
  updateDoctorInfo: (data) => request.put('/doctor/info', data),
  updateDoctorStatus: (data) => {
    const doctorId = getDoctorId()
    return request.put('/doctor/status', { ...data, doctorId })
  },
  getAuthStatus: () => {
    const doctorId = getDoctorId()
    return request.get('/doctor/authStatus', { params: { doctorId } })
  },
  getDoctorStatistics: (params) => {
    const doctorId = getDoctorId()
    return request.get('/doctor/statistics', { params: { ...params, doctorId } })
  },
  getAcceptStatistics: (params) => request.get('/doctor/statistics/accept', { params })
}

export default {
  accept: acceptModule,
  record: recordModule,
  prescription: prescriptionModule,
  consult: consultModule,
  doctor: doctorModule
}