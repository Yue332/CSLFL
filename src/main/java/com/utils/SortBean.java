package com.utils;

public class SortBean implements Comparable<SortBean>{
	private String str;
	private int funcIdx;
	private String[] arry;
	
	public SortBean(String str, int funcIdx) {
		this.str = str;
		this.funcIdx = funcIdx;
		arry = str.split(",");
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public int getFuncIdx() {
		return funcIdx;
	}

	public void setFuncIdx(int funcIdx) {
		this.funcIdx = funcIdx;
	}

	public double getFuncScore() {
		return Double.parseDouble(arry[funcIdx]);
	}

	@Override
	public int compareTo(SortBean o) {
		double a = o.getFuncScore() - this.getFuncScore();
		if(a > 0) {
			return 1;
		}else if (a == 0) {
			return 0;
		}else {
			return -1;
		}
	}
	
	@Override
	public String toString() {
		return str;
	}
}
