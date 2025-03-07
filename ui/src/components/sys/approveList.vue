<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>系统与结算</el-breadcrumb-item>
      <el-breadcrumb-item>注册审核</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-table
        :data="approveList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column
          prop="username"
          label="用户名"
        />
        <el-table-column
          prop="email"
          label="邮箱"
        />
        <el-table-column
          prop="name"
          label="姓名"
        />
        <el-table-column
          prop="idCardNum"
          label="身份证号"
        />
        <el-table-column
          prop="qq"
          label="QQ号"
        />
        <el-table-column
          prop="state"
          label="状态"
          width="100"
        >
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.state === 1 ? 'primary' : 'danger'"
              disable-transitions
              v-text="scope.row.state === 1 ? '': '等待审核'"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="option"
          label="操作"
          width="120"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              icon="el-icon-document-copy"
              size="small"
              @click="doApprove(scope.row)"
            >
              审核
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
  name: 'approveList',
  data() {
    return {
      total: 0,
      page: 1,
      limit: 10,
      approveList: []
    }
  },
  created() {
    this.getApproveList()
  },
  methods: {
    doApprove(row) {
      this.$confirm('你确定要审批通过此用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.$http.get('/admin/api/approve/' + row.id)
            .then(res => {
              if (res.data.code !== 0) return this.$message.error(res.data.message)
              this.$message.success('审核成功')
              this.getApproveList()
            }).catch(err => {
              console.log(err)
            })
        })
        .catch(() => {
          // console.log('放弃退出')
        })
    },
    async getApproveList() {
      const { data: res } = await this.$http.get('/admin/api/approve/list?page=' + this.page + '&limit=' + this.limit)
      if (res.code !== 0) return this.$message.error(res.message)
      this.approveList = res.data
      this.total = res.count
    },
    pageChange(page) {
      this.page = page
      this.getApproveList()
    },
    sizeChange(size) {
      this.limit = size
      this.getApproveList()
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
