<template xmlns="http://www.w3.org/1999/html">
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>模型管理</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-form :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item label="搜索">
          <el-input v-model="formInline.search" placeholder="搜索"></el-input>
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
        @expand-change="expandChange"
      >
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-form label-position="left" inline class="demo-table-expand">
              <el-form-item label="模型文件列表">
                <!--                <span v-html="props.row.files"></span>-->
                <ul>
                  <li v-for="(item, index) in props.row.files" :key="index">{{ item.fileName }} &nbsp;&nbsp;&nbsp;&nbsp;
                    {{ item.percent }} &nbsp;&nbsp;&nbsp;&nbsp; <el-button type="text" size="small" @click="delFile(item.id)">删除</el-button>
                  </li>
                </ul>
              </el-form-item>
              <el-form-item label="存储目录">
                <span>{{ props.row.saveDir }}</span>
              </el-form-item>
              <el-form-item label="创建时间">
                <span>{{ props.row.createTime }}</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column
          prop="repo"
          label="repo">
        </el-table-column>
        <el-table-column
          prop="name"
          label="模型名称">
        </el-table-column>
        <el-table-column
          prop="downloadPlatform"
          label="下载平台">
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          width="200">
          <template slot-scope="scope">
            <el-button @click="edit(scope.row, scope.$index)" type="primary" size="small">修改</el-button>
            <el-button @click="delModel(scope.row.id, scope.$index)" type="danger" size="small">删除</el-button>
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

    <el-dialog :title="dialogTitle"
               :visible.sync="showDialog"
               :close-on-press-escape=false
               :close-on-click-modal=false
               :destroy-on-close=true
               @close="resetDialog"
    >
      <el-form :model="modelForm" :rules="rules" ref="modelForm">
        <el-form-item label="下载平台" label-width="120px" prop="downloadPlatform">
          <el-select v-model="modelForm.downloadPlatform" placeholder="下载平台">
            <el-option label="modelscope" value="modelscope"></el-option>
            <el-option label="huggingface" value="huggingface" :disabled="true"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称" label-width="120px" prop="name">
          <el-input v-model="modelForm.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="repo" label-width="120px" prop="repo">
          <el-input v-model="modelForm.repo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="revision" label-width="120px">
          <el-input v-model="modelForm.revision" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="root" label-width="120px">
          <el-input v-model="modelForm.root" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="模型文件" label-width="120px">
          <el-table
            v-show="showModelFiles"
            :data="modelFiles"
            @selection-change="handleSelectionChange"
            style="width: 100%">
            <el-table-column
              type="selection"
              width="55">
            </el-table-column>
            <el-table-column
              prop="name"
              label="文件名"
              width="180">
            </el-table-column>
            <el-table-column
              prop="size"
              label="文件大小"
              width="180">
            </el-table-column>
            <el-table-column
              label="操作"
              width="100">
              <template slot-scope="scope">
                <el-button @click="download(scope.row, scope.$index)" type="text" size="small">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="selectRepo('modelForm')" v-show="showSubmit">确 定</el-button>
        <el-button type="success" @click="download(undefined)" v-show="showDownload">下 载</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {getRequestBodyJson, fetchFluxData} from "@/common/common"

