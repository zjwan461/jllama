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
            <el-button type="primary" @click="save">提交</el-button>
          </el-form-item>
        </el-form>
      </el-card>
  </div>
</template>

<script>
import {getRequestBodyJson} from "@/common/common";

export default {
  data() {
    return {
      settings: {
        llamaCppDir:'',
        modelSaveDir:'',
        logSaveDir:'',
        logLine: 50,
        logSaveDay: 7,
        gpuFlag: 0,
      }
    }
  },
  mounted() {
    this.getSettings();
  },
  methods: {
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
