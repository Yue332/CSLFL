package com.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utils {
	public static String[] executeCommandLine4D4j(Runtime runTime, String commandLine)throws Exception{
		File tmpFile = new File("/tmp/d4j.sh");
		tmpFile.deleteOnExit();
		tmpFile.createNewFile();
		FileUtils.writeStringToFile(tmpFile, "#!/bin/bash", true);
		FileUtils.writeStringToFile(tmpFile, commandLine, true);
		
		return executeCommandLine(runTime, "sh /tmp/d4j.sh");
		
	}
	
	public static String[] executeCommandLine(String bash, Runtime runTime, String... commandLine)throws Exception{
		System.out.println("[DEUG] execute commandLines :" + Arrays.toString(commandLine));
		String[] exeMsg = new String[] {"1",""};
		File f = new File("/bin");
		Process p = null;
		PrintWriter out = null;
		InputStream ins = null;
		BufferedReader read = null;
		try {
			p = runTime.exec(bash, null, f);
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(p.getOutputStream())), true);
			for(String command : commandLine) {
				out.println(command);
			}
			out.println("exit");
			StringBuilder msg = new StringBuilder();
			ins = p.getInputStream();
			read = new BufferedReader(new InputStreamReader(ins));
			String line = null;
			System.out.println("[INFO] commandLine execute result message:");
	        while((line = read.readLine())!=null){
	        	msg.append(line).append("\n");
	        	System.out.println(line);
	        }
	        exeMsg[0] = String.valueOf(p.waitFor());
	        exeMsg[1] = msg.toString();
	        System.out.println("[DEBUG] commandLine execute result code(0-success,1-fail) : " + exeMsg[0]);
//	        System.out.println(exeMsg[1]);
	        return exeMsg;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			if(p != null) {
				p.destroy();
			}
			if(read != null) {
				read.close();
			}
			if(ins != null) {
				ins.close();
			}
			if(out != null) {
				out.close();
			}
			
		}		
	}
	
	public static String[] executeCommandLine(Runtime runTime, String... commandLine)throws Exception{
		return executeCommandLine("/bin/bash", runTime, commandLine);

	}
	
