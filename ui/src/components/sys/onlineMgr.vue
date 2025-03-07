<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>系统与结算</el-breadcrumb-item>
      <el-breadcrumb-item>在线管理</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <div slot="header">
        <span>当前在线人数：<span v-text="onlineCount" style="color: crimson"></span></span>
      </div>
      <el-table :data="onlineList"
                style="width: 100%"
                stripe
                border>
        <el-table-column
          width="310"
          prop="sessionId"
          label="sessionID"/>
        <el-table-column
          prop="userId"
          label="用户ID"/>
        <el-table-column
          prop="username"
          label="用户名"/>
        <el-table-column
          prop="name"
          label="姓名"/>
        <el-table-column
          prop="qq"
          label="QQ号"/>
        <el-table-column
          prop="loginMinutes"
          label="已登录时长（分钟）"/>
        <el-table-column
          prop="option"
          label="操作"
          width="130"
        >
          <template slot-scope="scope">
            <el-button
              type="danger"
              icon="el-icon-document-copy"
              size="small"
              @click="kickOut(scope.row)"
            >
              强制下线
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        :current-page="page"
        :page-size="limit"
        @current-change="pageChange"
        @prev-click="prev"
        @next-click="next"
        @size-change="sizeChange"
      />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'onlineMgr',
  data() {
    return {
      onlineCount: 0,
      onlineList: [],
      total: 0,
      page: 1,
      limit: 10,
    }
  },
  created() {
    this.getOnlineList()
  },
  methods: {
    kickOut(row) {
      this.$confirm('你确定要踢他下线吗？', '提示',{
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post('/admin/api/user/offline', 'sessionId=' + row.sessionId)
          .then(res => {
            if (res.data.code !== 0) return this.$message.error(res.data.message)
            this.$message.success('操作成功')
            this.getOnlineList()
          }).catch(err => {
            console.log(err)
          })
      })
    },
    async getOnlineList() {
      const { data: res } = await this.$http.get('/admin/api/user/online?page=' + this.page + '&limit=' + this.limit)
      if (res.code !== 0) return this.$message.error(res.message)
      this.onlineList = res.data
      this.onlineCount = res.count
    },
    pageChange(page) {
      this.page = page
      this.getOnlineList()
    },
    sizeChange(size) {
      this.limit = size
      this.getOnlineList()
    },
    prev() {
      this.page--
      if (this.page < 1) {
        this.page = 1
      }
    },
    next() {
      this.page++
    }
  }
}
</script>

<style scoped lang="less">
.el-pagination {
  padding: 20px 5px;
}
</style>
