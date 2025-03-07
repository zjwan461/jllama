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
    path: '/aboutme',
    name: 'aboutme',
    component: () => import('../components/aboutme.vue')
  }, {
    path: '/userlist/:menuId',
    name: 'userlist',
    component: () => import('../components/user/userlist.vue')
  }, {
    path: '/rolelist/:menuId',
    name: 'rolelist',
    component: () => import('../components/rights/rolelist.vue')
  }, {
    path: '/rightlist/:menuId',
    name: 'rightlist',
    component: () => import('../components/rights/rightlist.vue')
  }, {
    path: '/page/bussiness/editInfo.html/:memuId',
    name: 'editInfo',
    component: () => import('../components/bussiness/editInfo.vue')
  }, {
    path: '/page/bussiness/changePwd.html/:memuId',
    name: 'changePwd',
    component: () => import('../components/bussiness/changePwd.vue')
  }, {
    path: '/page/product/productGroupList.html/:memuId',
    name: 'productGroupList',
    component: () => import('../components/service/productGroupList.vue')
  }, {
    path: '/page/product/productList.html/:memuId',
    name: 'productList',
    component: () => import('../components/service/productList.vue')
  }, {
    path: '/page/product/download.html/:memuId',
    name: 'download',
    component: () => import('../components/service/download.vue')
  }, {
    path: '/page/product/teach.html/:memuId',
    name: 'teach',
    component: () => import('../components/service/teach.vue')
  }, {
    path: '/page/order/orderList.html/:memuId',
    name: 'orderList',
    component: () => import('../components/order/orderList.vue')
  }, {
    path: '/page/order/resend.html/:memuId',
    name: 'resend',
    component: () => import('../components/order/resend.vue')
  }, {
    path: '/page/settle/settleList.html/:memuId',
    name: 'settleList',
    component: () => import('../components/settle/settleList.vue')
  }, {
    path: '/page/sys/settleMgr.html/:memuId',
    name: 'settleMgr',
    component: () => import('../components/sys/settleMgr.vue')
  }, {
    path: '/page/sys/mainSetting.html/:memuId',
    name: 'mainSetting',
    component: () => import('../components/sys/mainSetting.vue')
  }, {
    path: '/page/approve/approveList.html/:memuId',
    name: 'approveList',
    component: () => import('../components/sys/approveList.vue')
  }, {
    path: '/page/order/orderMgr.html/:memuId',
    name: 'orderMgr',
    component: () => import('../components/sys/orderMgr.vue')
  }, {
    path: '/page/sys/userMgr.html/:memuId',
    name: 'userMgr',
    component: () => import('../components/sys/userMgr.vue')
  }, {
    path: '/page/sys/onlineMgr.html/:memuId',
    name: 'onlineMgr',
    component: () => import('../components/sys/onlineMgr.vue')
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
  const token = window.sessionStorage.getItem('token')
  if (!token) return next('/login')
  next()
})

router.afterEach(() => {
  NProgress.done() // 关闭进度条
})

export default router
