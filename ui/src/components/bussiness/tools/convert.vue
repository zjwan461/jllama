<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>工具箱</el-breadcrumb-item>
      <el-breadcrumb-item>模型转换</el-breadcrumb-item>
    </el-breadcrumb>
    <el-row>
      <el-col :span="12" class="card-box">
        <el-card class="card">
          <div slot="header"><span><i class="el-icon-edit"></i> 模型格式转换</span></div>
          <el-form ref="form" :rules="rules" :model="form" label-width="130px">
            <el-form-item label="转换脚本" prop="scriptFile">
              <el-select v-model="form.scriptFile" placeholder="被转换模型目录">
                <el-option v-for="item in scriptFileList" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="被转换模型目录" prop="input">
              <el-input v-model="form.input" type="text" placeholder="被转换模型目录"></el-input>
            </el-form-item>
            <el-form-item label="转换输出文件" prop="output">
              <el-input v-model="form.output" type="text" placeholder="转换输出文件"></el-input>
            </el-form-item>
            <el-form-item label="异步执行">
              <el-switch v-model="form.async"></el-switch>
              <i style="color: #909399;"> 默认同步执行，开启则会在后台执行，不会阻塞页面</i>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSubmit('form')">提交</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12" class="card-box">
        <el-card class="card">
          <div slot="header"><span><i class="el-icon-edit"></i> 操作历史</span></div>
          <el-table :data="tableData" style="width: 100%">
            <el-table-column type="expand">
              <template slot-scope="props">
                <el-form label-position="left" inline class="demo-table-expand">
                  <el-form-item label="输入文件">
                    <span>{{ props.row.input }}</span>
                  </el-form-item>
                  <el-form-item label="输出文件">
                    <span>{{ props.row.output }}</span>
                  </el-form-item>
                  <el-form-item label="异步">
                    <span>{{ props.row.async }}</span>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>
            <el-table-column label="执行时间" prop="createTime">
            </el-table-column>
            <el-table-column label="脚本文件" prop="scriptFile">
            </el-table-column>
          </el-table>
          <div class="block">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                           :current-page="currentPage" :page-sizes="pageSizes" :page-size="pageSize"
                           layout="total, sizes, prev, pager, next, jumper" :total="total">
            </el-pagination>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {getRequestBodyJson} from "@/common/common";

export default {
  name: "convert",
  data() {
    return {
      total: 0,
      pageSize: 5,
      pageSizes: [5, 10, 20],
      currentPage: 1,
      tableData: [],
      scriptFileList: [],
      form: {
        input: '',
        output: '',
        scriptFile: '',
        async: false
      },
      rules: {
        scriptFile: [
          {required: true, message: '请选择转换脚本', trigger: 'blur'}
        ]
        , input: [
          {required: true, message: '请输入被转换模型目录', trigger: 'blur'}
        ],
        output: [
          {required: true, message: '请输入转换输出文件', trigger: 'blur'}
        ],
      },
    }
  },
  created() {
    this.getScriptFile()
    this.getTableData()
  },
  methods: {
    getScriptFile() {
      this.$http.get('/api/tools/script-list').then(res => {
        if (res.success === true) {
          this.scriptFileList = res.data
        }
      })
    },
    onSubmit(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {
          this.$http.post('/api/tools/convert', getRequestBodyJson(this.form)).then(res => {
            if (res.success === true) {
              this.$message({
                type: 'success',
                message: '操作成功,异步请求请留意站内推送'
              })
            }
          })
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
    },
    getTableData() {
      this.$http.get('/api/tools/convert-list?page=' + this.currentPage + "&limit=" + this.pageSize)
        .then(res => {
          if (res.success === true) {
            this.tableData = res.data.records
            this.total = res.data.total
          }
        })
    },
  }
}
</script>

<style>
.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 150px;
  margin-left: 10px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
</style>
