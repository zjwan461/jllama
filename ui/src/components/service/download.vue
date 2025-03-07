<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>服务设置</el-breadcrumb-item>
      <el-breadcrumb-item>修改资料</el-breadcrumb-item>
    </el-breadcrumb>
    <div style="margin: 20px 0">
      <div>
        <a href="#" id="download" @click="download()" style="color: #01AAED">点击下载充值网关(绿色版)</a>
      </div>
      <br>
      <div>
        <a href="#" id="downloadSetup" @click="downloadSetup" style="color: #01AAED">点击下载充值网关(安装版)</a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "download",
  methods: {
    download() {
      window.location.href = '/admin/api/file/download'
    },
    downloadSetup() {
      window.location.href = '/admin/api/file/downloadSetup'
    },
  }
}
</script>

<style scoped lang="less">

</style>
