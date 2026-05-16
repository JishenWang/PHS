<template>
  <div class="consult-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ $t('consult.title') }}</h2>
        <p class="page-subtitle">{{ $t('consult.subtitle') }}</p>
      </div>
      <el-button type="primary" size="large" @click="handleCreate" class="add-btn">
        <el-icon><Plus /></el-icon>
        {{ $t('consult.startConsultation') }}
      </el-button>
    </div>

    <!-- Consultation List -->
    <div class="consult-list" v-loading="loading">
      <div v-for="item in consultList" :key="item.id" class="consult-card" @click="viewDetail(item.id)">
        <!-- Doctor Avatar and Basic Info -->
        <div class="consult-header">
          <div class="doctor-info">
            <div class="doctor-avatar">
              <el-avatar :size="48">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="online-dot" v-if="item.status === 'ongoing'"></span>
            </div>
            <div class="doctor-detail">
              <div class="doctor-name">{{ item.doctorName || $t('consult.awaitingAssignment') }}</div>
              <div class="doctor-title">{{ item.doctorTitle || $t('consult.licensedPhysician') }}</div>
            </div>
          </div>
          <div class="consult-meta">
            <div class="consult-status" :class="item.status">
              {{ item.status === 'ongoing' ? $t('consult.ongoing') : $t('consult.completed') }}
            </div>
            <div class="consult-time">{{ formatDate(item.createTime) }}</div>
          </div>
        </div>

        <!-- Consultation Content -->
        <div class="consult-body">
          <div class="consult-title">{{ item.title }}</div>
          <div class="consult-preview">{{ item.content }}</div>
          <div class="consult-tags">
            <span class="tag">
              <el-icon><ChatLineSquare /></el-icon>
              {{ item.replyCount || 0 }} {{ $t('consult.replies') }}
            </span>
            <span class="tag" v-if="item.petName">
              <el-icon><Avatar /></el-icon>
              {{ item.petName }}
            </span>
          </div>
        </div>

        <!-- Bottom Actions -->
        <div class="consult-footer">
          <div class="reply-badge" v-if="item.unreadCount > 0">
            <span class="unread-dot"></span>
            {{ item.unreadCount }} {{ $t('consult.newReplies') }}
          </div>
          <el-button type="primary" link class="view-btn">
            {{ $t('common.detail') }}
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && consultList.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <h3>{{ $t('consult.noConsultationRecords') }}</h3>
        <p>{{ $t('consult.noConsultationYet') }}</p>
        <el-button type="primary" @click="handleCreate">{{ $t('consult.startConsultation') }}</el-button>
      </div>
    </div>

    <!-- Start Consultation Dialog -->
    <el-dialog v-model="dialogVisible" :title="$t('consult.startConsultation')" width="520px" class="consult-dialog">
      <div class="dialog-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>{{ $t('consult.dialogTip') }}</span>
      </div>
      <el-form :model="form" label-width="100px" size="default">
        <el-form-item :label="$t('consult.selectPet')">
          <el-select v-model="form.petId" :placeholder="$t('consult.pleaseSelectPet')" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span>{{ pet.gender === 'male' ? '🐕' : '🐱' }}</span>
                <span>{{ pet.name }}</span>
                <span style="color: #999; font-size: 12px;">{{ pet.breed }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('consult.selectDoctor')">
          <el-select v-model="form.doctorId" :placeholder="$t('consult.pleaseSelectDoctor')" style="width: 100%">
            <el-option v-for="doc in doctorList" :key="doc.id" :label="doc.name" :value="doc.id">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span>👨‍⚕️</span>
                <span>{{ doc.name }}</span>
                <span style="color: #999; font-size: 12px;">{{ doc.title }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('consult.consultationTitle')">
          <el-input v-model="form.title" :placeholder="$t('consult.brieflySummarize')" />
        </el-form-item>
        <el-form-item :label="$t('consult.problemDescription')">
          <el-input 
            type="textarea" 
            v-model="form.content" 
            :placeholder="$t('consult.describeSymptoms')"
            :rows="6"
          />
        </el-form-item>
        <el-form-item :label="$t('consult.uploadImages')">
          <el-upload
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="3"
            list-type="picture-card"
            :file-list="fileList"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">{{ $t('consult.uploadTip') }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ $t('consult.submitConsultation') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus, User, ChatLineSquare, Avatar, ArrowRight, InfoFilled } from '@element-plus/icons-vue'

const { t } = useI18n()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const consultList = ref([])
const petList = ref([])
const dialogVisible = ref(false)
const fileList = ref([])

const doctorList = ref([])

const form = ref({
  petId: '',
  doctorId: '',
  title: '',
  content: '',
  images: []
})

// Get token
const getToken = () => {
  return localStorage.getItem('pet_hospital_token')
}

// Format date
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + ' ' + t('consult.minutesAgo')
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + ' ' + t('consult.hoursAgo')
  } else {
    return `${date.getMonth() + 1}/${date.getDate()}`
  }
}

