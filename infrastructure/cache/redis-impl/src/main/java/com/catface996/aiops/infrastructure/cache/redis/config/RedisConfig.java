package com.catface996.aiops.infrastructure.cache.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 *
 * <p>配置 Redis 序列化方式和连接参数：</p>
 * <ul>
 *   <li>Key: String 序列化</li>
 *   <li>Value: JSON 序列化（Jackson）</li>
 *   <li>Key 前缀：aiops:（通过application.yml配置）</li>
 * </ul>
 *
 * <p>Key 命名规范：</p>
 * <ul>
 *   <li>资源缓存：resource:{resourceId}</li>
 *   <li>拓扑图缓存：topology:{topologyId}</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * 
     * <p>配置序列化方式：</p>
     * <ul>
     *   <li>Key: StringRedisSerializer</li>
     *   <li>Value: Jackson2JsonRedisSerializer</li>
     *   <li>Hash Key: StringRedisSerializer</li>
     *   <li>Hash Value: Jackson2JsonRedisSerializer</li>
     * </ul>
     * 
     * @param connectionFactory Redis 连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        // 使用 Jackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值
        // 使用新的构造函数方式，避免使用已废弃的 setObjectMapper 方法
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
            new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 使用 StringRedisSerializer 来序列化和反序列化 redis 的 key 值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key 采用 String 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value 序列化方式采用 jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash 的 value 序列化方式采用 jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
