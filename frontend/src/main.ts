import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'

// Create app instance
const app = createApp(App)

// Setup plugins
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// Initialize user store before mounting the app
const userStore = useUserStore(pinia)

// Only mount the app once user state is initialized
userStore.initialize().finally(() => {
  console.log('App initialization complete, mounting app')
  app.mount('#app')
})
