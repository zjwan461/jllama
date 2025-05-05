<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>模型进程</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-form :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item label="搜索">
          <el-input v-model="formInline.search" placeholder="搜索运行中的模型"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getTableData">查询</el-button>
          <el-button type="success" @click="addNew">运行</el-button>
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
          prop="cppDir"
          label="执行目录">
        </el-table-column>
        <el-table-column
          width="120px"
          prop="command"
          label="command">
        </el-table-column>
        <el-table-column
          prop="args"
          label="运行参数">
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          width="300">
          <template slot-scope="scope">
            <el-button @click="showLog(scope.row, scope.$index)" type="success" size="small">查看日志</el-button>
            <el-button @click="stop(scope.row, scope.$index)" type="primary" size="small">停止</el-button>
            <el-button @click="webui(scope.row, scope.$index)" type="primary" size="small">webui</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="创建服务进程"
               :visible.sync="showDialog"
               :close-on-press-escape=false
               :close-on-click-modal=false
               :destroy-on-close=true
               width="700px"
               @close="resetDialog"
    >
      <el-form :model="modelForm" :rules="rules" ref="modelForm">
        <el-form-item label="模型" label-width="80px" prop="modelId">
          <el-select v-model="modelForm.modelId" placeholder="模型" @change="modelChange">
            <el-option v-for="item in modelList" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="文件名" label-width="80px" prop="fileId">
          <el-select v-model="modelForm.fileId" placeholder="文件名">
            <el-option v-for="item in fileList" :key="item.id" :label="item.fileName" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="命令" label-width="80px" prop="llamaCommand">
          <el-select v-model="modelForm.llamaCommand" placeholder="命令">
            <el-option v-for="item in commandList" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="host" label-width="80px" prop="host">
          <el-input type="text" v-model="modelForm.host" placeholder="host,例如：localhost,默认值：127.0.0.1"></el-input>
        </el-form-item>
        <el-form-item label="端口" label-width="80px" prop="port">
          <el-input
            type="number"
            placeholder="请输入端口"
            v-model="modelForm.port"
            show-word-limit
          >
          </el-input>
        </el-form-item>
        <el-form-item label="-ngl" label-width="80px" prop="ngl">
          <el-input
            type="number"
            placeholder="存储在 VRAM 中的层数,通常数值越大性能越好,但是过大也会导致显存不足"
            v-model="modelForm.ngl"
            show-word-limit
          >
          </el-input>
        </el-form-item>
        <el-form-item label="-t" label-width="80px" prop="threads">
          <el-input
            type="number"
            placeholder="生成期间使用的线程数（默认值：-1）"
            v-model="modelForm.threads"
            show-word-limit
          >
          </el-input>
        </el-form-item>
        <el-form-item label="-c" label-width="80px" prop="ctxSize">
          <el-input
            type="number"
            placeholder="提示上下文的大小（默认值：4096，0 = 从模型加载）"
            v-model="modelForm.ctxSize"
            show-word-limit
          >
          </el-input>
        </el-form-item>
        <el-form-item label="-np" label-width="80px" prop="parallel">
          <el-input
            type="number"
            placeholder="要解码的并行序列数（默认值：1）"
            v-model="modelForm.parallel"
            show-word-limit
          >
          </el-input>
        </el-form-item>
        <el-form-item label="其他参数" label-width="80px" prop="args">
          <el-input type="textarea" v-model="modelForm.args"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="exec('modelForm')">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="log.logFilePath"
               :visible.sync="showLogDialog"
               :close-on-press-escape=false
               :close-on-click-modal=false
               :destroy-on-close=true
               @close="resetLogDialog"
    >
      <el-card>
        <div slot="header">
          <el-button style="float: right; padding: 3px 0" type="text" @click="copyLogPath">复制文件地址</el-button>
        </div>
        <div ref="scrollableDiv" id="scrollableDiv" class="logDialog">
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
import {copy, getRequestBodyJson, getSettings} from '@/common/common'

