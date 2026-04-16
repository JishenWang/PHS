<template>
  <div class="consult-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">在线咨询</h2>
        <p class="page-subtitle">专业医生在线解答您的疑问</p>
      </div>
      <el-button type="primary" size="large" @click="handleCreate" class="add-btn">
        <el-icon><Plus /></el-icon>
        发起咨询
      </el-button>
    </div>

    <!-- 咨询列表 -->
    <div class="consult-list" v-loading="loading">
      <div v-for="item in consultList" :key="item.id" class="consult-card" @click="viewDetail(item.id)">
        <!-- 医生头像和基本信息 -->
        <div class="consult-header">
          <div class="doctor-info">
            <div class="doctor-avatar">
              <el-avatar :size="48">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="online-dot" v-if="item.status === 'ongoing'"></span>
            </div>
            <div class="doctor-detail">
              <div class="doctor-name">{{ item.doctorName || '等待分配' }}</div>
              <div class="doctor-title">{{ item.doctorTitle || '执业医师' }}</div>
            </div>
          </div>
          <div class="consult-meta">
            <div class="consult-status" :class="item.status">
              {{ item.status === 'ongoing' ? '进行中' : '已完成' }}
            </div>
            <div class="consult-time">{{ formatDate(item.createTime) }}</div>
          </div>
        </div>

        <!-- 咨询内容 -->
        <div class="consult-body">
          <div class="consult-title">{{ item.title }}</div>
          <div class="consult-preview">{{ item.content }}</div>
          <div class="consult-tags">
            <span class="tag">
              <el-icon><ChatLineSquare /></el-icon>
              {{ item.replyCount || 0 }} 条回复
            </span>
            <span class="tag" v-if="item.petName">
              <el-icon><Avatar /></el-icon>
              {{ item.petName }}
            </span>
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="consult-footer">
          <div class="reply-badge" v-if="item.unreadCount > 0">
            <span class="unread-dot"></span>
            {{ item.unreadCount }} 条新回复
          </div>
          <el-button type="primary" link class="view-btn">
            查看详情
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && consultList.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <h3>暂无咨询记录</h3>
        <p>您还没有发起过咨询，点击下方按钮开始咨询</p>
        <el-button type="primary" @click="handleCreate">发起咨询</el-button>
      </div>
    </div>

    <!-- 发起咨询弹窗 -->
    <el-dialog v-model="dialogVisible" title="发起咨询" width="520px" class="consult-dialog">
      <div class="dialog-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>请详细描述您的问题，医生会尽快回复您</span>
      </div>
      <el-form :model="form" label-width="80px" size="default">
        <el-form-item label="选择宠物">
          <el-select v-model="form.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span>{{ pet.gender === 'male' ? '🐕' : '🐱' }}</span>
                <span>{{ pet.name }}</span>
                <span style="color: #999; font-size: 12px;">{{ pet.breed }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择医生">
          <el-select v-model="form.doctorId" placeholder="请选择医生" style="width: 100%">
            <el-option v-for="doc in doctorList" :key="doc.id" :label="doc.name" :value="doc.id">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span>👨‍⚕️</span>
                <span>{{ doc.name }}</span>
                <span style="color: #999; font-size: 12px;">{{ doc.title }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="咨询标题">
          <el-input v-model="form.title" placeholder="简要概括您的问题" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input 
            type="textarea" 
            v-model="form.content" 
            placeholder="请详细描述宠物的症状、持续时间、用药情况等，方便医生准确判断"
            :rows="6"
          />
        </el-form-item>
        <el-form-item label="上传图片">
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
          <div class="upload-tip">支持上传图片，帮助医生更好地了解情况</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">提交咨询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, User, ChatLineSquare, Avatar, ArrowRight, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const consultList = ref([])
const petList = ref([])
const dialogVisible = ref(false)
const fileList = ref([])

const doctorList = ref([
  { id: 1, name: '张医生', title: '主治医师' },
  { id: 2, name: '李医生', title: '执业医师' },
  { id: 3, name: '王医生', title: '主任医师' }
])

const form = ref({
  petId: '',
  doctorId: '',
  title: '',
  content: '',
  images: []
})

// 获取 token
const getToken = () => {
  return localStorage.getItem('pet_hospital_token')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 加载宠物列表
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
    console.error('加载宠物列表失败:', error)
  }
}

// 加载咨询列表
const loadConsultList = async () => {
  loading.value = true
  try {
    const token = getToken()
    const response = await fetch('/api/owner/consult/list?page=1&pageSize=50', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    console.log('咨询列表响应:', res)
    if (res.code === 200) {
      const records = res.data?.data || res.data?.records || []
      consultList.value = records.map(item => {
        const pet = petList.value.find(p => p.id === item.petId)
        const doctor = doctorList.value.find(d => d.id === item.doctorId)
        return {
          ...item,
          petName: pet?.name || '未知宠物',
          doctorName: doctor?.name || '等待分配',
          doctorTitle: doctor?.title || '执业医师',
          replyCount: 0,
          status: item.status === 'done' ? 'completed' : (item.status || 'ongoing')
        }
      })
    }
  } catch (error) {
    console.error('加载咨询列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 发起咨询
const handleCreate = () => {
  if (petList.value.length === 0) {
    ElMessage.warning('请先添加宠物')
    return
  }
  form.value = { petId: '', doctorId: '', title: '', content: '', images: [] }
  fileList.value = []
  dialogVisible.value = true
}

// 文件上传
const handleFileChange = (file) => {
  form.value.images.push(file.raw)
  fileList.value.push({ name: file.name, url: URL.createObjectURL(file.raw) })
}

// 文件移除
const handleFileRemove = (file) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index !== -1) {
    fileList.value.splice(index, 1)
    form.value.images.splice(index, 1)
  }
}

// 提交咨询
const submitForm = async () => {
  console.log('=== 开始提交咨询 ===')
  console.log('表单数据:', form.value)
  
  if (!form.value.petId) {
    ElMessage.warning('请选择宠物')
    return
  }
  if (!form.value.doctorId) {
    ElMessage.warning('请选择医生')
    return
  }
  if (!form.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('请输入问题描述')
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
    console.log('提交数据:', submitData)
    
    const response = await fetch('/api/owner/consult', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify(submitData)
    })
    const res = await response.json()
    console.log('提交响应:', res)
    
    if (res.code === 200) {
      ElMessage.success('咨询提交成功，医生会尽快回复')
      dialogVisible.value = false
      form.value = { petId: '', doctorId: '', title: '', content: '', images: [] }
      fileList.value = []
      await loadConsultList()
    } else {
      ElMessage.error(res.message || res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败：' + (error.message || '网络错误'))
  } finally {
    submitting.value = false
  }
}

// 查看详情
const viewDetail = (id) => {
  router.push(`/owner/consult/${id}`)
}

onMounted(() => {
  loadPetList()
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