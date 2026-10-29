<template>
  <div class="page detail-page" v-loading="loading">
    <div class="crumb">
      <router-link to="/">首页</router-link>
      <span>/</span>
      <span>{{ product?.categoryName || '商品详情' }}</span>
    </div>

    <div v-if="product" class="detail-card cm-card">
      <div class="gallery">
        <div class="main-image">
          <img v-if="currentImage" :src="currentImage" :alt="product.title" />
          <div v-else class="image-fallback">{{ (product.title || '闲').slice(0, 1) }}</div>
          <span class="status-badge" :class="`status-${(product.status || '').toLowerCase()}`">
            {{ statusText }}
          </span>
        </div>
        <div v-if="images.length > 1" class="thumbs">
          <button
            v-for="(img, index) in images"
            :key="img + index"
            class="thumb"
            :class="{ active: index === imageIndex }"
            @click="imageIndex = index"
          >
            <img :src="img" :alt="`图片 ${index + 1}`" />
          </button>
        </div>
      </div>

      <div class="info">
        <h1 class="title">{{ product.title }}</h1>

        <div class="price-row">
          <span class="price big">{{ product.price }}</span>
          <span v-if="product.originalPrice" class="origin">原价 ¥{{ product.originalPrice }}</span>
          <span v-if="discount" class="discount">省 {{ discount }}</span>
        </div>

        <div class="tags">
          <span class="tag">{{ product.categoryName || '未分类' }}</span>
          <span class="tag">{{ conditionText }}</span>
          <span class="tag">{{ product.viewCount || 0 }} 次浏览</span>
          <span class="tag">发布于 {{ formatDate(product.createdAt) }}</span>
        </div>

        <div class="desc-block">
          <h4>商品描述</h4>
          <p>{{ product.description || '卖家很懒，没有填写描述。' }}</p>
        </div>

        <div class="seller">
          <el-avatar :size="44" :src="product.sellerAvatar || ''">
            {{ (product.sellerNickname || '卖').slice(0, 1) }}
          </el-avatar>
          <div class="seller-info">
            <strong>{{ product.sellerNickname }}</strong>
            <span>同校卖家 · 支持当面交易</span>
          </div>
        </div>

        <div class="actions">
          <template v-if="isOwner">
            <el-button type="primary" size="large" @click="goEdit">编辑商品</el-button>
            <el-button size="large" @click="goManage">管理我的闲置</el-button>
          </template>
          <template v-else-if="product.status === 'ON_SALE'">
            <el-button type="primary" size="large" class="buy-btn" @click="openBuy">立即购买</el-button>
            <span class="tip">下单后商品将被锁定，请在订单页完成支付</span>
          </template>
          <template v-else>
            <el-button size="large" disabled>{{ statusText }}，暂时无法购买</el-button>
          </template>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="cm-card empty-detail">
      <el-empty description="商品不存在或已被删除">
        <el-button type="primary" round @click="router.push('/')">回到首页</el-button>
      </el-empty>
    </div>

    <el-dialog v-model="buyVisible" title="确认下单" width="420px">
      <div v-if="product" class="buy-dialog">
        <div class="buy-line">
          <span>商品</span>
          <strong>{{ product.title }}</strong>
        </div>
        <div class="buy-line">
          <span>金额</span>
          <span class="price">{{ product.price }}</span>
        </div>
        <div class="buy-line remark">
          <span>留言</span>
          <el-input v-model="remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="约个时间地点，比如：周三下午图书馆门口" />
        </div>
      </div>
      <template #footer>
        <el-button @click="buyVisible = false">再想想</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmBuy">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import userStore from '../store/user'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const images = ref([])
const imageIndex = ref(0)
const loading = ref(false)
const buyVisible = ref(false)
const submitting = ref(false)
const remark = ref('')

const currentImage = computed(() => images.value[imageIndex.value] || product.value?.cover || '')

const isOwner = computed(() => product.value && userStore.state.user?.id === product.value.sellerId)

const conditionText = computed(() => {
  const level = product.value?.conditionLevel
  if (!level) return '成色未知'
  return level >= 10 ? '全新未使用' : `${level} 成新`
})

