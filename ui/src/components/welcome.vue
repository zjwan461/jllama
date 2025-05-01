<template>
  <div class="app-container">
  <el-breadcrumb separator="/">
    <el-breadcrumb-item>系统监控</el-breadcrumb-item>
  </el-breadcrumb>
    <el-row>
      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header"><span><i class="el-icon-cpu"></i> CPU</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <thead>
              <tr>
                <th class="el-table__cell is-leaf"><div class="cell">属性</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">值</div></th>
              </tr>
              </thead>
              <tbody>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">核心数</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.cpuNum }}</div></td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">用户使用率</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.used }}%</div></td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">系统使用率</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.sys }}%</div></td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">当前空闲率</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.free }}%</div></td>
              </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>



      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header"><span><i class="el-icon-tickets"></i> 内存</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <thead>
              <tr>
                <th class="el-table__cell is-leaf"><div class="cell">属性</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">内存</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">JVM</div></th>
              </tr>
              </thead>
              <tbody>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">总内存</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.mem">{{ server.mem.total }}G</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.total }}M</div></td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">已用内存</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.mem">{{ server.mem.used}}G</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.used}}M</div></td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">剩余内存</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.mem">{{ server.mem.free }}G</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.free }}M</div></td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">使用率</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.mem" :class="{'text-danger': server.mem.usage > 80}">{{ server.mem.usage }}%</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm" :class="{'text-danger': server.jvm.usage > 80}">{{ server.jvm.usage }}%</div></td>
              </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header"><span><i class="el-icon-cpu"></i> GPU</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <thead>
              <tr>
                <th class="el-table__cell is-leaf"><div class="cell">GPU型号</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">显存</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">驱动版本</div></th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(gpu, index) in server.gpus" :key="index">
                <td class="el-table__cell is-leaf"><div class="cell">{{ gpu.name }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">{{ gpu.vRam }} GB</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">{{ gpu.version }}</div></td>
              </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header">
            <span><i class="el-icon-monitor"></i> 服务器信息</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <tbody>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">服务器名称</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.computerName }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">操作系统</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.osName }}</div></td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">服务器IP</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.computerIp }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">系统架构</div></td>
                <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.osArch }}</div></td>
              </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span><i class="el-icon-receiving"></i> jllama信息</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <thead>
              <tr>
                <th class="el-table__cell el-table__cell is-leaf"><div class="cell">系统平台</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">系统类型</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">GPU平台</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">llama.cpp版本</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">LLamaFactory版本</div></th>
                <th class="el-table__cell is-leaf"><div class="cell">jllama版本</div></th>
              </tr>
              </thead>
              <tbody v-if="server.jllamaInfo">
              <tr>
                <td class="el-table__cell is-leaf"><div class="cell">{{ server.jllamaInfo.platform }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">{{ server.jllamaInfo.osArch }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">{{ server.jllamaInfo.gpuPlatform }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">{{ server.jllamaInfo.cppVersion }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">{{ server.jllamaInfo.factoryVersion }}</div></td>
                <td class="el-table__cell is-leaf"><div class="cell">{{ server.jllamaInfo.selfVersion }}</div></td>
              </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import {Loading} from "element-ui";

export default {
  name: "Server",
  data() {
    return {
      // 服务器信息
      server: []
    };
  },
  created() {
    this.getList();
    // this.openLoading();
  },
  methods: {
    /** 查询服务器信息 */
    getList() {
      this.$http.get('/api/server/monitor').then(res => {
        // console.log(res.data)
        this.server = res.data;
      })
    },
    // 打开加载层
    openLoading() {
      Loading.service({
        lock: true,
        text: '拼命加载中...',
        background: 'rgba(255,255,255,0.5)',
        target: document.querySelector('body')
      })
    }
  }
};
</script>
