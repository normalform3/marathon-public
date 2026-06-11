import { createApp } from 'vue'
import router from './router'
//引入element
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme.css'


//组件 根基
import App from './App.vue'

//创建应用，以app作为根组件，挂载到#app容器
const app = createApp(App)

app.use(ElementPlus)
app.use(router)
app.mount('#app')
