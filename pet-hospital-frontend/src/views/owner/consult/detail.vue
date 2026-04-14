<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px 20px; border-radius: 0 0 30px 30px;">
      <div style="display: flex; align-items: center; gap: 16px;">
        <el-button circle :icon="ArrowLeft" style="background: rgba(255,255,255,0.2); border: none; color: white;" @click="goBack" />
        <div>
          <h2 style="color: white; font-size: 20px;">咨询详情</h2>
          <p style="color: rgba(255,255,255,0.9); font-size: 12px;">与医生在线沟通</p>
        </div>
      </div>
    </div>

    <div style="padding: 20px;" v-loading="loading">
      <!-- 咨询信息卡片 -->
      <div style="background: white; border-radius: 20px; padding: 16px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.03);">
        <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
          <span style="color: #999; font-size: 12px;">{{ detail.createTime }}</span>
        </div>
        <div style="font-weight: bold; font-size: 18px; margin-bottom: 8px;">{{ detail.title }}</div>
        <div style="margin-bottom: 12px;">
          <span style="background: #f0f2f5; padding: 2px 8px; border-radius: 12px; font-size: 11px; color: #666;">
            🐾 {{ detail.petName }}
          </span>
          <span style="margin-left: 8px; background: #f0f2f5; padding: 2px 8px; border-radius: 12px; font-size: 11px; color: #666;">
            👨‍⚕️ {{ detail.doctorName }}
          </span>
        </div>
        <div style="background: #f8f9fc; border-radius: 12px; padding: 12px; margin-top: 8px;">
          <div style="color: #999; font-size: 12px; margin-bottom: 8px;">问题描述：</div>
          <p style="color: #333; line-height: 1.6;">{{ detail.content }}</p>
        </div>
      </div>

      <!-- 回复列表 -->
      <div style="margin-bottom: 20px;">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px;">
          <el-icon><ChatLineSquare /></el-icon>
          <span style="font-weight: bold;">医生回复</span>
          <span style="color: #999; font-size: 12px;">共 {{ replies.length }} 条回复</span>
        </div>

        <div v-for="reply in replies" :key="reply.id" style="margin-bottom: 16px;">
          <!-- 医生回复 -->
          <div v-if="reply.senderType === 'doctor'" style="display: flex; gap: 12px;">
            <el-avatar :size="36" style="background: #667eea;">
              <el-icon size="20"><User /></el-icon>
            </el-avatar>
            <div style="flex: 1;">
              <div style="display: flex; align-items: baseline; gap: 8px; margin-bottom: 4px;">
                <span style="font-weight: bold; font-size: 14px;">{{ reply.senderName }}</span>
                <span style="color: #ccc; font-size: 10px;">{{ reply.createTime }}</span>
              </div>
              <div style="background: #f0f7ff; border-radius: 16px; border-top-left-radius: 4px; padding: 12px 16px;">
                <p style="color: #333; line-height: 1.6;">{{ reply.content }}</p>
              </div>
            </div>
          </div>

          <!-- 主人追问 -->
          <div v-else style="display: flex; gap: 12px; flex-direction: row-reverse;">
            <el-avatar :size="36" style="background: #f5a623;">
              <el-icon size="20"><User /></el-icon>
            </el-avatar>
            <div style="flex: 1; display: flex; justify-content: flex-end;">
              <div style="max-width: 80%;">
                <div style="display: flex; align-items: baseline; justify-content: flex-end; gap: 8px; margin-bottom: 4px;">
                  <span style="color: #ccc; font-size: 10px;">{{ reply.createTime }}</span>
                  <span style="font-weight: bold; font-size: 14px;">{{ reply.senderName }}</span>
                </div>
                <div style="background: #f5a62310; border-radius: 16px; border-top-right-radius: 4px; padding: 12px 16px;">
                  <p style="color: #333; line-height: 1.6;">{{ reply.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="replies.length === 0" style="text-align: center; padding: 40px;">
          <div style="font-size: 48px; margin-bottom: 12px;">💬</div>
          <p style="color: #999;">暂无回复，请耐心等待医生回复</p>
        </div>
      </div>

      <!-- 输入框 -->
      <div style="background: white; border-radius: 20px; padding: 16px; box-shadow: 0 2px 10px rgba(0,0,0,0.03);">
        <div style="display: flex; gap: 12px;">
          <el-input 
            type="textarea" 
            v-model="newReply" 
            placeholder="输入追问内容..."
            :rows="2"
            resize="none"
          />
          <el-button type="primary" :loading="replying" @click="submitReply" style="align-self: flex-end;">发送</el-button>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <router-link to="/pet" class="nav-item">
        <el-icon><Avatar /></el-icon>
        <span>宠物</span>
      </router-link>
      <router-link to="/health" class="nav-item">
        <el-icon><Notebook /></el-icon>
        <span>健康</span>
      </router-link>
      <router-link to="/reserve" class="nav-item">
        <el-icon><Calendar /></el-icon>
        <span>预约</span>
      </router-link>
      <router-link to="/consult" class="nav-item active">
        <el-icon><ChatDotRound /></el-icon>
        <span>咨询</span>
      </router-link>
      <router-link to="/profile" class="nav-item">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User, ChatDotRound, Avatar, Notebook, Calendar, ChatLineSquare } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const consultId = route.params.id

const loading = ref(false)
const replying = ref(false)
const newReply = ref('')

const detail = ref({
  id: '',
  title: '',
  content: '',
  doctorName: '',
  petName: '',
  createTime: ''
})

const replies = ref([])

const goBack = () => {
  router.push('/consult')
}

const loadDetail = () => {
  loading.value = true
  setTimeout(() => {
    detail.value = {
      id: consultId,
      title: '狗狗不吃东西',
      content: '我家狗狗最近两天都不怎么吃东西，精神也不太好，有点蔫蔫的。之前一直很活泼，食欲也很好。最近没有换狗粮，也没有呕吐腹泻的症状。请问可能是什么原因？',
      doctorName: '张医生',
      petName: '旺财',
      createTime: '2024-01-15 10:30'
    }
    
    replies.value = [
      { id: 1, senderType: 'doctor', senderName: '张医生', content: '您好，根据您的描述，狗狗精神状态不佳且食欲下降，可能有几种原因：1. 轻微消化不良 2. 口腔问题 3. 轻微感冒。建议您先观察一下狗狗的体温是否正常，可以尝试喂一些容易消化的食物，如白粥或鸡胸肉。如果症状持续，建议带狗狗来医院检查。', createTime: '2024-01-15 14:20' },
      { id: 2, senderType: 'owner', senderName: '我', content: '谢谢医生，我测了体温是正常的。今天喂了点白粥，它吃了一些。精神好像好一点了。需要吃药吗？', createTime: '2024-01-15 15:30' },
      { id: 3, senderType: 'doctor', senderName: '张医生', content: '体温正常是好现象。暂时不需要吃药，继续观察2-3天，保持清淡饮食。如果出现呕吐、腹泻或精神变差，请及时就医。', createTime: '2024-01-15 16:00' }
    ]
    
    loading.value = false
  }, 300)
}

const submitReply = () => {
  if (!newReply.value.trim()) {
    ElMessage.warning('请输入追问内容')
    return
  }
  
  replying.value = true
  setTimeout(() => {
    const newReplyObj = {
      id: Date.now(),
      senderType: 'owner',
      senderName: '我',
      content: newReply.value,
      createTime: new Date().toLocaleString()
    }
    replies.value.push(newReplyObj)
    newReply.value = ''
    ElMessage.success('发送成功')
    replying.value = false
  }, 500)
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.page-content {
  padding-bottom: 80px;
  min-height: 100vh;
  background: #f8f9fc;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 10px 20px 20px;
  box-shadow: 0 -2px 15px rgba(0,0,0,0.05);
  border-radius: 20px 20px 0 0;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #999;
  font-size: 12px;
  transition: all 0.3s;
}

.nav-item.active {
  color: #667eea;
}

.nav-item .el-icon {
  font-size: 22px;
}
</style>