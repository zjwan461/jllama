<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>服务设置</el-breadcrumb-item>
      <el-breadcrumb-item>分区管理</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-button type="primary" @click="showAdd">
        添加分区
      </el-button>
      <el-table
        :data="productListData"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-form label-position="left" inline class="demo-table-expand">
              <el-form-item label="分区名:">
                <span>{{ props.row.zone }}</span>
              </el-form-item>
              <el-form-item label="金币名称:">
                <span>{{ props.row.title }}</span>
              </el-form-item>
              <el-form-item label="兑换比例:">
                <span>{{ props.row.mag }}</span>
              </el-form-item>
              <el-form-item label="分组名:">
                <span>{{ props.row.groupName }}</span>
              </el-form-item>
              <el-form-item label="充值链接:">
                <span>{{ props.row.url }}</span>
              </el-form-item>
              <el-form-item label="网关IP:">
                <span>{{ props.row.gameServerIp }}</span>
              </el-form-item>
              <el-form-item label="网关端口:">
                <span>{{ props.row.gameServerPort }}</span>
              </el-form-item>
              <el-form-item label="满50赠送:">
                <span>{{ props.row.reward50 }}</span>
              </el-form-item>
              <el-form-item label="满100赠送:">
                <span>{{ props.row.reward100 }}</span>
              </el-form-item>
              <el-form-item label="满500赠送:">
                <span>{{ props.row.reward500 }}</span>
              </el-form-item>
              <el-form-item label="满1000赠送:">
                <span>{{ props.row.reward1000 }}</span>
              </el-form-item>
              <el-form-item label="满2000赠送:">
                <span>{{ props.row.reward2000 }}</span>
              </el-form-item>
              <el-form-item label="客户QQ:">
                <span>{{ props.row.qq }}</span>
              </el-form-item>
              <el-form-item label="网站url:">
                <span>{{ props.row.websiteUrl }}</span>
              </el-form-item>
              <el-form-item label="最低充值金额:">
                <span>{{ props.row.minMoney }}</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column
          prop="zone"
          label="分区名"
        />
        <el-table-column
          prop="title"
          label="金币名称"
        />
        <el-table-column
          prop="mag"
          label="兑换比例"
        />
        <el-table-column
          prop="groupName"
          label="分组"
        />
        <el-table-column
          prop="gameServerIp"
          label="网关IP"
        />
        <el-table-column
          prop="gameServerPort"
          label="网关端口"
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
              @click="copyUrl(scope.row)"
            >
              拷贝Url
            </el-button>
            <el-button
              type="success"
              icon="el-icon-edit"
              size="small"
              @click="editProduct(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              icon="el-icon-delete"
              size="small"
              @click="deleteProduct(scope.row)"
            >
              删除
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
    <el-dialog :title="dialogTitle" :visible.sync="showProductAdd" width="50%" @close="cancel">
      <el-form :inline="true" :model="productInfo" :rules="rules" ref="productInfo">
        <el-form-item label="分组" label-width="150px" prop="groupId">
          <el-select v-model="productInfo.groupId">
            <el-option
              v-for="item in productGroups"
              :key="item.id"
              :label="item.groupName"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="游戏币名称" label-width="150px" prop="title">
          <el-input v-model="productInfo.title" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="分区名称" label-width="150px" prop="zone">
          <el-input v-model="productInfo.zone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="兑换比例" label-width="150px" prop="mag">
          <el-input v-model="productInfo.mag" autocomplete="off" type="number"></el-input>
        </el-form-item>
        <el-form-item label="网关服务IP" label-width="150px" prop="gameServerIp">
          <el-input v-model="productInfo.gameServerIp" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="网关服务端口" label-width="150px" prop="gameServerPort">
          <el-input v-model="productInfo.gameServerPort" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="客服QQ" label-width="150px" prop="qq">
          <el-input v-model="productInfo.qq" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="最低充值金额(元)" label-width="150px" prop="minMoney">
          <el-input type="number" v-model="productInfo.minMoney" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="游戏网站" label-width="150px">
          <el-input v-model="productInfo.websiteUrl" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="满50元赠" label-width="150px">
          <el-input v-model="productInfo.reward50" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="满100元赠" label-width="150px">
          <el-input v-model="productInfo.reward100" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="满500元赠" label-width="150px">
          <el-input v-model="productInfo.reward500" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="满1000元赠" label-width="150px">
          <el-input v-model="productInfo.reward1000" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="满2000元赠" label-width="150px">
          <el-input v-model="productInfo.reward2000" autocomplete="off"></el-input>
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="addOrEdit('productInfo')">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { copy, getRequestBodyStr } from '@/common/common'

