# 模型下载
from modelscope import snapshot_download
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--model', type=str, help='要下载的模型名称')
parser.add_argument('--cache_dir', type=str, default="./models", help='模型保存位置')
parser.add_argument('--file_pattern', type=str, default="*", help='允许下载的文件pattern')
args = parser.parse_args()

# 替换为你要下载的模型名称
model_name = args.model
if model_name is None:
    print(f"you must provide a --model parameter ")
else:
    # 指定下载目录
    cache_dir = args.cache_dir
    # 允许下载的文件pattern
    file_pattern = args.file_pattern
    model_dir = snapshot_download(model_name, cache_dir=cache_dir, allow_file_pattern=file_pattern)
    print(f"Model downloaded to {model_dir}")
