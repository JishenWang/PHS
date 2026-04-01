import request from '@/utils/request'

export function getAdminTest() {
  return request({
    url: '/admin/test/info',
    method: 'get'
  })
}