// Load doctor list
const loadDoctorList = async () => {
  try {
    const token = getToken()
    const response = await fetch('/api/owner/reserve/doctors', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      const list = res.data || []
      // Deduplicate by name, keep first (smallest id)
      const seen = new Set()
      doctorList.value = list.filter(d => {
        if (seen.has(d.name)) return false
        seen.add(d.name)
        return true
      }).map(d => ({
        id: d.id,
        name: d.name,
        title: d.title || t('consult.doctor')
      }))
    }
  } catch (error) {
    console.error('Failed to load doctor list:', error)
  }
}

// Load pet list
const loadPetList = async () => {
  try {
    const token = getToken()
    const response = await fetch('/api/owner/pet/list?page=1&pageSize=100', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      petList.value = res.data?.data || res.data?.records || []
    }
  } catch (error) {
    console.error('Failed to load pet list:', error)
  }
}

// Load consultation list
const loadConsultList = async () => {
  loading.value = true
  try {
    const token = getToken()
    const response = await fetch('/api/owner/consult/list?page=1&pageSize=50', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    console.log('Consultation list response:', res)
    if (res.code === 200) {
      const records = res.data?.data || res.data?.records || []
      consultList.value = records.map(item => {
        const pet = petList.value.find(p => String(p.id) === String(item.petId))
        const doctor = doctorList.value.find(d => String(d.id) === String(item.doctorId))
        return {
          ...item,
          petName: item.petName || pet?.name || t('consult.unknownPet'),
          doctorName: item.doctorName || doctor?.name || t('consult.awaitingAssignment'),
          doctorTitle: doctor?.title || t('consult.licensedPhysician'),
          replyCount: item.replyCount ?? 0,
          status: item.status === 'done' ? 'completed' : (item.status || 'ongoing')
        }
      })
    }
  } catch (error) {
    console.error('Failed to load consultation list:', error)
  } finally {
    loading.value = false
  }
}

// Start consultation
const handleCreate = () => {
  if (petList.value.length === 0) {
    ElMessage.warning(t('consult.pleaseAddPetFirst'))
    return
  }
  form.value = { petId: '', doctorId: '', title: '', content: '', images: [] }
  fileList.value = []
  dialogVisible.value = true
}

// File upload
const handleFileChange = (file) => {
  form.value.images.push(file.raw)
  fileList.value.push({ name: file.name, url: URL.createObjectURL(file.raw) })
}

// File remove
const handleFileRemove = (file) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index !== -1) {
    fileList.value.splice(index, 1)
    form.value.images.splice(index, 1)
  }
}

