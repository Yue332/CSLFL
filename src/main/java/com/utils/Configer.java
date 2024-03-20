package com.utils;

import com.finalmodule.base.IFinalProcessModule;
import com.module.IProcessModule;
import com.utils.cal.IAnalysisFunc;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Configer {
	
	String configFilePath;
	Map<String, String> configMap;
	List<IProcessModule> processModuleList;
	List<IFinalProcessModule> finalProcessModuleList;
	List<IAnalysisFunc> funcList;
	String[] bugIdArr;
	String currentBugId = null;
	
	public Configer(String configFilePath) {
		super();
		this.configFilePath = configFilePath;
		this.processModuleList = new ArrayList<IProcessModule>();
		this.finalProcessModuleList = new ArrayList<IFinalProcessModule>();
		this.funcList = new ArrayList<IAnalysisFunc>();
	}
	
	public void loadConfig()throws Exception{
		File file = new File(configFilePath);
		if(!file.exists()) {
			throw new Exception("[ERROR] 配置文件 【"+configFilePath+"】 不存在！");
		}
		Properties pro = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			BufferedReader bf = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        	pro.load(bf);
        	this.configMap = (Map) pro;
        	if(this.configMap.size() == 0) {
        		throw new Exception("[ERROR] 配置文件无属性！");
        	}
        	// 读取配置里的模块，并实例化
        	String[] processModuleArray = configMap.get(ConfigUtils.PRO_PROCESS_MODULE_KEY).split(",");
        	for(String processModule : processModuleArray) {
        		System.out.println("[DEBUG] 开始加载处理模块 : " + processModule);
        		if("".equals(processModule)) {
        			System.out.println("[INFO] 处理模块为空，不加载！");
        			continue;
        		}
				Class clz = Class.forName(processModule);
				Constructor c = clz.getDeclaredConstructor(Configer.class);
				c.setAccessible(true);
				Object obj = c.newInstance(this);
				if(obj instanceof IProcessModule) {
					processModuleList.add((IProcessModule) obj);
				}else {
					throw new Exception("处理模块 【"+processModule+"】 必须实现接口类： " + IProcessModule.class.getName());
				}
        	}
        	// 读取配置文件中的最终模块（所有模块执行结束后，才执行的模块），并实例化
        	String[] finalProcessModuleArray = configMap.get(ConfigUtils.PRO_FINAL_PROCESS_MODULE_KEY).split(",");
        	for(String module : finalProcessModuleArray) {
        		if("".equals(module)) {
        			continue;
        		}
        		System.out.println("[DEBUG] 开始加载最终处理模块：" + module);
        		Class clz = Class.forName(module);
        		Object obj = clz.newInstance();
        		if(obj instanceof IFinalProcessModule) {
        			this.finalProcessModuleList.add((IFinalProcessModule) obj);
        		}else {
        			throw new Exception("处理模块 【"+module+"】 必须实现接口类： " + IFinalProcessModule.class.getName());
        		}
        	}
        	
        	// 读取配置或从4j中生成需要处理的bugid
        	String projectId = configMap.get(ConfigUtils.PRO_PROJECT_ID_KEY);
        	if(!projectId.contains(",")) {// 项目ID如果包含逗号，说明是多个项目，此时不加载bugid
        		this.bugIdArr = configMap.get(ConfigUtils.PRO_BUG_ID_KEY).split(",");
//        	System.out.println("[DEBUG] bugidArr = " + Arrays.toString(this.bugIdArr));
        		if(this.bugIdArr.length == 0 || "".equals(this.bugIdArr[0])) {
        			System.out.println("[INFO] 配置文件中未配置BUG_ID，使用全部bugid");
        			String d4jHome = configMap.get(ConfigUtils.PRO_D4J_HOME_KEY);
        			this.bugIdArr = ConfigUtils.getAllBugIdByD4J(d4jHome, projectId);
        			System.out.println("[INFO] 项目【"+projectId+"】中包含bug：" + Arrays.toString(this.bugIdArr));
        		}
        		
        	}
		}catch (Exception e) {
			throw e;
		}finally {
			if(in != null) {
				in.close();
			}
		}
	}
	
	public String getConfig(String configKey) {
		return this.configMap.get(configKey);
	}

	public String getConfigFilePath() {
		return configFilePath;
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}

	public List<IProcessModule> getProcessModuleList() {
		return processModuleList;
	}
	
	public List<IFinalProcessModule> getFinalProcessModuleList(){
		return finalProcessModuleList;
	}

	public void setProcessModuleList(List<IProcessModule> processModuleList) {
		this.processModuleList = processModuleList;
	}

	public String[] getBugIdArr() {
		return bugIdArr;
	}

	public void setBugIdArr(String[] bugIdArr) {
		this.bugIdArr = bugIdArr;
	}

	public String getCurrentBugId() {
		return currentBugId;
	}

	public void setCurrentBugId(String currentBugId) {
		this.currentBugId = currentBugId;
	}
	
	
	
}
