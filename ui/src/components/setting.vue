<template>
  <div>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>设置</el-breadcrumb-item>
      </el-breadcrumb>
      <el-card>
        <el-form :model="settings" class="setting-form" label-width="150px">
          <el-form-item label="LlamaCpp程序目录">
            <el-input v-model="settings.llamaCppDir"  placeholder=""></el-input>
          </el-form-item>
          <el-form-item label="模型存放目录">
            <el-input v-model="settings.modelSaveDir"  placeholder=""></el-input>
          </el-form-item>
          <el-form-item label="日志存放目录">
            <el-input v-model="settings.logSaveDir"  placeholder=""></el-input>
          </el-form-item>
          <el-form-item label="日志加载行数">
            <el-input-number v-model="settings.logLine" placeholder=""></el-input-number>
          </el-form-item>
          <el-form-item label="日志保存天数">
            <el-input-number v-model="settings.logSaveDay"  placeholder=""></el-input-number>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="">提交</el-button>
          </el-form-item>
        </el-form>
      </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      settings: {
        llamaCppDir:'',
        modelSaveDir:'',
        logSaveDir:'',
        logLine: 50,
        logSaveDay: 7
      }
    }
  },
  methods: {
  },
}
</script>

<style lang="less" scoped>
.setting-form {
  margin-top: 20px;
  width: 600px;
}
</style>
