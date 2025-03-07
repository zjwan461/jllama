<template>
  <div>
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>权限管理</el-breadcrumb-item>
      <el-breadcrumb-item>角色列表</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card>
      <el-button type="primary">
        添加角色
      </el-button>
      <el-table
        :data="rolelistData"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-row
              v-for="(item1 ,i1) in props.row.menus"
              :key="item1.menuId"
              :class="['bdbottom', i1 ===0 ? 'bdtop':'']"
            >
              <!-- 渲染一级权限 -->
              <el-col :span="5">
                <el-tag
                  type="primary"
                  :closable="true"
                >
                  {{ item1.menuName }}
                </el-tag>
                <i class="el-icon-caret-right" />
              </el-col>
              <!-- 渲染二级、三级权限 -->
              <el-col :span="19">
                <el-row
                  v-for="(item2 , i2) in item1.childrenMenus"
                  :key="item2.menuId"
                  :class="[i2 === 0 ? '':'bdtop']"
                >
                  <el-col :span="6">
                    <el-tag
                      type="success"
                      :closable="true"
                    >
                      {{ item2.menuName }}
                    </el-tag>
                    <i class="el-icon-caret-right" />
                  </el-col>
                  <!-- 三级权限 -->
                  <el-col :span="18">
                    <el-tag
                      v-for="(item3) in item2.childrenMenus"
                      :key="item3.menuId"
                      type="warning"
                      :closable="true"
                      @close="delRight(item3, props.row)"
                    >
                      {{ item3.menuName }}
                    </el-tag>
                  </el-col>
                </el-row>
              </el-col>
            </el-row>
            <!-- <pre>{{props.row}}</pre> -->
          </template>
        </el-table-column>
        <el-table-column
          type="index"
          label="#"
        />
        <el-table-column
          prop="roleName"
          label="角色名称"
        />
        <el-table-column
          prop="roleDesc"
          label="角色描述"
        />
        <el-table-column
          prop="option"
          label="操作"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              icon="el-icon-edit"
              size="small"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              icon="el-icon-delete"
              size="small"
            >
              删除
            </el-button>
            <el-button
              type="warning"
              icon="el-icon-setting"
              size="small"
            >
              分配权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      rolelistData: []
    }
  },
  created() {
    this.getRoleList()
  },
  methods: {
    delRight(menu, role) {
      // console.log(menuId, rid)
      this.$confirm(
        '这将会删除' + role.roleName + '角色的' + menu.menuName + '权限',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(async () => {
          const { data: res } = await this.$http.delete(
            '/apis/menudelref/' + menu.menuId + '/' + role.rid
          )
          if (res.code !== 0) return this.$message.error(res.msg)
          console.log(res)
          role.menus = res.data.menus
        })
        .catch(() => {
          console.log('取消了删除操作')
        })
    },
    async getRoleList() {
      const { data: res } = await this.$http.get('/apis/rolelist')
      // console.log(res)
      if (res.code !== 0) return this.$message.error(res.msg)

      this.rolelistData = res.data
    }
  }
}
</script>

<style lang="less" scoped>
.el-table {
  margin-top: 15px;
}
.el-tag {
  margin: 7px;
}
.bdtop {
  border-top: 1px solid #eee;
}
.bdbottom {
  border-bottom: 1px solid #eee;
}
</style>
