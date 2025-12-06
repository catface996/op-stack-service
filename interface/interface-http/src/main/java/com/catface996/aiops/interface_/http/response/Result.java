package com.catface996.aiops.interface_.http.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应对象
 *
 * <p>用于封装所有HTTP接口的响应数据，提供统一的响应格式。</p>
 *
 * <p>响应格式：</p>
 * <pre>
 * {
 *   "code": 0,           // 0表示成功，其他表示错误码
 *   "message": "操作成功", // 响应消息
 *   "data": {}           // 响应数据（可选）
 * }
 * </pre>
 *
 * <p>错误码规范：</p>
 * <ul>
 *   <li>0 - 成功</li>
 *   <li>400xxx - 客户端错误（参数错误、验证失败等）</li>
 *   <li>401xxx - 认证错误（用户名密码错误、Token无效等）</li>
 *   <li>403xxx - 权限错误（无权限访问）</li>
 *   <li>404xxx - 资源不存在</li>
 *   <li>409xxx - 冲突错误（用户名已存在等）</li>
 *   <li>423xxx - 资源被锁定（账号被锁定）</li>
 *   <li>500xxx - 服务器内部错误</li>
 * </ul>
 *
 * @param <T> 数据类型
 * @author AI Assistant
 * @since 2025-11-24
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     * <p>0 表示成功，非0表示失败</p>
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 私有构造函数
     */
    private Result() {
    }

    /**
     * 私有构造函数
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     */
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return new Result<>(0, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }

    /**
     * 成功响应（带自定义消息和数据）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    /**
     * 失败响应（无数据）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（带错误详情数据）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    错误详情数据
     * @param <T>     数据类型
     * @return 失败响应对象
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 判断是否成功
     *
     * @return true 成功，false 失败
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 0;
    }
}
