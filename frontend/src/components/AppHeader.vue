<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="brand">
        <span class="brand-mark">校</span>
        <span class="brand-text">
          <strong>校园二手</strong>
          <small>让闲置流动起来</small>
        </span>
      </router-link>

      <nav class="nav">
        <router-link to="/" class="nav-link" :class="{ active: route.path === '/' }">首页</router-link>
        <router-link to="/publish" class="nav-link" :class="{ active: route.path === '/publish' }">发布闲置</router-link>
        <router-link to="/my-products" class="nav-link" :class="{ active: route.path === '/my-products' }">我的闲置</router-link>
        <router-link to="/orders" class="nav-link" :class="{ active: route.path === '/orders' }">我的订单</router-link>
      </nav>

      <div class="header-right">
        <el-input
          v-model="keyword"
          class="search"
          placeholder="搜索教材、数码、生活用品…"
          :prefix-icon="Search"
          clearable
          @keyup.enter="doSearch"
          @clear="doSearch"
        />
        <el-button class="search-btn" type="primary" @click="doSearch">搜索</el-button>

        <template v-if="userStore.state.token">
          <el-dropdown trigger="click" @command="onCommand">
            <div class="user-chip">
              <el-avatar :size="32" :src="avatar">{{ initial }}</el-avatar>
              <span class="user-name">{{ userStore.state.user?.nickname || userStore.state.user?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="my-products">我的闲置</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text class="ghost-btn" @click="goLogin">登录</el-button>
          <el-button type="primary" plain @click="goLogin">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import userStore from '../store/user'

const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword || '')

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = value || ''
  }
)

const avatar = computed(() => userStore.state.user?.avatar || '')
const initial = computed(() => {
  const name = userStore.state.user?.nickname || userStore.state.user?.username || 'U'
  return name.slice(0, 1)
})

function doSearch() {
  router.push({ path: '/', query: keyword.value ? { keyword: keyword.value } : {} })
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

function onCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
    return
  }
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'my-products') {
    router.push('/my-products')
  } else if (command === 'orders') {
    router.push('/orders')
  }
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 68px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--cm-border);
}

.header-inner {
  max-width: 1180px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 150px;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 11px;
  background: linear-gradient(135deg, var(--cm-primary), #3fa987);
  color: #fff;
  font-weight: 700;
  font-size: 17px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 14px rgba(20, 121, 95, 0.24);
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.brand-text strong {
  font-size: 16px;
  letter-spacing: 1px;
}

.brand-text small {
  font-size: 11px;
  color: var(--cm-muted);
  letter-spacing: 0.5px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-link {
  padding: 7px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: var(--cm-muted);
  transition: all 0.2s;
  white-space: nowrap;
}

.nav-link:hover {
  color: var(--cm-primary);
  background: var(--cm-primary-light);
}

.nav-link.active {
  color: var(--cm-primary);
  background: var(--cm-primary-light);
  font-weight: 600;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search {
  width: 260px;
}

.search-btn {
  margin-left: -4px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px 6px 6px;
  border-radius: 999px;
  transition: background 0.2s;
}

.user-chip:hover {
  background: var(--cm-primary-light);
}

.user-name {
  font-size: 14px;
  max-width: 92px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ghost-btn {
  color: var(--cm-muted);
}

@media (max-width: 960px) {
  .nav {
    display: none;
  }
  .search {
    width: 170px;
  }
}

@media (max-width: 640px) {
  .search,
  .search-btn,
  .user-name {
    display: none;
  }
}
</style>
