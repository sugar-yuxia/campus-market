import axios from 'axios'
import { ElMessage } from 'element-plus'
import userStore from '../store/user'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  if (userStore.state.token) {
    config.headers.Authorization = `Bearer ${userStore.state.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== 200) {
        ElMessage.error(body.message || '操作失败')
        return Promise.reject(new Error(body.message || '操作失败'))
      }
      return body.data
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message
    if (status === 401) {
      userStore.logout()
      ElMessage.error(message || '登录已过期，请重新登录')
      if (!location.pathname.startsWith('/login')) {
        const redirect = encodeURIComponent(location.pathname + location.search)
        location.href = `/login?redirect=${redirect}`
      }
    } else if (status === 404 && message) {
      ElMessage.error(message)
    } else {
      ElMessage.error(message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export const api = {
  // 认证与用户
  register: (data) => http.post('/auth/register', data),
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/users/me'),
  updateProfile: (data) => http.put('/users/me', data),
  userDetail: (id) => http.get(`/users/${id}`),

  // 分类
  categories: () => http.get('/categories'),

  // 商品
  products: (params) => http.get('/products', { params }),
  hotProducts: () => http.get('/products/hot'),
  productDetail: (id) => http.get(`/products/${id}`),
  myProducts: (params) => http.get('/products/mine', { params }),
  createProduct: (data) => http.post('/products', data),
  updateProduct: (id, data) => http.put(`/products/${id}`, data),
  updateProductStatus: (id, status) => http.put(`/products/${id}/status`, { status }),
  deleteProduct: (id) => http.delete(`/products/${id}`),

  // 图片上传
  uploadImage: (file) => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/uploads/image', form)
  },

  // 订单
  createOrder: (data) => http.post('/orders', data),
  boughtOrders: (params) => http.get('/orders/bought', { params }),
  soldOrders: (params) => http.get('/orders/sold', { params }),
  orderDetail: (id) => http.get(`/orders/${id}`),
  payOrder: (id) => http.put(`/orders/${id}/pay`),
  confirmOrder: (id) => http.put(`/orders/${id}/confirm`),
  cancelOrder: (id) => http.put(`/orders/${id}/cancel`)
}

export default api
