<template>
  <article class="p-card" @click="goDetail">
    <div class="cover">
      <img v-if="product.cover" :src="product.cover" :alt="product.title" loading="lazy" />
      <div v-else class="cover-fallback">{{ (product.title || '闲').slice(0, 1) }}</div>
      <span v-if="rank" class="rank" :class="`rank-${rank}`">{{ rank }}</span>
      <span class="cond">{{ conditionText }}</span>
    </div>
    <div class="body">
      <h3 class="title">{{ product.title }}</h3>
      <div class="meta">
        <span class="price">{{ product.price }}</span>
        <span v-if="product.originalPrice" class="origin">¥{{ product.originalPrice }}</span>
        <span class="cat">{{ product.categoryName }}</span>
      </div>
      <div class="foot">
        <span class="seller">{{ product.sellerNickname }}</span>
        <span>{{ product.viewCount || 0 }} 浏览</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  product: { type: Object, required: true },
  rank: { type: Number, default: 0 }
})

const router = useRouter()

const conditionText = computed(() => {
  const level = props.product.conditionLevel
  if (!level) return ''
  return level >= 10 ? '全新' : `${level}成新`
})

function goDetail() {
  router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.p-card {
  background: #fff;
  border: 1px solid var(--cm-border);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.p-card:hover {
  transform: translateY(-4px);
 border-color: #cfe6dd;
  box-shadow: 0 16px 32px rgba(20, 121, 95, 0.12);
}

.cover {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(135deg, #e9f3ef, #f7faf8);
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
  font-size: 34px;
  color: #9cc7b8;
  font-weight: 700;
}

.cond {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(18, 43, 36, 0.72);
  color: #fff;
  font-size: 12px;
  padding: 3px 9px;
  border-radius: 999px;
  backdrop-filter: blur(4px);
}

.rank {
  position: absolute;
  left: 0;
  top: 0;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  border-bottom-right-radius: 14px;
  background: #9aa7a3;
}

.rank-1 {
  background: linear-gradient(135deg, #ff8a3d, #f4572c);
}

.rank-2 {
  background: linear-gradient(135deg, #ffb056, #ff8a3d);
}

.rank-3 {
  background: linear-gradient(135deg, #52b394, #14795f);
}

.body {
  padding: 13px 14px 15px;
}

.title {
  margin: 0 0 9px;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
  height: 44px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta {
  display: flex;
  align-items: baseline;
  gap: 7px;
}

.price {
  font-size: 20px;
}

.origin {
  color: #a6b3af;
  font-size: 12px;
  text-decoration: line-through;
}

.cat {
  margin-left: auto;
  font-size: 12px;
  color: var(--cm-primary);
  background: var(--cm-primary-light);
  padding: 2px 8px;
  border-radius: 999px;
}

.foot {
  margin-top: 11px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a29d;
}

.seller {
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