// Submit consultation
const submitForm = async () => {
  console.log('=== Start submitting consultation ===')
  console.log('Form data:', form.value)
  
  if (!form.value.petId) {
    ElMessage.warning(t('consult.pleaseSelectAPet'))
    return
  }
  if (!form.value.doctorId) {
    ElMessage.warning(t('consult.pleaseSelectADoctor'))
    return
  }
  if (!form.value.title) {
    ElMessage.warning(t('consult.pleaseEnterATitle'))
    return
  }
  if (!form.value.content) {
    ElMessage.warning(t('consult.pleaseEnterProblemDesc'))
    return
  }
  
  submitting.value = true
  try {
    const token = getToken()
    const submitData = {
      petId: Number(form.value.petId),
      doctorId: Number(form.value.doctorId),
      title: form.value.title,
      content: form.value.content,
      images: null
    }
    console.log('Submit data:', submitData)
    
    const response = await fetch('/api/owner/consult', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify(submitData)
    })
    const res = await response.json()
    console.log('Submit response:', res)
    
    if (res.code === 200) {
      ElMessage.success(t('consult.consultationSubmitted'))
      dialogVisible.value = false
      form.value = { petId: '', doctorId: '', title: '', content: '', images: [] }
      fileList.value = []
      await loadConsultList()
    } else {
      ElMessage.error(res.message || res.msg || t('consult.submissionFailed'))
    }
  } catch (error) {
    console.error('Submission failed:', error)
    ElMessage.error(t('consult.submissionFailed') + ': ' + (error.message || 'Network error'))
  } finally {
    submitting.value = false
  }
}

// View detail
const viewDetail = (id) => {
  router.push(`/owner/consult/${id}`)
}

onMounted(async () => {
  await loadDoctorList()
  await loadPetList()
  loadConsultList()
})
</script>

<style scoped lang="scss">
.consult-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 4px;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #64748b;
    }
    
    .add-btn {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
      }
    }
  }
  
  .consult-list {
    .consult-card {
      background: white;
      border-radius: 24px;
      margin-bottom: 20px;
      overflow: hidden;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      cursor: pointer;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
      }
      
      .consult-header {
        padding: 20px 24px;
        background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 16px;
        
        .doctor-info {
          display: flex;
          align-items: center;
          gap: 16px;
          
          .doctor-avatar {
            position: relative;
            
            .online-dot {
              position: absolute;
              bottom: 2px;
              right: 2px;
              width: 12px;
              height: 12px;
              background: #10b981;
              border-radius: 50%;
              border: 2px solid white;
            }
          }
          
          .doctor-detail {
            .doctor-name {
              font-size: 16px;
              font-weight: 600;
              color: #1e293b;
            }
            
            .doctor-title {
              font-size: 12px;
              color: #64748b;
              margin-top: 2px;
            }
          }
        }
        
        .consult-meta {
          text-align: right;
          
          .consult-status {
            font-size: 12px;
            padding: 4px 12px;
            border-radius: 20px;
            display: inline-block;
            margin-bottom: 8px;
            
            &.ongoing {
              background: #fef3c7;
              color: #d97706;
            }
            
            &.completed {
              background: #d1fae5;
              color: #059669;
            }
          }
          
          .consult-time {
            font-size: 12px;
            color: #94a3b8;
          }
        }
      }
      
      .consult-body {
        padding: 20px 24px;
        
        .consult-title {
          font-size: 18px;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 12px;
          line-height: 1.4;
        }
        
        .consult-preview {
          font-size: 14px;
          color: #64748b;
          line-height: 1.6;
          margin-bottom: 16px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .consult-tags {
          display: flex;
          gap: 16px;
          flex-wrap: wrap;
          
          .tag {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            color: #94a3b8;
            
            .el-icon {
              font-size: 14px;
            }
          }
        }
      }
      
      .consult-footer {
        padding: 12px 24px 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .reply-badge {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 13px;
          color: #3b82f6;
          
          .unread-dot {
            width: 8px;
            height: 8px;
            background: #ef4444;
            border-radius: 50%;
          }
        }
        
        .view-btn {
          color: #3b82f6;
          font-size: 14px;
          
          .el-icon {
            margin-left: 4px;
            transition: transform 0.3s;
          }
          
          &:hover .el-icon {
            transform: translateX(4px);
          }
        }
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 80px 20px;
    
    .empty-icon {
      font-size: 80px;
      margin-bottom: 20px;
    }
    
    h3 {
      font-size: 20px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 8px;
    }
    
    p {
      font-size: 14px;
      color: #64748b;
      margin-bottom: 24px;
    }
  }
}

.consult-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
  }
  
  .dialog-tip {
    background: #f0fdf4;
    border-radius: 12px;
    padding: 12px 16px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #059669;
    
    .el-icon {
      font-size: 18px;
    }
  }
  
  .upload-tip {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 8px;
  }
}
</style>
