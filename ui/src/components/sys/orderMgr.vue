<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>系统与结算</el-breadcrumb-item>
      <el-breadcrumb-item>订单管理</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-form inline :model="queryParam">
        <el-form-item>
          <el-select v-model="queryParam.gatewayStatus">
            <el-option value="all" label="全部"></el-option>
            <el-option value="未发送" label="未发送"></el-option>
            <el-option value="发送成功" label="发送成功"></el-option>
            <el-option value="发送失败" label="发送失败"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="queryParam.showSysOnly">只看系统充值</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getOrderList">
            查询
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-input-number placeholder="选择同步多少天内的数据" v-model="asyncDays"></el-input-number>
        </el-form-item>
        <el-form-item>
          <el-button type="info" @click="asyncDeep">
            同步状态
          </el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="orderList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column
          prop="orderNo"
          label="订单号"
          width="220"
        />
        <el-table-column
          prop="paymentType"
          label="支付类型"
        />
        <el-table-column
          prop="tradeState"
          label="交易状态"
          width="110"
        >
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.tradeState === 'TRADE_SUCCESS' || scope.row.tradeState === 'SUCCESS' ? 'primary' : 'danger'"
              disable-transitions v-text="formatTradeState(scope.row.tradeState)"></el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="payerTotal"
          label="支付金额"
          :formatter="formatMoney"
        />
        <el-table-column
          prop="gameAccount"
          label="游戏账号"
        />
        <el-table-column
          prop="custQq"
          label="充值人QQ"
        />
        <el-table-column
          prop="userQq"
          label="用户QQ"
        />
        <el-table-column
          prop="username"
          label="用户名"
        />
        <el-table-column
          prop="gatewayState"
          label="发送状态"
        >
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.gatewayState === '发送成功' ? 'primary' : 'danger'"
              disable-transitions v-text="formatGatewayState(scope.row.gatewayState)"></el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="type"
          label="类型"
          :formatter="formatType"
        />
        <el-table-column
          prop="settleState"
          label="结算状态"
        >
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.settleState !== 0 ? 'primary' : 'danger'"
              disable-transitions v-text="formatSettleState(scope.row.settleState)"></el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="160"
        />
        <el-table-column
          prop="option"
          label="操作"
          width="245"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              icon="el-icon-document-copy"
              size="small"
              @click="syncState(scope.row)"
            >
              同步状态
            </el-button>
            <el-button
              type="success"
              icon="el-icon-delete"
              size="small"
              @click="gatewayInfo(scope.row)"
            >
              网关信息
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
        :page-sizes="limits"
        @current-change="pageChange"
        @prev-click="prev"
        @next-click="next"
        @size-change="sizeChange"
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
import { MessageBox } from 'element-ui';

export default {
  name: 'orderMgr',
  created() {
    this.getOrderList()
  },
  data() {
    return {
      asyncDays: 1,
      orderList: [],
      total: 0,
      page: 1,
      limit: 10,
      limits: [5, 10, 20],
      queryParam: {
        gatewayStatus: 'all',
        showSysOnly: false
      }
    }
  },
  methods: {
    formatGatewayState(gatewayState) {
      if (gatewayState) {
        return gatewayState
      } else {
        return 'N/A'
      }
    },
    formatTradeState(cellValue) {
      if (cellValue === 'TRADE_SUCCESS' || cellValue === 'SUCCESS') {
        return '交易成功'
      } else if (cellValue === 'NOTPAY' || cellValue === 'WAIT_BUYER_PAY') {
        return '未支付'
      } else if (cellValue === 'CLOSED' || cellValue === 'TRADE_CLOSED') {
        return '已关闭'
      } else {
        return '无'
      }
    },
    formatMoney(row, column, cellValue, index) {
      return cellValue / 100
    },
    formatType(row, column, cellValue, index) {
      return cellValue === 0 ? '系统充值' : '补发'
    },
    formatSettleState(cellValue) {
      return cellValue === 0 ? '未结算' : '已结算'
    },
    async getOrderList() {
      const { data: res } = await this.$http.get('/admin/api/payment/listMgr?page=' + this.page + '&limit=' + this.limit + '&gatewayStatus=' + this.queryParam.gatewayStatus + '&showSysOnly=' + this.queryParam.showSysOnly)
      if (res.code !== 0) return this.$message.error(res.message)
      this.orderList = res.data
      this.total = res.count
    },
    sizeChange(size) {
      this.limit = size
      this.getOrderList()
    },
    pageChange(current) {
      this.page = current
      this.getOrderList()
    },
    prev() {
      this.page--
      if (this.page < 1) {
        this.page = 1
      }
    },
    next() {
      this.page++
    },
    async syncState(row) {
      const { data: res } = await this.$http.get('/admin/api/payment/sync-order-state/' + row.orderNo)
      if (res.code !== 0) return this.$message.error(res.message)
      this.$message.success('同步成功')
      await this.getOrderList()
    },
    async asyncDeep() {
      this.$confirm('您确定要同步' + this.asyncDays + '天内的订单状态吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.$http.get('/admin/api/payment/sync-order-state-deep/' + this.asyncDays)
            .then(res => {
              if (res.data.code !== 0) return this.$message.error(res.data.message)
              this.$message.success('同步成功')
              this.getOrderList()
            }).catch(err => {
              console.log(err)
            })
        })
        .catch(() => {

        })
    },
    gatewayInfo(row) {
      const gatewayState = row.gatewayState
      if (gatewayState === '未发送') {
        MessageBox('订单尚未发送网关')
      } else if (gatewayState === '发送成功') {
        MessageBox('发送成功')
      } else {
        MessageBox({
          message: row.gatewayMsg,
          title: '提示'
        })
      }
    }
  }
}
</script>

<style scoped lang="less">
.el-pagination {
  padding: 20px 5px;
}
</style>
