<template>
  <div class="consult-detail-page">
    <!-- 返回按钮 -->
    <div class="page-nav">
      <el-button link @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-loading="loading">
      <!-- 咨询信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <div class="doctor-info">
            <el-avatar :size="48" class="doctor-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div>
              <div class="doctor-name">{{ detail.doctorName || '等待接诊' }}</div>
              <div class="consult-status" :class="detail.status">
                {{ detail.status === 'ongoing' ? '进行中' : '已完成' }}
              </div>
            </div>
          </div>
          <div class="consult-time">{{ detail.createTime }}</div>
        </div>
        <div class="consult-content">
          <h3>{{ detail.title }}</h3>
          <div class="question-box">
            <div class="question-label">
              <span class="label-icon">📝</span>
              问题描述
            </div>
            <p>{{ detail.content }}</p>
          </div>
          <div class="pet-info">
            <span class="pet-tag">🐾 {{ detail.petName }}</span>
          </div>
        </div>
      </div>

      <!-- 回复列表 -->
      <div class="replies-card">
        <div class="card-header">
          <span class="title">医生回复</span>
          <span class="reply-count">共 {{ replies.length }} 条回复</span>
        </div>
        <div class="replies-list">
          <!-- 医生回复 -->
          <div v-for="reply in replies" :key="reply.id" class="reply-item" :class="reply.senderType">
            <div class="reply-avatar">
              <el-avatar :size="36">
                <el-icon>{{ reply.senderType === 'doctor' ? 'User' : 'User' }}</el-icon>
              </el-avatar>
            </div>
            <div class="reply-content">
              <div class="reply-header">
                <span class="reply-name">{{ reply.senderName }}</span>
                <span class="reply-time">{{ reply.createTime }}</span>
              </div>
              <div class="reply-text">{{ reply.content }}</div>
            </div>
          </div>
          <el-empty v-if="replies.length === 0" description="暂无回复，请耐心等待" />
        </div>
      </div>

      <!-- 输入框 -->
      <div v-if="detail.status === 'ongoing'" class="input-card">
        <div class="input-wrapper">
          <el-input 
            type="textarea" 
            v-model="newReply" 
            placeholder="输入追问内容..."
            :rows="3"
            resize="none"
          />
          <div class="input-actions">
            <el-button type="primary" @click="submitReply" :loading="replying">发送</el-button>
          </div>
        </div>
      </div>

      <!-- 评价区域 -->
      <div v-if="detail.status === 'completed' && !detail.rated" class="rate-card">
        <div class="card-header">
          <span class="title">评价服务</span>
        </div>
        <div class="rate-content">
          <el-rate v-model="rating" :texts="['很差', '较差', '一般', '较好', '非常好']" show-text />
          <el-input 
            type="textarea" 
            v-model="ratingComment" 
            placeholder="请输入评价内容（选填）"
            :rows="2"
          />
          <el-button type="primary" @click="submitRate" :loading="ratingSubmit">提交评价</el-button>
        </div>
      </div>

      <!-- 已评价显示 -->
      <div v-if="detail.rated" class="rated-card">
        <div class="rated-content">
          <span class="rated-label">已评价</span>
          <el-rate v-model="detail.rating" disabled />
          <span class="rated-comment">{{ detail.ratingComment }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User } from '@element-plus/icons-vue'
import { getConsultDetail, replyConsult, rateConsult } from '@/api/owner/owner'

const route = useRoute()
const router = useRouter()
const consultId = route.params.id

const loading = ref(false)
const replying = ref(false)
const ratingSubmit = ref(false)
const newReply = ref('')
const rating = ref(5)
const ratingComment = ref('')

const detail = ref({
  title: '',
  content: '',
  status: '',
  doctorName: '',
  petName: '',
  createTime: '',
  rated: false,
  rating: 0,
  ratingComment: ''
})

const replies = ref([])

const goBack = () => {
  router.push('/owner/consult')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getConsultDetail(consultId)
    if (res.code === 200) {
      detail.value = res.data
      replies.value = res.data.replies || []
    }
  } catch (error) {
    console.error('加载咨询详情失败:', error)
  } finally {
    loading.value = false
  }
}

