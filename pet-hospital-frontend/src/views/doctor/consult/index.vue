<template>
    <div class="consult-page">
      <!-- 未读消息提示 -->
      <transition name="slide-down">
        <div v-if="unreadCount > 0" class="unread-banner">
          <div class="banner-content">
            <el-icon><Bell /></el-icon>
            <span>您有 <strong>{{ unreadCount }}</strong> 条未回复的咨询消息等待处理</span>
            <el-button type="primary" size="small" round @click="scrollToUnread">
              立即查看
            </el-button>
          </div>
          <el-icon class="close-icon" @click="unreadCount = 0"><Close /></el-icon>
        </div>
      </transition>

      <!-- 主内容区 -->
      <el-card shadow="hover" class="main-card">
        <!-- 标签页 -->
        <div class="tabs-header">
          <div class="tabs-nav">
            <div 
              v-for="tab in tabs" 
              :key="tab.name"
              class="tab-item"
              :class="{ active: activeTab === tab.name }"
              @click="activeTab = tab.name"
            >
              <el-icon :size="18">
                <component :is="tab.icon" />
              </el-icon>
              <span>{{ tab.label }}</span>
              <el-badge 
                v-if="tab.badge && unreadCount > 0" 
                :value="unreadCount" 
                class="tab-badge"
              />
            </div>
          </div>
          
          <div class="tabs-extra">
            <el-input
              v-model="searchForm.petName"
              placeholder="搜索宠物名称"
              clearable
              :prefix-icon="Search"
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" :icon="Search" circle @click="handleSearch" />
          </div>
        </div>

        <!-- 咨询列表 -->
        <div class="consult-list" v-loading="loading">
          <div 
            v-for="item in tableData" 
            :key="item.consultId"
            class="consult-item"
            :class="{ unread: item.replyStatus === 0 }"
            @click="item.replyStatus === 0 ? handleReply(item) : handleView(item)"
          >
            <div class="item-avatar">
              <el-avatar :size="48" :src="item.petAvatar">
                <el-icon><FirstAidKit /></el-icon>
              </el-avatar>
              <div v-if="item.replyStatus === 0" class="unread-dot"></div>
            </div>
            
            <div class="item-content">
              <div class="content-header">
                <div class="pet-info">
                  <span class="pet-name">{{ item.petName }}</span>
                  <el-tag size="small" :type="item.replyStatus === 0 ? 'danger' : 'success'" round>
                    {{ item.replyStatus === 0 ? '待回复' : '已回复' }}
                  </el-tag>
                </div>
                <span class="time">{{ formatTime(item.consultTime) }}</span>
              </div>
              
              <div class="content-body">
                <h4 class="title">{{ item.title }}</h4>
                <p class="description">{{ item.content }}</p>
              </div>
              
              <div class="content-footer">
                <div class="owner-info">
                  <el-icon><User /></el-icon>
                  <span>{{ item.ownerName }}</span>
                  <el-divider direction="vertical" />
                  <el-icon><Phone /></el-icon>
                  <span>{{ item.ownerPhone }}</span>
                </div>
                <el-button 
                  v-if="item.replyStatus === 0" 
                  type="primary" 
                  size="small"
                  round
                  class="reply-btn"
                >
                  立即回复
                </el-button>
                <span v-else class="reply-info">
                  <el-icon><CircleCheck /></el-icon>
                  已回复
                </span>
              </div>
            </div>
            
            <div v-if="item.images" class="item-images">
              <el-image
                v-for="(img, idx) in item.imageList?.slice(0, 3)"
                :key="idx"
                :src="img"
                fit="cover"
                class="thumb-image"
                :preview-src-list="item.imageList"
              />
              <div v-if="item.imageList?.length > 3" class="more-images">
                +{{ item.imageList.length - 3 }}
              </div>
            </div>
          </div>
          
          <el-empty v-if="tableData.length === 0" description="暂无咨询记录" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            layout="prev, pager, next, jumper"
            @current-change="handlePageChange"
            background
          />
        </div>
      </el-card>

      <!-- 回复对话框 -->
      <el-dialog
        v-model="replyDialogVisible"
        title="咨询回复"
        width="800px"
        destroy-on-close
        class="reply-dialog"
        :show-close="false"
      >
        <template #header>
          <div class="dialog-header">
            <div class="header-left">
              <el-avatar :size="40" :src="currentConsult.petAvatar">
                <el-icon><FirstAidKit /></el-icon>
              </el-avatar>
              <div class="consult-meta">
                <div class="pet-name">{{ currentConsult.petName }}</div>
                <div class="consult-time">{{ currentConsult.consultTime }}</div>
              </div>
            </div>
            <el-button circle @click="replyDialogVisible = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </template>
        
        <div class="dialog-body">
          <!-- 咨询内容 -->
          <div class="consult-section">
            <div class="section-label">
              <el-icon><ChatDotRound /></el-icon>
              咨询内容
            </div>
            <div class="consult-content">
              <h4>{{ currentConsult.title }}</h4>
              <p>{{ currentConsult.content }}</p>
              <div v-if="currentConsult.imageList?.length" class="image-grid">
                <el-image
                  v-for="(img, idx) in currentConsult.imageList"
                  :key="idx"
                  :src="img"
                  fit="cover"
                  class="grid-image"
                  :preview-src-list="currentConsult.imageList"
                />
              </div>
            </div>
          </div>

          <!-- 回复表单 -->
          <div class="reply-section">
            <div class="section-label">
              <el-icon><EditPen /></el-icon>
              医生回复
            </div>
            <el-input
              v-model="replyForm.replyContent"
              type="textarea"
              :rows="5"
              placeholder="请输入专业的诊疗建议，包括可能的原因、建议检查项目、临时处理措施等..."
              maxlength="1000"
              show-word-limit
              class="reply-textarea"
            />
            
            <div class="upload-section">
              <div class="upload-label">上传图片（可选）</div>
              <el-upload
                action="/api/common/upload"
                list-type="picture-card"
                :on-success="handleUploadSuccess"
                :on-remove="handleRemove"
                :file-list="replyForm.imageList"
                :limit="6"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="replyDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReply" size="large" :disabled="!replyForm.replyContent.trim()">
              <el-icon><Promotion /></el-icon>
              发送回复
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 查看对话框 -->
      <el-dialog
        v-model="viewDialogVisible"
        title="咨询详情"
        width="800px"
        class="view-dialog"
      >
        <div class="view-content">
          <div class="consult-detail">
            <div class="detail-header">
              <el-avatar :size="48" :src="currentConsult.petAvatar" />
              <div class="detail-meta">
                <div class="title">{{ currentConsult.title }}</div>
                <div class="time">{{ currentConsult.consultTime }}</div>
              </div>
            </div>
            <div class="detail-body">
              <p>{{ currentConsult.content }}</p>
              <div v-if="currentConsult.imageList?.length" class="image-list">
                <el-image
                  v-for="(img, idx) in currentConsult.imageList"
                  :key="idx"
                  :src="img"
                  fit="cover"
                  class="detail-image"
                  :preview-src-list="currentConsult.imageList"
                />
              </div>
            </div>
          </div>

          <el-divider>
            <el-icon><Bottom /></el-icon>
            医生回复
          </el-divider>

          <div class="reply-detail">
            <div class="reply-header">
              <el-avatar :size="36" :src="userInfo.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="doctor-name">{{ userInfo.realName || '医生' }}</span>
              <span class="reply-time">{{ currentConsult.replyTime }}</span>
            </div>
            <div class="reply-body">
              <p>{{ currentConsult.replyContent }}</p>
              <div v-if="currentConsult.replyImageList?.length" class="image-list">
                <el-image
                  v-for="(img, idx) in currentConsult.replyImageList"
                  :key="idx"
                  :src="img"
                  fit="cover"
                  class="detail-image"
                  :preview-src-list="currentConsult.replyImageList"
                />
              </div>
            </div>
          </div>

          <!-- 评价 -->
          <div v-if="currentConsult.rating" class="rating-section">
            <el-divider />
            <div class="rating-content">
              <div class="rating-header">用户评价</div>
              <el-rate v-model="currentConsult.rating" disabled show-score />
              <p class="rating-text">{{ currentConsult.comment }}</p>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Bell, Close, Search, FirstAidKit, User, Phone, 
  CircleCheck, ChatDotRound, EditPen, Plus, Promotion,
  Bottom, UserFilled, Message, Check
} from '@element-plus/icons-vue'
import { consultModule } from '@/api/doctor/doctor'
import { useUserStore } from '@/store/user'


