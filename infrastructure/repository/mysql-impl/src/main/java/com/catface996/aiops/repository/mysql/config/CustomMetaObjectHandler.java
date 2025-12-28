package com.catface996.aiops.repository.mysql.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元数据自动填充处理器
 *
 * <p>自动填充审计字段：createdAt, updatedAt, createdBy, updatedBy, deleted, version</p>
 *
 * <p>注意：createdBy 和 updatedBy 字段需要通过 UserContext 获取当前用户 ID。
 * 如果 UserContext 未设置，这些字段将不会自动填充。</p>
 */
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContext.getCurrentUserId();

        // 时间字段
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);

        // 用户字段（如果 UserContext 有值）
        if (userId != null) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }

        // 逻辑删除和版本号
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContext.getCurrentUserId();

        // 时间字段
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);

        // 用户字段（如果 UserContext 有值）
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        }
    }
}
