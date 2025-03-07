<template>
  <el-container class="home-container">
    <el-header class="header">
      <!-- 头部导航区域 -->
      <div>
        <img
          src="../assets/logo.png"
          width="100"
          alt
        >
        <span>九节兑换平台</span>
      </div>
      <el-dropdown @command="handleCommand">
        <div class="el-dropdown-link">
          <img
            src="../assets/logo.png"
            alt
            width="150"
          >
          <i class="el-icon-arrow-down el-icon--right" />
        </div>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="aboutme">
            关于我
          </el-dropdown-item>
          <el-dropdown-item
            divided
            command="logout"
          >
            退出
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <!-- <el-button >退出</el-button> -->
    </el-header>
    <el-container>
      <!-- 左侧菜单栏区域 -->
      <el-aside :width="asideWidth">
        <el-tooltip
          effect="dark"
          content="点击展开或折叠"
          placement="right-start"
          :hide-after="1000"
        >
          <div
            class="toggle-button"
            @click="toggoleCollapse"
          >
            |||
          </div>
        </el-tooltip>
        <el-menu
          background-color="#393D49"
          text-color="#fff"
          active-text-color="#21A0FE"
          unique-opened
          :collapse-transition="false"
          :collapse="collapse"
          router
          :default-active="active"
          @select="handleSelect"
        >
          <el-menu-item index="/home" >
            <i class="el-icon-s-home"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-submenu
            v-for="(item) in menuData"
            :key="item.id"
            :index="item.id + ''"
          >
            <template slot="title">
              <i :class="item.icon" />
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item
              v-for="(child) in item.children"
              :key="child.id"
              :index="child.href !== '/welcome' ? child.href + '/' + child.id : '/welcome'"
            >
              <template slot="title">
                <i :class="child.icon" />
                <span>{{ child.title }}</span>
              </template>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-main>
          <router-view />
        </el-main>
        <el-footer>
          <span>&copy;三牛工作室版权所有</span>
        </el-footer>
      </el-container>
    </el-container>
  </el-container>
</template>

<script>
export default {
  data() {
    return {
      collapse: false,
      asideWidth: '200px',
      menuData: [],
      active: ''
    }
  },
  created() {
    this.getSiteInfo()
    this.getMenuTree()
    this.active = window.sessionStorage.getItem('menu-active-path')
  },
  methods: {
    handleSelect(index, indexPath) {
      // console.log(index, indexPath)
      window.sessionStorage.setItem('menu-active-path', index)
      this.active = index
    },
    getSiteInfo() {
      this.$http.get('/admin/api/site-info/')
        .then(res => {
          sessionStorage.setItem('siteInfo', JSON.stringify(res.data.data.siteInfo))
        }).catch(err => {
          console.log(err)
      })
    },
    handleCommand(command) {
      if (command === 'aboutme') {
        this.$router.push({ path: '/aboutme' })
      } else {
        this.$confirm('您确定要退出吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.logout()
          })
          .catch(() => {
            // console.log('放弃退出')
          })
      }
    },
    getMenuTree() {
      this.$http
        .get('/admin/api/auth/v2/nav')
        .then(res => {
          // console.log(res.data)
          this.menuData = res.data
          // let homePage = {
          //   title: '后台首页',
          //   id: 0,
          //   icon: 'el-icon-s-home',
          //   href: '/welcome',
          //   children: [
          //     {
          //       title: '后台首页',
          //       id: 0,
          //       icon: 'el-icon-s-home',
          //       href: '/welcome'
          //     }
          //   ]
          // }
          // this.menuData.unshift(homePage)
        })
        .catch(err => {
          console.log(err)
        })
    },
    logout() {
      window.sessionStorage.clear()
      this.$http.get('/admin/api/auth/logout', (res) => { console.log })
      this.$router.push('/')
    },
    // 点击按钮，切换菜单折叠与展开
    toggoleCollapse() {
      this.collapse = !this.collapse
      this.asideWidth = this.collapse ? '64px' : '200px'
    }
  }
}
</script>

<style lang="less" scoped>
.el-header {
  background-color: #393d49;
}

.el-aside {
  background-color: #393d49;

  .el-menu {
    border-right: none;
  }
}

.el-main {
  background-color: #eaedf1;
}

.el-footer {
  background-color: #fff;
}

.home-container {
  height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  color: #fff;
  margin-left: 0px;
  align-items: center;

  div {
    display: flex;
    align-items: center;

    span {
      margin-left: 20px;
    }
  }
}

.toggle-button {
  background-color: #4a5064;
  color: #fff;
  font-size: 10px;
  line-height: 24px;
  text-align: center;
  letter-spacing: 0.2em;
  cursor: pointer;
}

.el-dropdown-link {
  color: #fff;
  cursor: pointer;

  img {
    width: 35px;
    border-radius: 50%;
  }
}
</style>>
