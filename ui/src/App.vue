<template>
  <div id="app">
    <page-navigation/>
    <router-view />
  </div>
</template>

<script>
import pageNavigation from "@/components/pageNavigation.vue";
export default {
  name: 'App',
  components: {
    pageNavigation
  }
}
</script>

<style>
</style>
