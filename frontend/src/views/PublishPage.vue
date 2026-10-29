<template>
  <div class="page">
    <div class="head-row">
      <div>
        <h1 class="plain-page-title">{{ isEdit ? '编辑闲置' : '发布闲置' }}</h1>
        <p class="section-sub">拍几张清晰的照片，写清成色和交易方式，更容易被秒下哦</p>
      </div>
      <el-button v-if="isEdit" text type="primary" @click="backToDetail">查看商品详情</el-button>
    </div>

    <div class="cm-card form-card" v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-section">
          <h3>商品图片</h3>
          <div class="upload-grid">
            <div v-for="(img, index) in form.images" :key="img" class="upload-item">
              <img :src="img" :alt="`图片 ${index + 1}`" />
              <span v-if="index === 0" class="cover-badge">封面</span>
              <button class="remove" type="button" title="移除图片" @click="removeImage(index)">×</button>
            </div>
            <label v-if="form.images.length < 9" class="upload-tile" :class="{ disabled: uploading }">
              <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" multiple hidden @change="onSelectFiles" />
              <span class="plus">+</span>
              <span class="upload-text">{{ uploading ? '上传中…' : '上传图片' }}</span>
              <span class="upload-hint">{{ form.images.length }}/9</span>
            </label>
          </div>
          <p class="hint">第一张图片会作为封面，支持 jpg / png / gif / webp，单张不超过 5MB</p>
        </div>

        <div class="form-section">
          <h3>基本信息</h3>
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="例如：《高等数学》同济第八版 上下册，九成新" />
          </el-form-item>

          <div class="two-col">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" class="full">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="成色" prop="conditionLevel">
              <el-select v-model="form.conditionLevel" class="full">
                <el-option v-for="item in conditionOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </div>

          <div class="two-col">
            <el-form-item label="售价（元）" prop="price">
              <el-input-number v-model="form.price" :min="0.01" :max="99999" :precision="2" :step="1" class="full" />
            </el-form-item>
            <el-form-item label="原价（元，选填）">
              <el-input-number v-model="form.originalPrice" :min="0.01" :max="99999" :precision="2" :step="1" class="full" />
            </el-form-item>
          </div>

          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              maxlength="2000"
              show-word-limit
              placeholder="说明使用时长、瑕疵位置、配件是否齐全、交易方式等"
            />
          </el-form-item>
        </div>

        <div class="form-footer">
          <el-button size="large" @click="cancel">取消</el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="submit">
            {{ isEdit ? '保存修改' : '立即发布' }}
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const formRef = ref(null)

const editId = ref(null)
const isEdit = computed(() => Boolean(editId.value))

const form = reactive({
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  categoryId: null,
  conditionLevel: 9,
  images: []
})

const conditionOptions = Array.from({ length: 10 }, (_, i) => ({
  value: i + 1,
  label: i + 1 === 10 ? '全新未使用' : `${i + 1} 成新`
}))

const rules = {
  title: [{ required: true, message: '请填写商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  price: [{ required: true, message: '请填写售价', trigger: 'change' }]
}

onMounted(async () => {
  try {
    categories.value = await api.categories()
  } catch (e) {
    // 统一错误提示
  }
  const id = route.query.id
  if (id) {
    editId.value = id
    await loadProduct(id)
  }
})

async function loadProduct(id) {
  loading.value = true
  try {
    const data = await api.productDetail(id)
    form.title = data.title
    form.description = data.description || ''
    form.price = Number(data.price)
    form.originalPrice = data.originalPrice ? Number(data.originalPrice) : null
    form.categoryId = data.categoryId
    form.conditionLevel = data.conditionLevel || 9
    form.images = data.images?.length ? [...data.images] : data.cover ? [data.cover] : []
  } catch (e) {
    router.replace('/my-products')
  } finally {
    loading.value = false
  }
}

function removeImage(index) {
  form.images.splice(index, 1)
}

async function onSelectFiles(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length) return

  const room = 9 - form.images.length
  if (files.length > room) {
    ElMessage.warning(`最多上传 9 张图片，本次只会处理前 ${room} 张`)
  }

  uploading.value = true
  try {
    for (const file of files.slice(0, room)) {
      if (!file.type.startsWith('image/')) {
        ElMessage.error('只支持上传图片文件')
        continue
      }
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.error(`${file.name} 超过 5MB，已跳过`)
        continue
      }
      const res = await api.uploadImage(file)
      form.images.push(res.url)
    }
  } finally {
    uploading.value = false
  }
}

function buildPayload() {
  return {
    title: form.title.trim(),
    description: form.description?.trim() || null,
    price: form.price,
    originalPrice: form.originalPrice || null,
    categoryId: form.categoryId,
    conditionLevel: form.conditionLevel,
    images: form.images
  }
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (!form.images.length) {
    ElMessage.warning('至少上传一张商品图片')
    return
  }
  submitting.value = true
  try {
    const payload = buildPayload()
    if (isEdit.value) {
      await api.updateProduct(editId.value, payload)
      ElMessage.success('修改已保存')
      router.push(`/product/${editId.value}`)
    } else {
      const created = await api.createProduct(payload)
      ElMessage.success('发布成功，快去邀请同学来看看')
      router.push(`/product/${created.id}`)
    }
  } finally {
    submitting.value = false
  }
}

function cancel() {
  if (isEdit.value) {
    router.push(`/product/${editId.value}`)
  } else {
    router.push('/')
  }
}

function backToDetail() {
  router.push(`/product/${editId.value}`)
}
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

.form-card {
  padding: 30px 34px 26px;
}

.form-section + .form-section {
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px dashed var(--cm-border);
}

.form-section h3 {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 700;
  color: #2c4a41;
}

.upload-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-item {
  position: relative;
  width: 128px;
  height: 128px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--cm-border);
}

.upload-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-badge {
  position: absolute;
  left: 6px;
  bottom: 6px;
  font-size: 11px;
  background: rgba(18, 43, 36, 0.72);
  color: #fff;
  padding: 2px 8px;
  border-radius: 999px;
}

.remove {
  position: absolute;
  right: 6px;
  top: 6px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: none;
  background: rgba(18, 43, 36, 0.65);
  color: #fff;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
}

.remove:hover {
  background: #e2542c;
}

.upload-tile {
  width: 128px;
  height: 128px;
  border-radius: 12px;
  border: 1.5px dashed #b9d6cb;
  background: #f7fbf9;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-tile:hover {
  border-color: var(--cm-primary);
  background: var(--cm-primary-light);
}

.upload-tile.disabled {
  pointer-events: none;
  opacity: 0.7;
}

.plus {
  font-size: 26px;
  color: var(--cm-primary);
  line-height: 1;
}

.upload-text {
  font-size: 13px;
  color: #3f7361;
}

.upload-hint {
  font-size: 11px;
  color: #9aa7a3;
}

.hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--cm-muted);
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.full {
  width: 100%;
}

.form-footer {
  margin-top: 30px;
  padding-top: 22px;
  border-top: 1px solid var(--cm-border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.form-footer .el-button {
  min-width: 130px;
}

@media (max-width: 720px) {
  .form-card {
    padding: 20px 16px;
  }
  .two-col {
    grid-template-columns: 1fr;
    gap: 0;
  }
  .upload-item,
  .upload-tile {
    width: 100px;
    height: 100px;
  }
}
</style>
