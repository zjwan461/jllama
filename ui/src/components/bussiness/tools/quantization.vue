<template>
    <div>
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>工具箱</el-breadcrumb-item>
            <el-breadcrumb-item>模型量化</el-breadcrumb-item>
        </el-breadcrumb>
        <el-row>
            <el-col :span="12" class="card-box">
                <el-card class="card">
                    <div slot="header"><span><i class="el-icon-edit"></i> 量化</span></div>
                    <el-steps :active="step">
                        <el-step title="选择模型" description="选择需要量化的模型文件"></el-step>
                        <el-step title="设置量化参数" description="设置量化的参数"></el-step>
                        <el-step title="指定输出目录" description="量化模型的保存目录"></el-step>
                    </el-steps>
                </el-card>
            </el-col>
            <el-col :span="12" class="card-box">
                <el-card class="card">
                    <div slot="header"><span><i class="el-icon-edit"></i> 操作历史</span></div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
export default {
    name: "quantization",
    data() {
        return {
            step: 1
        }
    }
}
</script>

<style></style>