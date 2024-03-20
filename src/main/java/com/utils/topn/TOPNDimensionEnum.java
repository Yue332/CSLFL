package com.utils.topn;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2022/9/16
 **/
public enum TOPNDimensionEnum {
    TOPN_DIMENSION_CLASS("class", "类"),
    TOPN_DIMENSION_ELEMENT("element", "语句"),
    TOPN_DIMENSION_METHOD("method", "方法");


    private String code;
    private String desc;

    TOPNDimensionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
