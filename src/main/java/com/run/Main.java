package com.run;

import com.finalmodule.base.IFinalProcessModule;
import com.module.IProcessModule;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

//	public static void main(String[] args) {
//		String command1 = "cd /root/defects4j/framework/bin/";
//		String command2 = "./defects4j -p Chart -v 1b -w ";
//	}
	public static void main(String[] args) {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("windows")){
			System.setProperty("user.home", System.getProperty("user.home") + File.separator + "Desktop");
			System.out.println("��ǰΪwindowsϵͳ�����ü�Ŀ¼Ϊ" + System.getProperty("user.home"));
		}
		
		System.out.println("�����������ļ�ȫ·��������س���ʹ��Ĭ�������ļ��� :");
		Scanner sc = new Scanner(System.in);
		String configPath = sc.nextLine();
		sc.close();
		//����Ĭ�ϵ������ļ� Ĭ��·��Ϊ ��Ŀ¼/config.properties
		if("".equals(configPath)) {
			configPath = ConfigUtils.DEF_CONFIG_FILE_PATH;
			System.out.println("[INFO] δ���������ļ���ʹ��Ĭ�������ļ� ("+configPath+")��");
			if(!new File(ConfigUtils.DEF_CONFIG_FILE_PATH).exists()) {
				System.out.println("[INFO] �����ļ������ڣ���ʼ���������ļ���");
				try {
					ConfigUtils.generateDefaultPropertyFile();
					System.out.println("[INFO] �����ļ�������ɣ�");
					System.out.println("[!!!INFO!!!] �������������ļ����ٴ����г���");
					System.exit(0);
				}catch(Exception e) {
					System.out.println("[ERROR] �����ļ������쳣��" + e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		// ��ȡ������Ϣ
		Configer config = new Configer(configPath);
		try {
			config.loadConfig();
		} catch (Exception e) {
			System.out.println("[ERROR] �����ļ������쳣��" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		Runtime runTime = Runtime.getRuntime();
		List<IProcessModule> processModuleList = config.getProcessModuleList();
		List<String> failBugIdList = new ArrayList<String>();
		String[] bugIdArr = config.getBugIdArr();
		StringBuilder failMsg = new StringBuilder();
		long startTime = System.currentTimeMillis();
		if(processModuleList != null && processModuleList.size() != 0) {
			for(String bugId : bugIdArr) {
//				config.setCurrentBugId(bugId);
				System.out.println("[INFO] ��ʼ����bug��" + bugId);
				for(IProcessModule module : processModuleList) {
					try {
						module.setBugId(bugId);
						module.setProjectPath(config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bugId + File.separator);
						module.onPrepare(runTime);
						module.process(runTime);
					} catch (Exception e) {
						failBugIdList.add(bugId);
						failMsg.append("bug["+bugId+"]ģ��["+module.getClass().getName()+"]ִ���쳣���쳣ԭ��" + Utils.getExceptionString(e)).append("\r\n");
						System.out.println("[ERROR] ģ�� ��"+module.getClass().getName()+"�� ִ���쳣��" + e.getMessage());
						e.printStackTrace();
						System.exit(1);
					}
				}
				System.out.println("[INFO] bug��"+bugId+"��������ɣ�");
			}
		}
		// ���մ���ģ��
		List<IFinalProcessModule> fianlProcessModuleList = config.getFinalProcessModuleList();
		StringBuilder finalProcessLog = new StringBuilder();
		for(IFinalProcessModule module : fianlProcessModuleList) {
			System.out.println("[INFO] ��ʼִ��ģ��" + module.getClass().getName());
			finalProcessLog.append("ģ��").append(module.getClass().getName()).append("ִ����־��\r\n");
			try {
				module.setConfig(config);
				module.setFailBugId(failBugIdList);
				module.onPrepare();
				module.process(runTime, finalProcessLog);
			} catch (Exception e) {
				failMsg.append("����ģ�顾"+module.getClass().getName()+"��ִ���쳣���쳣ԭ��" + Utils.getExceptionString(e)).append("\r\n");
				System.out.println("[ERROR] ����ģ�顾"+module.getClass().getName()+"��ִ���쳣��" + e.getMessage());
				e.printStackTrace();
			}
			System.out.println("[INFO] ģ��" + module.getClass().getName() + "ִ�н���");
		}
		long endTime = System.currentTimeMillis();
		
		String failFilePath = config.getConfig(ConfigUtils.FAIL_FILE_PATH_KEY);
		if(!"".contentEquals(failFilePath)) {
			System.out.println("[INFO] ʧ����Ϣ��Ϊ�գ�д��ʧ���ļ�");
			File failFile = new File(failFilePath);
			try {
				FileUtils.writeStringToFile(failFile, failMsg + "\r\n" + finalProcessLog, false);
			} catch (Exception e) {
				System.out.println("[WARNING] ʧ����Ϣд���쳣��" + e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("ִ��ʱ�䣺" + (endTime - startTime) + "ms");
		System.out.println("[INFO] ����ģ��ִ����ɣ����������");
	}

}
