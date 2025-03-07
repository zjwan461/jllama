<template>
  <div>
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>权限管理</el-breadcrumb-item>
      <el-breadcrumb-item>权限列表</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-table
        :data="rightData"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column
          type="index"
          label="#"
        />
        <el-table-column
          prop="authenName"
          label="权限名称"
        />
        <el-table-column
          prop="url"
          label="路径"
        />
        <el-table-column
          prop="level"
          label="权限等级"
        >
          <template slot-scope="scope">
            <el-tag
              v-if="scope.row.level === 1"
              type="primary"
            >
              一级权限
            </el-tag>
            <el-tag
              v-else-if="scope.row.level === 2"
              type="success"
            >
              二级权限
            </el-tag>
            <el-tag
              v-else
              type="warning"
              @click="handleClick(scope.row)"
            >
              三级权限
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      rightData: []
    }
  },
  created() {
    this.getRightsData()
  },
  methods: {
    handleClick(row) {
      // console.log(row)
    },
    async getRightsData() {
      const { data: res } = await this.$http.get('/apis/rightlist')
      if (res.code !== 0) return this.$message.error(res.msg)
      this.rightData = res.data
    }
  }
}
</script>

<style lang="less" scoped>
</style>
