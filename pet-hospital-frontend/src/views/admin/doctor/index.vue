<template>
  <div class="doctor-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="医生姓名">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入医生姓名" 
            clearable
            prefix-icon="User"
          />
        </el-form-item>
        
        <!-- 修复：科室选项值改为中文，与数据库匹配 -->
        <el-form-item label="科室">
          <el-select 
            v-model="searchForm.department" 
            placeholder="请选择科室" 
            clearable
            style="width: 140px"
          >
            <el-option label="内科" value="内科" />
            <el-option label="外科" value="外科" />
            <el-option label="皮肤科" value="皮肤科" />
            <el-option label="眼科" value="眼科" />
            <el-option label="牙科" value="牙科" />
            <el-option label="影像科" value="影像科" />
            <el-option label="口腔科" value="口腔科" />
            <el-option label="检验科" value="检验科" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="职称">
          <el-select 
            v-model="searchForm.title" 
            placeholder="请选择职称" 
            clearable
            style="width: 140px"
          >
            <el-option label="主任医师" value="主任医师" />
            <el-option label="副主任医师" value="副主任医师" />
            <el-option label="主治医师" value="主治医师" />
            <el-option label="执业医师" value="执业医师" />
            <el-option label="检验医师" value="检验医师" />
            <el-option label="医师" value="医师" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="RefreshRight">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格（保持不变） -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">医生列表</span>
            <el-tag type="info" effect="plain">共 {{ tableData.length }} 位医生</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增医生</el-button>
            <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20" class="doctor-grid" v-loading="loading">
        <el-col :span="8" v-for="doctor in tableData" :key="doctor.id">
          <el-card class="doctor-card" shadow="hover">
            <div class="doctor-header">
              <el-avatar :size="60" :src="doctor.avatar || defaultAvatar" />
              <div class="doctor-basic">
                <div class="doctor-name">{{ doctor.name }}</div>
                <div class="doctor-title">
                  <el-tag :type="getTitleType(doctor.title)" size="small" effect="light">
                    {{ doctor.title }}
                  </el-tag>
                  <el-tag type="info" size="small" effect="plain">{{ doctor.department }}</el-tag>
                </div>
              </div>
              <el-switch 
                v-model="doctor.status" 
                :active-value="1" 
                :inactive-value="0"
                @change="handleStatusChange(doctor)"
              />
            </div>
            
            <div class="doctor-info">
              <div class="info-item">
                <el-icon><Phone /></el-icon>
                <span>{{ doctor.phone }}</span>
              </div>
              <div class="info-item">
                <el-icon><Star /></el-icon>
                <span class="specialty">{{ doctor.specialty }}</span>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>接诊 {{ doctor.patientCount || 0 }} 位患者</span>
              </div>
            </div>
            
            <div class="doctor-actions">
              <el-button type="primary" link :icon="Edit" @click="handleEdit(doctor)">编辑</el-button>
              <el-button type="primary" link :icon="Calendar" @click="handleSchedule(doctor)">排班</el-button>
              <el-button type="danger" link :icon="Delete" @click="handleDelete(doctor)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无医生数据" />
    </el-card>

    <!-- 新增/编辑对话框 - 同样修复科室选项 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="700px"
      destroy-on-close
      class="doctor-dialog"
    >
      <el-form 
        :model="formData" 
        :rules="formRules" 
        ref="formRef" 
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="医生姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入医生姓名" prefix-icon="User" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室" prop="department">
              <el-select v-model="formData.department" placeholder="请选择科室" style="width: 100%">
                <el-option label="内科" value="内科" />
                <el-option label="外科" value="外科" />
                <el-option label="皮肤科" value="皮肤科" />
                <el-option label="眼科" value="眼科" />
                <el-option label="牙科" value="牙科" />
                <el-option label="影像科" value="影像科" />
                <el-option label="口腔科" value="口腔科" />
                <el-option label="检验科" value="检验科" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="formData.title" placeholder="请选择职称" style="width: 100%">
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="执业医师" value="执业医师" />
                <el-option label="检验医师" value="检验医师" />
                <el-option label="医师" value="医师" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" prefix-icon="Phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="专业特长" prop="specialty">
          <el-input 
            v-model="formData.specialty" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入专业特长，如：小动物内科疾病诊治、软组织手术等"
          />
        </el-form-item>

        <el-form-item label="医生简介">
          <el-input 
            v-model="formData.introduction" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入医生简介"
          />
        </el-form-item>

        <el-form-item label="医生头像">
          <el-upload
            class="avatar-uploader"
            :http-request="handleUpload"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="formData.avatar" :src="formData.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight, Download, Edit, Delete, Phone, Star, User, Calendar } from '@element-plus/icons-vue'
import { 
  getDoctorList, 
  addDoctor, 
  updateDoctor, 
  deleteDoctor, 
  updateDoctorStatus,
  exportDoctors 
} from '@/api/admin/admin'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增医生')
const formRef = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const searchForm = reactive({
  name: '',
  department: '',
  title: ''
})

