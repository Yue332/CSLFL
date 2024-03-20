package com.utils.cal;

import com.utils.ConfigUtils;
import com.utils.Configer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class TFuncRegister {
	public static LinkedHashMap<String, IAnalysisFunc> FUNC_MAP = null;
	public static final String SCAN_PACKAGE = "com/utils/cal/func";

	public static LinkedHashMap<String, IAnalysisFunc> getRegistClass(Configer config, String configKey) throws Exception{
		String inputStr = config.getConfig(configKey);
		if("all".equalsIgnoreCase(inputStr)){
			System.out.println("[INFO] 为配置公式，使用" + SCAN_PACKAGE + "下的所有公式！！！");
			inputStr = loadAllFuncs();
			if(StringUtils.isEmpty(inputStr)){
				throw new RuntimeException("[ERROR] 包" + SCAN_PACKAGE + "下未扫描到公式！！！");
			}
		}

		String[] funcArr = inputStr.split(",");
		FUNC_MAP = new LinkedHashMap<>();
		for(String func : funcArr) {
			Class clz = Class.forName(func);
			Object o = clz.newInstance();
			if(!(o instanceof IAnalysisFunc)) {
				throw new Exception("注册方法类【"+func+"】需要实现" + IAnalysisFunc.class.getName());
			}
			FUNC_MAP.put(clz.getSimpleName(), (IAnalysisFunc) o);
		}
		return FUNC_MAP;
	}

	public static String loadAllFuncs(){
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Enumeration<URL> urls = loader.getResources(SCAN_PACKAGE);
			while(urls.hasMoreElements()){
				URL url = urls.nextElement();
				if(url != null){
					String protocol = url.getProtocol();
					if("file".equals(protocol)){
						String filePath = URLDecoder.decode(url.getFile(), "utf-8");
						return findClassesInPackageByFile(filePath);
					}else if("jar".equals(protocol)){
						JarFile jarFile = ((JarURLConnection)url.openConnection()).getJarFile();
						return findAllClassNameByJar(jarFile);
					}
				}
			}
			return null;
		}catch (Exception e){
			throw new RuntimeException("[ERROR] 加载全部公式异常！" + e.getMessage());
		}
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 */
	private static String findClassesInPackageByFile(String packagePath) {
		String realPackageName = SCAN_PACKAGE.replace("/", ".");
		StringBuilder builder = new StringBuilder();
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return null;
		}
		//以.class结尾的文件
		File[] dirfiles = dir.listFiles(file -> file.getName().endsWith(".class"));
		if(dirfiles == null){
			return null;
		}
		// 循环所有文件
		for (File file : dirfiles) {
			if (file.isFile()) {
				// 去掉后面的.class 只留下类名
				String className = realPackageName + "." + file.getName().substring(0, file.getName().length() - 6);
				builder.append(className).append(",");
			}
		}
		return builder.length() >= 1 ? builder.substring(0, builder.length() - 1) : null;
	}

	private static String findAllClassNameByJar(JarFile jarFile){
		StringBuilder builder = new StringBuilder();
		String realPackageName = SCAN_PACKAGE.replace("/", ".");
		Enumeration<JarEntry> entry = jarFile.entries();
		while (entry.hasMoreElements()){
			JarEntry jarEntry = entry.nextElement();
			String name = jarEntry.getName();
			if(name.endsWith(".class")){
				name = name.replace(".class", "").replace("/", ".");
				if(name.startsWith(realPackageName) && !name.contains("$")){
					builder.append(name).append(",");
				}
			}
		}
		return builder.length() >= 1 ? builder.substring(0, builder.length() - 1) : null;
	}


	public static LinkedHashMap<String, IAnalysisFunc> getRegistClass(Configer config) throws Exception{
		return getRegistClass(config, ConfigUtils.PRO_FUNC_KEY);
	}
}
