# 模型下载
from modelscope import snapshot_download

model_name = "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF"
# 指定下载目录
cache_dir = "/Users/suben/models"
# 允许下载的文件pattern
file_pattern = "DeepSeek-R1-Distill-Qwen-1.5B-Q8_0.gguf"
model_dir = snapshot_download(model_name, cache_dir=cache_dir, allow_file_pattern=file_pattern)
print(f"Model downloaded to {model_dir}")
