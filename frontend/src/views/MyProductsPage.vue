<template>
  <div class="page">
    <div class="head-row">
      <div>
        <h1 class="plain-page-title">我的闲置</h1>
        <p class="section-sub">共发布 {{ total }} 件商品，可随时上下架或修改信息</p>
      </div>
      <el-button type="primary" round @click="router.push('/publish')">+ 发布闲置</el-button>
    </div>

    <div class="filter-bar">
      <button
        v-for="item in statusFilters"
        :key="item.value"
        class="chip"
        :class="{ active: state.status === item.value }"
        @click="changeStatus(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-loading="loading" class="list">
      <div v-for="item in list" :key="item.id" class="mine-item cm-card">
        <div class="cover" @click="router.push(`/product/${item.id}`)">
          <img v-if="item.cover" :src="item.cover" :alt="item.title" />
          <div v-else class="cover-fallback">{{ (item.title || '闲').slice(0, 1) }}</div>
        </div>
        <div class="main">
          <div class="row-title">
            <h3 @click="router.push(`/product/${item.id}`)">{{ item.title }}</h3>
            <span class="status-tag" :class="`status-${(item.status || '').toLowerCase()}`">{{ statusText(item.status) }}</span>
          </div>
          <div class="row-meta">
            <span class="price">{{ item.price }}</span>
            <span v-if="item.originalPrice" class="origin">原价 ¥{{ item.originalPrice }}</span>
            <span class="dot">·</span>
            <span>{{ item.categoryName }}</span>
            <span class="dot">·</span>
            <span>{{ item.conditionLevel >= 10 ? '全新' : `${item.conditionLevel} 成新` }}</span>
            <span class="dot">·</span>
            <span>{{ item.viewCount || 0 }} 次浏览</span>
          </div>
          <div class="row-actions">
            <el-button size="small" @click="router.push(`/product/${item.id}`)">查看</el-button>
            <el-button v-if="editable(item)" size="small" @click="router.push({ path: '/publish', query: { id: item.id } })">编辑</el-button>
            <el-button
              v-if="editable(item)"
              size="small"
              :type="item.status === 'ON_SALE' ? 'warning' : 'success'"
              plain
              @click="toggleStatus(item)"
            >
              {{ item.status === 'ON_SALE' ? '下架' : '重新上架' }}
            </el-button>
            <el-button v-if="editable(item)" size="small" type="danger" text @click="remove(item)">删除</el-button>
            <span v-if="!editable(item)" class="locked-tip">
              {{ item.status === 'LOCKED' ? '有买家已下单，先处理订单吧' : '已售出的商品不可编辑' }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="!loading && !list.length" class="cm-card empty-wrap">
        <el-empty description="这里还没有商品，发布你的第一件闲置吧">
          <el-button type="primary" round @click="router.push('/publish')">去发布</el-button>
        </el-empty>
      </div>
    </div>

    <div v-if="total > state.size" class="pagination-row">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="state.size"
        :current-page="state.page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()

const list = ref([])
const total = ref(0)
const loading = ref(false)

const state = reactive({ page: 1, size: 10, status: '' })

const statusFilters = [
  { label: '全部', value: '' },
  { label: '在售', value: 'ON_SALE' },
  { label: '已下架', value: 'OFF_SHELF' },
  { label: '交易中', value: 'LOCKED' },
  { label: '已售出', value: 'SOLD' }
]

const statusMap = {
  ON_SALE: '在售',
  OFF_SHELF: '已下架',
  LOCKED: '交易中',
  SOLD: '已售出'
}

function statusText(status) {
  return statusMap[status] || status
}

function editable(item) {
  return item.status === 'ON_SALE' || item.status === 'OFF_SHELF'
}

async function load() {
  loading.value = true
  try {
    const data = await api.myProducts({
      page: state.page,
      size: state.size,
      status: state.status || undefined
    })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function changeStatus(value) {
  state.status = value
  state.page = 1
  load()
}

function onPageChange(page) {
  state.page = page
  load()
}

async function toggleStatus(item) {
  const target = item.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE'
  await api.updateProductStatus(item.id, target)
  ElMessage.success(target === 'ON_SALE' ? '已重新上架' : '已下架')
  load()
}

async function remove(item) {
  try {
    await ElMessageBox.confirm(`确定删除「${item.title}」吗？删除后无法恢复。`, '删除商品', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await api.deleteProduct(item.id)
  ElMessage.success('已删除')
  if (list.value.length === 1 && state.page > 1) {
    state.page -= 1
  }
  load()
}

onMounted(load)
</script>

<style scoped>
.head-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.head-row .plain-page-title {
  margin-bottom: 6px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.chip {
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid var(--cm-border);
  background: #fff;
  color: var(--cm-muted);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.chip.active {
  background: var(--cm-primary);
  border-color: var(--cm-primary);
  color: #fff;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 120px;
}

.mine-item {
  display: flex;
  gap: 18px;
  padding: 16px;
  align-items: stretch;
}

.cover {
  width: 150px;
  height: 112px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  background: #eef4f1;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9cc7b8;
  font-size: 28px;
  font-weight: 700;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.row-title {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.row-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  cursor: pointer;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.row-title h3:hover {
  color: var(--cm-primary);
}

.row-meta {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #8b9a95;
}

.row-meta .price {
  font-size: 20px;
}

.origin {
  text-decoration: line-through;
  color: #aebbb7;
}

.dot {
  color: #d3ddd9;
}

.row-actions {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.locked-tip {
  font-size: 12px;
  color: #b98600;
}

.empty-wrap {
  padding: 50px 0;
}

@media (max-width: 720px) {
  .mine-item {
    flex-direction: column;
  }
  .cover {
    width: 100%;
    height: 170px;
  }
}
</style>
