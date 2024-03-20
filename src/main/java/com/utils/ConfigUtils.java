package com.utils;


import com.module.Defects4j;
import com.utils.cal.func.Ochiai;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ConfigUtils {
	public static String USER_OS_NAME = System.getProperty("os.name");
	
	public static String DEF_CONFIG_FILE_PATH = System.getProperty("user.home", "/tmp") + File.separator + "config.properties";
	
	public static String PRO_PROCESS_MODULE_KEY = "PROCESS_MODULE";
	public static String PRO_PROCESS_MODULE_COMMENT = "# 处理模块多个使用英文逗号隔开";
	
	public static String PRO_FINAL_PROCESS_MODULE_KEY = "FINAL_PROCESS_MODULE";
	public static String PRO_FINAL_PROCESS_MODULE_COMMENT = "# 最终处理模块 多个使用英文逗号隔开";
	
	public static String PRO_D4J_HOME_KEY = "D4J_HOME";
	
	public static String PRO_D4J_HOME_COMMENT = "# defects4j 的全路径";
	
	public static String PRO_FAULT_LOCALIZATION_DATA_HOME_KEY = "FAULT_LOCALIZATION_DATA_HOME";
	
	public static String PRO_FAULT_LOCALIZATION_DATA_HOME_COMMENT = "# fault-localization-data 的全路径";
	
	public static String PRO_LITHIUM_SLICER_HOME_KEY = "LITHIUM_SLICER_HOME";
	
	public static String PRO_LITHIUM_SLICER_HOME_COMMENT = "# lithium-slicer 的全路径";
	
	public static String PRO_PROJECT_ID_KEY = "PROJECT_ID";
	
	public static String PRO_PROJECT_ID_COMMENT = "# defects4j项目ID [Lang, Chart, Closure, Math, Mockito, Time]";
	
	public static String PRO_BUG_ID_KEY = "BUG_ID";
	
	public static String PRO_BUG_ID_COMMENT = "# defects4j项目BUG_ID，多个使用英文逗号分隔，不配置则使用全部bug";
	
	public static String PRO_TOP_KEY = "TOP";
	
	public static String PRO_TOP_COMMENT = "# lithium slicer的TOP属性";
	
	public static String PRO_PROJECT_PATH_KEY = "PROJECT_PATH";
	
	public static String PRO_PROJECT_PATH_COMMENT = "# defects4j检出项目的全路径";
	
	public static String PRO_TOOL_KEY = "TOOL";
	
	public static String PRO_TOOL_COMMENT = "# gzoltar的TOOL属性 [developer, evosuite, randoop]";
	
	public static String PRO_FUNC_KEY = "FUNC";
	
	public static String PRO_FUNC_COMMENT = "# 分析频谱所用方法，多个使用英文逗号隔开，例：" + Ochiai.class.getName();
	
	public static String FAIL_FILE_PATH_COMMENT = "# 执行失败时失败日志写入得文件全路径（为空不写入）";
	
	public static String FAIL_FILE_PATH_KEY = "FAIL_FILE_PATH";
	
	public static String TOP_N_KEY = "TOP_N";
	
	public static String TOP_N_COMMENT = "# TOP-N";
	
	public static String SORT_FUNC = "SORT_FUNC";
	
	public static String SORT_FUNC_COMMENT = "# csv文件中排倒序的公式名（如为空则默认使用第一个公式）";
	
	public static final String DEFAULT_PROCESS_MODULES = "com.module.Defects4j,com.module.Gzoltar,com.module.LithiumSlicer,com.module.Analysis,com.module.CalculateValueAndGenCSV";
	
	public static Map<String, String> PRO_MAP = new LinkedHashMap<String, String>();
	static {
		PRO_MAP.put(PRO_D4J_HOME_KEY, PRO_D4J_HOME_COMMENT);
		PRO_MAP.put(PRO_PROCESS_MODULE_KEY, PRO_PROCESS_MODULE_COMMENT);
		PRO_MAP.put(PRO_FINAL_PROCESS_MODULE_KEY, PRO_FINAL_PROCESS_MODULE_COMMENT);
		PRO_MAP.put(PRO_FAULT_LOCALIZATION_DATA_HOME_KEY, PRO_FAULT_LOCALIZATION_DATA_HOME_COMMENT);
		PRO_MAP.put(PRO_LITHIUM_SLICER_HOME_KEY, PRO_LITHIUM_SLICER_HOME_COMMENT);
		PRO_MAP.put(PRO_PROJECT_ID_KEY, PRO_PROJECT_ID_COMMENT);
		PRO_MAP.put(PRO_PROJECT_PATH_KEY, PRO_PROJECT_PATH_COMMENT);
		PRO_MAP.put(PRO_BUG_ID_KEY, PRO_BUG_ID_COMMENT);
		PRO_MAP.put(PRO_TOP_KEY, PRO_TOP_COMMENT);
		PRO_MAP.put(SORT_FUNC, SORT_FUNC_COMMENT);
		PRO_MAP.put(PRO_TOOL_KEY, PRO_TOOL_COMMENT);
		PRO_MAP.put(PRO_FUNC_KEY, PRO_FUNC_COMMENT);
		PRO_MAP.put(TOP_N_KEY, TOP_N_COMMENT);
		PRO_MAP.put(FAIL_FILE_PATH_KEY, FAIL_FILE_PATH_COMMENT);
	}
	
	public static void generateDefaultPropertyFile()throws Exception{
		
		File f = new File(DEF_CONFIG_FILE_PATH);
		if(f.exists()) {
			return;
		}
		f.createNewFile();
		FileWriter writer = new FileWriter(f);
    	try {
    		String key;
    		for(Map.Entry<String, String> entry : PRO_MAP.entrySet()) {
    			writer.write(entry.getValue() + "\n");
    			key = entry.getKey();
    			writer.write(key + "=" + (PRO_PROCESS_MODULE_KEY.equals(key) ? DEFAULT_PROCESS_MODULES : "") + "\n");
    		}
    	}catch (Exception e) {
			throw e;
		}finally {
			if(writer != null) {
				writer.close();
			}
		}
	}
	
	public static String[] getAllBugIdByD4J(String d4jHome, String projectId)throws Exception {
		String[] allBugArr = getAllBugIdByFile(projectId);
		if(allBugArr == null) {
			String command = Defects4j.D4J_BASE_COMMAND.replaceAll("@:D4J_HOME@", d4jHome) + "defects4j query -p "+projectId+" -q bug.id -o " + System.getProperty("user.home") + File.separator + projectId + "_allbugs";
			Runtime runTime = Runtime.getRuntime();
			String[] msg = Utils.executeCommandLine(runTime, command);
			System.out.println("[DEBUG] " + Arrays.toString(msg));
			allBugArr = getAllBugIdByFile(projectId);
		}
		return allBugArr;
	}
	
	public static String[] getAllBugIdByFile(String projectId) throws Exception {
		File allBugIdFile = new File(System.getProperty("user.home") + File.separator + projectId + "_allbugs");
		if(!allBugIdFile.exists()) {
			System.out.println("[INFO] 文件【"+allBugIdFile.getAbsolutePath()+"】不存在，开始使用defects4j命令生成");
			return null;
		}
		List<String> list = FileUtils.readLines(allBugIdFile, "UTF-8");
		return list.toArray(new String[] {});
	}
}
