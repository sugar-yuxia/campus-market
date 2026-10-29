<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="panel-brand">
        <span class="panel-mark">校</span>
        <div>
          <strong>校园二手</strong>
          <p>让闲置流动起来</p>
        </div>
      </div>
      <h1>把用不上的好物<br />交给下一位同学</h1>
      <p class="panel-desc">教材、数码、生活用品，同校当面交易，省心又环保。</p>
      <ul class="panel-list">
        <li>真实图片 + 成色描述，避免踩坑</li>
        <li>在线下单，订单状态全程可查</li>
        <li>热门榜单由 Redis 缓存加速</li>
      </ul>
      <div class="panel-bubbles" aria-hidden="true">
        <span class="bubble b1"></span>
        <span class="bubble b2"></span>
        <span class="bubble b3"></span>
      </div>
    </div>

    <div class="auth-form-wrap">
      <div class="auth-card">
        <router-link to="/" class="back-link">← 返回首页</router-link>
        <el-tabs v-model="tab" class="auth-tabs" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top" @submit.prevent>
              <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" :prefix-icon="User" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="loginForm.password"
                  size="large"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  @keyup.enter="submitLogin"
                />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="submitting" @click="submitLogin">
                登录
              </el-button>
            </el-form>
            <div class="demo-tip">
              <span>演示账号：demo / 123456（买家）</span>
              <el-button link type="primary" @click="fillDemo('demo')">填入</el-button>
            </div>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top" @submit.prevent>
              <el-form-item label="用户名" prop="username">
                <el-input v-model="registerForm.username" size="large" placeholder="3-20 位字母、数字或下划线" :prefix-icon="User" />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="registerForm.nickname" size="large" placeholder="同学们怎么称呼你（可选）" :prefix-icon="Avatar" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="registerForm.password"
                  size="large"
                  type="password"
                  show-password
                  placeholder="6-20 位密码"
                  :prefix-icon="Lock"
                  @keyup.enter="submitRegister"
                />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="submitting" @click="submitRegister">
                注册并登录
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar } from '@element-plus/icons-vue'
import api from '../api'
import userStore from '../store/user'

const route = useRoute()
const router = useRouter()

const tab = ref('login')
const submitting = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', nickname: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '用户名为 3-20 位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ]
}

function fillDemo(username) {
  loginForm.username = username
  loginForm.password = '123456'
  tab.value = 'login'
}

async function afterAuth(data, tip) {
  userStore.setSession(data.token, data.user)
  ElMessage.success(tip)
  const redirect = route.query.redirect
  router.replace(redirect ? decodeURIComponent(redirect) : '/')
}

async function submitLogin() {
  try {
    await loginFormRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const data = await api.login({ username: loginForm.username.trim(), password: loginForm.password })
    await afterAuth(data, `欢迎回来，${data.user.nickname || data.user.username}`)
  } finally {
    submitting.value = false
  }
}

async function submitRegister() {
  try {
    await registerFormRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const data = await api.register({
      username: registerForm.username.trim(),
      password: registerForm.password,
      nickname: registerForm.nickname.trim() || undefined
    })
    await afterAuth(data, '注册成功，已自动登录')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  background: #f4f7f5;
}

.auth-panel {
  position: relative;
  overflow: hidden;
  padding: 64px clamp(32px, 6vw, 88px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(150deg, #0d5442 0%, #14795f 52%, #2f9c7c 100%);
  color: #fff;
}

.panel-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-mark {
  width: 44px;
  height: 44px;
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 20px;
}

.panel-brand strong {
  font-size: 18px;
  letter-spacing: 1.5px;
}

.panel-brand p {
  margin: 2px 0 0;
  font-size: 12px;
  opacity: 0.7;
  letter-spacing: 2px;
}

.auth-panel h1 {
  margin: 54px 0 0;
  font-size: clamp(30px, 3.4vw, 46px);
  line-height: 1.35;
  letter-spacing: 1px;
}

.panel-desc {
  margin: 18px 0 0;
  max-width: 420px;
  font-size: 15px;
  line-height: 1.8;
  opacity: 0.82;
}

.panel-list {
  margin: 40px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-size: 14px;
  opacity: 0.92;
}

.panel-list li::before {
  content: '—';
  margin-right: 10px;
  color: #9fe6cf;
}

.panel-bubbles .bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.bubble.b1 {
  width: 240px;
  height: 240px;
  right: -70px;
  bottom: -60px;
}

.bubble.b2 {
  width: 120px;
  height: 120px;
  right: 90px;
  bottom: 120px;
  background: rgba(255, 138, 61, 0.16);
}

.bubble.b3 {
  width: 64px;
  height: 64px;
  right: 190px;
  top: 70px;
}

.auth-form-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.auth-card {
  width: min(430px, 100%);
  background: #fff;
  border-radius: 20px;
  padding: 28px 34px 34px;
  box-shadow: 0 24px 60px rgba(20, 60, 50, 0.1);
}

.back-link {
  font-size: 13px;
  color: var(--cm-muted);
}

.back-link:hover {
  color: var(--cm-primary);
}

.auth-tabs {
  margin-top: 14px;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
  letter-spacing: 4px;
}

.demo-tip {
  margin-top: 18px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--cm-primary-light);
  font-size: 12px;
  color: #3f7361;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }
  .auth-panel {
    display: none;
  }
  .auth-form-wrap {
    min-height: 100vh;
  }
}
</style>
