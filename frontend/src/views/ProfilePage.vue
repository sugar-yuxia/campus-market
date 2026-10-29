<template>
  <div class="page">
    <h1 class="plain-page-title">个人中心</h1>

    <div class="profile-layout">
      <div class="cm-card side-card">
        <el-avatar :size="86" :src="form.avatar || ''" class="big-avatar">
          {{ initial }}
        </el-avatar>
        <h3>{{ user?.nickname || user?.username }}</h3>
        <p class="username">@{{ user?.username }}</p>
        <p class="joined" v-if="user?.createdAt">加入于 {{ user.createdAt.slice(0, 10) }}</p>

        <div class="side-actions">
          <router-link to="/my-products" class="side-link">我的闲置 <span>→</span></router-link>
          <router-link to="/orders" class="side-link">我的订单 <span>→</span></router-link>
          <router-link to="/publish" class="side-link">发布闲置 <span>→</span></router-link>
        </div>
      </div>

      <div class="cm-card main-card">
        <h3 class="card-title">资料设置</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="头像">
            <div class="avatar-row">
              <el-avatar :size="56" :src="form.avatar || ''">{{ initial }}</el-avatar>
              <label class="upload-btn">
                <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" hidden @change="onAvatarSelected" />
                {{ uploading ? '上传中…' : '上传新头像' }}
              </label>
              <span class="avatar-hint">支持 jpg / png，不超过 5MB</span>
            </div>
          </el-form-item>

          <el-form-item label="用户名">
            <el-input :model-value="user?.username" disabled />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" maxlength="20" show-word-limit placeholder="给自己取个昵称" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="20" placeholder="方便同学联系你（可选）" />
          </el-form-item>

          <div class="footer-actions">
            <el-button size="large" @click="logout">退出登录</el-button>
            <el-button type="primary" size="large" :loading="saving" @click="save">保存修改</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import userStore from '../store/user'

const router = useRouter()

const user = ref(userStore.state.user)
const saving = ref(false)
const uploading = ref(false)
const formRef = ref(null)

const form = reactive({ nickname: '', phone: '', avatar: '' })

const initial = computed(() => {
  const name = user.value?.nickname || user.value?.username || 'U'
  return name.slice(0, 1)
})

const rules = {
  nickname: [{ max: 20, message: '昵称最多 20 个字符', trigger: 'blur' }],
  phone: [{ pattern: /^[0-9]{0,20}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const data = await api.me()
    user.value = data
    userStore.setUser(data)
    form.nickname = data.nickname || ''
    form.phone = data.phone || ''
    form.avatar = data.avatar || ''
  } catch (e) {
    // 统一错误处理
  }
})

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const res = await api.uploadImage(file)
    form.avatar = res.url
    ElMessage.success('头像已上传，记得点击保存')
  } finally {
    uploading.value = false
  }
}

async function save() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    const data = await api.updateProfile({
      nickname: form.nickname || undefined,
      phone: form.phone || undefined,
      avatar: form.avatar || undefined
    })
    user.value = data
    userStore.setUser(data)
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

function logout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  align-items: start;
}

.side-card {
  padding: 30px 24px;
  text-align: center;
}

.big-avatar {
  box-shadow: 0 12px 26px rgba(20, 121, 95, 0.16);
}

.side-card h3 {
  margin: 16px 0 4px;
  font-size: 18px;
}

.username {
  margin: 0;
  color: var(--cm-muted);
  font-size: 13px;
}

.joined {
  margin: 6px 0 0;
  color: #a2afab;
  font-size: 12px;
}

.side-actions {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px dashed var(--cm-border);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.side-link {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  color: #40514c;
  transition: all 0.2s;
}

.side-link:hover {
  background: var(--cm-primary-light);
  color: var(--cm-primary);
}

.main-card {
  padding: 28px 32px 30px;
}

.card-title {
  margin: 0 0 20px;
  font-size: 16px;
  font-weight: 700;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.upload-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #b9d6cb;
  background: #f7fbf9;
  color: #3f7361;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-btn:hover {
  border-color: var(--cm-primary);
  background: var(--cm-primary-light);
}

.avatar-hint {
  font-size: 12px;
  color: #a2afab;
}

.footer-actions {
  margin-top: 26px;
  padding-top: 22px;
  border-top: 1px solid var(--cm-border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.footer-actions .el-button {
  min-width: 120px;
}

@media (max-width: 860px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
  .main-card {
    padding: 20px 16px;
  }
}
</style>
