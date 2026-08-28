#!/bin/bash

# 设置Node.js路径
export PATH="/opt/homebrew/Cellar/node@18/18.20.8/bin:$PATH"

# 进入项目目录
cd "$(dirname "$0")"

# 检查Node.js和npm是否可用
if ! command -v node &> /dev/null; then
    echo "错误: 未找到 Node.js"
    echo "请确保 Node.js 已安装并在 PATH 中"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo "错误: 未找到 npm"
    echo "Node.js 版本: $(node --version)"
    echo "PATH: $PATH"
    exit 1
fi

echo "Node.js 版本: $(node --version)"
echo "npm 版本: $(npm --version)"

# 检查依赖是否已安装
if [ ! -d "node_modules" ]; then
    echo "正在安装依赖..."
    npm install
fi

# 启动开发服务器
echo "正在启动开发服务器..."
npm run dev

