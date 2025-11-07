#!/bin/bash

# Lombok问题快速修复脚本

echo "=========================================="
echo "    Lombok问题快速修复脚本"
echo "=========================================="

# 检查Java版本
echo "检查Java版本..."
java -version

# 检查Maven版本
echo "检查Maven版本..."
mvn -version

# 清理项目
echo "清理项目..."
mvn clean

# 强制更新依赖
echo "强制更新依赖..."
mvn dependency:purge-local-repository

# 重新编译
echo "重新编译项目..."
mvn compile -U

# 检查Lombok依赖
echo "检查Lombok依赖..."
mvn dependency:tree | grep lombok

echo ""
echo "=========================================="
echo "    修复完成！"
echo "=========================================="
echo ""
echo "如果问题仍然存在，请检查："
echo "1. IntelliJ IDEA是否安装了Lombok插件"
echo "2. 是否启用了注解处理 (Annotation Processing)"
echo "3. 项目设置中的JDK版本是否为17"
echo "4. 尝试重启IntelliJ IDEA"
echo ""
echo "详细配置说明请查看: LOMBOK_CONFIG.md"
