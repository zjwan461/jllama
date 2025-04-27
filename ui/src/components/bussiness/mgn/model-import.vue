<template>
    <div>
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>模型管理</el-breadcrumb-item>
            <el-breadcrumb-item>模型导入</el-breadcrumb-item>
        </el-breadcrumb>

        <el-card>
            <el-form ref="form" :model="form" label-width="80px">
                <el-form-item label="选择模型" prop="model">
                    <el-select v-model="form.modelId" placeholder="请选择模型" @change="modelChange">
                        <el-option v-for="item in modelList" :key="item.id" :label="item.name"
                            :value="item.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="输入文件">
                    <el-upload class="upload-demo" action="/api/mgn/import-file" :limit="1" :file-list="fileList"
                        :before-upload="beforeUpload" :data="form">
                        <el-button size="small" type="primary">点击上传</el-button>
                        <div slot="tip" class="el-upload__tip">上传你的GGUF文件</div>
                    </el-upload>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script>
export default {
    data() {
        return {
            form: {
                modelId: ''
            },
            modelList: [],
            fileList: []
        }
    },
    created() {
        this.getModelList()
    },
    methods: {
        getModelList() {
            this.$http.get('/api/mgn/list-model').then(res => {
                if (res.success === true) {
                    this.modelList = res.data
                }
            })
        },
        modelChange() {
        },
        beforeUpload(file) {
            if (file.name.indexOf(".gguf") <= 0) {
                this.$message.error('文件格式必须是.gguf')
                return false
            }
            if (!this.form.modelId || this.form.modelId === '') {
                this.$message.error('请先选择模型')
                return false
            }
            return true
        }
    }
}
</script>

<style></style>