-- 更新现有资源的 createdBy 字段
-- 由于移除了认证功能，现有资源的 createdBy 为 NULL
-- 设置为默认操作者 ID 1，以便权限检查能够通过
UPDATE resource SET created_by = 1 WHERE created_by IS NULL;
