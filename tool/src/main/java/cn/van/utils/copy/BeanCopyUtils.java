package cn.van.utils.copy;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: BeanCopyUtils
 *
 * @author: Van
 * Date:     2019-09-12 11:38
 * Description: ${DESCRIPTION}
 * Version： V1.0
 * 1.当源类和目标类的属性名称、类型都相同，拷贝结果棒棒哒。
 * 2.当源对象和目标对象的属性名称相同、类型不同,那么名称相同而类型不同的属性不会被拷贝。
 * 另外注意，原始类型（int，short，char）和 他们的包装类型，在这里都被当成了不同类型。因此不会被拷贝。
 * 3.源类或目标类的setter比getter少，拷贝没问题，此时setter多余，但是不会报错。
 * 4.源类和目标类有相同的属性（两者的getter都存在），但是目标类的setter不存在，
 * 此时会抛出NullPointerException(这个在高版本bug已经修改测试通过)
 * <p>
 * * 测试发现:
 * * 场景           耗时(百万次调用)           原理
 * * get/set          22ms              setter/getter调用
 * * BeanCopier       22ms              基于cglib,修改字节码
 * * PropertyUtils    922ms                反射
 * * BeanUtils       12983ms               反射
 * * ^^ <p>
 * * 凡是和反射相关的操作，基本都是低性能的。凡是和字节码相关的操作，基本都是高性能的。
 * <p/>
 */
@Slf4j
public class BeanCopyUtils {

    /**
     * 创建过的BeanCopier实例放到缓存中，下次可以直接获取，提升性能
     */
    private static final Map<String, BeanCopier> BEAN_COPIERS = new ConcurrentHashMap<String, BeanCopier>();

    private static final Table<Class, Class, BeanCopier> BEAN_COPIER_TABLE = HashBasedTable.create();


    /**
     * 该方法没有自定义Converter,简单进行常规属性拷贝
     *
     * @param srcObj  源对象
     * @param destObj 目标对象
     */
    public static void copy(Object srcObj, Object destObj) {
        String key = genKey(srcObj.getClass(), destObj.getClass());
        BeanCopier copier;
        if (!BEAN_COPIERS.containsKey(key)) {
            copier = BeanCopier.create(srcObj.getClass(), destObj.getClass(), false);
            BEAN_COPIERS.put(key, copier);
        } else {
            copier = BEAN_COPIERS.get(key);
        }
        copier.copy(srcObj, destObj, null);
    }
    // https://www.jianshu.com/p/00f952eb17c6
    // public static <T, R> R copy(T t, Class<R> klass) {
    //     BeanCopier beanCopier = BEAN_COPIER_TABLE.get(t.getClass(), klass);
    //     if (beanCopier == null) {
    //         beanCopier = BeanCopier.create(t.getClass(), klass, false);
    //         BEAN_COPIER_TABLE.put(t.getClass(), klass, beanCopier);
    //     }
    //     try {
    //         R r = klass.newInstance();
    //         beanCopier.copy(t, r, null);
    //         return r;
    //     } catch (InstantiationException | IllegalAccessException e) {
    //         throw new RuntimeException(e);
    //     }
    // }
    private static String genKey(Class<?> srcClazz, Class<?> destClazz) {
        return srcClazz.getName() + destClazz.getName();
    }
}
