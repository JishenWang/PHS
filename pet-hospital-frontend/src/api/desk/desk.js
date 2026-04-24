import request from '@/utils/request'

const LS_KEYS = {
  customers: 'phs_desk_customers',
  registers: 'phs_desk_registers',
  charges: 'phs_desk_charges',
  orders: 'phs_desk_orders',
  reserves: 'phs_desk_reserves',
  messages: 'phs_desk_messages',
  doctors: 'phs_desk_doctors'
}

const RegisterStatus = {
  WAITING: 'WAITING',
  IN_PROGRESS: 'IN_PROGRESS',
  DONE: 'DONE',
  CANCELED: 'CANCELED'
}

const ReserveStatus = {
  BOOKED: 'BOOKED',
  VERIFIED: 'VERIFIED',
  CANCELED: 'CANCELED'
}

const ChargeStatus = {
  PENDING: 'PENDING',
  PAID: 'PAID',
  REFUNDING: 'REFUNDING',
  REFUNDED: 'REFUNDED'
}

const DoctorStatus = {
  FREE: 'FREE',
  BUSY: 'BUSY',
  REST: 'REST'
}
const DEFAULT_DOCTORS = [
  { id: 'd_3001', name: '王医生', status: DoctorStatus.FREE },
  { id: 'd_3002', name: '刘医生', status: DoctorStatus.BUSY },
  { id: 'd_3003', name: '陈医生', status: DoctorStatus.REST }
]

const PayMethods = ['现金', '微信', '支付宝', '刷卡']

function nowIso() {
  return new Date().toISOString()
}

function genId(prefix) {
  return `${prefix}_${Date.now()}_${Math.random().toString(16).slice(2, 10)}`
}

function safeJsonParse(str, fallback) {
  try {
    return JSON.parse(str)
  } catch {
    return fallback
  }
}

function lsGetArray(key) {
  const raw = localStorage.getItem(key)
  const arr = safeJsonParse(raw, [])
  return Array.isArray(arr) ? arr : []
}

function lsSetArray(key, arr) {
  localStorage.setItem(key, JSON.stringify(arr))
}

function normalizeDoctorList(list = []) {
  if (!Array.isArray(list)) return []
  return list
    .filter(x => x && (x.id || x.name))
    .map(x => {
      const status = [DoctorStatus.FREE, DoctorStatus.BUSY, DoctorStatus.REST].includes(x.status) ? x.status : DoctorStatus.FREE
      return { id: String(x.id || genId('doc')), name: String(x.name || '医生'), status }
    })
}

function includesLike(haystack, needle) {
  if (!needle) return true
  const h = String(haystack ?? '').toLowerCase()
  const n = String(needle).toLowerCase()
  return h.includes(n)
}

function paginate(list, page = 1, pageSize = 10) {
  const p = Number(page) || 1
  const ps = Number(pageSize) || 10
  const start = (p - 1) * ps
  return {
    list: list.slice(start, start + ps),
    total: list.length,
    page: p,
    pageSize: ps
  }
}

async function tryRequestOrFallback(requester, fallback) {
  try {
    const data = await requester()
    if (data !== undefined && data !== null) return { data, source: 'api' }
  } catch (error) {
    // 只有“后端不可达”（断网/超时/服务未启动）才切 mock，避免接口报错却显示成功
    if (!shouldFallbackToMock(error)) {
      throw error
    }
  }
  return { data: await fallback(), source: 'mock' }
}

function shouldFallbackToMock(error) {
  if (!error) return true
  // axios: 有 response 说明后端已返回，必须按真实错误处理
  if (error.response) return false
  // 请求超时 / 网络中断 / 服务不可达，允许切到本地 mock
  const code = String(error.code || '').toUpperCase()
  if (code === 'ECONNABORTED' || code === 'ERR_NETWORK') return true
  const msg = String(error.message || '').toLowerCase()
  return msg.includes('network') || msg.includes('timeout') || msg.includes('failed to fetch')
}

