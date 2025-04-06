# Jllama

## 介绍

Jllama 是一个基于Java构建的AI模型运行Web管理控制台。Jllama集成了 **模型下载**、**模型部署、**服务监控**、**模型量化(todo)**、**权重文件拆分合并(todo)** 、**权重格式转换(todo)**、**模型微调(todo)** 等工具合集。让使用者无需掌握各种AI相关技术就能快速在本地运行模型进行模型推理、量化等操作。我的目标是做一个web版的[ollama](https://ollama.com) ,同时在它的基础上再做一些功能增强。

##  限制

和ollama一样，Jllama仅支持GGUF的模型权重文件格式。这里重要是因为AI模型部署的技术底座选择的是`llama.cpp`

##  技术栈

### 整体技术方案

业务技术底座：SpringBoot、vue

AI模型部署技术底座：[llama.cpp](https://github.com/ggml-org/llama.cpp)

AI模型微调技术底座：[LLaMA-Factory](https://github.com/hiyouga/LLaMA-Factory)  todo

## 功能介绍

###  一、模型下载

功能截图： 

![image-20250406155403259](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406155403259.png) 

![image-20250406155520650](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406155520650.png) 

说明：

使用者可以在模型下载菜单中点击新增按钮。选择模型下载平台（暂时只支持从[modelscope](https://modelscope.cn/models)下载模型，主要是huggingface需要科学上网不方便，后续会加上）。模型名称可自定义，repo需要和modelscope的模型仓库名一致。比如如下图的`unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF`。点击确认后`Jllama`将会从modelscope上拉取可供下载的文件列表。选择你需要下载的GGUF模型权重文件进行下载即可。

![image-20250406160019339](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406160019339.png)

![image-20250406160439702](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406160439702.png)  

![image-20250406160757093](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406160757093.png) 

###  二、部署模型

功能截图：

![image-20250406160918420](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406160918420.png) 

![image-20250406161013853](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406161013853.png) 

说明：

使用者在下载完模型权重文件以后可以在模型进程菜单下新增模型进程。选择你下载好的模型权重文件，填写相应参数后点击确认。关于llama_server的参数请参考[llama-server.md](llama-server.md)或者参考llama.cpp方法Github仓库。点击提交按钮后，将会生成一条模型运行进程数据。你可以查看日志、停止进程或者点击webui按钮打开llama-server的原生webui页面。

![image-20250406161545133](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406161545133.png) 

运行日志：

![image-20250406161812622](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406161812622.png) 

webui：

点击webui将会打开llama.cpp提供的原生webui，你可以直接使用此webui和大模型进行聊天。当然，llama.cpp提供的web服务也兼容openAI api。你可以将其添加到其他第三方AI RAG软件中，如[AnythingLLM](https://anythingllm.com/)、[Dify](https://dify.ai)、[MaxKB](https://maxkb.cn/)、[chatboxAi](https://chatboxai.app/)等

![image-20250406161841296](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406161841296.png) 

![image-20250406162959242](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406162959242.png) 

###  三、GPU加速

Jllama默认不会开启GPU加速，这主要是考虑到兼容所有的个人电脑。默认情况下使用的是CPU进行模型部署和推理。想要使用GPU加速功能，需要进行如下设置。

1. 点击进入设置页面

<img src="https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406163346893.png" alt="image-20250406163346893" style="zoom:50%;" /> 

2. 修改LlamaCpp程序目录

   你需要从https://github.com/ggml-org/llama.cpp/releases页面中下载支持你的个人电脑支持的操作系统和GPU类型的release程序包。如下图，如果你的电脑使用的是Windows操作系统且使用NVIDIA显卡。你需要下载带`win-xxx-cuxx`结尾的程序包，并确保你的电脑安装了`cuda`工具集。如果你的电脑使用的是AMD的显卡，请下载带`hip-xx-gfxxx`结尾的程序包。下载完成后，解压程序包，并将程序包目录拷贝到Jllama中即可。

   ![image-20250406163638659](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406163638659.png) 

   安装并部署cuda：

   你可以从[官方网站](https://developer.nvidia.com/cuda-toolkit-archive)下载并安装它。安装完成后，将环境变量 *CUDA_HOME* 设置为 CUDA 工具包的安装路径，并确保 *nvcc* 编译器在您的 *PATH* 中。例如：

   ```shell
   # 以下是Linux或Mac环境配置环境变量的方式，Windows下请自行百度/google一下环境变量设置
   export CUDA_HOME=/usr/local/cuda
   export PATH="${CUDA_HOME}/bin:$PATH"
   ```
## 如何部署Jllama

   

   

   