export default {
  name: 'productList',
  data() {
    const ipValidator = (rule, value, callback) => {
      if (!/^((25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))$/.test(value)) {
        callback(new Error('请输入合法的IP'))
      } else {
        callback()
      }
    }
    return {
      rules: {
        groupId: [
          { required: true, message: '请选择分组', trigger: 'blur' }
        ],
        title: [
          { required: true, message: '请输入游戏币名称', trigger: 'blur' }
        ],
        zone: [
          { required: true, message: '请输入分区名', trigger: 'blur' }
        ],
        mag: [
          { required: true, message: '请输入兑换比例', trigger: 'blur' }
        ],
        gameServerIp: [
          { required: true, message: '请输入网关IP', trigger: 'blur' },
          { validator: ipValidator, trigger: 'blur', required: true }
        ],
        gameServerPort: [
          { required: true, message: '请输入网关端口', trigger: 'blur' },
          { pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/, message: '格式：0-65535,不能以0开头！' }
        ],
        qq: [
          { required: true, message: '请输入客服QQ', trigger: 'blur' },
          { pattern: /^[1-9]{1}[0-9]{4,14}$/, message: '请输入合法的QQ', trigger: 'blur' }
        ],
        minMoney: [
          { required: true, message: '请输入最低充值金额', trigger: 'blur' }
        ]
      },
      dialogTitle: '新增分区',
      showProductAdd: false,
      productListData: [],
      total: 0,
      page: 1,
      limit: 10,
      limits: [5, 10, 15],
      productGroups: [],
      productInfo: {
        id: '',
        title: '',
        mag: '',
        zone: '',
        groupId: '',
        groupName: '',
        gameServerIp: '',
        gameServerPort: '',
        reward50: '',
        reward100: '',
        reward500: '',
        reward1000: '',
        reward2000: '',
        qq: '',
        url: '',
        minMoney: '',
        websiteUrl: '',
        createTime: '',
        updateTime: ''
      }
    }
  },
  created() {
    this.getProductList()
    this.getGroups()
  },
  methods: {
    showAdd() {
      this.showProductAdd = true
      this.dialogTitle = '新增分区'
    },
    async getGroups() {
      const { data: res } = await this.$http.get('/admin/api/product/list-group?page=1&limit=1000')
      if (res.code !== 0) return this.$message.error(res.message)
      this.productGroups = res.data
    },
    cancel() {
      this.showProductAdd = false
      this.productInfo = {}
      this.$refs['productInfo'].clearValidate()
    },
    async edit() {
      const reqStr = getRequestBodyStr(this.productInfo)
      const { data: res } = await this.$http.post('/admin/api/product/edit', reqStr)
      if (res.code !== 0) return this.$message.error(res.message)
      this.$message.success('修改成功')
      this.showProductAdd = false
      this.productInfo = {}
      await this.getProductList()
    },
    async add() {
      const reqStr = getRequestBodyStr(this.productInfo)
      const { data: res } = await this.$http.post('/admin/api/product/add', reqStr)
      if (res.code !== 0) return this.$message.error(res.message)
      this.$message.success('添加成功')
      this.showProductAdd = false
      this.productInfo = {}
      await this.getProductList()
    },
    addOrEdit(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return
        if (this.productInfo.id && this.productInfo.id !== '') {
          // edit
          this.edit()
        } else {
          // add
          this.add()
        }
      })
    },
    editProduct(row) {
      this.showProductAdd = true
      this.dialogTitle = '编辑分区'
      console.log(row)
      this.productInfo = row
    },
    async doDelete(id) {
      const { data: res } = await this.$http.post('/admin/api/product/del', 'id=' + id)
      if (res.code !== 0) return this.$message.error(res.message)
      this.$message.success('删除成功')
      await this.getProductList()
    },
    deleteProduct(row) {
      this.$confirm('您确定要删除此项吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.doDelete(row.id)
      })
        .catch(() => {
          // console.log('放弃退出')
        })
    },
    async getProductList() {
      const { data: res } = await this.$http.get('/admin/api/product/list?page=' + this.page + '&limit=' + this.limit)
      if (res.code !== 0) return this.$message.error(res.message)
      this.productListData = res.data
      this.total = res.count
    },
    copyUrl(row) {
      console.log(row)
      copy(row.url)
    },
    pageChange(current) {
      this.page = current
      this.getProductList()
    },
    sizeChange(size) {
      this.limit = size
      this.getProductList()
    },
    prev() {
      this.page--
      if (this.page < 1) {
        this.page = 1
      }
      // this.getProductList()
    },
    next() {
      this.page++
      // this.getProductList()
    }
  }
}
</script>

<style scoped lang="less">
.el-table {
  margin-top: 15px;
}

.el-tag {
  margin: 7px;
}

.demo-table-expand {
  font-size: 0;
}

.demo-table-expand .label {
  width: 90px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  padding: 0 20px;
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
.el-pagination {
  padding: 20px 5px;
}
</style>
