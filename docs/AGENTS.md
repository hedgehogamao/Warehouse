<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# docs

## Purpose
项目文档目录，包含需求规格说明、数据库设计文档、API 文档等所有项目相关的文档资料。

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `specs/` | 各阶段功能规格说明 (see `specs/AGENTS.md`) |
| `database/` | 数据库设计文档与 SQL 脚本 (see `database/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Spec 文件是功能开发的唯一需求来源，开发前必须先阅读对应 Phase 的 spec
- 任何需求变更必须同步更新 spec 文件
- 数据库设计变更需同步更新 schema 和 ER 图
- 所有文档使用 Markdown 格式

### File Naming
- Spec 文件: `phase-{编号}-{module-name}.md`
- 数据库文档: `schema.md`, `init.sql`, `data.sql`

<!-- MANUAL: -->
