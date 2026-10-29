<template>
  <div class="app-shell">
    <AppHeader v-if="!route.meta.hideHeader" />
    <main :class="['app-main', { 'with-header': !route.meta.hideHeader }]">
      <router-view />
    </main>
    <footer v-if="!route.meta.hideHeader" class="app-footer">
      <span>校园二手交易平台 · Spring Boot + MyBatis + MySQL + Redis + Vue3</span>
    </footer>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'

const route = useRoute()
</script>
