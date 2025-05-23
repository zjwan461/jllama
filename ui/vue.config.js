module.exports = {
  publicPath: '/app',
  lintOnSave: false,

  devServer: {
    open: true,
    host: '0.0.0.0',
    port: 8001,
    https: false,
    // 以上的ip和端口是我们本机的;下面为需要跨域的
    proxy: { // 配置跨域
      '/api': {
        target: 'http://localhost:21434/api', // 这里后台的地址模拟的;应该填写你们真实的后台接口
        ws: true,
        changOrigin: true, // 允许跨域
        pathRewrite: {
          '^/api': '' // 请求的时候使用这个api就可以
        }
      }

    }
  },

  outputDir: 'dist'
}
