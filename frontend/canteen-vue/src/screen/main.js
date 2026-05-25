import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import 'material-symbols/outlined.css'
import './screen-style.css'

const app = createApp(App)
app.use(createPinia())
app.mount('#app')
