<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>模型监控</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-form :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item label="搜索">
          <el-input v-model="formInline.search" placeholder="搜索运行中的模型"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getTableData">查询</el-button>
          <el-button type="success" @click="create">新增</el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column
          prop="name"
          label="模型名称"
          width="180">
        </el-table-column>

      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'watch',
  data() {
    return {
      formInline: {
        search: ''
      },
      tableData: [],
    }
  },
  created() {

  },
  methods: {
    getTableData() {
      this.$http.get('/api/process/list?page=' + this.currentPage + '&limit=' + this.pageSize + '&search=' + this.formInline.search).then(res => {
        if (res.success === true) {
          this.tableData = res.data.records;
          this.total = res.data.total
        }
      })
    }
  }
}
</script>

<style>

</style>
