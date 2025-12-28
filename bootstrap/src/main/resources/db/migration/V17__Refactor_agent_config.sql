-- ==============================================
-- Agent 配置重构迁移脚本
-- Feature: 029-refactor-agent-config
-- Date: 2025-12-28
-- ==============================================
-- 说明：
-- 1. 将 agent.config (JSON) 拆分为独立字段
-- 2. 添加 prompt_template_id 关联提示词模板
-- 3. 添加更多 LLM 参数: top_p, max_tokens, max_runtime
-- ==============================================

-- 1. 添加新字段
ALTER TABLE agent
    ADD COLUMN prompt_template_id BIGINT NULL COMMENT '提示词模板ID' AFTER specialty,
    ADD COLUMN model VARCHAR(100) NULL COMMENT '模型标识' AFTER prompt_template_id,
    ADD COLUMN temperature DECIMAL(3,2) DEFAULT 0.3 COMMENT '温度参数 (0.0-2.0)' AFTER model,
    ADD COLUMN top_p DECIMAL(3,2) DEFAULT 0.9 COMMENT 'Top P 参数 (0.0-1.0)' AFTER temperature,
    ADD COLUMN max_tokens INT DEFAULT 4096 COMMENT '最大输出 token 数' AFTER top_p,
    ADD COLUMN max_runtime INT DEFAULT 300 COMMENT '最长运行时间（秒）' AFTER max_tokens;

-- 2. 从 config JSON 迁移数据到新字段
UPDATE agent
SET model = JSON_UNQUOTE(JSON_EXTRACT(config, '$.model')),
    temperature = COALESCE(JSON_EXTRACT(config, '$.temperature'), 0.3)
WHERE config IS NOT NULL AND config != '';

-- 3. 添加外键约束（可选，根据需要启用）
-- ALTER TABLE agent
--     ADD CONSTRAINT fk_agent_prompt_template
--     FOREIGN KEY (prompt_template_id) REFERENCES prompt_template(id);

-- 4. 添加索引以支持反向查询
CREATE INDEX idx_agent_prompt_template_id ON agent(prompt_template_id);
CREATE INDEX idx_agent_model ON agent(model);

-- 5. 删除旧的 config 字段
ALTER TABLE agent DROP COLUMN config;
