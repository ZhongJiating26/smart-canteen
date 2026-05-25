import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './admin-style.css'

// Material Icons font (local, no Google Fonts dependency)
import 'material-symbols/outlined.css'
// Register Material 3 web components
import '@material/web/textfield/outlined-text-field.js'
import '@material/web/switch/switch.js'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