const formData = reactive({
  id: null,
  name: '',
  department: '',
  title: '',
  phone: '',
  specialty: '',
  introduction: '',
  avatar: ''
})

const formRules = {
  name: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
  department: [{ required: true, message: '请选择科室', trigger: 'change' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  specialty: [{ required: true, message: '请输入专业特长', trigger: 'blur' }]
}

const tableData = ref([])

// 获取医生列表 - 修复参数传递
const fetchDoctorList = async () => {
  loading.value = true
  try {
    // 只传递非空参数
    const params = {}
    if (searchForm.name?.trim()) params.name = searchForm.name.trim()
    if (searchForm.department) params.department = searchForm.department
    if (searchForm.title) params.title = searchForm.title
    
    console.log('搜索参数:', params)
    
    const res = await getDoctorList(params)
    if (res.code === 200) {
      tableData.value = res.data || []
      console.log('获取到医生数据:', tableData.value.length, '条')
    } else {
      ElMessage.error(res.message || '获取医生列表失败')
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  } finally {
    loading.value = false
  }
}

const getTitleType = (title) => {
  const types = { 
    '主任医师': 'danger', 
    '副主任医师': 'warning', 
    '主治医师': 'success', 
    '执业医师': 'success',
    '检验医师': 'info',
    '医师': 'info' 
  }
  return types[title] || 'info'
}

const handleSearch = () => {
  fetchDoctorList()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.department = ''
  searchForm.title = ''
  fetchDoctorList()
}

const handleExport = async () => {
  try {
    const params = {}
    if (searchForm.name?.trim()) params.name = searchForm.name.trim()
    if (searchForm.department) params.department = searchForm.department
    if (searchForm.title) params.title = searchForm.title
    
    const res = await exportDoctors(params)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `医生列表_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增医生'
  Object.assign(formData, {
    id: null,
    name: '',
    department: '',
    title: '',
    phone: '',
    specialty: '',
    introduction: '',
    avatar: ''
  })
  dialogVisible.value = true
}

const handleEdit = (doctor) => {
  dialogTitle.value = '编辑医生'
  Object.assign(formData, { ...doctor })
  dialogVisible.value = true
}

const handleSchedule = (doctor) => {
  ElMessage.info(`查看 ${doctor.name} 的排班`)
}

const handleDelete = (doctor) => {
  ElMessageBox.confirm(`确定删除医生 "${doctor.name}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDoctor(doctor.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchDoctorList()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleStatusChange = async (doctor) => {
  try {
    const res = await updateDoctorStatus(doctor.id, doctor.status)
    if (res.code === 200) {
      ElMessage.success(`${doctor.name} 已${doctor.status === 1 ? '启用' : '禁用'}`)
    }
  } catch (error) {
    doctor.status = doctor.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const handleUpload = async (options) => {
  const uploadFormData = new FormData()
  uploadFormData.append('file', options.file)
  
  try {
    const res = await request.post('/common/upload', uploadFormData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (res.code === 200) {
      formData.avatar = res.data.url
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败')
    console.error(error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const api = formData.id ? updateDoctor : addDoctor
    const res = await api(formData)
    
    if (res.code === 200) {
      ElMessage.success(formData.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchDoctorList()
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchDoctorList()
})
</script>

<style scoped>
.doctor-management {
  padding: 20px;
  background: var(--bg-color);
  min-height: calc(100vh - 84px);
}

.search-card {
  margin-bottom: 20px;
  border-radius: var(--radius-large);
}

.search-card :deep(.el-card__body) {
  padding: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

/* 修复 el-select 宽度 */
.search-form :deep(.el-select) {
  width: 140px;
}

.table-card {
  border-radius: var(--radius-large);
}

.table-card :deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left .title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  gap: 10px;
}

.doctor-grid {
  margin-top: 10px;
}

.doctor-card {
  margin-bottom: 20px;
  border-radius: var(--radius-large);
  transition: all 0.3s;
}

.doctor-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-dark);
}

.doctor-card :deep(.el-card__body) {
  padding: 20px;
}

.doctor-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-lighter);
}

.doctor-basic {
  flex: 1;
}

.doctor-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.doctor-title {
  display: flex;
  gap: 8px;
}

.doctor-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-regular);
}

.info-item .el-icon {
  color: var(--primary-color);
  font-size: 14px;
}

.info-item .specialty {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doctor-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding-top: 15px;
  border-top: 1px solid var(--border-lighter);
}

.avatar-uploader {
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-base);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  transition: var(--transition-base);
}

.avatar-uploader:hover {
  border-color: var(--primary-color);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--text-placeholder);
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.doctor-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.doctor-dialog :deep(.el-dialog__body) {
  padding: 30px 20px;
}
</style>