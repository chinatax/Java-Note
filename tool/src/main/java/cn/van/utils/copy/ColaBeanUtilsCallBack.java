package cn.van.utils.copy;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: ColaBeanUtilsCallBack
 *
 * @author: Van
 * Date:     2019-12-31 16:05
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
@FunctionalInterface
public interface ColaBeanUtilsCallBack<S, T> {

    void callBack(S t, T s);
}
