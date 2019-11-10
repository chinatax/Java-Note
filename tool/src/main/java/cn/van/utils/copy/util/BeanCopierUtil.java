package cn.van.utils.copy.util;

import cn.van.utils.copy.domain.UserDO;
import cn.van.utils.copy.domain.UserDTO;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: BeanCopierUtil1
 *
 * @author: Van
 * Date:     2019-11-06 15:21
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
public class BeanCopierUtil {

    /**
     * 创建过的BeanCopier实例放到缓存中，下次可以直接获取，提升性能
     */
    private static final Map<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>();

    /**
     * 对象复制
     * @param source 被复制对象，为空会抛出异常
     * @param targetClass 复制类型
     * @param <T>
     * @return
     */
    public static <T> T copyObject(Object source, Class<T> targetClass) {
        T t =  commonDeal(source, targetClass);
        copyProperties(source, t, null);
        return t;
    }


    /**
     * 对象复制(自定义类型转换)
     * @param source 被复制对象，为空会抛出异常
     * @param targetClass 复制类型
     * @param <T>
     * @return
     */
    public static <T> T copyWithConverter(Object source, Class<T> targetClass) {
        T t =  commonDeal(source, targetClass);
        ConverterModel converter = new ConverterModel();
        copyProperties(source, t, converter);
        return t;
    }

    private static  <T> T commonDeal(Object source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            throw new IllegalArgumentException("复制对象或者被复制类型为空!");
        }
        T t;
        try {
            t = targetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(format("Create new instance of %s failed: %s", targetClass, e.getMessage()));
        }
        return t;
    }
    /**
     * 复制队列
     * @param sourceList 被复制队列
     * @param targetClass 复制类型
     * @param <T>
     * @return
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            throw new IllegalArgumentException("被复制的队列为空!");
        }
        List<T> resultList = new LinkedList<>();
        for (Object result : sourceList) {
            resultList.add(copyObject(result, targetClass));
        }
        return resultList;
    }

    private static void copyProperties(Object source, Object target, Converter converter) {
        boolean isConverter = false;
        if (null != converter) {
            isConverter = true;
        }
        BeanCopier copier = getBeanCopier(source.getClass(), target.getClass(), isConverter);
        copier.copy(source, target, converter);
    }

    private static BeanCopier getBeanCopier(Class sourceClass, Class targetClass, boolean isConverter) {
        String beanKey = generateKey(sourceClass, targetClass);
        BeanCopier copier;
        if (!BEAN_COPIER_CACHE.containsKey(beanKey)) {
            copier = BeanCopier.create(sourceClass, targetClass, isConverter);
            BEAN_COPIER_CACHE.put(beanKey, copier);
        } else {
            copier = BEAN_COPIER_CACHE.get(beanKey);
        }
        return copier;
    }

    private static String generateKey(Class<?> sourceClass, Class<?> targetClass) {
        return sourceClass.getName() + targetClass.getName();
    }

    /**
     * 当源和目标类的属性类型不同时，不能拷贝该属性，此时我们可以通过实现Converter接口来自定义属性转换
     */
    private static class ConverterModel implements Converter {

        /**
         * 时间转换的格式
         */
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * 自定义属性转换
         *
         * @param value   源对象属性类
         * @param target  目标对象里属性对应set方法名,eg.setId
         * @param context 目标对象属性类
         * @return
         */
        @Override
        public Object convert(Object value, Class target, Object context) {
            if (value instanceof Integer) {
                return value;
            } else if (value instanceof LocalDateTime) {
                LocalDateTime date = (LocalDateTime) value;
                return dtf.format(date);
            } else if (value instanceof BigDecimal) {
                BigDecimal bd = (BigDecimal) value;
                return bd.toPlainString();
            }
            // 更多类型转换请自定义
            return value;
        }
    }
    public static void main(String[] args) {
        // UserDO user = new UserDO();
        // user.setId(11);
        // user.setUserName("大鸡腿");
        //
        // UserDTO vo = copyObject(user, UserDTO.class);
        // System.out.println(vo);
        //
        // UserDTO userDTO = copyWithConverter(user, UserDTO.class);
        // System.out.println(userDTO);

    }

}
