<template>
  <div class="loginContent">
    <div class="loginArea">
      <div class="loginLogo">
        <img
          src="../assets/face.jpg"
          alt
        >
      </div>
      <!-- 表单登录区域 -->
      <el-form
        ref="loginForm"
        label-width="0px"
        class="loginForm"
        :model="loginForm"
        :rules="rules"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user-solid"
            placeholder="账号"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="el-icon-lock"
            placeholder="密码"
            type="password"
          />
        </el-form-item>
        <el-form-item prop="yzm">
          <el-row>
            <el-col :span="18">
              <el-input
                v-model="loginForm.yzm"
                prefix-icon="el-icon-info"
                placeholder="验证码"
              />
            </el-col>
            <el-col :span="6">
              <img
                :src="this.yzmImg"
                style="width: 100px; padding: 0 5px;"
                @click="getYzm"
              >
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item class="btns">
          <el-button
            type="primary"
            @click="submitForm('loginForm')"
          >
            登录
          </el-button>
          <el-button
            type="info"
            @click="resetForm('loginForm')"
          >
            重置
          </el-button>
        </el-form-item>
        <div style="">
          <a href="/admin#/register" style="color: #01AAED">账号注册</a>
          <a href="javascript:;" style="color: #01AAED;margin: 0 20px" target="_blank">审核联系QQ：626071842</a>
        </div>
      </el-form>
    </div>
    <div style="position: absolute; bottom: 110px; left: 50%; translate: -50%;">
      <div style="text-align: center">
        Copyright © Corporation. All rights reserved
      </div>
      <div>
        本站严禁一切钓鱼、色情、赌博、私彩及违反国家法律法规等使用.一经核实将冻结其ID,决不姑息.
      </div>
      <div style="text-align: center">
        九节兑换平台 版权所有 京ICP备16210784号-4
      </div>
    </div>
  </div>
</template>

<script>
import { Date } from 'core-js'

export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        yzm: ''
      },
      yzmImg: '',
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 14, message: '长度在 3 到 14 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 16, message: '长度在 3 到 16 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    let _self = this
    document.onkeydown = function (e) {
      let key = window.event.keyCode
      if (key === 13) {
        _self.submitForm('loginForm')
      }
    }
    this.getYzm()
  },
  methods: {
    getYzm() {
      this.$http.get('/admin/api/auth/yzm?t=' + new Date()).then(res => {
        this.yzmImg = res.data
      })
    },
    submitForm(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return

        this.$http
          .post('/admin/api/auth/login', 'username=' + this.loginForm.username + '&password=' + this.loginForm.password + '&yzm=' + this.loginForm.yzm)
          .then(res => {
            // console.log(res.data)
            if (res.data.code === 0) {
              window.sessionStorage.setItem('token', res.data.data.token)
              this.$message({
                message: '登录成功，页面即将跳转...',
                type: 'info'
              })
              var that = this
              setTimeout(function () {
                that.$router.push({ path: '/home' })
              }, 1000)
            } else {
              let msg = res.data.message
              if (msg === '验证码错误') {
                this.getYzm()
              }
              this.$message({
                message: msg,
                type: 'error'
              })
            }
          })
          .catch(err => {
            console.log('err', err)
          })
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>

<style lang="less" scoped>
.loginContent {
  background: #2b4b6b;
  width: 100%;
  height: 100%;
}

.loginArea {
  width: 450px;
  height: 450px;
  background: #fff;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 3px;

  .loginLogo {
    width: 300px;
    height: 100px;
    padding: 20px;
    position: absolute;
    left: 50%;
    transform: translate(-50%, 0%);
    background: #fff;

    img {
      width: 100%;
      height: 100%;
    }
  }
}

.loginForm {
  position: absolute;
  bottom: 0;
  width: 100%;
  top: 35%;
  padding: 0 20px;
  box-sizing: border-box;
}

.btns {
  display: flex;
  justify-content: flex-end;
}
</style>
