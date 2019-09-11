package cn.van.utils.result;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: ResultCode
 *
 * @author: Van
 * Date:     2019-06-09 16:31
 * Description: 状态码枚举
 * Version： V1.0
 */
public enum  ResultCode {
    /*** 通用部分 100 - 599***/

    // 成功请求
    SUCCESS(200, "successful"),
    // 重定向
    REDIRECT(301, "redirect"),
    // 资源未找到
    NOT_FOUND(404, "not found"),
    // 服务器错误
    SERVER_ERROR(500,"server error"),


    /*** 这里可以根据不同模块用不同的区级分开错误码，例如:  ***/

    // 1000～1999 区间表示用户模块错误
    // 2000～2999 区间表示订单模块错误
    // 3000～3999 区间表示商品模块错误
    // 。。。


    ;


    private int code;
    private String message;



    ResultCode(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean success() {
        return this.code == 200;
    }

    public static ResultCode valueOf(int code) {
        for (ResultCode value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return SUCCESS;
    }
}
