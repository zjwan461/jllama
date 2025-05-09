<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>工具箱</el-breadcrumb-item>
      <el-breadcrumb-item>模型微调</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-alert :title="tips" type="warning">
      </el-alert>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="简单模式" name="simple">
          <el-row>
            <el-col :span="12">
              <el-form ref="form" :rules="rules" :model="form" label-width="130px">
                <el-form-item label="模型名称" prop="modelName">
                  <el-input v-model="form.modelName" type="text" placeholder="模型名称"></el-input>
                </el-form-item>
                <el-form-item label="模型路径" prop="modelPath">
                  <el-input v-model="form.modelPath" type="text" placeholder="模型路径"></el-input>
                </el-form-item>
                <el-form-item label="数据集路径" prop="datasetPath">
                  <el-input v-model="form.datasetPath" type="text" placeholder="数据集路径"></el-input>
                </el-form-item>
                <el-form-item label="数据集" prop="dataset">
                  <el-input v-model="form.dataset" type="text" placeholder="数据集"></el-input>
                </el-form-item>
                <el-form-item label="训练轮数" prop="trainTimes">
                  <el-input-number v-model="form.trainTimes" placeholder="训练轮数"></el-input-number>
                </el-form-item>
                <el-form-item label="截断长度" prop="cutLen">
                  <el-input-number v-model="form.cutLen" placeholder="截断长度"></el-input-number>
                </el-form-item>
                <el-form-item label="输出目录" prop="outputDir">
                  <el-input v-model="form.outputDir" placeholder="输出目录"></el-input>
                </el-form-item>
                <el-form-item label="配置路径" prop="outputConfigPath">
                  <el-input v-model="form.outputConfigPath" placeholder="配置路径"></el-input>
                </el-form-item>
                <el-form-item label="日志输出" prop="logOutput">
                  <el-switch v-model="form.logOutput"></el-switch>
                  <i style="color: #909399;"> 将训练日志输出到网页</i>
                </el-form-item>
                <el-form-item>
                  <el-button type="info" @click="preview('form')">预览</el-button>
                  <el-button type="primary" @click="onSubmit('form')">提交</el-button>
                </el-form-item>
              </el-form>
            </el-col>
            <el-col :span="12">
              <div class="logBox">
                命令预览：
                <div class="markdown-it-preview" v-html="markdownItContent"></div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="原生模式" name="origin">
          <el-button type="text" @click="openOrigin()">打开LlamaFactory原生网页</el-button>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getRequestBodyJson } from "@/common/common";
import MarkdownIt from 'markdown-it'
const md = new MarkdownIt({
  breaks: true // 转换换行符为<br>，更多配置可查看官网
})
export default {
  name: "train",
  data() {
    return {
      llamaFactoryUrl: '',
      commandPreview: '',
      tips: '要使用此功能必须先在设置中配置Python运行环境，并按照指引安装LlamaFactory依赖',
      activeName: 'simple',
      form: {
        modelName: '',
        modelPath: '',
        datasetPath: '',
        dataset: '',
        trainTimes: 30,
        cutLen: 2048,
        outputDir: '',
        outputConfigPath: '',
        logOutput: false
      },
      rules: {
        modelName: [
          { required: true, message: '请选择模型名称', trigger: 'blur' }
        ]
        , modelPath: [
          { required: true, message: '请输入模型路径', trigger: 'blur' }
        ],
        datasetPath: [
          { required: true, message: '请输入数据集路径', trigger: 'blur' }
        ],
        dataset: [
          { required: true, message: '请输入数据集', trigger: 'blur' }
        ],
        trainTimes: [
          { required: true, message: '训练轮数', trigger: 'blur' }
        ],
        cutLen: [
          { required: true, message: '截断长度', trigger: 'blur' }
        ],
        outputDir: [
          { required: true, message: '输出目录', trigger: 'blur' }
        ],
        outputConfigPath: [
          { required: true, message: '配置路径', trigger: 'blur' }
        ],
        logOutput: [
          { required: true, message: '日志输出', trigger: 'blur' }
        ],
      },
    }
  },
  created() {
    this.getLlamaFactoryUrl()
  },
  computed: {
    markdownItContent() {
      return md.render(this.commandPreview)
    }
  },
  methods: {
    handleClick(tab, event) {
    },
    onSubmit(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {

        }
      })
    },
    preview(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {
          this.$http.post('/api/tools/train/command-preview', getRequestBodyJson(this.form)).then(res => {
            if (res.success === true) {
              this.commandPreview = res.data
            }
          })
        }
      })
    },
    openOrigin() {
      if (this.llamaFactoryUrl.length > 0) {
        window.open(this.llamaFactoryUrl, '_blank')
      }
    },
    getLlamaFactoryUrl() {
      this.$http.get('/api/tools/train/llamafactory-url').then(res => {
        if (res.success === true) {
          this.llamaFactoryUrl = res.data
        }
      })
    }
  }
}
</script>

<style>
.form {
  width: 600px;
}

.logBox {
  margin: auto 20px;
}
</style>
