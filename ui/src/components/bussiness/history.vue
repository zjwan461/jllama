<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>模型进程</el-breadcrumb-item>
      <el-breadcrumb-item>运行历史</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-form :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item label="搜索">
          <el-input v-model="formInline.search" placeholder="搜索模型运行历史"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getTableData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column
          prop="modelName"
          label="模型名称">
        </el-table-column>
        <el-table-column
          prop="llamaCppDir"
          label="执行目录">
        </el-table-column>
        <el-table-column
          width="120px"
          prop="llamaCppCommand"
          label="Command">
        </el-table-column>
        <el-table-column
          prop="llamaCppArgs"
          label="运行参数">
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          width="200">
          <template slot-scope="scope">
            <el-button @click="showLog(scope.row, scope.$index)" type="success" size="small">查看日志</el-button>
            <el-button @click="del(scope.row, scope.$index)" type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="pageSizes"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog :title="log.logFilePath"
               :visible.sync="showDialog"
               :close-on-press-escape=false
               :close-on-click-modal=false
               :destroy-on-close=true
               @close="resetDialog"
    >
      <el-card>
        <div slot="header">
          <el-button style="float: right; padding: 3px 0" type="text" @click="copyLogPath">复制文件地址</el-button>
        </div>
        <div style="white-space:pre-line; overflow-y: auto; height: 700px">
          {{ log.logContent }}
        </div>
        <div>
          <el-button style="float: right; padding: 3px 0" type="text" @click="loadLog(-1)">加载更多</el-button>
        </div>
      </el-card>
    </el-dialog>
  </div>
</template>

<script>
// import { getRequestBodyJson } from '@/common/common'

import {copy} from "@/common/common";

export default {
  name: 'history',
  data() {
    return {
      logIndex: 1,
      logLine: 30,
      showDialog: false,
      log: {
        logFilePath: '',
        logContent: '',
        id: -1
      },
      formInline: {
        search: ''
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      pageSizes: [10, 20, 50]
    }
  },
  created() {
    this.getTableData()
  },
  methods: {
    copyLogPath() {
      if (this.log.logFilePath.length > 0) {
        copy(this.log.logFilePath)
      }
    },
    resetDialog() {
      this.showDialog = false
      this.log = {
        logFilePath: '',
        logContent: '',
        id: -1
      }
      this.logIndex = 1
    },
    showLog(item) {
      this.showDialog = true
      this.log.logFilePath = item.logFilePath
      this.log.id = item.id
      this.loadLog(item.id)
    },
    loadLog(id) {
      if (id === -1) {
        id = this.log.id
      }
      this.$http.get('/api/process/log-history/' + id + '/' + this.logIndex + '/' + this.logLine).then(res => {
        if (res.success === true) {
          if (res.data.length > 0) {
            this.log.logContent += res.data
            this.logIndex = this.logIndex + this.logLine
          } else {
            this.$message({
              type: 'info',
              message: '没有更多日志了'
            })
          }
        }
      })
    },
    del(item) {
      this.$confirm('此操作将永久删除该记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delete('/api/process/del-history/' + item.id).then(res => {
          if (res.success === true) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.getTableData()
          }
        })
      })
    },
    history() {
      this.$router.push('/history')
    },
    getTableData() {
      this.$http.get('/api/process/list-history?page=' + this.currentPage + '&limit=' + this.pageSize + '&search=' + this.formInline.search).then(res => {
        if (res.success === true) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      })
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.getTableData()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getTableData()
    }
  }
}
</script>

<style>

</style>
