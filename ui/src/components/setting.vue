<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>设置</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-form :model="settings" :rules="rules" class="setting-form" label-width="150px">
        <el-form-item label="LlamaCpp程序目录" prop="llamaCppDir">
          <el-input v-model="settings.llamaCppDir" placeholder=""></el-input>
        </el-form-item>
        <el-form-item label="模型存放目录" prop="modelSaveDir">
          <el-input v-model="settings.modelSaveDir" placeholder=""></el-input>
        </el-form-item>
        <el-form-item label="模型日志存放目录" prop="logSaveDir">
          <el-input v-model="settings.logSaveDir" placeholder=""></el-input>
        </el-form-item>
        <el-form-item label="模型日志加载行数" prop="logLine">
          <el-input-number v-model="settings.logLine" placeholder=""></el-input-number>
        </el-form-item>
        <el-form-item label="模型日志保存天数" prop="logSaveDay">
          <el-input-number v-model="settings.logSaveDay" placeholder=""></el-input-number>
        </el-form-item>
        <el-form-item label="更新提醒">
          <el-switch v-model="settings.updatePush"></el-switch> &nbsp;&nbsp;&nbsp;&nbsp;
          <el-button type="text" @click="checkUpdate">检查更新</el-button>
        </el-form-item>
        <el-form-item label="Python程序目录">
          <el-input v-model="settings.pyDir" placeholder="Python程序目录"></el-input>
          <el-button type="text" @click="downloadPy" :disabled="settings.pyDir!=undefined && settings.pyDir!=''">下载</el-button>
        </el-form-item>
        <el-form-item label="代理IP地址">
          <el-input v-model="settings.proxyIp" placeholder="代理IP地址"></el-input>
        </el-form-item>
        <el-form-item label="代理IP端口">
          <el-input-number v-model="settings.proxyPort" placeholder="代理IP端口"></el-input-number>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getRequestBodyJson } from "@/common/common";

export default {
  data() {
    return {
      rules: {
        llamaCppDir: [
          {required: true, message: 'llama.cpp程序目录必填', trigger: 'blur'}
        ]
        , modelSaveDir: [
          {required: true, message: '模型保存目录必填', trigger: 'blur'}
        ],
        logSaveDir: [
          {required: true, message: 'llama执行日志保存目录必填', trigger: 'blur'}
        ],
        logLine: [
          {required: true, message: 'llama执行日志加载行数必填', trigger: 'blur'}
        ],
        logSaveDay: [
          {required: true, message: 'llama执行日志保存天数必填', trigger: 'blur'}
        ],
      },
      settings: {
        llamaCppDir: '',
        modelSaveDir: '',
        logSaveDir: '',
        logLine: 50,
        logSaveDay: 7,
        gpuFlag: false,
        updatePush: false,
        pyDir: ''
      }
    }
  },
  mounted() {
    this.getSettings();
  },
  methods: {
    downloadPy(){
      window.open('https://github.com/zjwan461/jllama/releases', '_blank');
    },
    checkUpdate() {
      this.$http.get('/api/check-update/cpp').then(res => {
        if (res.success === true) {
          const data = res.data
          if (data.update === true) {
            this.$alert('llama.cpp有新的版本更新：' + data.version + '是否前往下载？', 'llama.cpp更新提醒', {
              confirmButtonText: '确定'
            }).then(() => {
              window.open(data.updateUrl, '_blank');
            })
          }
        }
      })
      this.$http.get('/api/check-update/factory').then(res => {
        if (res.success === true) {
          if (res.data.update === true) {
            this.$alert('LlamaFactory有新的版本更新：' + data.version + '是否前往下载？', 'LlamaFactory更新提醒', {
              confirmButtonText: '确定'
            }).then(() => {
              window.open(data.updateUrl, '_blank');
            })
          }
        }
      })
      this.$http.get('/api/check-update/self').then(res => {
        if (res.success === true) {
          if (res.data.update === true) {
            this.$alert('jllama有新的版本更新：' + data.version + '是否前往下载？', 'jllama更新提醒', {
              confirmButtonText: '确定'
            }).then(() => {
              window.open(data.updateUrl, '_blank');
            })
          }

        }
      })
    },
    save() {
      this.$http.post('/api/base/update-settings', getRequestBodyJson(this.settings)).then(res => {
        if (res.success === true) {
          this.$message({
            type: 'success',
            message: '保存成功'
          })
        }
      })
    },
    getSettings() {
      this.$http.get('/api/base/settings').then(res => {
        if (res.success === true) {
          this.settings = res.data;
        }
      })
    }
  },
}
</script>

<style lang="less" scoped>
.setting-form {
  margin-top: 20px;
  width: 600px;
}
</style>
