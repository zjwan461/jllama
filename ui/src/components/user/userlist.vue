<template>
  <div>
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>用户管理</el-breadcrumb-item>
      <el-breadcrumb-item>用户列表</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 卡片区域 -->
    <el-card>
      <el-row :gutter="20">
        <el-col
          v-if="options.searchUser"
          :span="6"
        >
          <el-input
            v-model="searchValue"
            placeholder="搜索用户"
            clearable
            @clear="handleClearSearch"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              @click="searchUser()"
            />
          </el-input>
        </el-col>
        <el-col :span="18">
          <el-button
            v-if="options.addUser"
            type="primary"
            @click="openAddNewUserDialog"
          >
            添加用户
          </el-button>
        </el-col>
      </el-row>

      <!-- 用户列表 -->
      <el-table
        style="width: 100%"
        stripe
        border
        :data="userListData"
      >
        <el-table-column
          prop="id"
          label="id"
          width="100"
        />
        <el-table-column
          prop="username"
          label="用户名"
          width="180"
        />
        <el-table-column
          prop="email"
          label="邮箱"
          width="180"
        />
        <el-table-column
          prop="mobile"
          label="电话"
        />
        <el-table-column
          prop="stat"
          label="状态"
        >
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.stat"
              active-color="#13ce66"
              inactive-color="#eeee"
              @change="changeStat(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="roleName"
          label="角色"
        />
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-tooltip
              v-if="options.editUser"
              content="编辑"
              placement="top"
              :enterable="false"
            >
              <el-button
                type="primary"
                icon="el-icon-edit"
                size="mini"
                @click="handleEdit(scope.row)"
              />
            </el-tooltip>
            <el-tooltip
              v-if="options.deleteUser"
              content="删除"
              placement="top"
              :enterable="false"
            >
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                @click="handleDel(scope.row)"
              />
            </el-tooltip>
            <el-tooltip
              v-if="options.assignRole"
              content="分配权限"
              placement="top"
              :enterable="false"
            >
              <el-button
                type="warning"
                icon="el-icon-setting"
                size="mini"
                @click="handleRole(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页组件 -->
      <el-pagination
        :page-sizes="[2, 5, 10, 20]"
        :page-size="userQuery.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="userQuery.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog
      :title="dialogTitle"
      :visible.sync="isShowUserAddDialog"
      width="60%"
      @close="handleClose"
    >
      <el-form
        ref="newUserInfoRef"
        :model="newUserInfo"
        :rules="newUserInfoRul"
        label-width="80px"
      >
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input
            v-model="newUserInfo.username"
            :disabled="dialogType === 1?false:true"
          />
        </el-form-item>
        <el-form-item
          label="邮箱"
          prop="email"
        >
          <el-input v-model="newUserInfo.email" />
        </el-form-item>
        <el-form-item
          label="电话"
          prop="mobile"
        >
          <el-input v-model="newUserInfo.mobile" />
        </el-form-item>
      </el-form>
      <p class="tips">
        Tips: 新建用户默认密码为
        <strong>password</strong>，默认角色为
        <strong>会员</strong>
      </p>
      <span slot="footer">
        <el-button @click="isShowUserAddDialog = false">取 消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
        >确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="分配权限"
      :visible.sync="isShowRole"
      width="60%"
      @close="handelRoleClose"
    >
      <el-form
        ref="userRoleInfoRef"
        :model="userRoleInfo"
        :rules="userRoleInfoRul"
        label-width="80px"
      >
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input
            v-model="userRoleInfo.username"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="当前角色"
          prop="roleName"
        >
          <el-input
            v-model="userRoleInfo.roleName"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="分配权限"
          prop="newRoleName"
        >
          <el-select
            v-model="userRoleInfo.newRoleName"
            placeholder="请选择权限"
          >
            <el-option
              v-for="item in roleList"
              :key="item.id"
              :value="item.roleName"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="isShowRole = false">取 消</el-button>
        <el-button
          type="primary"
          @click="handleSubmitRole"
        >确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    var emailRul = (rule, value, callback) => {
      const emailReg = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/
      if (!emailReg.test(value)) {
        callback(new Error('请输入正确格式的邮箱'))
      }
      callback()
    }

    var mobileRul = (rule, value, callback) => {
      const mobileReg = /^1(3[0-9]|4[5,7]|5[0,1,2,3,5,6,7,8,9]|6[2,5,6,7]|7[0,1,7,8]|8[0-9]|9[1,8,9])\d{8}$/
      if (!mobileReg.test(value)) {
        callback(new Error('请输入正确格式的手机号'))
      }
      callback()
    }
    return {
      options: {
        addUser: false,
        editUser: false,
        deleteUser: false,
        assignRole: false,
        searchUser: false
      },
      roleList: [],
      userRoleInfo: {
        username: '',
        roleName: '',
        newRoleName: '',
        roleId: ''
      },
      searchValue: '',
      userQuery: {
        currentPage: 1,
        pageSize: 2,
        total: 0
      },
      userListData: [],
      isShowUserAddDialog: false,
      isShowRole: false,
      dialogTitle: '',
      newUserInfo: {
        username: '',
        email: '',
        mobile: '',
        id: 0
      },
      dialogType: 0,

      newUserInfoRul: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 16, message: '长度在 3 到 16 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { validator: emailRul, trigger: 'blur' }
        ],
        mobile: [
          { required: true, message: '请输入电话号码', trigger: 'blur' },
          {
            validator: mobileRul,
            trigger: 'blur'
          }
        ]
      },
      userRoleInfoRul: {
        newRoleName: [{ required: true, message: '此项必填', trigger: 'blur' }]
      }
    }
  },
  created() {
    const menuId = this.$route.params.menuId
    // console.log(menuId)
    this.getRoleMenu(menuId)
    this.getUserList()
  },
  methods: {
    async getRoleMenu(menuId) {
      const { data: res } = await this.$http.get(
        '/apis/usermenubutton/' + menuId
      )
      console.log(res.data)
      const arr = res.data

      const _self = this
      arr.forEach(x => {
        // console.log(x)
        if (x === 12) {
          _self.options.addUser = true
        } else if (x === 13) {
          _self.options.editUser = true
        } else if (x === 14) {
          _self.options.deleteUser = true
        } else if (x === 15) {
          _self.options.assignRole = true
        } else if (x === 16) {
          _self.options.searchUser = true
        }
      })
    },
    handelRoleClose() {
      this.$refs.userRoleInfoRef.resetFields()
    },
    handleSubmitRole() {
      this.$refs.userRoleInfoRef.validate(async validate => {
        if (!validate) return

        // console.log('验证通过')
        const { data: res } = await this.$http.put(
          '/apis/userupdaterole',
          this.userRoleInfo
        )
        if (res.code !== 0) return this.$message.error(res.msg)

        this.$message.success('修改成功')
        this.isShowRole = false
        this.$refs.userRoleInfoRef.resetFields()
        this.getUserList()
      })
    },
    async handleRole(row) {
      // console.log(row)
      this.userRoleInfo.username = row.username
      this.userRoleInfo.roleName = row.roleName
      // this.userRoleInfo.roleId = row.id
      const { data: res } = await this.$http.get('/apis/userrolelist')
      // console.log(res)
      if (res.code !== 0) return this.$message.error(res.msg)

      this.roleList = res.data
      this.isShowRole = true
    },
    handleClearSearch() {
      this.getUserList()
    },
    async searchUser() {
      if (this.searchValue === '') return this.getUserList()

      const { data: res } = await this.$http.get(
        '/apis/usersearch/' +
          this.searchValue +
          '/' +
          this.userQuery.currentPage +
          '/' +
          this.userQuery.pageSize
      )
      if (res.code !== 0) return this.$message.error(res.msg)
      // console.log(res)
      this.userQuery.total = res.data.total
      this.reloadUserList(res.data.users)
    },
    handleCurrentChange(val) {
      // console.log(val)
      this.userQuery.currentPage = val
      this.getUserList()
    },
    handleSizeChange(val) {
      // console.log(val)
      this.userQuery.pageSize = val
      this.getUserList()
    },
    handleSubmit() {
      this.$refs.newUserInfoRef.validate(async valid => {
        if (!valid) return false

        if (this.dialogType === 1) {
          const { data: res } = await this.$http.post(
            '/apis/useradd',
            this.newUserInfo
          )
          if (res.code === 1) return this.$message.error('添加用户失败')

          this.$message.success('添加用户成功')
          // this.newUserInfo = {}
          this.$refs.newUserInfoRef.resetFields()
          this.isShowUserAddDialog = false
          this.getUserList()
        } else if (this.dialogType === 2) {
          const { data: res } = await this.$http.put(
            '/apis/userupdate',
            this.newUserInfo
          )
          if (res.code === 1) return this.$message.error('修改用户信息失败')

          this.$message.success('修改成功')
          // this.newUserInfo = {}
          this.isShowUserAddDialog = false
          this.getUserList()
        }
      })
    },
    handleClose() {
      // this.newUserInfo = {} 加上会导致newUserInfo 定义的属性失效
      this.$refs.newUserInfoRef.resetFields()
    },
    openAddNewUserDialog(type) {
      this.dialogType = 1
      this.dialogTitle = '添加新用户'
      this.isShowUserAddDialog = true
    },
    handleEdit(row) {
      // console.log(row)
      this.dialogType = 2
      this.dialogTitle = '修改用户信息'
      this.isShowUserAddDialog = true
      this.newUserInfo.username = row.username
      this.newUserInfo.email = row.email
      this.newUserInfo.mobile = row.mobile
      this.newUserInfo.id = row.id
    },
    handleDel(row) {
      this.$confirm('您确定要删除这个用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(res => {
          this.deleteUser(row)
        })
        .catch(err => err)
    },
    async deleteUser(row) {
      const { data: res } = await this.$http.delete('/apis/userdel/' + row.id)
      if (res.code !== 0) return this.$message.error(res.msg)

      this.$message.success('删除成功')
      this.getUserList()
    },
    async changeStat(obj) {
      // console.log(obj.id, obj.stat)
      const { data: res } = await this.$http.put('/apis/userupdate', {
        id: obj.id,
        stat: obj.stat ? 'Y' : 'N'
      })
      if (res.code != 0) return this.$message.error(res.msg)

      this.$message.success('修改成功')
    },
    async getUserList() {
      const { data: res } = await this.$http.get(
        '/apis/userlist/' +
          this.userQuery.currentPage +
          '/' +
          this.userQuery.pageSize
      )
      if (res.code !== 0) return this.$message.error(res.msg)
      // console.log(res)
      this.userQuery.total = res.data.total
      this.reloadUserList(res.data.users)
      // console.log(this.userListData)
    },
    reloadUserList(userlist) {
      this.userListData = []
      userlist.forEach(item => {
        let obj = new Object()
        obj.id = item.id
        obj.roleName = item.roleName
        obj.username = item.username
        obj.email = item.email
        obj.mobile = item.mobile
        obj.stat = item.stat === 'Y'
        this.userListData.push(obj)
      })
    }
  }
}
</script>

<style lang="less" scoped>
.el-table {
  margin-top: 20px;
  font-size: 15px;
  text-align: center;
}
.tips {
  display: flex;
  font-size: 12px;
  color: gray;
  justify-content: center;
  strong {
    font-weight: 150%;
    color: #409eff;
  }
}
.el-pagination {
  margin-top: 10px;
}
</style>
