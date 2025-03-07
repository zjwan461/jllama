<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>订单查询</el-breadcrumb-item>
      <el-breadcrumb-item>人工补发</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-row>
        <el-col :span="12">
          <el-form :model="resendForm"
                   ref="resendForm"
                   label-width="120px"
                   label-position="left"
                   :rules="rules"
          >
            <el-form-item prop="id" label="分区-游戏币">
              <el-select v-model="resendForm.id" placeholder="请选择分区-游戏币" @change="change">
                <el-option
                  v-for="item in productList"
                  :key="item.id"
                  :label="showLabel(item)"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item prop="gameAccount" label="账号">
              <el-input placeholder="游戏账号" v-model="resendForm.gameAccount"/>
            </el-form-item>
            <el-form-item prop="moneyRmb" label="金额">
              <el-input placeholder="金额" v-model="resendForm.moneyRmb" type="number"/>
            </el-form-item>
            <el-form-item prop="payType" label="支付类型">
              <el-radio v-model="resendForm.payType" label="WXPAY">微信</el-radio>
              <el-radio v-model="resendForm.payType" label="ALIPAY">支付宝</el-radio>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="resend('resendForm')">补发</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

    </el-card>
  </div>
</template>
<script>
import { getRequestBodyStr } from '@/common/common';

export default {
  name: 'resend',
  created() {
    this.getProductList()
  },
  data() {
    return {
      rules: {
        id: [
          { required: true, message: '请选择分组', trigger: 'change' }
        ],
        gameAccount: [
          { required: true, message: '请输入游戏账号', trigger: 'blur' }
        ],
        moneyRmb: [
          { required: true, message: '请输入金额', trigger: 'blur' }
        ],
        payType: [
          { required: true, message: '请输入金额', trigger: 'blur' }
        ]
      },
      productList: [],
      resendForm: {
        id: '',
        gameAccount: '',
        moneyRmb: '',
        payType: 'WXPAY'
      }
    }
  },
  methods: {
    async getProductList() {
      const { data: res } = await this.$http.get('/admin/api/product/list?page=1&limit=1000')
      if (res.code !== 0) return this.$message.error(res.message)
      this.productList = res.data
    },
    showLabel (item) {
      return item.zone + '-' + item.title
    },
    resend(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return
        // this.$message.info(JSON.stringify(this.resendForm))
        this.$http.post('/admin/api/payment/resend', getRequestBodyStr(this.resendForm))
          .then(res => {
            if (res.data.code === 0) {
              this.$message.success('重发成功')
            } else {
              this.$message.error(res.data.message)
            }
          }).catch(err => {
            console.log(err)
        })
      })
    },
    change(val) {
      // alert(val)
      this.resendForm = JSON.parse(JSON.stringify(this.resendForm))
    }
  }
}
</script>

<style scoped lang="less">

</style>