function seedOnce() {
  if (lsGetArray(LS_KEYS.customers).length === 0) {
    lsSetArray(LS_KEYS.customers, [
      {
        id: 'c_1001',
        name: '张三',
        phone: '13800000001',
        address: '北京市朝阳区',
        isTemp: false,
        pets: [
          {
            id: 'p_2001',
            name: '豆豆',
            species: '猫',
            gender: '母',
            age: 2,
            allergyHistory: '海鲜过敏',
            healthRecords: ['2026-03-12: 体检正常', '2026-04-01: 疫苗接种完成']
          }
        ]
      },
      {
        id: 'c_1002',
        name: '李四',
        phone: '13800000002',
        address: '上海市浦东新区',
        isTemp: false,
        pets: [
          {
            id: 'p_2002',
            name: '皮皮',
            species: '狗',
            gender: '公',
            age: 4,
            allergyHistory: '',
            healthRecords: ['2026-02-08: 驱虫完成']
          }
        ]
      }
    ])
  }

  if (lsGetArray(LS_KEYS.reserves).length === 0) {
    const t = nowIso()
    lsSetArray(LS_KEYS.reserves, [
      {
        id: 'res_1',
        customerId: 'c_1001',
        customerName: '张三',
        phone: '13800000001',
        petId: 'p_2001',
        petName: '豆豆',
        doctorId: 'd_3001',
        doctorName: '王医生',
        serviceType: '普通门诊',
        reserveTime: t,
        status: ReserveStatus.BOOKED,
        createdAt: t,
        updatedAt: t
      }
    ])
  }

  if (lsGetArray(LS_KEYS.registers).length === 0) lsSetArray(LS_KEYS.registers, [])
  if (lsGetArray(LS_KEYS.charges).length === 0) lsSetArray(LS_KEYS.charges, [])
  if (lsGetArray(LS_KEYS.orders).length === 0) lsSetArray(LS_KEYS.orders, [])
  if (lsGetArray(LS_KEYS.messages).length === 0) {
    lsSetArray(LS_KEYS.messages, [
      { id: genId('msg'), type: 'notice', content: '管理员通知：请核对今日收费', read: false, createdAt: nowIso() }
    ])
  }
  if (lsGetArray(LS_KEYS.doctors).length === 0) {
    lsSetArray(LS_KEYS.doctors, DEFAULT_DOCTORS)
  }
}

seedOnce()

export function getDeskEnums() {
  return {
    RegisterStatus,
    ReserveStatus,
    ChargeStatus,
    DoctorStatus,
    payMethods: PayMethods,
    doctors: normalizeDoctorList(lsGetArray(LS_KEYS.doctors))
  }
}

export async function queryCustomers(params = {}) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: '/desk/customers', method: 'get', params }),
    async () => {
      const { phone, name, petName, keyword, page = 1, pageSize = 10 } = params
      const list = lsGetArray(LS_KEYS.customers).filter(c => {
        const hitPhone = !phone || includesLike(c.phone, phone)
        const hitName = !name || includesLike(c.name, name)
        const hitPet = !petName || (c.pets || []).some(p => includesLike(p.name, petName))
        const hitKeyword =
          !keyword ||
          includesLike(c.name, keyword) ||
          includesLike(c.phone, keyword) ||
          (c.pets || []).some(p => includesLike(p.name, keyword))
        return hitPhone && hitName && hitPet && hitKeyword
      })
      return paginate(list, page, pageSize)
    }
  )
}

export async function createTempCustomer(payload) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: '/desk/customers/temp', method: 'post', data: payload }),
    async () => {
      const customers = lsGetArray(LS_KEYS.customers)
      const customer = {
        id: genId('c'),
        name: payload.name,
        phone: payload.phone,
        address: payload.address || '',
        isTemp: true,
        pets: [
          {
            id: genId('p'),
            name: payload.petName,
            species: payload.petSpecies || '未知',
            gender: payload.petGender || '未知',
            age: Number(payload.petAge || 0),
            allergyHistory: payload.allergyHistory || '',
            healthRecords: []
          }
        ]
      }
      customers.unshift(customer)
      lsSetArray(LS_KEYS.customers, customers)
      return customer
    }
  )
}

export async function getReserveList(params = {}) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: '/desk/reserves', method: 'get', params }),
    async () => {
      const { status, keyword, page = 1, pageSize = 10 } = params
      const list = lsGetArray(LS_KEYS.reserves)
        .filter(r => (!status || r.status === status) && (!keyword || includesLike(`${r.customerName}${r.petName}${r.phone}`, keyword)))
        .sort((a, b) => new Date(b.reserveTime).getTime() - new Date(a.reserveTime).getTime())
      return paginate(list, page, pageSize)
    }
  )
}

