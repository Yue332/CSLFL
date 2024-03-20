package com.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SuspValueBean implements Comparable<SuspValueBean>{
    private String line;
    private String element;
    private BigDecimal[] suspValueArr;
    private int funcIdx;
    private String clz;
    private int lineNumber;

    public SuspValueBean(String line, int funcIdx){
        this.line = line;
        this.funcIdx = funcIdx;
        String[] tmp = line.split(",");
        this.element = tmp[0];
        String[] elementArray = this.element.split("#");
        this.clz = elementArray[0];
        this.lineNumber = Integer.parseInt(elementArray[1]);
        this.suspValueArr = new BigDecimal[tmp.length - 1];
        for(int i = 1; i < tmp.length; i ++){
            this.suspValueArr[i - 1] = new BigDecimal(tmp[i]);
        }
    }

    public String getLine() {
        return line;
    }

    public String getElement() {
        return element;
    }

    public BigDecimal[] getSuspValueArr() {
        return suspValueArr;
    }

    public BigDecimal getSuspValueByFuncIdx(int idx){
        return this.suspValueArr[idx];
    }
    
    public BigDecimal getSuspValue() {
    	return this.suspValueArr[this.funcIdx];
    }
    

    public String getClz() {
		return clz;
	}

	public void setClz(String clz) {
		this.clz = clz;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public static List<SuspValueBean> getBeanList(List<String> lineList, int funcIdx){
        List<SuspValueBean> beanList = new ArrayList<>();
        for(String line : lineList){
            beanList.add(new SuspValueBean(line, funcIdx));
        }
        return beanList;
    }
    
    public static void removeElement(List<SuspValueBean> beanList, String element) {
    	for(SuspValueBean bean : beanList) {
    		if(element.equals(bean.getElement())) {
    			beanList.remove(bean);
    			return;
    		}
    	}
    }
    
    @Override
    public String toString() {
    	return this.line;
    }

	@Override
	public int compareTo(SuspValueBean o) {
		return o.getSuspValue().compareTo(this.getSuspValue());
//		return this.getSuspValue().compareTo(o.getSuspValue());
	}
}
