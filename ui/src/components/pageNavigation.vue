<template>
  <div class="page-navigation" style="position: fixed; bottom: 10px; right: 10px; z-index: 1000;">
    <el-button
      @click="refresh"
      type="primary"
      class="navigation-button"
      round
    >
      &#x21BB; <!-- 刷新符号 HTML 实体 -->
    </el-button>
    <el-button
      @click="goPrevPage"
      :disabled="!canGoBack"
      type="primary"
      class="navigation-button"
      round
    >
      &larr; <!-- 左箭头 HTML 实体 -->
    </el-button>
    <el-button
      @click="goNextPage"
      :disabled="!canGoForward"
      type="primary"
      class="navigation-button"
      round
    >
      &rarr; <!-- 右箭头 HTML 实体 -->
    </el-button>
  </div>
</template>

<script>
export default {
  name: 'pageNavigation',
  data() {
    return {
      canGoBack: true,
      canGoForward: true
    };
  },
  mounted() {
    window.addEventListener('popstate', this.handlePopState);
    this.updateNavigationState();
  },
  beforeDestroy() {
    window.removeEventListener('popstate', this.handlePopState);
  },
  methods: {
    refresh() {
      window.location.reload()
    },
    goPrevPage() {
      window.history.back();
    },
    goNextPage() {
      window.history.forward();
    },
    handlePopState() {
      this.updateNavigationState();
    },
    updateNavigationState() {
      // this.canGoBack = window.history.length > 1;
      // this.canGoForward = window.history.length > 0 && window.history.state!== null;
    }
  }
};
</script>

<style scoped>
.page-navigation {
  display: flex;
  gap: 10px;
}

.navigation-button {
  padding: 8px 16px;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.navigation-button:hover {
  background-color: #2b82ff; /* 鼠标悬停时的背景色 */
}

.navigation-button:disabled {
  background-color: #ccc; /* 禁用时的背景色 */
  cursor: not-allowed;
}
</style>
