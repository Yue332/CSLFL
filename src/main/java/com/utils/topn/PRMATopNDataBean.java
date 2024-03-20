package com.utils.topn;


import com.utils.csv2bean.Csv2BeanAnno;

import java.math.BigDecimal;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2022/9/17
 **/
public class PRMATopNDataBean {

    @TopNDimension
    @Csv2BeanAnno(value = "class")
    private String clz;

    @TopNDimension(value = TOPNDimensionEnum.TOPN_DIMENSION_METHOD)
    private String method;

    @TopNDimension(sortField = true)
    @Csv2BeanAnno(value = "weighted_score")
    private BigDecimal weightedScore;

    public PRMATopNDataBean() {
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

    public BigDecimal getWeightedScore() {
        return weightedScore;
    }

    public void setWeightedScore(BigDecimal weightedScore) {
        this.weightedScore = weightedScore;
    }
}