export default {
  name: 'watch',
  data() {
    const validatePort = (rule, value, callback) => {
      if (value < 1 || value > 65535) {
        callback(new Error('请输入1~65535的端口值'))
      } else {
        callback()
      }
    }
    return {
      logIndex: 1,
      logLine: 30,
      showLogDialog: false,
      log: {
        logFilePath: '',
        logContent: '',
        id: -1
      },
      showDialog: false,
      modelForm: {
        modelId: '',
        fileId: '',
        port: 8000,
      },
      modelList: [],
      fileList: [],
      commandList: [],
      rules: {
        modelId: [
          {required: true, message: '请输入模型名称', trigger: 'blur'}
        ],
        fileId: [
          {required: true, message: '请输入文件名', trigger: 'blur'}
        ],
        port: [
          {required: true, message: '请输入端口', trigger: 'blur'},
          {validator: validatePort, trigger: 'blur'}
        ],
        llamaCommand: [
          {required: true, message: '请输入命令', trigger: 'blur'}
        ],
      },
      formInline: {
        search: ''
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10
    }
  },
  created() {
    const settings = getSettings();
    this.logLine = settings.logLine
    this.getModelList()
    this.getTableData()
    this.getCommandList()
  },
  methods: {
    webui(row, index) {
      let argArray = JSON.parse(row.args);
      if (argArray.length > 0) {
        let port = 8000
        let portIndex = argArray.findIndex(item => item.indexOf('--port') >= 0)
        if (portIndex !== -1) {
          port = argArray[portIndex + 1]
        }
        let host = '127.0.0.1'
        let hostIndex = argArray.findIndex(item => item.indexOf('--host') >= 0)
        if (hostIndex !== -1) {
          host = argArray[hostIndex + 1] === '0.0.0.0' ? '127.0.0.1' : argArray[hostIndex + 1];
        }
        window.open('http://' + host + ':' + port + '/', '_blank')
      }
    },
    stop(row, index) {
      this.$http.get('/api/process/stop/' + row.execId).then(res => {
        if (res.success === true) {
          this.$message({
            type: 'success',
            message: '停止成功'
          })
          this.getTableData()
        }
      })
    },
    copyLogPath() {
      if (this.log.logFilePath.length > 0) {
        copy(this.log.logFilePath)
      }
    },
    resetLogDialog() {
      this.showDialog = false
      this.log = {
        logFilePath: '',
        logContent: '',
        id: -1
      }
      this.logIndex = 1
    },
    loadLog() {
      this.$http.get('/api/process/log?logFilePath=' + encodeURIComponent(this.log.logFilePath) + '&index=' + this.logIndex + '&line=' + this.logLine).then(res => {
        if (res.success === true) {
          if (res.data.length > 0) {
            this.log.logContent += res.data
            this.logIndex = this.logIndex + this.logLine
            setTimeout(() => {
              if (this.$refs.scrollableDiv) {
                this.$refs.scrollableDiv.scrollTo({
                  top: this.$refs.scrollableDiv.scrollHeight,
                  behavior: 'smooth'
                });
              }
            }, 100)
          } else {
            this.$message({
              type: 'info',
              message: '没有更多日志了'
            })
          }
        }
      })
    },
    showLog(item) {
      this.showLogDialog = true
      let argsArr = JSON.parse(item.args)
      this.log.logFilePath = argsArr[argsArr.length - 1]
      this.log.id = item.id
      this.loadLog(item.id)
    },
    modelChange(modelId) {
      this.getFileList(modelId)
    },
    resetDialog() {
      this.showDialog = false
      this.modelForm = {
        modelId: '',
        fileId: '',
        port: 8000
      }
    },
    addNew() {
      this.showDialog = true
    },
    exec(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {
          this.$http.post('/api/process/create', getRequestBodyJson(this.modelForm)).then(res => {
            if (res.success === true) {
              this.$message.success('创建成功')
              this.resetDialog()
              this.getTableData()
            }
          })
        }
      })
    },
    getTableData() {
      this.$http.get('/api/process/list?page=' + this.currentPage + '&limit=' + this.pageSize + '&search=' + this.formInline.search).then(res => {
        if (res.success === true) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      })
    },
    getModelList() {
      this.$http.get('/api/mgn/list-model').then(res => {
        if (res.success === true) {
          this.modelList = res.data
        }
      })
    },
    getFileList(modelId) {
      this.$http.get('/api/mgn/list-download-file?modelId=' + modelId).then(res => {
        if (res.success === true) {
          this.fileList = res.data
        }
      })
    },
    getCommandList() {
      this.$http.get('/api/process/list-command').then(res => {
        if (res.success === true) {
          this.commandList = res.data
        }
      })
    }
  }
}
</script>

<style>
.logDialog {
  white-space: pre-line;
  overflow-y: auto;
  height: 60vh
}
</style>
