import request from '@/utils/request'

/**
 * 个人中心相关 API
 */
export const profileModule = {
  // ========== 基本信息 ==========
  
  /**
   * 获取医生详细信息
   */
  getDoctorDetail() {
    return request({
      url: '/doctor/profile/detail',
      method: 'get'
    })
  },
  
  /**
   * 更新医生基本信息
   * @param {Object} data - 医生信息
   */
  updateDoctorInfo(data) {
    return request({
      url: '/doctor/profile/update',
      method: 'put',
      data
    })
  },
  
  /**
   * 上传头像
   * @param {FormData} formData - 包含文件的表单数据
   */
  uploadAvatar(formData) {
    return request({
      url: '/common/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // ========== 账号安全 ==========
  
  /**
   * 修改密码
   * @param {Object} data - { oldPassword, newPassword, confirmPassword }
   */
  changePassword(data) {
    return request({
      url: '/doctor/profile/change-password',
      method: 'put',
      data
    })
  },
  
  /**
   * 发送验证码
   * @param {String} phone - 手机号
   * @param {String} type - 类型：old（验证原手机）/ new（绑定新手机）/ email（绑定邮箱）
   */
  sendCode(phone, type = 'old') {
    return request({
      url: '/common/send-code',
      method: 'post',
      data: { phone, type }
    })
  },
  
  /**
   * 更换手机号
   * @param {Object} data - { oldCode, newPhone, newCode }
   */
  changePhone(data) {
    return request({
      url: '/doctor/profile/change-phone',
      method: 'put',
      data
    })
  },
  
  /**
   * 绑定/更换邮箱
   * @param {Object} data - { email, code }
   */
  bindEmail(data) {
    return request({
      url: '/doctor/profile/bind-email',
      method: 'put',
      data
    })
  },
  
  // ========== 执业资质 ==========
  
  /**
   * 获取资质信息
   */
  getQualification() {
    return request({
      url: '/doctor/profile/qualification',
      method: 'get'
    })
  },
  
  /**
   * 上传执业资质
   * @param {Object} data - 资质信息
   */
  uploadQualification(data) {
    return request({
      url: '/doctor/profile/qualification',
      method: 'post',
      data
    })
  },
  
  /**
   * 上传资质图片
   * @param {FormData} formData - 包含文件的表单数据
   */
  uploadQualificationImage(formData) {
    return request({
      url: '/common/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // ========== 统计数据 ==========
  
  /**
   * 获取医生统计数据（接诊数、咨询数、处方数）
   */
  getStatistics() {
    return request({
      url: '/doctor/statistics',
      method: 'get'
    })
  }
}