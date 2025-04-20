<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>工具箱</el-breadcrumb-item>
      <el-breadcrumb-item>gguf拆分、合并</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <el-form ref="form" :rules="rules" :model="form" label-width="80px" class="form">
        <el-form-item label="操作选项" prop="options">
          <el-select v-model="form.options" placeholder="请选择操作选项">
            <el-option label="拆分" value="split"></el-option>
            <el-option label="合并" value="merge"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="输入文件" v-show="form.options === 'merge'">
          <el-input type="text" v-model="form.input" placeholder="输入合并文件的目录和名称全路径"></el-input>
          <i style="color: #909399;">合并文件名规则：模型名-00001-of-00003.gguf。例如：DeepSeek-R1-Distill-Qwen-1.5B-Q2_K-00001-of-00003.gguf</i>
        </el-form-item>
        <el-form-item label="输入文件" v-show="form.options === 'split'">
          <el-input type="text" v-model="form.input" placeholder="输入拆分文件的目录和名称全路径"></el-input>
        </el-form-item>
        <el-form-item label="拆分选项" v-show="form.options === 'split'">
          <el-radio-group v-model="form.splitOption" @change="handleSplitOptionChange">
            <el-radio label="split-max-tensors"></el-radio>
            <el-radio label="split-max-size"></el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="split参数" v-show="form.options === 'split'">
          <el-input type="text" v-model="form.splitParam" placeholder="split参数，参考如下"></el-input>
          <i style="color: #909399">对于split-max-tensors为拆分的tensors张量（如：256），对于split-max-size则为拆分的文件大小（如：1G/512M）</i>
        </el-form-item>
        <el-form-item label="输出文件" prop="output">
          <el-input type="text" v-model="form.output" placeholder="输出文件的目录和名称全路径"></el-input>
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
  </div>
</template>

<script>
import {copy, getRequestBodyJson} from "@/common/common";

export default {
  name: "split-merge",
  data() {
    return {
      form: {
        options: 'split',
        input: '',
        output: '',
        splitOption: 'split-max-tensors',
        splitParam: '',
        async: false
      },
      rules: {
        options: [
          {required: true, message: '请输入模型名称', trigger: 'blur'}
        ], output: [
          {required: true, message: '请输入模型名称', trigger: 'blur'}
        ],
      },
    }
  },
  methods: {
    handleSplitOptionChange(e) {
    },
    onSubmit(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {
          this.$http.post('/api/tools/split-merge', getRequestBodyJson(this.form)).then(res => {
            if (res.success === true) {
              this.$message({
                type: 'success',
                message: '操作成功,请打开output文件目录查看'
              })
            }
          })
        }
      })
    }
  }
}

</script>

<style scoped>
.form {
  width: 800px;
}
</style>
