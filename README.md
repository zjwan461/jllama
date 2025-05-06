# Jllama

## 环境依赖

1. node.js
2. jdk17
3. maven

## 介绍

Jllama 是一个基于Java构建的AI模型运行Web管理控制台。Jllama集成了 **模型下载**、**模型部署**、**服务监控**、**模型量化**、**权重文件拆分合并** 、**权重格式转换**、**模型微调(todo)** 等工具合集。让使用者无需掌握各种AI相关技术就能快速在本地运行模型进行模型推理、量化等操作。我的目标是做一个web版的[ollama](https://ollama.com) ,同时在它的基础上再做一些功能增强。

##  限制

和ollama一样，Jllama仅支持GGUF的模型权重文件格式。这里重要是因为AI模型部署的技术底座选择的是`llama.cpp`

##  技术栈

### 整体技术方案

业务技术底座：SpringBoot、Vue、elementui

AI模型部署技术底座：[llama.cpp](https://github.com/ggml-org/llama.cpp)

AI模型微调技术底座：[LLaMA-Factory](https://github.com/hiyouga/LLaMA-Factory)  developing

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

   你需要从https://github.com/ggml-org/llama.cpp/releases 页面中下载支持你的个人电脑支持的操作系统和GPU类型的release程序包。如下图，如果你的电脑使用的是Windows操作系统且使用NVIDIA显卡。你需要下载带`win-xxx-cuxx`结尾的程序包，并确保你的电脑安装了`cuda`工具集。如果你的电脑使用的是AMD的显卡，请下载带`hip-xx-gfxxx`结尾的程序包。下载完成后，解压程序包，并将程序包目录拷贝到Jllama中即可。

   ![image-20250406163638659](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250406163638659.png) 

   安装并部署cuda：

   你可以从[官方网站](https://developer.nvidia.com/cuda-toolkit-archive)下载并安装它。安装完成后，将环境变量 *CUDA_HOME* 设置为 CUDA 工具包的安装路径，并确保 *nvcc* 编译器在您的 *PATH* 中。例如：

   ```shell
   # 以下是Linux或Mac环境配置环境变量的方式，Windows下请自行百度/google一下环境变量设置
   export CUDA_HOME=/usr/local/cuda
   export PATH="${CUDA_HOME}/bin:$PATH"
   ```
### 四、gguf模型权重拆分、合并

#### 拆分

填写好输入文件、拆分选项、拆分参数、输出文件点击提交。jllama使用了llama.cpp的`llama-gguf-split`功能把原始的gguf拆分为多个gguf文件。利于超大模型权重文件的传输和保存。

![image-20250427233851017](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250427233851017.png) 

![image-20250427234450388](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250427234450388.png) 

#### 合并

填写好输入文件，即需要合并的gguf文件路径（00001-of-0000n命名的文件）、输出文件。jllama使用了llama.cpp的`llama-gguf-split`功能把分散的几个gguf文件合并为一个gguf模型权重文件。

![image-20250427234636207](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250427234636207.png)

![image-20250427235016630](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250427235016630.png) 

### 五、模型量化

对于较大参数量的模型权重文件直接进行本地部署的硬件要求是非常高的，即使使用了CUDA这种GPU加速的方案，其对显存大小的要求也是比较高的。部署一个7B的gguf模型所需显存资源大致如下：

- **16 位浮点数（FP16）**：通常情况下，每个参数占用 2 字节（16 位）。7B 模型意味着有 70 亿个参数，因此大约需要 70 亿 ×2 字节 = 140 亿字节，换算后约为 13.5GB 显存。
- **8 位整数（INT8）**：如果采用 8 位量化，每个参数占用 1 字节（8 位），那么 7B 模型大约需要 70 亿 ×1 字节 = 70 亿字节，即约 6.6GB 显存。

因此，如果使用模型量化技术，将一个FP16的模型量化为INT8的模型，能够减少一半左右的显存占用。能够帮助个人利用有限的硬件进行模型的本地部署。



如下，这里我准备了一个BF16精度的deepseek-r1:1.5B的模型权重文件，将其量化为一个Q4_0精度的模型权重文件。量化完成后，得到一个Q4_0的量化后的模型权重文件。文件大小从BF16的3.3G降低到了1G。

![image-20250427235832621](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250427235832621.png) 

![image-20250428000106123](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428000106123.png) 

![image-20250428000217785](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428000217785.png)

![image-20250428000344346](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428000344346.png)

### 六、模型导入

对于量化后的模型权重文件或者从网上额外下载的模型权重文件jllama提供了模型导入的功能。可以在“模型管理/模型导入”菜单下进行导入。你需要选择一个模型仓库，并上传你的gguf模型权重文件即可完成导入。完成后，在模型管理页面即可看到此权重文件。你可以在“模型进程”菜单中部署此模型权重文件。

![image-20250428000840651](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428000840651.png) 

![image-20250428001209021](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428001209021.png) 

![image-20250428001247546](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428001247546.png) 

​	创建模型部署进程：

![image-20250428001410419](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428001410419.png) 

​	ds_q4_0模型权重运行日志：

![image-20250428001510041](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250428001510041.png) 

 

## 如何构建和部署Jllama

### Windows

运行根目录下的`build.bat` 

###  Linux & Macos

运行根目录下的`build.sh`

运行成功后会在jllama的工程下生成一个build文件夹，Windows操作系统运行**startup.bat,** Linux和Mac运行**startup.sh**脚本，等待几秒钟会自动打开浏览器访问`http://127.0.0.1:21434/app`

![image-20250427224517657](https://jerrysu232.oss-cn-shenzhen.aliyuncs.com/img/image-20250427224517657.png) 

   

   

   







