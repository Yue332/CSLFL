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
			System.out.println("[INFO] Ϊ���ù�ʽ��ʹ��" + SCAN_PACKAGE + "�µ����й�ʽ������");
			inputStr = loadAllFuncs();
			if(StringUtils.isEmpty(inputStr)){
				throw new RuntimeException("[ERROR] ��" + SCAN_PACKAGE + "��δɨ�赽��ʽ������");
			}
		}

		String[] funcArr = inputStr.split(",");
		FUNC_MAP = new LinkedHashMap<>();
		for(String func : funcArr) {
			Class clz = Class.forName(func);
			Object o = clz.newInstance();
			if(!(o instanceof IAnalysisFunc)) {
				throw new Exception("ע�᷽���ࡾ"+func+"����Ҫʵ��" + IAnalysisFunc.class.getName());
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
			throw new RuntimeException("[ERROR] ����ȫ����ʽ�쳣��" + e.getMessage());
		}
	}

	/**
	 * ���ļ�����ʽ����ȡ���µ�����Class
	 */
	private static String findClassesInPackageByFile(String packagePath) {
		String realPackageName = SCAN_PACKAGE.replace("/", ".");
		StringBuilder builder = new StringBuilder();
		// ��ȡ�˰���Ŀ¼ ����һ��File
		File dir = new File(packagePath);
		// ��������ڻ��� Ҳ����Ŀ¼��ֱ�ӷ���
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("�û�������� " + packageName + " ��û���κ��ļ�");
			return null;
		}
		//��.class��β���ļ�
		File[] dirfiles = dir.listFiles(file -> file.getName().endsWith(".class"));
		if(dirfiles == null){
			return null;
		}
		// ѭ�������ļ�
		for (File file : dirfiles) {
			if (file.isFile()) {
				// ȥ�������.class ֻ��������
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