const statusText = computed(() => {
  const map = {
    ON_SALE: '在售',
    OFF_SHELF: '已下架',
    LOCKED: '交易中',
    SOLD: '已售出'
  }
  return map[product.value?.status] || '未知状态'
})

const discount = computed(() => {
  const price = Number(product.value?.price)
  const origin = Number(product.value?.originalPrice)
  if (!origin || origin <= price) return ''
  return `¥${(origin - price).toFixed(2)}`
})

function formatDate(value) {
  return value ? value.slice(0, 10) : ''
}

async function load() {
  loading.value = true
  try {
    const data = await api.productDetail(route.params.id)
    product.value = data
    images.value = data.images?.length ? data.images : data.cover ? [data.cover] : []
    imageIndex.value = 0
  } catch (e) {
    product.value = null
  } finally {
    loading.value = false
  }
}

function goEdit() {
  router.push({ path: '/publish', query: { id: product.value.id } })
}

function goManage() {
  router.push('/my-products')
}

function openBuy() {
  if (!userStore.state.token) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  remark.value = ''
  buyVisible.value = true
}

async function confirmBuy() {
  submitting.value = true
  try {
    const order = await api.createOrder({ productId: product.value.id, remark: remark.value || undefined })
    buyVisible.value = false
    ElMessage.success(`下单成功，订单号 ${order.orderNo}，商品已为你锁定`)
    router.push('/orders')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.detail-page {
  padding-top: 20px;
}

.crumb {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: var(--cm-muted);
  margin-bottom: 14px;
}

.crumb a:hover {
  color: var(--cm-primary);
}

.detail-card {
  display: grid;
  grid-template-columns: 1.02fr 0.98fr;
  gap: 34px;
  padding: 28px;
}

.main-image {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(135deg, #e9f3ef, #f7faf8);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 54px;
  color: #9cc7b8;
  font-weight: 700;
}

.status-badge {
  position: absolute;
  left: 14px;
  top: 14px;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(18, 43, 36, 0.7);
}

.status-sold {
  background: #5b5fbe;
}

.status-locked {
  background: #c06a12;
}

.status-off_shelf {
  background: #6b7a75;
}

.thumbs {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.thumb {
  width: 72px;
  height: 58px;
  border-radius: 10px;
  overflow: hidden;
  padding: 0;
  border: 2px solid transparent;
  background: #fff;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.2s;
}

.thumb.active,
.thumb:hover {
  border-color: var(--cm-primary);
  opacity: 1;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.info {
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0;
  font-size: 26px;
  line-height: 1.45;
  letter-spacing: 0.5px;
}

.price-row {
  margin-top: 18px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price.big {
  font-size: 32px;
}

.origin {
  color: #a6b3af;
  text-decoration: line-through;
  font-size: 13px;
}

.discount {
  font-size: 12px;
  color: #ff8a3d;
  background: #fff3e8;
  padding: 3px 9px;
  border-radius: 999px;
}

.tags {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  font-size: 12px;
  color: #4c6a60;
  background: #f2f6f4;
  border: 1px solid var(--cm-border);
  padding: 5px 11px;
  border-radius: 999px;
}

.desc-block {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px dashed var(--cm-border);
}

.desc-block h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--cm-muted);
  font-weight: 600;
}

.desc-block p {
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  color: #40514c;
  white-space: pre-wrap;
}

.seller {
  margin-top: auto;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 14px;
  background: #f7faf8;
  border: 1px solid var(--cm-border);
}

.seller-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.seller-info strong {
  font-size: 14px;
}

.seller-info span {
  font-size: 12px;
  color: var(--cm-muted);
}

.actions {
  margin-top: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.buy-btn {
  min-width: 150px;
  letter-spacing: 2px;
}

.tip {
  font-size: 12px;
  color: var(--cm-muted);
}

.empty-detail {
  padding: 60px 0;
}

.buy-dialog {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.buy-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  font-size: 14px;
}

.buy-line > span:first-child {
  color: var(--cm-muted);
  flex-shrink: 0;
}

.buy-line strong {
  text-align: right;
  font-weight: 600;
}

.buy-line.remark {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

@media (max-width: 900px) {
  .detail-card {
    grid-template-columns: 1fr;
    gap: 22px;
    padding: 18px;
  }
  .title {
    font-size: 21px;
  }
}
</style>
