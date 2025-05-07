<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>设置</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-row>
        <el-col :span="12">
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
              <el-button type="text" @click="downloadPy"
                :disabled="settings.pyDir != undefined && settings.pyDir != ''">下载</el-button>
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
        </el-col>
        <el-col :span="12">
          <el-form>
            <el-form-item>
              <el-button type="text" @click="downloadConvertDep">下载模型转换依赖</el-button>
              <el-button type="text" @click="downloadLlamaFactoryDep">下载LlamaFactory运行依赖</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="text" @click="checkConvertEnv">检查模型转换运行环境</el-button>
              <el-button type="text" @click="checkLlamaFactoryEnv">检查LlamaFactory运行环境</el-button>
            </el-form-item>
            <el-form-item>
              <div class="markdown-it-preview" v-html="markdownItContent"></div>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getRequestBodyJson } from "@/common/common";
import MarkdownIt from 'markdown-it'

export default {
  data() {
    return {
      envInit: '',
      rules: {
        llamaCppDir: [
          { required: true, message: 'llama.cpp程序目录必填', trigger: 'blur' }
        ]
        , modelSaveDir: [
          { required: true, message: '模型保存目录必填', trigger: 'blur' }
        ],
        logSaveDir: [
          { required: true, message: 'llama执行日志保存目录必填', trigger: 'blur' }
        ],
        logLine: [
          { required: true, message: 'llama执行日志加载行数必填', trigger: 'blur' }
        ],
        logSaveDay: [
          { required: true, message: 'llama执行日志保存天数必填', trigger: 'blur' }
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
  computed: {
    markdownItContent() {
      const md = new MarkdownIt({
        breaks: true // 转换换行符为<br>，更多配置可查看官网
      })
      return md.render(this.envInit)
    }
  },
  created() {
    this.getSettings()
    this.getEnvInitMd()
  },
  methods: {
    downloadPy() {
      window.open('https://www.python.org/downloads', '_blank');
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
    },
    checkEnv() {
      this.checkConvertEnv()
      this.checkLlamaFactoryEnv()
    },
    checkConvertEnv() {
      this.$http.post('/api/base/check-convert-env').then(res => {
        if (res.success === true) {
          this.$message({
            type: 'success',
            message: '模型转换环境正常'
          })
        }
      })
    },
    checkLlamaFactoryEnv() {
      this.$http.post('/api/base/check-llamaFactory-env').then(res => {
        if (res.success === true) {
          this.$message({
            type: 'success',
            message: 'Llamafactory环境正常'
          })
        }
      })
    },
    getEnvInitMd() {
      this.$http.get('/api/base/env_init').then(res => {
        if (res.success === true) {
          this.envInit = res.data
        }
      })
    },
    downloadLlamaFactoryDep() {
      window.open('https://github.com/hiyouga/LLaMA-Factory/releases/download/v0.9.2/llamafactory-0.9.2-py3-none-any.whl','_blank')
    },
    downloadConvertDep(){
      window.open('/api/base/download-requirements','_blank')
    }
  },
}
</script>

<style lang="less" scoped>
.setting-form {
  padding: 0 20px;
}
</style>
