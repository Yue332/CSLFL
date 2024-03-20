package com.module;

public interface IProcessModule {
	public static final int PROCESS_TYPE_MULTI = 1;
	public static final int PROCESS_TYPE_SINGLE = 0;
	
	public void process(Runtime runTime)throws Exception;
	
	public void setBugId(String bugId);
	
	public void setProjectPath(String projectPath);
	
	public void onPrepare(Runtime runTime);
	
	public int getProcessType();
	
}