export async function verifyReserve(reserveId) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: `/desk/reserves/${encodeURIComponent(reserveId)}/verify`, method: 'post' }),
    async () => {
      const reserves = lsGetArray(LS_KEYS.reserves)
      const idx = reserves.findIndex(r => r.id === reserveId)
      if (idx < 0) throw new Error('预约不存在')
      reserves[idx].status = ReserveStatus.VERIFIED
      reserves[idx].updatedAt = nowIso()
      lsSetArray(LS_KEYS.reserves, reserves)
      const r = reserves[idx]
      const registerNo = `RG${Date.now()}`
      return createRegister({
        customerId: r.customerId,
        customerName: r.customerName,
        phone: r.phone,
        petId: r.petId,
        petName: r.petName,
        petSpecies: r.petSpecies || '',
        doctorId: r.doctorId,
        doctorName: r.doctorName,
        reason: '预约到店核销',
        serviceType: r.serviceType,
        registerNo
      })
    }
  )
}

export async function createRegister(payload) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: '/desk/registers', method: 'post', data: payload }),
    async () => {
      const createdAt = nowIso()
      const register = {
        id: genId('reg'),
        registerNo: payload.registerNo || `RG${Date.now()}`,
        customerId: payload.customerId,
        customerName: payload.customerName,
        phone: payload.phone,
        petId: payload.petId,
        petName: payload.petName,
        petSpecies: payload.petSpecies || '',
        doctorId: payload.doctorId,
        doctorName: payload.doctorName,
        serviceType: payload.serviceType || '普通门诊',
        reason: payload.reason || '',
        visitTime: payload.visitTime || createdAt,
        status: RegisterStatus.WAITING,
        createdAt,
        updatedAt: createdAt
      }
      const registers = lsGetArray(LS_KEYS.registers)
      registers.unshift(register)
      lsSetArray(LS_KEYS.registers, registers)
      pushMessage(`新挂号单 ${register.registerNo} 已创建`)
      return register
    }
  )
}

export async function getRegisterList(params = {}) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: '/desk/registers', method: 'get', params }),
    async () => {
      const { status, doctorId, keyword, page = 1, pageSize = 10 } = params
      const list = lsGetArray(LS_KEYS.registers)
        .filter(r => {
          const hitStatus = !status || r.status === status
          const hitDoctor = !doctorId || r.doctorId === doctorId
          const hitKeyword =
            !keyword ||
            includesLike(r.registerNo, keyword) ||
            includesLike(r.customerName, keyword) ||
            includesLike(r.petName, keyword) ||
            includesLike(r.phone, keyword)
          return hitStatus && hitDoctor && hitKeyword
        })
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      return paginate(list, page, pageSize)
    }
  )
}

export async function updateRegisterStatus(id, nextStatus) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: `/desk/registers/${encodeURIComponent(id)}/status`, method: 'put', data: { status: nextStatus } }),
    async () => {
      const registers = lsGetArray(LS_KEYS.registers)
      const idx = registers.findIndex(r => r.id === id)
      if (idx < 0) throw new Error('挂号单不存在')
      registers[idx].status = nextStatus
      registers[idx].updatedAt = nowIso()
      lsSetArray(LS_KEYS.registers, registers)
      if (nextStatus === RegisterStatus.DONE) ensureChargeForRegister(registers[idx])
      return registers[idx]
    }
  )
}

function ensureChargeForRegister(register) {
  const charges = lsGetArray(LS_KEYS.charges)
  if (charges.some(c => c.registerId === register.id)) return
  charges.unshift({
    id: genId('chg'),
    orderNo: `OD${Date.now()}`,
    registerId: register.id,
    registerNo: register.registerNo,
    customerName: register.customerName,
    phone: register.phone,
    petName: register.petName,
    petSpecies: register.petSpecies || '',
    doctorName: register.doctorName,
    detail: [
      { name: '诊疗服务费', amount: 50 }
    ],
    subtotal: 50,
    discount: 0,
    reduction: 0,
    adjustAmount: 0,
    adjustReason: '',
    total: 50,
    status: ChargeStatus.PENDING,
    payMethod: '',
    paidAt: '',
    refundStatus: '',
    refundReason: '',
    createdAt: nowIso(),
    updatedAt: nowIso()
  })
  lsSetArray(LS_KEYS.charges, charges)
  pushMessage(`医生处方/服务单已接收，待收费：${register.registerNo}`)
}

