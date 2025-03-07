<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>系统与结算</el-breadcrumb-item>
      <el-breadcrumb-item>用户管理</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-form inline>
        <el-form-item prop="searchText">
          <el-input placeholder="搜索，用户名，姓名，QQ号" v-model="searchText"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="search" type="primary">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="userList"
        stripe
        border>
        <el-table-column
          prop="username"
          label="用户名"
        />
        <el-table-column
          prop="name"
          label="姓名"
        />
        <el-table-column
          prop="skm"
          align="center"
          label="收款码"
        >
          <template slot-scope="scope">
            <img :src="skmUrl(scope.row.skm)" min-width="50" height="50" v-if="scope.row.skm"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="email"
          label="邮箱"
        />
        <el-table-column
          prop="idCardNum"
          label="身份证号"
          width="180"
        />
        <el-table-column
          prop="qq"
          label="QQ号"
        />
        <el-table-column
          prop="status"
          align="center"
          label="状态">
          <template slot-scope="scope">
            <el-tag
              :type="formatUserStatusTag(scope.row.status)"
              disable-transitions v-text="formatUserStatus(scope.row.status)"></el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="lastLoginTime"
          label="上次登录时间"
        />
        <el-table-column
          prop="option"
          label="操作"
          width="300"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              icon="el-icon-document-copy"
              size="small"
              @click="changeState(scope.row)"
            >
              禁/启用
            </el-button>
            <el-button
              type="success"
              icon="el-icon-delete"
              size="small"
              @click="showSkm(scope.row)"
            >
              收款码
            </el-button>
            <el-button
              type="info"
              icon="el-icon-edit"
              size="small"
              @click="edit(scope.row)"
            >
              编辑
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
      >
      </el-pagination>
    </el-card>
    <el-dialog title="编辑用户信息" :visible.sync="showEdit" width="50%" @close="cancel">
      <el-form :inline="true" :model="userInfo" :rules="rules" ref="userInfo">
        <el-form-item label="用户名" label-width="150px" prop="username">
          <el-input v-model="userInfo.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="姓名" label-width="150px" prop="name">
          <el-input v-model="userInfo.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" label-width="150px" prop="email">
          <el-input v-model="userInfo.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="身份证号" label-width="150px" prop="idCardNum">
          <el-input v-model="userInfo.idCardNum" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="QQ号" label-width="150px" prop="qq">
          <el-input v-model="userInfo.qq" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="doEdit('userInfo')">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { MessageBox } from 'element-ui';
import { getRequestBodyStr } from '@/common/common'

export default {
  name: 'userMgr',
  data() {
    return {
      showEdit: false,
      userInfo: {
        username: '',
        name: '',
        email: '',
        idCardNum: '',
        qq: ''
      },
      rules: {
        username: [
          { required: true, message: '用户名必填', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '姓名必填', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '邮箱必填', trigger: 'blur' }
        ],
        idCardNum: [
          { required: true, message: '身份证号必填', trigger: 'blur' }
        ],
        qq: [
          { required: true, message: 'QQ号必填', trigger: 'blur' }
        ]
      },
      searchText: '',
      page: 1,
      limit: 10,
      total: 0,
      userList: [],
      siteInfo: {}
    }
  },
  created() {
    this.siteInfo = JSON.parse(sessionStorage.getItem('siteInfo'))
    this.getUserList()
  },
  methods: {
    edit(row) {
      this.userInfo = JSON.parse(JSON.stringify(row))
      this.showEdit = true
    },
    doEdit(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return
        this.$http.post('/admin/api/user/edit', getRequestBodyStr(this.userInfo))
          .then(res => {
            if (res.data.code !== 0) return this.$message.error(res.data.message)
            this.$message.success('修改成功')
            this.showEdit = false
            this.getUserList()
          })
      })
    },
    cancel() {
      this.showEdit = false
      this.userInfo = {}
      this.$refs['userInfo'].clearValidate()
    },
    formatUserStatusTag(status) {
      if (status === 0) {
        return 'info'
      } else if (status === 1) {
        return 'primary'
      } else if (status === 2) {
        return 'danger'
      }
    },
    formatUserStatus(status) {
      if (status === 0) {
        return '待审核'
      } else if (status === 1) {
        return '正常'
      } else if (status === 2) {
        return '停用'
      }
    },
    skmUrl(skm) {
      if (skm) {
        return this.siteInfo.domain + skm
      }
    },
    search() {
      this.page = 1
      this.getUserList()
    },
    async getUserList() {
      const { data: res } = await this.$http.get('/admin/api/user/listMgr?page=' + this.page + '&limit=' + this.limit + '&searchText=' + this.searchText)
      if (res.code !== 0) return this.$message.error(res.message)
      this.userList = res.data
      this.total = res.count
    },
    changeState(row) {
      let status = -1
      if (row.status === 0 || row.status === 2) {
        status = 1
      } else {
        status = 2
      }
      let reqObj = { userId: row.id, status: status }
      this.$http.post('/admin/api/user/change-status', getRequestBodyStr(reqObj))
        .then(res => {
          if (res.data.code !== 0) return this.$message.error(res.data.message)
          this.getUserList()
        }).catch(err => {
          console.log(err)
        })
    },
    showSkm(row) {
      if (row.skm) {
        MessageBox({
          message: '<img src="' + this.siteInfo.domain + row.skm + '" width="500px;">',
          dangerouslyUseHTMLString: true,
          showClose: true
        })
      }
    },
    sizeChange(size) {
      this.limit = size
      this.getUserList()
    },
    pageChange(current) {
      this.page = current
      this.getUserList()
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
