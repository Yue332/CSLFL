package com.utils.csv2bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2022/9/17
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Csv2BeanAnno {
    boolean line() default false;//字段是否是当前行
    boolean ignore() default false;//是否忽略该字段
    String value() default "";//对应csv字段名，用于当csv中head里的字段名与javabean中字段名不一致的情况
    Class dealFunc() default Object.class;//自定义处理类，需要实现ICsv2BeanDealFunc接口
}
