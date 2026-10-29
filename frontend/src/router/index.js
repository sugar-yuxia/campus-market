import { createRouter, createWebHistory } from 'vue-router'
import userStore from '../store/user'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomePage.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginPage.vue'),
    meta: { hideHeader: true }
  },
  {
    path: '/product/:id',
    name: 'product-detail',
    component: () => import('../views/ProductDetailPage.vue')
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('../views/PublishPage.vue'),
    meta: { auth: true }
  },
  {
    path: '/my-products',
    name: 'my-products',
    component: () => import('../views/MyProductsPage.vue'),
    meta: { auth: true }
  },
  {
    path: '/orders',
    name: 'orders',
    component: () => import('../views/MyOrdersPage.vue'),
    meta: { auth: true }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfilePage.vue'),
    meta: { auth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.auth && !userStore.state.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
