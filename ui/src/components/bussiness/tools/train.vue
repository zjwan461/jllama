<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>工具箱</el-breadcrumb-item>
      <el-breadcrumb-item>模型微调</el-breadcrumb-item>
    </el-breadcrumb>
    
    <el-card>
      
    </el-card>
  </div>
</template>

<script>
import {getRequestBodyJson} from "@/common/common";

export default {
  name: "train",
  data() {
    return {
      
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
   
  },
  methods: {
    
  }
}
</script>

<style>

</style>
