package com.utils.topn;


import com.utils.csv2bean.Csv2BeanAnno;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2022/9/17
 **/
public class BuggyMethod {
    @Csv2BeanAnno(line = true)
    private String line;
    @Csv2BeanAnno(value = "class")
    @TopNDimension
    private String clz;
    @TopNDimension(value = TOPNDimensionEnum.TOPN_DIMENSION_METHOD)
    private String method;

    public BuggyMethod() {
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getClz() {
        return clz;
    }

    public void setClz(String clz) {
        this.clz = clz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "BuggyMethod{" +
                "line='" + line + '\'' +
                ", clz='" + clz + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