const submitReply = async () => {
  if (!newReply.value.trim()) {
    ElMessage.warning('请输入追问内容')
    return
  }
  replying.value = true
  try {
    await replyConsult(consultId, { content: newReply.value })
    ElMessage.success('发送成功')
    newReply.value = ''
    loadDetail()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    replying.value = false
  }
}

const submitRate = async () => {
  ratingSubmit.value = true
  try {
    await rateConsult(consultId, { rating: rating.value, comment: ratingComment.value })
    ElMessage.success('评价成功')
    loadDetail()
  } catch {
    ElMessage.error('评价失败')
  } finally {
    ratingSubmit.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.consult-detail-page {
  .page-nav {
    margin-bottom: 20px;
    
    .back-btn {
      color: #64748b;
      font-size: 14px;
      
      &:hover {
        color: #3b82f6;
      }
    }
  }
  
  .info-card, .replies-card, .input-card, .rate-card, .rated-card {
    background: white;
    border-radius: 20px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }
  
  .info-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #e2e8f0;
      
      .doctor-info {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .doctor-avatar {
          background: #e0e7ff;
        }
        
        .doctor-name {
          font-size: 16px;
          font-weight: 600;
          color: #1e293b;
        }
        
        .consult-status {
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 20px;
          display: inline-block;
          
          &.ongoing {
            background: #fef3c7;
            color: #d97706;
          }
          
          &.completed {
            background: #d1fae5;
            color: #059669;
          }
        }
      }
      
      .consult-time {
        font-size: 12px;
        color: #94a3b8;
      }
    }
    
    .consult-content {
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #1e293b;
        margin-bottom: 16px;
      }
      
      .question-box {
        background: #f8fafc;
        border-radius: 16px;
        padding: 16px;
        margin-bottom: 16px;
        
        .question-label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          font-weight: 500;
          color: #64748b;
          margin-bottom: 12px;
          
          .label-icon {
            font-size: 16px;
          }
        }
        
        p {
          font-size: 14px;
          color: #1e293b;
          line-height: 1.6;
        }
      }
      
      .pet-info {
        .pet-tag {
          font-size: 12px;
          padding: 4px 12px;
          background: #e2e8f0;
          border-radius: 20px;
          color: #64748b;
        }
      }
    }
  }
  
  .replies-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      .title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }
      
      .reply-count {
        font-size: 12px;
        color: #94a3b8;
      }
    }
    
    .replies-list {
      .reply-item {
        display: flex;
        gap: 12px;
        margin-bottom: 20px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        &.doctor {
          .reply-avatar {
            background: #dbeafe;
          }
          
          .reply-content {
            background: #f0f9ff;
          }
        }
        
        &.owner {
          flex-direction: row-reverse;
          
          .reply-content {
            background: #f1f5f9;
          }
        }
        
        .reply-avatar {
          width: 36px;
          height: 36px;
          background: #e2e8f0;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }
        
        .reply-content {
          flex: 1;
          padding: 12px 16px;
          border-radius: 16px;
          
          .reply-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;
            
            .reply-name {
              font-size: 13px;
              font-weight: 600;
              color: #1e293b;
            }
            
            .reply-time {
              font-size: 11px;
              color: #94a3b8;
            }
          }
          
          .reply-text {
            font-size: 14px;
            color: #64748b;
            line-height: 1.5;
          }
        }
      }
    }
  }
  
  .input-card {
    .input-wrapper {
      .input-actions {
        margin-top: 12px;
        text-align: right;
      }
    }
  }
  
  .rate-card {
    .rate-content {
      text-align: center;
      
      .el-rate {
        margin-bottom: 16px;
      }
      
      .el-button {
        margin-top: 16px;
      }
    }
  }
  
  .rated-card {
    .rated-content {
      display: flex;
      align-items: center;
      gap: 16px;
      flex-wrap: wrap;
      
      .rated-label {
        font-size: 14px;
        font-weight: 500;
        color: #10b981;
      }
      
      .rated-comment {
        font-size: 13px;
        color: #64748b;
      }
    }
  }
}
</style>