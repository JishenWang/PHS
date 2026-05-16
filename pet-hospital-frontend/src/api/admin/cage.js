import request from '@/utils/request'

export function getCagePage(params) {
  return request({
    url: '/cage/page',
    method: 'get',
    params
  })
}

export function getAvailableCages() {
  return request({
    url: '/cage/available',
    method: 'get'
  })
}

export function getCageById(id) {
  return request({
    url: `/cage/${id}`,
    method: 'get'
  })
}

export function addCage(data) {
  return request({
    url: '/cage',
    method: 'post',
    data
  })
}

export function updateCage(data) {
  return request({
    url: '/cage',
    method: 'put',
    data
  })
}

export function deleteCage(id) {
  return request({
    url: `/cage/${id}`,
    method: 'delete'
  })
}
