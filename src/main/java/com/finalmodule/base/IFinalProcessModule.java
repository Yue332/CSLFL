package com.finalmodule.base;

import com.utils.Configer;

import java.util.List;

public interface IFinalProcessModule {
	public void process(Runtime runTime, StringBuilder processLog)throws Exception;
	
	public void setConfig(Configer conf);
	
	public void setFailBugId(List<String> failBugIdList);
	
	public void onPrepare();
}
