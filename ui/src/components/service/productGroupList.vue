<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>服务设置</el-breadcrumb-item>
      <el-breadcrumb-item>服务器分组</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-button type="primary" @click="showGroupAdd = true">
        添加分组
      </el-button>
      <el-table
        :data="productGroupListData"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column
          type="index"
          label="#"
        />
        <el-table-column
          prop="groupName"
          label="分组名称"
        />
        <el-table-column
          prop="url"
          label="充值链接"
          width="500"
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
        />
        <el-table-column
          prop="updateTime"
          label="更新时间"
        >
        </el-table-column>
        <el-table-column
          prop="option"
          label="操作"
          width="200"
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
              type="danger"
              icon="el-icon-delete"
              size="small"
              @click="deleteGroup(scope.row)"
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

    <el-dialog title="新增分组" :visible.sync="showGroupAdd" width="30%">
      <el-form :model="groupForm">
        <el-form-item label="分组名称" label-width="120px">
          <el-input v-model="groupForm.groupName" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showGroupAdd = false">取 消</el-button>
        <el-button type="primary" @click="addGroup">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { copy } from '@/common/common'

export default {
  name: 'productGroupList',
  data() {
    return {
      productGroupListData: [],
      total: 0,
      page: 1,
      limit: 5,
      limits: [5, 10, 20],
      showGroupAdd: false,
      groupForm: {
        groupName: ''
      }
    }
  },
  created() {
    this.getProductGroupList()
  },
  methods: {
    sizeChange(size) {
      this.limit = size
      this.getProductGroupList()
    },
    pageChange(current) {
      this.page = current
      this.getProductGroupList()
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
    async addGroup() {
      const { data: res } = await this.$http.post('/admin/api/product/add-group', 'groupName=' + this.groupForm.groupName)
      if (res.code !== 0) return this.$message.error(res.message)
      this.$message.success('创建成功')
      this.showGroupAdd = false
      await this.getProductGroupList()
    },
    async doDelete(id) {
      const { data: res } = await this.$http.post('/admin/api/product/del-group', 'id=' + id)
      if (res.code !== 0) return this.$message.error(res.message)
      this.$message.success('删除成功')
      await this.getProductGroupList()
    },
    deleteGroup(row) {
      this.$confirm('您确定要删除此项吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.doDelete(row.id)
        })
        .catch(() => {
          // console.log('放弃退出')
        })
    },
    copyUrl(row) {
      console.log(row)
      copy(row.url)
    },
    async getProductGroupList() {
      const { data: res } = await this.$http.get('/admin/api/product/list-group?page=' + this.page + '&limit=' + this.limit)
      if (res.code !== 0) return this.$message.error(res.message)
      this.productGroupListData = res.data
      this.total = res.count
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

.bdtop {
  border-top: 1px solid #eee;
}

.bdbottom {
  border-bottom: 1px solid #eee;
}
.el-pagination {
  padding: 20px 5px;
}
</style>