export default {
  name: 'mgn',
  data() {
    return {
      fluxCache: new Map(),
      model: {},
      multipleSelection: [],
      showSubmit: true,
      showDownload: false,
      modelFiles: [],
      showModelFiles: false,
      rules: {
        name: [
          {required: true, message: '请输入模型名称', trigger: 'blur'}
        ],
        repo: [
          {required: true, message: '请输入repo仓库名称', trigger: 'blur'}
        ],
        downloadPlatform: [
          {required: true, message: '请输入下载平台', trigger: 'blur'}
        ]
      },
      modelForm: {
        downloadPlatform: 'modelscope',
        revision: '',
        root: ''
      },
      dialogTitle: '新增模型',
      showDialog: false,
      formInline: {
        search: ''
      },
      tableData: [],
      total: 0,
      pageSize: 10,
      pageSizes: [10, 20, 50],
      currentPage: 1
    }
  },
  created() {
    this.getTableData()
  },
  methods: {
    delFile(id) {
      this.$http
        .delete('/api/mgn/del-file/' + id)
        .then(res => {
          if (res.success === true) {
            this.$message({
              message: '已删除',
              type: 'success'
            })
            this.getTableData()
          }
        })
    },
    resetDialog() {
      this.modelForm = {
        downloadPlatform: 'modelscope',
        revision: '',
        root: ''
      }
      this.getTableData()
      this.modelFiles = [];
      this.showModelFiles = false
      this.showDownload = false
      this.showSubmit = true
    },
    edit(row) {
      this.dialogTitle = '修改模型'
      this.showDialog = true
      this.modelForm = row
      if (!this.modelForm.revision) {
        this.modelForm.revision = ''
      }
      if (!this.modelForm.root) {
        this.modelForm.root = ''
      }
    },
    delModel(id) {
      this.$confirm('你确定要删除此模型配置？', '提示').then(() => {
        this.$confirm('是否删除已经下载的模型？').then(() => {
          this.$http.delete('/api/mgn/del/' + id + '/true').then(res => {
            if (res.success === true) {
              this.$message({
                message: '已删除',
                type: 'success'
              })
              this.getTableData()
            }
          })
        }).catch(() => {
          this.$http.delete('/api/mgn/del/' + id + '/false').then(res => {
            if (res.success === true) {
              this.$message({
                message: '已删除',
                type: 'success'
              })
              this.getTableData()
            }
          })
        })
      })
    },
    expandChange(row) {
      if (!row.files) {
        this.$http.get('/api/mgn/list-dl-file?modelId=' + row.id).then(res => {
          if (res.success === true) {
            let line = this.tableData.find(item => item.id === row.id)
            line.files = res.data
            line.files.forEach((item, index) => {
              if (item.percent.indexOf('100.00%') < 0) {
                fetchFluxData('/api/mgn/dl-percent?fileId=' + item.id, (res) => {
                  item.percent = res
                  console.log(res, res.length)
                });
              }
            })
          }
        });
      }
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    download(row, index) {
      if (row) {
        this.$http.post("/api/mgn/create-download", getRequestBodyJson({
          modelId: this.model.id,
          modelName: this.model.name,
          fileName: row.name,
          fileSize: row.size,
        }))
          .then(res => {
            if (res.success === true) {
              this.$http.get('/api/mgn/dl?repo=' + this.modelForm.repo + '&filename=' + row.name)
                .then(res => {
                  if (res.success === true) {
                    this.$message({
                      message: row.name + '开始下载',
                      type: "success"
                    })
                  }
                })
            }
          })
      } else {
        console.log(this.multipleSelection)
        this.multipleSelection.forEach(item => {
          this.$http.post("/api/mgn/create-download", getRequestBodyJson({
            modelId: this.model.id,
            modelName: this.model.name,
            fileName: item.name,
            fileSize: item.size,
          })).then(res => {
            if (res.success === true) {
              this.$http.get('/api/mgn/dl?repo=' + this.modelForm.repo + '&filename=' + item.name)
                .then(res => {
                  if (res.success === true) {
                    this.$message({
                      message: item.name + '开始下载',
                      type: "success"
                    })
                  }
                })
            }
          })
        })
      }

    },
    selectRepo(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http.post('/api/mgn/create', getRequestBodyJson(this.modelForm))
            .then(res => {
              if (res.success === true) {
                this.model = res.data
                this.$http.get('/api/mgn/dl/files?repo=' + this.modelForm.repo + '&revision=' + this.modelForm.revision + '&root=' + this.modelForm.root)
                  .then(res => {
                    if (res.success === true) {
                      this.modelFiles = res.data;
                      this.showModelFiles = true
                      this.showDownload = true
                      this.showSubmit = false
                    }
                  });
              }
            })
        } else {
          return false
        }
      })
    },
    create() {
      this.dialogTitle = '新增模型'
      this.showDialog = true
    },
    getTableData() {
      this.$http.get('/api/mgn/list?page=' + this.currentPage + '&limit=' + this.pageSize + '&search=' + this.formInline.search).then(res => {
        if (res.success === true) {
          this.tableData = res.data.records;
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
