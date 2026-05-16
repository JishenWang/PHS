import request from '@/utils/request'

export function createHospitalization(data) {
  return request({
    url: '/hospitalization',
    method: 'post',
    data
  })
}

export function getHospitalizationPage(params) {
  return request({
    url: '/hospitalization/page',
    method: 'get',
    params
  })
}

export function getHospitalizationById(id) {
  return request({
    url: `/hospitalization/${id}`,
    method: 'get'
  })
}

export function dischargeHospitalization(id, bedFeePerDay) {
  return request({
    url: `/hospitalization/discharge/${id}`,
    method: 'put',
    params: { bedFeePerDay }
  })
}

export function interimCharge(id) {
  return request({
    url: `/hospitalization/charge/${id}`,
    method: 'post'
  })
}

export function getHospitalizationPrescriptions(id) {
  return request({
    url: `/hospitalization/prescriptions/${id}`,
    method: 'get'
  })
}