const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const activeTab = ref('unread')
const unreadCount = ref(0)
const tableData = ref([])
let refreshTimer = null

const tabs = [
  { name: 'unread', label: '未回复', icon: 'Message', badge: true },
  { name: 'replied', label: '已回复', icon: 'Check' },
  { name: 'all', label: '全部', icon: 'ChatDotRound' }
]

const searchForm = reactive({
  petName: '',
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const replyDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentConsult = ref({})

const replyForm = reactive({
  consultId: null,
  replyContent: '',
  imageList: []
})

const userInfo = computed(() => userStore.userInfo || {})

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    const res = await consultModule.getUnreadConsultCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      petName: searchForm.petName,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1],
      replyStatus: activeTab.value === 'unread' ? 0 : (activeTab.value === 'replied' ? 1 : undefined)
    }
    const res = await consultModule.getConsultList(params)
    tableData.value = (res.data?.list || []).map(item => ({
      ...item,
      imageList: item.images?.split(',') || []
    }))
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return datetime.split(' ')[0]
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchList()
}

const handlePageChange = (val) => {
  pagination.pageNum = val
  fetchList()
}

// 回复
const handleReply = (row) => {
  currentConsult.value = row
  replyForm.consultId = row.consultId
  replyForm.replyContent = ''
  replyForm.imageList = []
  replyDialogVisible.value = true
}

