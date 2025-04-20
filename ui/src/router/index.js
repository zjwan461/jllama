import Vue from 'vue'
import VueRouter from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

Vue.use(VueRouter)

const routes = [{
  path: '/',
  redirect: '/login'
},
{
  path: '/login',
  name: 'login',
  // route level code-splitting
  // this generates a separate chunk (about.[hash].js) for this route
  // which is lazy-loaded when the route is visited.
  component: () => import('../components/login.vue')
},
{
  path: '/register',
  name: 'register',
  component: () => import('../components/register.vue')
},
{
  path: '/home',
  name: 'home',
  component: () => import('../components/home.vue'),
  redirect: '/welcome',
  children: [{
    path: '/welcome',
    name: 'welcome',
    component: () => import('../components/welcome.vue')
  }, {
    path: '/setting',
    name: 'setting',
    component: () => import('../components/setting.vue')
  }, {
    path: '/watch',
    name: 'watch',
    component: () => import('../components/bussiness/watch.vue')
  }, {
    path: '/mgn',
    name: 'mgn',
    component: () => import('../components/bussiness/mgn.vue')
  }, {
    path: '/history',
    name: 'watch',
    component: () => import('../components/bussiness/history.vue')
  }, {
    path: '/tools',
    name: 'tools',
    component: () => import('../components/bussiness/tools.vue')
  },{
    path: '/tools/split-merge',
    name: 'split-merge',
    component: () => import('../components/bussiness/tools/split-merge.vue')
  }
  ]
}]

const router = new VueRouter({
  routes
})

NProgress.configure({
  easing: 'ease', // 动画方式
  speed: 100, // 递增进度条的速度
  showSpinner: false, // 是否显示加载ico
  trickleSpeed: 200, // 自动递增间隔
  minimum: 0.3 // 初始化时的最小百分比
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  console.log(to.path)
  if (to.path === '/login' || to.path === '/register') return next()
  const login = window.sessionStorage.getItem('login')
  if (!login) return next('/login')
  next()
})

router.afterEach(() => {
  NProgress.done() // 关闭进度条
})

export default router
