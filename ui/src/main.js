import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './plugins/element.js'
import { Loading, Message } from 'element-ui'
import './assets/css/global.css'
import axios from 'axios'

let loading

function startLoading() {
  loading = Loading.service({
    lock: true,
    text: '拼命加载中...',
    background: 'rgba(255,255,255,0.5)',
    target: document.querySelector('body')
  })
}

function endLoading() { //  关闭加载动画
  if (loading) {
    loading.close()
  }
}

// axios.defaults.baseURL = 'http://localhost:8888/apis/' 因为在vue.config.js中配置跨域访问所以这里不再需要设置base url
axios.interceptors.request.use(config => {
  startLoading()
  config.headers.token = window.sessionStorage.getItem('token')
  return config
})
axios.interceptors.response.use(config => {
  endLoading()
  // console.log("response", config)
  if (config.status === 200) {
    if (config.data.code !== 0) {
      // console.log("请求异常", config)
    }
  } else if (config.status === 401) {
    return router.replace({
      path: '/login'
    })
  }
  return config
}, error => {
  endLoading()
  Message({
    message: error.message,
    type: 'error'
  })
})
axios.defaults.timeout = 5000// 默认超时时间
Vue.prototype.$http = axios
Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