// 查看
const handleView = async (row) => {
  try {
    const res = await consultModule.getConsultDetail(row.consultId)
    currentConsult.value = {
      ...res.data,
      imageList: res.data.images?.split(',') || [],
      replyImageList: res.data.replyImages?.split(',') || []
    }
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 上传成功
const handleUploadSuccess = (response, file) => {
  replyForm.imageList.push({
    name: file.name,
    url: response.data
  })
}

// 移除图片
const handleRemove = (file) => {
  const index = replyForm.imageList.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    replyForm.imageList.splice(index, 1)
  }
}

// 提交回复
const submitReply = async () => {
  if (!replyForm.replyContent.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    await consultModule.replyConsult({
      consultId: replyForm.consultId,
      replyContent: replyForm.replyContent,
      replyImages: replyForm.imageList.map(img => img.url).join(',')
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    fetchList()
    fetchUnreadCount()
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const scrollToUnread = () => {
  activeTab.value = 'unread'
  fetchList()
}

onMounted(() => {
  fetchList()
  fetchUnreadCount()
  refreshTimer = setInterval(() => {
    fetchUnreadCount()
    if (activeTab.value === 'unread') {
      fetchList()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped lang="scss">
.consult-page {
  .unread-banner {
    background: linear-gradient(135deg, #fef3c7, #fde68a);
    border-radius: 12px;
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    animation: slideDown 0.3s ease;
    
    .banner-content {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #92400e;
      
      .el-icon {
        font-size: 24px;
        color: #f59e0b;
      }
      
      strong {
        color: #dc2626;
        font-size: 20px;
      }
    }
    
    .close-icon {
      cursor: pointer;
      color: #92400e;
      font-size: 20px;
      
      &:hover {
        color: #78350f;
      }
    }
  }
  
  .main-card {
    border-radius: 16px;
    
    .tabs-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 20px;
      border-bottom: 1px solid #f1f5f9;
      margin-bottom: 20px;
      
      .tabs-nav {
        display: flex;
        gap: 10px;
        
        .tab-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px 24px;
          border-radius: 10px;
          cursor: pointer;
          transition: all 0.3s;
          color: #64748b;
          font-weight: 500;
          
          &:hover {
            background: #f1f5f9;
            color: #334155;
          }
          
          &.active {
            background: #3b82f6;
            color: white;
            
            .tab-badge {
              :deep(.el-badge__content) {
                background: white;
                color: #3b82f6;
              }
            }
          }
          
          .tab-badge {
            margin-left: 4px;
          }
        }
      }
      
      .tabs-extra {
        display: flex;
        gap: 10px;
        
        .search-input {
          width: 240px;
        }
      }
    }
    
    .consult-list {
      min-height: 400px;
      
      .consult-item {
        display: flex;
        gap: 20px;
        padding: 24px;
        border-radius: 12px;
        margin-bottom: 16px;
        background: white;
        border: 1px solid #e2e8f0;
        transition: all 0.3s;
        cursor: pointer;
        
        &:hover {
          box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
          transform: translateY(-2px);
          border-color: #3b82f6;
        }
        
        &.unread {
          background: #eff6ff;
          border-color: #3b82f6;
          
          .item-avatar {
            .unread-dot {
              position: absolute;
              top: 0;
              right: 0;
              width: 12px;
              height: 12px;
              background: #ef4444;
              border-radius: 50%;
              border: 2px solid white;
            }
          }
        }
        
        .item-avatar {
          position: relative;
          
          .el-avatar {
            border: 2px solid #e2e8f0;
          }
        }
        
        .item-content {
          flex: 1;
          min-width: 0;
          
          .content-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            
            .pet-info {
              display: flex;
              align-items: center;
              gap: 10px;
              
              .pet-name {
                font-size: 18px;
                font-weight: 600;
                color: #1e293b;
              }
            }
            
            .time {
              color: #94a3b8;
              font-size: 13px;
            }
          }
          
          .content-body {
            margin-bottom: 16px;
            
            .title {
              font-size: 16px;
              font-weight: 600;
              color: #334155;
              margin-bottom: 8px;
            }
            
            .description {
              color: #64748b;
              line-height: 1.6;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }
          }
          
          .content-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .owner-info {
              display: flex;
              align-items: center;
              gap: 8px;
              color: #94a3b8;
              font-size: 13px;
              
              .el-icon {
                font-size: 14px;
              }
            }
            
            .reply-btn {
              background: linear-gradient(135deg, #3b82f6, #2563eb);
              border: none;
            }
            
            .reply-info {
              display: flex;
              align-items: center;
              gap: 6px;
              color: #10b981;
              font-size: 13px;
            }
          }
        }
        
        .item-images {
          display: flex;
          gap: 8px;
          
          .thumb-image {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            object-fit: cover;
          }
          
          .more-images {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            background: #f1f5f9;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #64748b;
            font-weight: 600;
          }
        }
      }
    }
    
    .pagination-wrapper {
      margin-top: 30px;
      display: flex;
      justify-content: center;
    }
  }
  
  .reply-dialog {
    .dialog-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-right: 20px;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 15px;
        
        .consult-meta {
          .pet-name {
            font-size: 18px;
            font-weight: 600;
            color: #1e293b;
          }
          
          .consult-time {
            font-size: 13px;
            color: #94a3b8;
            margin-top: 4px;
          }
        }
      }
    }
    
    .dialog-body {
      .section-label {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        color: #334155;
        margin-bottom: 15px;
        
        .el-icon {
          color: #3b82f6;
        }
      }
      
      .consult-section {
        background: #f8fafc;
        border-radius: 12px;
        padding: 20px;
        margin-bottom: 24px;
        
        .consult-content {
          h4 {
            font-size: 16px;
            color: #1e293b;
            margin-bottom: 10px;
          }
          
          p {
            color: #475569;
            line-height: 1.6;
            margin-bottom: 15px;
          }
          
          .image-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 10px;
            
            .grid-image {
              width: 100%;
              height: 100px;
              border-radius: 8px;
              object-fit: cover;
            }
          }
        }
      }
      
      .reply-section {
        .reply-textarea {
          :deep(.el-textarea__inner) {
            border-radius: 12px;
            padding: 16px;
            font-size: 15px;
            line-height: 1.6;
          }
        }
        
        .upload-section {
          margin-top: 20px;
          
          .upload-label {
            font-size: 14px;
            color: #64748b;
            margin-bottom: 10px;
          }
        }
      }
    }
    
    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }
  
  .view-dialog {
    .view-content {
      .consult-detail {
        .detail-header {
          display: flex;
          align-items: center;
          gap: 15px;
          margin-bottom: 20px;
          
          .detail-meta {
            .title {
              font-size: 18px;
              font-weight: 600;
              color: #1e293b;
            }
            
            .time {
              font-size: 13px;
              color: #94a3b8;
              margin-top: 4px;
            }
          }
        }
        
        .detail-body {
          p {
            color: #475569;
            line-height: 1.8;
            margin-bottom: 15px;
          }
          
          .image-list {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 10px;
            
            .detail-image {
              width: 100%;
              height: 120px;
              border-radius: 8px;
              object-fit: cover;
            }
          }
        }
      }
      
      .reply-detail {
        background: #f0fdf4;
        border-radius: 12px;
        padding: 20px;
        
        .reply-header {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 15px;
          
          .doctor-name {
            font-weight: 600;
            color: #1e293b;
          }
          
          .reply-time {
            margin-left: auto;
            font-size: 13px;
            color: #94a3b8;
          }
        }
        
        .reply-body {
          p {
            color: #334155;
            line-height: 1.8;
          }
          
          .image-list {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 10px;
            margin-top: 15px;
            
            .detail-image {
              width: 100%;
              height: 100px;
              border-radius: 8px;
              object-fit: cover;
            }
          }
        }
      }
      
      .rating-section {
        .rating-content {
          text-align: center;
          padding: 20px;
          
          .rating-header {
            font-weight: 600;
            color: #334155;
            margin-bottom: 15px;
          }
          
          .rating-text {
            margin-top: 15px;
            color: #64748b;
            font-style: italic;
          }
        }
      }
    }
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>