package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import com.utils.SortBean;
import com.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CalculateTopN extends FinalBean implements IFinalProcessModule {
	private String buggyLineFilePath;
	private String suspValueFilePath;
	private String projectId;
	private String[] funcArr;
	private String[] bugArr;
	private int top;
	
	@Override
	public void process(Runtime runTime, StringBuilder processLog) throws Exception {
		String title = "bugId,function,Top-N,contained,total_lines" + "\r\n";
		for(int i = 0; i < funcArr.length; i ++) {
			File outputFile = new File(config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "Top"+top+"-"+funcArr[i]+".csv");
			FileUtils.writeStringToFile(outputFile, title, false);
			int funcSum = 0;
			for(String bugId : bugArr) {
				int sum = 0;
				File buggyLineFile = new File(buggyLineFilePath.replaceAll("@:BUG_ID@", bugId));
				if(!buggyLineFile.exists()) {
					throw new Exception("[ERROR] 文件【"+buggyLineFile.getName()+"】不存在，请将文件放入目录【"+buggyLineFile.getParent()+"】中后重试！");
				}
				List<SortBean> list = Utils.getSuspValueListAfterSort(suspValueFilePath.replaceAll("@:BUG_ID@", bugId), i);
				int size = list.size();
				int topNsize = Math.min(size, top);
				if(size < top) {
					System.out.println("[INFO] " + buggyLineFile.getName() + "中语句的行数("+size+")小于topN("+top+")");
				}
				List<String> topNList = new ArrayList<String>(topNsize);
				List<String> topNOnlyElementList = new ArrayList<String>(topNsize);
				for(int j = 0; j < topNsize; j ++) {
					topNList.add(list.get(j).toString());
					topNOnlyElementList.add(list.get(j).toString().split(",")[0]);
				}
				System.out.println("[INFO] bug【" + bugId + "】公式【" + funcArr[i] + "】倒叙排序前【" + topNsize + "】包含的语句： " + topNList.toString());
				List<String> tmpList = org.apache.commons.io.FileUtils.readLines(buggyLineFile);
				List<BuggyLine> buggyLineBeanList = BuggyLine.getBuggyLineList(tmpList);
				List<String> buggyLineList = BuggyLine.getAllElements(buggyLineBeanList);
//				List<String> buggyLineList = FileUtils.readLines(buggyLineFile);
//				for(int j = 0, length = buggyLineList.size(); j < length; j ++) {
//					String[] buggyLine = buggyLineList.get(j).split("#");
//					String newBuggyLine = buggyLine[0].replace("/", ".").replace(".java", "") + "#" + buggyLine[1];
//					buggyLineList.remove(j);
//					buggyLineList.add(j, newBuggyLine);
//				}
				System.out.println("[INFO] " + buggyLineFile.getName() + "包含的bugyyline为：" + buggyLineList.toString());
				for(String buggyLine : buggyLineList) {
					if(topNOnlyElementList.contains(buggyLine)) {
						System.out.println("[INFO] topN中包含buggyLine:" + buggyLine);
						sum += 1;
					}
				}
				funcSum += sum;
				FileUtils.writeStringToFile(outputFile, projectId+"-"+bugId + "," + funcArr[i] + "," + topNsize + "," + (sum > 0 ? "1" : "0") + "," + sum + "\r\n", true);
				System.out.println("--------------------");
			}
			System.out.println("[INFO] 公式【"+funcArr[i]+"】top【"+top+"】sum=【"+funcSum+"】");
		}
		
		
	}

	@Override
	public void onPrepare() {
		super.onPrepare();
		this.projectId = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
		this.buggyLineFilePath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "get_buggy_lines_" + 
				projectId + File.separator + projectId + "-@:BUG_ID@.buggy.lines";
		this.suspValueFilePath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + projectId + "_@:BUG_ID@" + File.separator +
				"gzoltar_output" + File.separator + projectId + File.separator + "@:BUG_ID@" + File.separator + projectId + "-@:BUG_ID@-suspValue.csv";
		this.funcArr = config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
		this.bugArr = config.getBugIdArr();
		this.top = Integer.parseInt(config.getConfig(ConfigUtils.TOP_N_KEY));
	}
}