//	public static String[] executeCommand(Runtime runTime, String commandLine)throws Exception{
//		Process p = runTime.exec(commandLine);
//		StringBuilder successMsg = new StringBuilder();
//		StringBuilder failMsg = new StringBuilder();
//		InputStream ins = p.getInputStream();
//		InputStream errs = p.getErrorStream();
//		Scanner scanner = new Scanner(ins);
//		Scanner scanner1 = new Scanner(errs);
//		try {
//			while(scanner.hasNextLine()) {
//				successMsg.append(scanner.nextLine());
//			}
//			while(scanner1.hasNextLine()) {
//				failMsg.append(scanner1.nextLine());
//			}
//			return new String[] {successMsg.toString(), failMsg.toString()};
//		}catch (Exception e) {
//			throw e;
//		}finally {
//			if(scanner != null) {
//				scanner.close();
//			}
//			if(scanner1 != null) {
//				scanner1.close();
//			}
//			if(ins != null) {
//				ins.close();
//			}
//			if(errs != null) {
//				errs.close();
//			}
//		}
//	}
	
	public static String getCompilePathByProjectID(String projectId,String bugId) {
		int bugid = Integer.parseInt(bugId);
		if("Mockito".equals(projectId)) {
			if (bugid >= 1 && bugid <= 11) {
			return "build" + File.separator + "classes" + File.separator + "main" + File.separator;
			}else if(bugid >= 12 && bugid <= 17) {
				return "target" + File.separator + "classes" + File.separator;
			}else if(bugid >= 18 && bugid <= 21) {
				return "build" + File.separator + "classes" + File.separator + "main" + File.separator;
			}else {
				return "target" + File.separator + "classes" + File.separator;
			}
		}else if("Chart".equals(projectId)) {
			return "build";
		}else if("Closure".equals(projectId)) {
			return "build" + File.separator + "classes";
		}else if ("Time".equals(projectId)){
			 if (bugid >= 1 && bugid <= 11) {
				 return "target" + File.separator + "classes" + File.separator;
				}else if(bugid >= 12 && bugid <= 27) {
					return "build" + File.separator + "classes" + File.separator;
				}else {
					return "target" + File.separator + "classes" + File.separator;
				}
		}else {
			return "target" + File.separator + "classes" + File.separator;
		}
       
	}
	
	public static String getSourcePathByProjectID(String projectId) {
		if("Chart".equals(projectId)) {
			return "source";
		}else if("Gson".equals(projectId)) {
			return "gson/src";
		}else {
			return "src";
		}
//		switch (projectId) {
//		case "Lang":
//			return "src";
//		case "Chart":
//			return "source";
//		case "Cli":
//			return "src";
//		case "Closure":
//			return "src";
//		case "Codec":
//			return "src";
//		case "Collections":
//			return "src";
//		case "Compress":
//			return "src";
//		case "Csv":
//			return "src";
//		case "Gson":
//			return "gson/src";
//		case "JacksonCore":
//			return "src";
//		case "JacksonDatabind":
//			return "src";
//		case "JacksonXml":
//			return "src";
//		case "Jsoup":
//			return "src";
//		case "JxPath":
//			return "src";
//		case "Math":
//			return "src";
//		case "Mockito":
//			return "src";
//		case "Time":
//			return "src";
//		default:
//			return "";
//		}
	}
	
    public static String getExceptionString(Throwable e) {
        StringWriter pSw = new StringWriter();
        PrintWriter pPw = new PrintWriter(pSw);
        e.printStackTrace(pPw);
        String pEStr = pSw.toString();
        pPw.close();

        try {
            pSw.close();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        return pEStr;
    }
    
	public static String getType(String matrixName) {
		if(!matrixName.contains("_")) {
			return "";
		}
		String[] names = matrixName.split("_");
		return names[names.length - 1];
	}

	public static List<SortBean> getSuspValueListAfterSort(String suspValuePath, int funcIdx)throws Exception{
		File suspValueFile = new File(suspValuePath);
		if(!suspValueFile.exists()) {
			throw new Exception("[ERROR] �ļ���"+suspValueFile.getName()+"�������ڣ��뽫�ļ�����Ŀ¼��"+suspValueFile.getParent()+"���к����ԣ�");
		}
		List<String> suspValueStrList = FileUtils.readLines(suspValueFile);
		suspValueStrList.remove(0);
		int size = suspValueStrList.size();
		// �����ɶȵ�������
		List<SortBean> list = new ArrayList<SortBean>(size);
		for(String s : suspValueStrList) {
			list.add(new SortBean(s, funcIdx + 1));
		}
		Collections.sort(list);
		return list;
	}

	public static String COMMAND = "cd @PROJECT_PATH@,defects4j test";
	public static String[] getFailingTestArray(Runtime runTime, String projectPath, String project, String bugId) throws Exception {
		String[] command = COMMAND.replace("@PROJECT_PATH@", projectPath + File.separator + project + "_" + bugId + File.separator).split(",");
		String[] ret = Utils.executeCommandLine(runTime, command);
		String[] tmpArr = ret[1].split("-");
		List<String> list = new ArrayList<>(tmpArr.length - 1);
		for(String str : tmpArr) {
			if(str.contains("Failing tests")) {
				continue;
			}
			list.add(str.replace("::", ".").trim());
		}
		return list.toArray(new String[] {});
	}

	public static final String COMMAND_GETALLTESTS = "cd @PROJECT_PATH@, defects4j export -p tests.all";
	public static String[] getAllTestArray(Runtime runTime, String projectPath, String project, String bugId) throws Exception {
		String[] command = COMMAND_GETALLTESTS.replace("@PROJECT_PATH@", projectPath + File.separator + project + "_" + bugId + File.separator).split(",");
		String[] ret = Utils.executeCommandLine(runTime, command);
		return ret[1].split("\r\n");
	}

	public static String[] getAllTestArray(Runtime runTime, String projectPath) throws Exception {
		String[] command = COMMAND_GETALLTESTS.replace("@PROJECT_PATH@", projectPath).split(",");
		String[] ret = Utils.executeCommandLine(runTime, command);
		return ret[1].split("\n");
	}

	static String COMMAND_CD = "cd @PROJECT_PATH@";
	static String COMMAND_LOAD_CLASS = "defects4j export -p dir.bin.classes";

	public static String getSrcDir(Runtime runTime, String projectPath)throws Exception{
		String[] ret = Utils.executeCommandLine(runTime, COMMAND_CD.replace("@PROJECT_PATH@", projectPath), COMMAND_LOAD_CLASS);
		if(!"0".equals(ret[0])) {
			throw new Exception("[ERROR] ��ȡsrcClasses_Dir�쳣��");
		}
		return projectPath + ret[1].replace("\n", "");
	}

	static String COMMAND_BUILD_TEST = "defects4j export -p dir.bin.tests";
	public static String getBuildTest(Runtime runTime, String projectPath)throws Exception{
		String[] ret = Utils.executeCommandLine(runTime, COMMAND_CD.replace("@PROJECT_PATH@", projectPath), COMMAND_BUILD_TEST);
		if(!"0".equals(ret[0])) {
			throw new Exception("[ERROR] ��ȡsrcClasses_Dir�쳣��");
		}
		return projectPath + ret[1].replace("\n", "");
	}

	static String COMMAND_DIR_SRC_TESTS = "defects4j export -p dir.src.tests";
	public static String getSrcTestDir(Runtime runTime, String projectPathWithBug)throws Exception{
		String[] ret = Utils.executeCommandLine(runTime, COMMAND_CD.replace("@PROJECT_PATH@", projectPathWithBug), COMMAND_DIR_SRC_TESTS);
		if(!"0".equals(ret[0])) {
			throw new Exception("[ERROR] ��ȡdir.src.tests�쳣��");
		}
		return projectPathWithBug + ret[1].replace("\n", "");
	}

	public static <T> T[] deleteArrayElements(T[] array, T... removeObj){
		List<T> list = new ArrayList<>(array.length - removeObj.length);
		List<T> removeList = Arrays.asList(removeObj);
		for (T t : array) {
			if (removeList.contains(t)) {
				continue;
			}
			list.add(t);
		}
		T[] tmp = Arrays.copyOf(array, list.size());
		return list.toArray(tmp);
	}

	//����(ע����������ʹ��LinkedList��ӣ�����Ҫ�ж��Ƿ������Ԫ�أ��������л�����ظ���Ԫ��)
	public static String[] intersect(String[] arr1, String[] arr2){
		List<String> l = new LinkedList<>();
		Set<String> common = new HashSet<>();
		for(String str:arr1){
			if(!l.contains(str)){
				l.add(str);
			}
		}
		for(String str:arr2){
			if(l.contains(str)){
				common.add(str);
			}
		}
		String[] result={};
		return common.toArray(result);
	}

	public static String getStringWithFileSeparator(String... str){
		StringBuilder ret = new StringBuilder();
		for (String s : str) {
			ret.append(s);
			if(ArrayUtils.indexOf(str, s) != str.length - 1){
				ret.append(File.separator);
			}
		}
		return ret.toString();
	}

	public static List<String> getMatrixList(String projectBasePath, String projectId, String bugId) throws Exception {
		File file = new File(getStringWithFileSeparator(projectBasePath, projectId + "_" + bugId, "gzoltar_output", projectId, bugId, "matrix"));
		if (!file.exists()) {
			throw new Exception(String.format("�ļ�%s�����ڣ����飡", file.getAbsolutePath()));
		}
		return org.apache.commons.io.FileUtils.readLines(file, StandardCharsets.UTF_8);
	}

	public static List<String> getSpectraList(String projectBasePath, String projectId, String bugId) throws Exception {
		File file = new File(getStringWithFileSeparator(projectBasePath, projectId + "_" + bugId, "gzoltar_output", projectId, bugId, "spectra"));
		if (!file.exists()) {
			throw new Exception(String.format("�ļ�%s�����ڣ����飡", file.getAbsolutePath()));
		}
		return org.apache.commons.io.FileUtils.readLines(file, StandardCharsets.UTF_8);
	}
}
