<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>工具箱</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <!-- 渲染解析后的 Markdown 内容 -->
      <div v-html="parsedMarkdown"></div>
    </el-card>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css';
const md = new MarkdownIt({
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value;
      } catch (__) {}
    }

    return ''; // 应使用额外的默认 escaping
  }
});
export default {
  name: "tools",
  data() {
    return {
      markdownContent: ''
    }
  },
  created() {
    this.$http.get('/api/base/tools-md').then(res=>{
      if (res.success === true) {
        this.markdownContent = res.data;
      }
    })
  },
  computed:{
    parsedMarkdown() {
      return md.render(this.markdownContent);
    }
  }
}
</script>


<style scoped>

</style>
