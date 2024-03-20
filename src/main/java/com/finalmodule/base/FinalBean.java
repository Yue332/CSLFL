package com.finalmodule.base;

import com.utils.Configer;

import java.util.ArrayList;
import java.util.List;

public class FinalBean {
	protected Configer config;
	protected List<String> failBugIdList;
	
	public void onPrepare() {
		this.failBugIdList = new ArrayList<String>();
	}
	
	public void setConfig(Configer config) {
		this.config = config;
	}
	
	public void setFailBugId(List<String> bugId) {
		this.failBugIdList = bugId;
	}
}
