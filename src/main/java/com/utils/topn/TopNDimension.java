package com.utils.topn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: TopN的维度 分为类 语句 和 方法
 * Author: zhangziyi
 * Date: 2022/9/16
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TopNDimension {

    TOPNDimensionEnum value() default TOPNDimensionEnum.TOPN_DIMENSION_CLASS;

    boolean sortField() default false;
}
