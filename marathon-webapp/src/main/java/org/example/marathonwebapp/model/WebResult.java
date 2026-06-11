package org.example.marathonwebapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
public class WebResult<T> implements Serializable {
    // 状态码，用于表示接口调用的结果状态，例如 200 表示成功，400 表示参数错误等
    private Integer code;
    // 消息，用于描述接口调用的结果信息，例如 "操作成功"、"参数错误" 等
    private String message;
    // 数据，用于存储接口返回的具体业务数据
    private T data;

    // 无参构造函数
    public WebResult() {
    }

    // 全参构造函数，方便创建对象时直接初始化所有属性
    public WebResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 静态方法，用于创建表示成功的 WebResult 对象，包含数据
    public static <T> WebResult<T> success(T data) {
        return new WebResult<>(200, "操作成功", data);
    }

    // 静态方法，用于创建表示成功的 WebResult 对象，不包含数据
    public static <T> WebResult<T> success() {
        return new WebResult<>(200, "操作成功", null);
    }

    // 静态方法，用于创建表示失败的 WebResult 对象，包含错误消息
    public static <T> WebResult<T> fail(String message) {
        return new WebResult<>(500, message, null);
    }

    // 静态方法，用于创建表示失败的 WebResult 对象，包含状态码和错误消息
    public static <T> WebResult<T> fail(int code, String message) {
        return new WebResult<>(code, message, null);
    }
}
