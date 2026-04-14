import request from '@/utils/request'

// 前台客户查询
export function queryCustomers(params) {
  return request({
    url: '/desk/customers',
    method: 'get',
    params
  })
}

// 前台挂号列表
export function getRegisterList(params) {
  return request({
    url: '/desk/registers',
    method: 'get',
    params
  })
}

// 前台收费列表
export function getChargeList(params) {
  return request({
    url: '/desk/charges',
    method: 'get',
    params
  })
}
