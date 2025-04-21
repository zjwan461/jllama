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
                    <el-form ref="form1" :rules="rules1" :model="form1" label-width="80px" v-show="step === 1">
                        <el-form-item label="模型" prop="model">
                            <el-select v-model="form1.model" placeholder="请选择" @change="modelChange">
                                <el-option v-for="item in modelList" :key="item.id" :label="item.name"
                                    :value="item.id"></el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item label="文件名" prop="file">
                            <el-select v-model="form1.file" placeholder="请选择">
                                <el-option v-for="item in fileList" :key="item.id" :label="item.fileName"
                                    :value="item.filePath + '/' + item.fileName"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-form>
                    <el-form ref="form2" :rules="rules2" :model="form2" label-width="80px" v-show="step === 2">
                        <el-form-item label="量化精度" prop="quantizeParam">
                            <el-select v-model="form2.quantizeParam" placeholder="请选择">
                                <el-option v-for="item in supportedQuantizeList" :key="item" :label="item"
                                    :value="item"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-form>
                    <el-form ref="form3" :rules="rules3" :model="form3" label-width="80px" v-show="step === 3">
                        <el-form-item label="输出路径" prop="output">
                            <el-input type="text" v-model="form3.output" placeholder="输出gguf文件的位置"></el-input>
                        </el-form-item>
                    </el-form>
                    <div class="stepButton">
                        <el-button type="info" size="small" :disabled="step === 1" @click="back()">上一步</el-button>
                        <el-button type="primary" size="small" @click="next('form1')"
                            v-show="step === 1">下一步</el-button>
                        <el-button type="primary" size="small" @click="next('form2')"
                            v-show="step === 2">下一步</el-button>
                        <el-button type="primary" size="small" @click="submit('form3')"
                            v-show="step >= 3">提交</el-button>
                    </div>
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
import { getRequestBodyJson } from "@/common/common";
export default {
    name: "quantization",
    data() {
        return {
            step: 1,
            form1: {
                model: '',
                file: ''
            },
            modelList: [],
            fileList: [],
            rules1: {
                model: [
                    { required: true, message: '请输入模型名称', trigger: 'blur' }
                ], file: [
                    { required: true, message: '请输入文件名称', trigger: 'blur' }
                ],
            },
            supportedQuantizeList: [],
            form2: {
                quantizeParam: ''
            },
            rules2: {
                quantizeParam: [
                    { required: true, message: '请输入量化精度', trigger: 'blur' }
                ],
            },
            form3: {
                output: ''
            },
            rules3: {
                output: [
                    { required: true, message: '请输入GGUF文件输出全路径', trigger: 'blur' }
                ],
            }
        }
    },
    created() {
        this.getModelList()
        this.getSupportedQuantize()
    },
    methods: {
        getSupportedQuantize() {
            this.$http.get('/api/tools/list-quantize-param').then(res => {
                if (res.success === true) {
                    this.supportedQuantizeList = res.data
                }
            })
        },
        back() {
            this.step--
        },
        getModelList() {
            this.$http.get('/api/mgn/list-model').then(res => {
                if (res.success === true) {
                    this.modelList = res.data
                }
            })
        },
        modelChange(modelId) {
            this.getFileList(modelId)
        },
        getFileList(modelId) {
            this.$http.get('/api/mgn/list-download-file?modelId=' + modelId).then(res => {
                if (res.success === true) {
                    this.fileList = res.data
                }
            })
        },
        next(form) {
            this.$refs[form].validate((valid) => {
                if (valid) {
                    this.step++
                }
            })
        },
        submit(form) {
            this.$refs[form].validate((valid) => {
                if (valid) {
                    this.step++
                }
                const req = {
                    originModel: this.form1.file,
                    quantizeParam: this.form2.quantizeParam,
                    outputModel: this.form3.output,
                    async: false
                }
                if (this.step >= 3) {
                    this.$http.post('/api/tools/llama-quantize', getRequestBodyJson(req)).then(res => {
                        if (res.success === true) {
                            this.$message({
                                type: 'success',
                                message: '操作成功'
                            })
                        }
                    })
                }
            })
        }
    }
}
</script>

<style>
.stepButton {
    margin: 10px auto;
    height: 50px;
}
</style>