<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# specs

## Purpose
存放项目各开发阶段的功能规格说明文档（Spec），按 Phase 编号组织。每个 spec 文件详细定义该阶段的功能需求、接口设计、数据结构和验收标准。

## Key Files
| File | Description |
|------|-------------|
| `phase-00-scaffold.md` | Phase 0: 项目骨架搭建与数据库初始化 |
| `phase-01-user-management.md` | Phase 1: 用户管理与登录鉴权 |
| `phase-02-product-management.md` | Phase 2: 商品管理模块 |
| `phase-03-stock-in-inventory.md` | Phase 3: 入库管理与库存管理 |
| `phase-04-sales-order.md` | Phase 4: 销售收银与订单管理 |
| `phase-05-history-statistics.md` | Phase 5: 历史记录与数据统计 |
| `phase-06-mini-program.md` | Phase 6: 微信小程序客户端 |
| `phase-07-deployment.md` | Phase 7: 测试与部署上线 |

## For AI Agents

### Working In This Directory
- 每个 spec 文件包含: 功能概述、API 设计、数据表变更、前端页面、验收标准
- 开发时必须严格按照 spec 实现，不得擅自增减功能
- Phase 之间可能有依赖关系（如 Phase 3 依赖 Phase 2 的商品数据）

### Spec Template Structure
```markdown
# Phase {N}: {Module Name}

## 功能概述
## 数据表变更
## API 接口设计
## 前端页面
## 验收标准
## 依赖项
```

<!-- MANUAL: -->