export async function getChargeList(params = {}) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: '/desk/charges', method: 'get', params }),
    async () => {
      const { status, keyword, page = 1, pageSize = 10 } = params
      const list = lsGetArray(LS_KEYS.charges)
        .filter(c => (!status || c.status === status) && (!keyword || includesLike(`${c.orderNo}${c.customerName}${c.petName}${c.phone}${c.doctorName}`, keyword)))
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      return paginate(list, page, pageSize)
    }
  )
}

export async function updateChargePricing(id, payload) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: `/desk/charges/${encodeURIComponent(id)}/pricing`, method: 'put', data: payload }),
    async () => {
      const charges = lsGetArray(LS_KEYS.charges)
      const idx = charges.findIndex(c => c.id === id)
      if (idx < 0) throw new Error('收费单不存在')
      const detail = Array.isArray(payload.detail) ? payload.detail : charges[idx].detail
      const subtotal = detail.reduce((s, x) => s + Number(x.amount || 0), 0)
      const discount = Number(payload.discount || 0)
      const reduction = Number(payload.reduction || 0)
      const adjustAmount = Number(payload.adjustAmount || 0)
      const total = Math.max(subtotal - discount - reduction + adjustAmount, 0)
      charges[idx] = {
        ...charges[idx],
        detail,
        subtotal,
        discount,
        reduction,
        adjustAmount,
        adjustReason: payload.adjustReason || '',
        total,
        updatedAt: nowIso()
      }
      lsSetArray(LS_KEYS.charges, charges)
      return charges[idx]
    }
  )
}

export async function confirmCharge(id, payMethod) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: `/desk/charges/${encodeURIComponent(id)}/confirm`, method: 'post', data: { payMethod } }),
    async () => {
      const charges = lsGetArray(LS_KEYS.charges)
      const idx = charges.findIndex(c => c.id === id)
      if (idx < 0) throw new Error('收费单不存在')
      charges[idx].status = ChargeStatus.PAID
      charges[idx].payMethod = payMethod || '现金'
      charges[idx].paidAt = nowIso()
      charges[idx].updatedAt = nowIso()
      lsSetArray(LS_KEYS.charges, charges)

      const orders = lsGetArray(LS_KEYS.orders)
      orders.unshift({
        id: genId('ord'),
        orderNo: charges[idx].orderNo,
        chargeId: charges[idx].id,
        customerName: charges[idx].customerName,
        doctorName: charges[idx].doctorName,
        total: charges[idx].total,
        payMethod: charges[idx].payMethod,
        status: 'PAID',
        createdAt: nowIso(),
        billDetail: charges[idx].detail
      })
      lsSetArray(LS_KEYS.orders, orders)
      return charges[idx]
    }
  )
}

export async function getOrderList(params = {}) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: '/desk/orders', method: 'get', params }),
    async () => {
      const { keyword, page = 1, pageSize = 10 } = params
      const list = lsGetArray(LS_KEYS.orders)
        .filter(o => !keyword || includesLike(`${o.orderNo}${o.customerName}${o.doctorName}${o.payMethod}`, keyword))
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      return paginate(list, page, pageSize)
    }
  )
}

export async function applyRefund(chargeId, reason) {
  seedOnce()
  return tryRequestOrFallback(
    () => request({ url: `/desk/charges/${encodeURIComponent(chargeId)}/refund`, method: 'post', data: { reason } }),
    async () => {
      const charges = lsGetArray(LS_KEYS.charges)
      const idx = charges.findIndex(c => c.id === chargeId)
      if (idx < 0) throw new Error('订单不存在')
      charges[idx].status = ChargeStatus.REFUNDING
      charges[idx].refundStatus = 'PENDING_ADMIN_APPROVAL'
      charges[idx].refundReason = reason || ''
      charges[idx].updatedAt = nowIso()
      lsSetArray(LS_KEYS.charges, charges)
      pushMessage(`退款申请待审核：${charges[idx].orderNo}`)
      return charges[idx]
    }
  )
}

