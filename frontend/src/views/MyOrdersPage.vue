<template>
  <div class="page">
    <h1 class="plain-page-title">我的订单</h1>

    <div class="tabs-row">
      <el-radio-group v-model="role" @change="onRoleChange">
        <el-radio-button label="bought">我买到的</el-radio-button>
        <el-radio-button label="sold">我卖出的</el-radio-button>
      </el-radio-group>

      <el-select v-model="status" class="status-select" @change="onStatusChange">
        <el-option label="全部状态" value="" />
        <el-option label="待付款" value="PENDING_PAY" />
        <el-option label="待收货" value="PAID" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>

    <div v-loading="loading" class="list">
      <div v-for="order in list" :key="order.id" class="order-card cm-card">
        <div class="order-head">
          <span class="order-no">订单号 {{ order.orderNo }}</span>
          <span class="order-time">{{ order.createdAt }}</span>
          <span class="status-tag" :class="`status-${statusClass(order.status)}`">{{ statusText(order.status) }}</span>
        </div>

        <div class="order-body">
          <div class="order-cover" @click="router.push(`/product/${order.productId}`)">
            <img v-if="order.productCover" :src="order.productCover" :alt="order.productTitle" />
            <div v-else class="cover-fallback">闲</div>
          </div>
          <div class="order-main">
            <h3 @click="router.push(`/product/${order.productId}`)">{{ order.productTitle }}</h3>
            <div class="order-meta">
              <span>{{ role === 'bought' ? '卖家' : '买家' }}：{{ role === 'bought' ? order.sellerNickname : order.buyerNickname }}</span>
              <span v-if="order.remark" class="remark">留言：{{ order.remark }}</span>
              <span v-if="order.paidAt">支付时间：{{ order.paidAt }}</span>
              <span v-if="order.finishedAt">完成时间：{{ order.finishedAt }}</span>
            </div>
          </div>
          <div class="order-amount">
            <span class="price">{{ order.amount }}</span>
          </div>
        </div>

        <div v-if="hasActions(order)" class="order-footer">
          <template v-if="role === 'bought' && order.status === 'PENDING_PAY'">
            <el-button size="small" @click="cancel(order)">取消订单</el-button>
            <el-button size="small" type="primary" @click="pay(order)">去支付</el-button>
          </template>
          <template v-if="role === 'bought' && order.status === 'PAID'">
            <el-button size="small" type="success" @click="confirm(order)">确认收货</el-button>
          </template>
          <template v-if="role === 'sold' && order.status === 'PENDING_PAY'">
            <el-button size="small" @click="cancel(order)">关闭交易</el-button>
          </template>
          <template v-if="order.status === 'COMPLETED'">
            <el-button size="small" text @click="router.push(`/product/${order.productId}`)">查看商品</el-button>
          </template>
        </div>
      </div>

      <div v-if="!loading && !list.length" class="cm-card empty-wrap">
        <el-empty :description="role === 'bought' ? '还没有买到的宝贝，去首页逛逛吧' : '还没有卖出的订单，先去发布闲置吧'">
          <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
        </el-empty>
      </div>
    </div>

    <div v-if="total > size" class="pagination-row">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()

const role = ref('bought')
const status = ref('')
const page = ref(1)
const size = 10
const list = ref([])
const total = ref(0)
const loading = ref(false)

const statusMap = {
  PENDING_PAY: '待付款',
  PAID: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

function statusText(value) {
  return statusMap[value] || value
}

function statusClass(value) {
  return {
    PENDING_PAY: 'locked',
    PAID: 'on_sale',
    COMPLETED: 'sold',
    CANCELLED: 'off_shelf'
  }[value] || 'off_shelf'
}

function hasActions(order) {
  return ['PENDING_PAY', 'PAID', 'COMPLETED'].includes(order.status)
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size, status: status.value || undefined }
    const data = role.value === 'bought' ? await api.boughtOrders(params) : await api.soldOrders(params)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onRoleChange() {
  page.value = 1
  load()
}

function onStatusChange() {
  page.value = 1
  load()
}

function onPageChange(value) {
  page.value = value
  load()
}

async function pay(order) {
  await api.payOrder(order.id)
  ElMessage.success('支付成功，等待与卖家完成线下交付')
  load()
}

async function confirm(order) {
  try {
    await ElMessageBox.confirm('确认已经收到商品了吗？确认后商品将标记为已售出。', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '再等等',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await api.confirmOrder(order.id)
  ElMessage.success('交易完成，感谢你让闲置流动起来')
  load()
}

async function cancel(order) {
  try {
    await ElMessageBox.confirm('取消后商品会恢复为在售状态，确定取消这笔订单吗？', '取消订单', {
      confirmButtonText: '取消订单',
      cancelButtonText: '先留着',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await api.cancelOrder(order.id)
  ElMessage.success('订单已取消，商品恢复在售')
  load()
}

onMounted(load)
</script>

<style scoped>
.tabs-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.status-select {
  width: 150px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 120px;
}

.order-card {
  padding: 16px 20px 18px;
}

.order-head {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #8b9a95;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f4f2;
}

.order-no {
  font-weight: 600;
  color: #4c6a60;
}

.order-time {
  margin-left: auto;
}

.order-body {
  display: flex;
  gap: 16px;
  padding: 16px 0 4px;
  align-items: center;
}

.order-cover {
  width: 96px;
  height: 74px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background: #eef4f1;
  cursor: pointer;
}

.order-cover img {
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
  font-weight: 700;
}

.order-main {
  flex: 1;
  min-width: 0;
}

.order-main h3 {
  margin: 0 0 9px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.order-main h3:hover {
  color: var(--cm-primary);
}

.order-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 12px;
  color: #8b9a95;
}

.order-meta .remark {
  color: #c06a12;
}

.order-amount .price {
  font-size: 20px;
}

.order-footer {
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px dashed var(--cm-border);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.empty-wrap {
  padding: 50px 0;
}
</style>
