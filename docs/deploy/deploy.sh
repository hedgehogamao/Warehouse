#!/bin/bash
# =============================================
# 汽车配件门店系统 - 一键部署脚本
# 使用方法: sudo bash deploy.sh
# =============================================

set -e

# 配置
APP_NAME="autoparts"
APP_DIR="/opt/autoparts"
ADMIN_DIR="/var/www/autoparts-admin"
DB_NAME="auto_parts_store"
DB_USER="autoparts"
DB_PASSWORD="请修改为强密码"
DOMAIN="your-domain.com"

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# =============================================
# 1. 检查环境
# =============================================
check_environment() {
    log_info "检查系统环境..."

    if [ "$(id -u)" -ne 0 ]; then
        echo "请使用 root 权限运行此脚本"
        exit 1
    fi

    # 检查必要工具
    command -v java >/dev/null 2>&1 || { log_warn "未找到 Java，正在安装..."; apt install openjdk-17-jdk -y; }
    command -v mysql >/dev/null 2>&1 || { log_warn "未找到 MySQL，正在安装..."; apt install mysql-server -y; }
    command -v nginx >/dev/null 2>&1 || { log_warn "未找到 Nginx，正在安装..."; apt install nginx -y; }
    command -v node >/dev/null 2>&1 || { log_warn "未找到 Node.js，正在安装..."; curl -fsSL https://deb.nodesource.com/setup_18.x | bash -; apt install nodejs -y; }

    log_info "环境检查完成"
}

# =============================================
# 2. 配置数据库
# =============================================
setup_database() {
    log_info "配置数据库..."

    mysql -u root -p << EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
EOF

    # 导入数据
    mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < docs/database/schema.sql
    mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < docs/database/init-data.sql

    log_info "数据库配置完成"
}

# =============================================
# 3. 部署后端
# =============================================
deploy_backend() {
    log_info "部署后端..."

    # 创建目录
    mkdir -p $APP_DIR

    # 构建
    cd server
    mvn clean package -DskipTests -q
    cd ..

    # 复制文件
    cp server/target/auto-parts-server.jar $APP_DIR/

    # 配置生产环境
    cp server/src/main/resources/application-prod.yml $APP_DIR/

    # 安装服务
    cp docs/deploy/autoparts.service /etc/systemd/system/
    systemctl daemon-reload
    systemctl enable $APP_NAME
    systemctl start $APP_NAME

    log_info "后端部署完成"
}

# =============================================
# 4. 部署前端
# =============================================
deploy_frontend() {
    log_info "部署前端..."

    cd admin
    npm install -q
    npm run build
    cd ..

    # 创建目录并复制
    mkdir -p $ADMIN_DIR
    cp -r admin/dist/* $ADMIN_DIR/

    log_info "前端部署完成"
}

# =============================================
# 5. 配置 Nginx
# =============================================
setup_nginx() {
    log_info "配置 Nginx..."

    # 复制配置
    cp docs/deploy/nginx.conf /etc/nginx/sites-available/$APP_NAME

    # 替换域名
    sed -i "s/your-domain.com/$DOMAIN/g" /etc/nginx/sites-available/$APP_NAME

    # 启用站点
    ln -sf /etc/nginx/sites-available/$APP_NAME /etc/nginx/sites-enabled/
    rm -f /etc/nginx/sites-enabled/default

    # 测试配置
    nginx -t

    # 重启
    systemctl restart nginx

    log_info "Nginx 配置完成"
}

# =============================================
# 6. 配置 HTTPS
# =============================================
setup_https() {
    log_info "配置 HTTPS..."

    apt install certbot python3-certbot-nginx -y
    certbot --nginx -d $DOMAIN --non-interactive --agree-tos -m admin@$DOMAIN

    log_info "HTTPS 配置完成"
}

# =============================================
# 7. 验证部署
# =============================================
verify_deployment() {
    log_info "验证部署..."

    # 检查后端
    if curl -s http://localhost:8080/api/v1/health | grep -q "success"; then
        log_info "✅ 后端服务正常"
    else
        log_warn "❌ 后端服务异常"
    fi

    # 检查 Nginx
    if systemctl is-active --quiet nginx; then
        log_info "✅ Nginx 服务正常"
    else
        log_warn "❌ Nginx 服务异常"
    fi

    # 检查前端
    if [ -f "$ADMIN_DIR/index.html" ]; then
        log_info "✅ 前端文件已部署"
    else
        log_warn "❌ 前端文件未找到"
    fi

    log_info "部署验证完成"
    echo ""
    echo "========================================="
    echo "  部署完成！"
    echo "  管理后台: https://$DOMAIN"
    echo "  默认账号: admin / admin123"
    echo "========================================="
}

# =============================================
# 主流程
# =============================================
main() {
    log_info "开始部署汽车配件门店系统..."

    check_environment
    setup_database
    deploy_backend
    deploy_frontend
    setup_nginx
    setup_https
    verify_deployment

    log_info "部署完成！"
}

main "$@"
