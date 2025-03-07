<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>系统与结算</el-breadcrumb-item>
      <el-breadcrumb-item>结算管理</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-form inline>
        <el-form-item>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="getSettleList"
          >
            查询
          </el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="settleList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column
          prop="username"
          label="用户名"
        />
        <el-table-column
          prop="name"
          label="姓名"
        />
        <el-table-column
          prop="settleDate"
          label="结算日"
        />
        <el-table-column
          prop="settleMoney"
          label="结算金额(元)"
          :formatter="formatMoney"
        />
        <el-table-column
          prop="billMoney"
          label="账单金额(元)"
          :formatter="formatMoney"
        />
        <el-table-column
          prop="rate"
          label="当天平台费率"
        />
        <el-table-column
          prop="revenueMoney"
          label="平台抽成(元)"
          :formatter="formatMoney"
        />
        <el-table-column
          prop="settleTime"
          label="结算时间"
          width="160"
        />
        <el-table-column
          prop="skmUrl"
          label="收款码"
        >
          <template slot-scope="scope">
            <img :src="scope.row.skmUrl" min-width="50" height="70"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="state"
          label="状态"
          width="100"
        >
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.state === 1 ? 'primary' : 'danger'"
              disable-transitions
              v-text="formatSettleState(scope.row.state)"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="option"
          label="操作"
          width="220"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              icon="el-icon-document-copy"
              size="small"
              @click="doSettle(scope.row)"
            >
              结算
            </el-button>
            <el-button
              type="success"
              icon="el-icon-edit"
              size="small"
              @click="showSkm(scope.row)"
            >
              查看收款码
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
    <el-dialog title="收款码" :visible.sync="showSkmDialog" width="500px" >
      <div style="width: 100%; margin: 0 auto">
        <img :src="skmUrl" width="400px" height="450px">
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'settleMgr',
  data() {
    return {
      showSkmDialog: false,
      skmUrl: '',
      settleList: [],
      total: 0,
      page: 1,
      limit: 5,
      dateRange: [],
      pickerOptions: {
        shortcuts: [{
          text: '最近一周',
          onClick(picker) {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            picker.$emit('pick', [start, end])
          }
        }, {
          text: '最近一个月',
          onClick(picker) {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            picker.$emit('pick', [start, end])
          }
        }, {
          text: '最近三个月',
          onClick(picker) {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            picker.$emit('pick', [start, end])
          }
        }]
      }
    }
  },
  created() {
    this.getCurrentDateRange()
    this.getSettleList()
  },
  methods: {
    doSettle(row) {
      // console.log(menuId, rid)
      this.$confirm(
        '你确定要结算此笔款项？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(async () => {
          const { data: res } = await this.$http.get(
            '/admin/api/settle/' + row.id
          )
          if (res.code !== 0) return this.$message.error(res.msg)
          this.$message.success('结算成功')
          row.state = 1
        })
        .catch(() => {
          console.log('取消了结算操作')
        })
    },
    showSkm(row) {
      this.showSkmDialog = true
      this.skmUrl = row.skmUrl
    },
    formatSettleState(state) {
      return state === 0 ? '未结算' : '已结算'
    },
    formatMoney(row, column, cellValue, index) {
      return cellValue / 100
    },
    getCurrentDateRange() {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      this.dateRange = [this.formatDate(start), this.formatDate(end)]
    },
    formatDate(date) {
      let fullYear = date.getFullYear()
      let month = date.getMonth() + 1
      let day = date.getDate()
      return fullYear + '-' + month + '-' + day
    },
    getSettleList() {
      let dateScope = ''
      if (this.dateRange) {
        dateScope = this.dateRange.join(' - ')
      }
      this.$http.get('/admin/api/settle/listMgr?page=' + this.page + '&limit=' + this.limit + '&dateScope=' + dateScope)
        .then(res => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.message)
            return false
          }
          this.settleList = res.data.data
          this.total = res.data.count
        }).catch(err => {
        console.log(err)
      })
    },
    sizeChange(size) {
      this.limit = size
      this.getSettleList()
    },
    pageChange(current) {
      this.page = current
      this.getSettleList()
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