export async function getDoctorStatusList() {
  seedOnce()
  const result = await tryRequestOrFallback(
    () => request({ url: '/desk/doctors/status', method: 'get' }),
    async () => normalizeDoctorList(lsGetArray(LS_KEYS.doctors))
  )
  const payload = result.data
  let doctorList = []
  if (Array.isArray(payload)) {
    doctorList = payload
  } else if (Array.isArray(payload?.data)) {
    doctorList = payload.data
  } else if (Array.isArray(payload?.list)) {
    doctorList = payload.list
  }
  const normalized = normalizeDoctorList(doctorList)
  if (normalized.length >= 2) {
    lsSetArray(LS_KEYS.doctors, normalized)
    result.data = normalized
    return result
  }
  lsSetArray(LS_KEYS.doctors, DEFAULT_DOCTORS)
  result.data = DEFAULT_DOCTORS
  result.source = 'mock'
  return result
}

export async function getTodayStats() {
  seedOnce()
  const today = new Date().toDateString()
  const registers = lsGetArray(LS_KEYS.registers)
  const charges = lsGetArray(LS_KEYS.charges)
  const todayRegisters = registers.filter(r => new Date(r.createdAt).toDateString() === today)
  const todayDone = registers.filter(r => r.status === RegisterStatus.DONE && new Date(r.updatedAt).toDateString() === today)
  const todayPaid = charges.filter(c => c.status === ChargeStatus.PAID && c.paidAt && new Date(c.paidAt).toDateString() === today)
  return {
    data: {
      registerCount: todayRegisters.length,
      doneCount: todayDone.length,
      chargeTotal: todayPaid.reduce((s, x) => s + Number(x.total || 0), 0)
    },
    source: 'mock'
  }
}

export async function getMessages() {
  seedOnce()
  return { data: lsGetArray(LS_KEYS.messages), source: 'mock' }
}

export async function markMessageRead(id) {
  seedOnce()
  const list = lsGetArray(LS_KEYS.messages)
  const idx = list.findIndex(x => x.id === id)
  if (idx >= 0) list[idx].read = true
  lsSetArray(LS_KEYS.messages, list)
  return { data: true, source: 'mock' }
}

function pushMessage(content) {
  const list = lsGetArray(LS_KEYS.messages)
  list.unshift({ id: genId('msg'), type: 'biz', content, read: false, createdAt: nowIso() })
  lsSetArray(LS_KEYS.messages, list)
}

export function buildRegisterExportCsv(list = []) {
  const header = ['挂号编号', '客户', '手机号', '宠物', '医生', '服务类型', '主诉', '状态', '时间']
  const rows = list.map(x => [x.registerNo, x.customerName, x.phone, x.petName, x.doctorName, x.serviceType, x.reason, x.status, x.visitTime])
  return [header, ...rows].map(r => r.map(x => `"${String(x ?? '').replaceAll('"', '""')}"`).join(',')).join('\n')
}

export function buildChargeExportText(charge) {
  const details = (charge?.detail || []).map(x => `${x.name}: ${Number(x.amount || 0).toFixed(2)}`).join('\n')
  return [
    '【宠物医院收费小票】',
    `订单号：${charge?.orderNo || ''}`,
    `客户：${charge?.customerName || ''} 手机：${charge?.phone || ''}`,
    `宠物：${charge?.petName || ''} 医生：${charge?.doctorName || ''}`,
    details,
    `小计：${Number(charge?.subtotal || 0).toFixed(2)}`,
    `优惠：${Number(charge?.discount || 0).toFixed(2)}`,
    `满减：${Number(charge?.reduction || 0).toFixed(2)}`,
    `调整：${Number(charge?.adjustAmount || 0).toFixed(2)} (${charge?.adjustReason || '-'})`,
    `应收：${Number(charge?.total || 0).toFixed(2)}`,
    `支付方式：${charge?.payMethod || ''}`,
    `支付时间：${charge?.paidAt || ''}`
  ].join('\n')
}
