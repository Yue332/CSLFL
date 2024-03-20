package com.utils;

import com.module.IProcessModule;

public class Bean {
	protected String projectId;
	protected String projectPath;
	protected String bugId;
	protected Configer config;
	
	public Bean(Configer config) {
		this.projectId = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);

		this.config = config;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public String getBugId() {
		return bugId;
	}

	public void setBugId(String bugId) {
		this.bugId = bugId;
	}

	public Configer getConfig() {
		return config;
	}

	public void setConfig(Configer config) {
		this.config = config;
	}

	public int getProcessType() {
		return IProcessModule.PROCESS_TYPE_SINGLE;
	}

	
	
}
