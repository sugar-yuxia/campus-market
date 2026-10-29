<template>
  <div class="page">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">CAMPUS MARKET · 校园二手</p>
        <h1>把闲置，留给需要的人</h1>
        <p class="desc">
          教材、数码、生活好物…… 每一次转手，都是一次更轻盈的校园生活。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="goPublish">发布闲置</el-button>
          <el-button size="large" round class="light-btn" @click="scrollToList">开始逛逛</el-button>
        </div>
        <div class="hero-points">
          <span>同校当面交易</span>
          <span>真实图片描述</span>
          <span>在线下单留痕</span>
        </div>
      </div>
      <div class="hero-art" aria-hidden="true">
        <div class="art-card art-card-1">
          <span class="art-label">教材书籍</span>
          <span class="art-line"></span>
          <span class="art-line short"></span>
        </div>
        <div class="art-card art-card-2">
          <span class="art-label">数码电子</span>
          <span class="art-line"></span>
          <span class="art-line short"></span>
        </div>
        <div class="art-chip">全新 · 九成新 · 可小刀</div>
      </div>
    </section>

    <section class="category-bar">
      <button class="chip" :class="{ active: !state.categoryId }" @click="selectCategory(null)">全部</button>
      <button
        v-for="cat in categories"
        :key="cat.id"
        class="chip"
        :class="{ active: state.categoryId === cat.id }"
        @click="selectCategory(cat.id)"
      >
        {{ cat.name }}
      </button>
    </section>

    <section v-if="!state.keyword && hot.length" class="section">
      <div class="section-head">
        <div>
          <h2 class="section-title">本周热门</h2>
          <p class="section-sub">Redis 缓存的热门商品，按浏览量实时排序</p>
        </div>
        <el-button text type="primary" @click="loadMoreHot">查看更多</el-button>
      </div>
      <div class="product-grid hot-grid">
        <ProductCard v-for="(item, index) in hot" :key="item.id" :product="item" :rank="index + 1" />
      </div>
    </section>

    <section ref="listSection" class="section all-section">
      <div class="section-head">
        <div>
          <h2 class="section-title">{{ state.keyword ? `“${state.keyword}” 的搜索结果` : '全部闲置' }}</h2>
          <p class="section-sub">共 {{ total }} 件在售商品</p>
        </div>
        <el-select v-model="state.sort" class="sort-select" @change="reload">
          <el-option label="最新发布" value="newest" />
          <el-option label="价格从低到高" value="priceAsc" />
          <el-option label="价格从高到低" value="priceDesc" />
          <el-option label="最多浏览" value="hot" />
        </el-select>
      </div>

      <div v-loading="loading" class="list-wrap">
        <div v-if="list.length" class="product-grid">
          <ProductCard v-for="item in list" :key="item.id" :product="item" />
        </div>
        <div v-else-if="!loading" class="empty-block">
          <el-empty :description="state.keyword ? '没有找到相关闲置，换个关键词试试' : '暂时还没有在售商品，去发布第一件吧'">
            <el-button type="primary" round @click="goPublish">发布闲置</el-button>
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
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
import ProductCard from '../components/ProductCard.vue'
import userStore from '../store/user'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const hot = ref([])
const list = ref([])
const total = ref(0)
const loading = ref(false)
const listSection = ref(null)

const state = reactive({
  page: 1,
  size: 12,
  keyword: '',
  categoryId: null,
  sort: 'newest'
})

watch(
  () => route.query.keyword,
  (value) => {
    state.keyword = value || ''
    state.page = 1
    loadList()
  }
)

onMounted(async () => {
  state.keyword = route.query.keyword || ''
  try {
    categories.value = await api.categories()
    hot.value = await api.hotProducts()
  } catch (e) {
    // 错误提示已在请求拦截器中统一处理
  }
  loadList()
})

async function loadList() {
  loading.value = true
  try {
    const data = await api.products({
      page: state.page,
      size: state.size,
      keyword: state.keyword || undefined,
      categoryId: state.categoryId || undefined,
      sort: state.sort
    })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  state.page = 1
  loadList()
}

function selectCategory(id) {
  state.categoryId = id
  state.page = 1
  loadList()
}

function onPageChange(page) {
  state.page = page
  loadList()
  listSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goPublish() {
  if (userStore.state.token) {
    router.push('/publish')
  } else {
    router.push({ path: '/login', query: { redirect: '/publish' } })
  }
}

function scrollToList() {
  listSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function loadMoreHot() {
  state.sort = 'hot'
  state.page = 1
  loadList()
  scrollToList()
}
</script>

<style scoped>
.hero {
  position: relative;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 20px;
  padding: 44px 46px;
  border-radius: 24px;
  background: linear-gradient(120deg, #0f5f4a 0%, #14795f 45%, #2f9c7c 100%);
  color: #fff;
  overflow: hidden;
}

.hero::after {
  content: '';
  position: absolute;
  width: 420px;
  height: 420px;
  right: -120px;
  top: -180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18), transparent 68%);
}

.hero-copy {
  position: relative;
  z-index: 2;
}

.eyebrow {
  margin: 0 0 14px;
  font-size: 12px;
  letter-spacing: 3px;
  text-transform: uppercase;
  opacity: 0.75;
}

.hero-copy h1 {
  margin: 0;
  font-size: 40px;
  line-height: 1.25;
  letter-spacing: 1px;
}

.desc {
  margin: 16px 0 0;
  max-width: 460px;
  font-size: 15px;
  line-height: 1.8;
  opacity: 0.86;
}

.hero-actions {
  margin-top: 28px;
  display: flex;
  gap: 12px;
}

.light-btn {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.35);
  color: #fff;
}

.light-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: #fff;
  color: #fff;
}

.hero-points {
  margin-top: 26px;
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13px;
  opacity: 0.8;
}

.hero-points span::before {
  content: '·';
  margin-right: 6px;
  color: #9fe6cf;
}

.hero-art {
  position: relative;
  z-index: 2;
  min-height: 250px;
}

.art-card {
  position: absolute;
  width: 190px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.26);
  backdrop-filter: blur(6px);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.12);
}

.art-card-1 {
  top: 6px;
  left: 12px;
  transform: rotate(-6deg);
}

.art-card-2 {
  top: 84px;
  left: 92px;
  transform: rotate(5deg);
  background: rgba(255, 255, 255, 0.2);
}

.art-label {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
}

.art-line {
  display: block;
  height: 6px;
  margin-top: 12px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.32);
}

.art-line.short {
  width: 62%;
  margin-top: 8px;
  background: rgba(255, 255, 255, 0.2);
}

.art-chip {
  position: absolute;
  bottom: 4px;
  right: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  background: #ff8a3d;
  color: #fff;
  font-size: 12px;
  box-shadow: 0 10px 24px rgba(255, 138, 61, 0.35);
}

.category-bar {
  margin-top: 22px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip {
  padding: 9px 18px;
  border-radius: 999px;
  border: 1px solid var(--cm-border);
  background: #fff;
  color: var(--cm-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.chip:hover {
  border-color: #9ecfbe;
  color: var(--cm-primary);
}

.chip.active {
  background: var(--cm-primary);
  border-color: var(--cm-primary);
  color: #fff;
  box-shadow: 0 8px 18px rgba(20, 121, 95, 0.22);
}

.sort-select {
  width: 150px;
}

.list-wrap {
  min-height: 200px;
}

@media (max-width: 860px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 30px 24px;
  }
  .hero-art {
    display: none;
  }
  .hero-copy h1 {
    font-size: 28px;
  }
}
</style>
