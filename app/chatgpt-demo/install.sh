#!/bin/bash

# 设置Node.js路径
export PATH="/opt/homebrew/Cellar/node@18/18.20.8/bin:$PATH"

# 进入项目目录
cd "$(dirname "$0")"

echo "正在安装依赖..."
npm install

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ 依赖安装成功！"
    echo ""
    echo "现在可以运行: npm run dev"
else
    echo ""
    echo "✗ 安装失败，请检查错误信息"
    exit 1
fi

