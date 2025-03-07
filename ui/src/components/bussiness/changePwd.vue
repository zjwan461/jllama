<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>商户设置</el-breadcrumb-item>
      <el-breadcrumb-item>修改密码</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-form
        ref="changePwdForm"
        :model="changePwdForm"
        label-width="80px"
        :rules="rules"
      >
        <el-form-item
          label="登录名"
          prop="username"
        >
          <el-input
            v-model="changePwdForm.username"
            type="text" readonly disabled
          />
        </el-form-item>
        <el-form-item
          label="旧密码"
          prop="oldPwd"
        >
          <el-input
            v-model="changePwdForm.oldPwd"
            type="password"
          />
        </el-form-item>
        <el-form-item
          label="新密码"
          prop="newPwd"
        >
          <el-input
            v-model="changePwdForm.newPwd"
            type="password"
          />
        </el-form-item>
        <el-form-item
          label="确认密码"
          prop="reNewPwd"
        >
          <el-input
            v-model="changePwdForm.reNewPwd"
            type="password"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="onSubmit('changePwdForm')"
          >
            立即修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {

  data() {
    let reNewPwdValidator = (rule, value, callback) => {
      if (this.changePwdForm.newPwd !== value) {
        callback(new Error('两次密码输入不一致'))
      } else {
        callback()
      }
    }
    return {
      changePwdForm: {
        username: '',
        oldPwd: '',
        newPwd: '',
        reNewPwd: ''
      },
      rules: {
        username: [
          {
            required: true, message: '用户名必输', trigger: 'blur'
          }
        ],
        oldPwd: [
          {
            required: true, message: '旧密码必输', trigger: 'blur'
          }
        ],
        newPwd: [
          {
            required: true, message: '新密码必输', trigger: 'blur'
          }
        ],
        reNewPwd: [
          {
            required: true, message: '重复密码必填', trigger: 'blur'
          },
          {
            validator: reNewPwdValidator, trigger: 'blur'
          }
        ]
      }
    }
  },
  created() {
    this.getUserInfo()
  },
  methods: {
    onSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return

        this.$http.post('/admin/api/user/change-pwd', 'username=' + this.changePwdForm.username + '&oldPwd=' + this.changePwdForm.oldPwd + '&newPwd=' + this.changePwdForm.newPwd)
          .then(res => {
            if (res.data.code === 0) {
              this.$message({
                message: '修改成功',
                type: 'success'
              })
            } else {
              this.$message({
                message: res.data.message,
                type: 'error'
              })
            }
          }).catch(err => {
          console.log(err)
          this.$message({
            message: '网络错误',
            type: 'error'
          })
        })
      })
    },
    getUserInfo() {
      this.$http.get('/admin/api/auth/user')
        .then(res => {
          if (res.data.code === 0) {
            let user = res.data.data.user
            this.changePwdForm.username = user.username
          } else {
            this.$message({
              message: res.data.message,
              type: 'error'
            })
          }
        })
        .catch(err => {
          console.log(err)
          this.$message({
            message: '网络异常',
            type: 'error'
          })
        })
    }
  }
}
</script>

<style>

</style>
