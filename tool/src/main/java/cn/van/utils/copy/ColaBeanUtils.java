package cn.van.utils.copy;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: ColaBeanUtils
 *
 * @author: Van
 * Date:     2019-12-31 16:05
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
public class ColaBeanUtils extends BeanUtils {

    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
        return copyListProperties(sources, target, null);
    }

    /**
     * @author Johnson
     * 使用场景：Entity、Bo、Vo层数据的复制，因为BeanUtils.copyProperties只能给目标对象的属性赋值，却不能在List集合下循环赋值，因此添加该方法
     * 如：List<AdminEntity> 赋值到 List<AdminVo> ，List<AdminVo>中的 AdminVo 属性都会被赋予到值
     * S: 数据源类 ，T: 目标类::new(eg: AdminVo::new)
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target, ColaBeanUtilsCallBack<S, T> callBack) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();
            copyProperties(source, t);
            list.add(t);
            if (callBack != null) {
                // 回调
                callBack.callBack(source, t);
            }
        }
        return list;
    }
}
