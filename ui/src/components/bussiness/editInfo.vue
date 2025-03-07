<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>商户设置</el-breadcrumb-item>
      <el-breadcrumb-item>修改资料</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-form
        ref="editInfo"
        :model="editInfo"
        label-width="80px"
        :rules="rules"
      >
        <el-form-item
          label="登录名"
          prop="username"
        >
          <el-input
            v-model="editInfo.username"
            type="text"
          />
        </el-form-item>
        <el-form-item
          label="邮箱"
          prop="email"
        >
          <el-input
            v-model="editInfo.email"
            type="email"
          />
        </el-form-item>
        <el-form-item
          label="身份证"
          prop="idCardNum"
        >
          <el-input
            v-model="editInfo.idCardNum"
            type="text"
          />
        </el-form-item>
        <el-form-item
          label="QQ号"
          prop="qq"
        >
          <el-input
            v-model="editInfo.qq"
            type="text"
          />
        </el-form-item>
        <el-form-item
          label="姓名"
          prop="name"
        >
          <el-input
            v-model="editInfo.name"
            type="text"
          />
        </el-form-item>
        <el-form-item
          label="通讯密钥"
          prop="accessToken"
        >
          <el-input
            v-model="editInfo.accessToken"
            type="text"
          />
        </el-form-item>
        <el-form-item label="收款码">
          <el-row>
            <el-col :span="6">
              <el-upload
                action="/admin/api/file/upload"
                list-type="picture"
                :data="uploadData"
                :on-success="uploadSuccess"
              >
                <el-button size="small">
                  <i class="el-icon-upload" />点击上传
                </el-button>
              </el-upload>
              <input
                ref="file"
                type="file"
                style="display: none;"
              >
            </el-col>
            <el-col :span="18">
              <el-image
                :src="apiPrefix+editInfo.skm"
                style="width: 60px"
              >
                <div
                  slot="placeholder"
                  class="image-slot"
                >
                  加载中<span class="dot">...</span>
                </div>
              </el-image>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="editInfo.status"
            placeholder="请选择活动区域"
            disabled
          >
            <el-option
              label="待审核"
              value="0"
            />
            <el-option
              label="正常"
              value="1"
            />
            <el-option
              label="禁用"
              value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="onSubmit('editInfo')"
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
    const idCardNumValidator = (rule, value, callback) => {
      if (!/^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test(value)) {
        callback(new Error('请输入合法的身份证'))
      } else {
        callback()
      }
    }
    return {
      uploadData: { static: true },
      apiPrefix: 'http://localhost:8090',
      editInfo: {
        id: '',
        username: '',
        email: '',
        idCardNum: '',
        qq: '',
        name: '',
        status: '正常',
        accessToken: '',
        skm: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 14, message: '长度在 3 到 14 个字符', trigger: 'blur' }
        ],
        email: [
          { type: 'email', required: true, message: '请输入正确的邮箱', trigger: 'blur' }
        ],
        idCardNum: [
          { validator: idCardNumValidator, trigger: 'blur', required: true }
        ],
        qq: [
          { required: true, message: '请输入QQ号', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        accessToken: [
          { required: true, message: '请输入通讯密钥', trigger: 'blur' },
          { min: 32, max: 32, message: '长度为32位', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getUserInfo()
    const siteInfo = JSON.parse(sessionStorage.getItem('siteInfo'))
    if (siteInfo) {
      this.apiPrefix = siteInfo.domain
    }
  },
  methods: {
    uploadSuccess(response, file, fileList) {
      // console.log(response)
      if (response.code === 0) {
        this.editInfo.skm = '/' + response.data.fileInfo.fileName
      }
    },
    onSubmit(formName) {
      let state = 0
      if (this.editInfo.status === '待审核') {
        state = 0
      } else if (this.editInfo.status === '正常') {
        state = 1
      } else if (this.editInfo.status === '禁用') {
        state = 2
      }

      this.$refs[formName].validate(valid => {
        if (!valid) return
        // this.$message.info(JSON.stringify(this.editInfo))
        this.$http.post('/admin/api/user/edit-self', 'id=' + this.editInfo.id + '&username=' + this.editInfo.username + '&email=' +
          this.editInfo.email + '&idCardNum=' + this.editInfo.idCardNum + '&name=' + this.editInfo.name + '&qq=' + this.editInfo.qq + '&accessToken=' +
          this.editInfo.accessToken + '&skm=' + this.editInfo.skm + '&status=' + state)
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
              message: '网络异常',
              type: 'error'
            })
          })
      })
    },
    getUserInfo() {
      this.$http.get('/admin/api/auth/user').then((res) => {
        if (res.data.code === 0) {
          this.editInfo = res.data.data.user
          if (this.editInfo.status === 0) {
            this.editInfo.status = '待审核'
          } else if (this.editInfo.status === 1) {
            this.editInfo.status = '正常'
          } else if (this.editInfo.status === 2) {
            this.editInfo.status = '禁用'
          }
        } else {
          this.$message({
            message: res.data.message,
            type: 'error'
          })
        }
      })
    }
  }
}
</script>

<style>

</style>
