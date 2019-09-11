package cn.van.utils;

import cn.van.utils.result.HttpResult;
import cn.van.utils.result.ResultCode;
import org.junit.Test;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: HttpResultTest
 *
 * @author: Van
 * Date:     2019-09-09 18:37
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
public class HttpResultTest {

    @Test
    public void success() {
        HttpResult httpResult = HttpResult.success();
        System.out.println(httpResult.toString());
    }

    @Test
    public void successWithMsg() {
        HttpResult httpResult = HttpResult.success("data");
        System.out.println(httpResult.toString());
    }

    @Test
    public void failure() {
        HttpResult httpResult = HttpResult.failure(1,"error");
        System.out.println(httpResult.toString());
    }
    @Test
    public void failureWithMsg() {
        HttpResult httpResult = HttpResult.failure(ResultCode.NOT_FOUND);
        System.out.println(httpResult.toString());
    }
}
