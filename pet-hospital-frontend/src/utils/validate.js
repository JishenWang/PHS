// 手机号验证
export function isValidPhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

// 邮箱验证
export function isValidEmail(email) {
  const reg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/
  return reg.test(email)
}

// 身份证号验证
export function isValidIdCard(idCard) {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return reg.test(idCard)
}

// 非空验证
export function isNotEmpty(value) {
  return value !== null && value !== undefined && value !== ''
}