package com.finalmodule;


import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;
import com.utils.csv2bean.Csv2BeanUtil;
import com.utils.topn.BuggyMethod;
import com.utils.topn.PRMATopNDataBean;
import com.utils.topn.TopN;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2022/9/20
 **/
public class CalMAR extends FinalBean implements IFinalProcessModule {

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectId = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String funcName = config.getConfig(ConfigUtils.PRO_FUNC_KEY);
        if(funcName.startsWith(TopN.FUNC_PACKAGE)){
            funcName = funcName.replace(TopN.FUNC_PACKAGE, "");
        }
        //weighted中的bugid
        List<String> bugIdList = getBugIdList(projectId, funcName);
        //buggymethod中的bugid
        List<String> bugIdListFromBuggyMethod = getBugIdListFromBuggyMethod(projectId);
        //取交集
        List<String> readBugIdList = new ArrayList<>(bugIdListFromBuggyMethod);
        readBugIdList.retainAll(bugIdList);

        MAR mar = new MAR(projectId);

        String finalFuncName = funcName;
        readBugIdList.forEach(bugid ->{
            File buggyMethodFile = new File("/home/yy/BuggyMethod/" +
                    projectId + "-" + bugid + ".buggy.methods");
            File weightedFile = new File("/home/yy/MBFL4-weighted/" +
                    projectId + "-" + bugid + "-" + finalFuncName + "-Metallaxis-weighted.csv");
            BigDecimal buggyMethodScore = BigDecimal.ZERO;
            List<BuggyMethod> buggyMethodList;
            try {
                buggyMethodList = Csv2BeanUtil.convertCsv2Bean(buggyMethodFile, BuggyMethod.class);
                if(CollectionUtils.isEmpty(buggyMethodList)){
                    throw new Exception(String.format("%s内容为空，请检查！", buggyMethodFile.getAbsolutePath()));
                }
                List<PRMATopNDataBean> weightedList = Csv2BeanUtil.convertCsv2Bean(weightedFile, PRMATopNDataBean.class);
                for (int i = 0; i < weightedList.size(); i++) {
                    PRMATopNDataBean weighted = weightedList.get(i);
                    boolean ifMatchBuggyMethod = buggyMethodList.stream().anyMatch(buggyMethod -> weighted.getClz().equals(buggyMethod.getClz()) && weighted.getMethod().equals(buggyMethod.getMethod()));
                    if(ifMatchBuggyMethod){
                        BigDecimal score = new BigDecimal(i + 1).divide(new BigDecimal(weightedList.size()), 8, RoundingMode.HALF_UP);
                        buggyMethodScore = buggyMethodScore.add(score);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            mar.put(bugid, buggyMethodScore.divide(new BigDecimal(buggyMethodList.size()), 8, RoundingMode.HALF_UP));
        });
        File outputFile = new File("/home/yy/MAR/" + projectId + "-" + funcName + "-MAR.csv");
        mar.output(outputFile);
    }

    private List<String> getBugIdList(String projectId, String funcName)throws Exception{
        File dir = new File("/home/yy/MBFL4-weighted/");
        File[] files = dir.listFiles(file -> file.getName().contains(projectId) && file.getName().contains(funcName));
        if(ArrayUtils.isEmpty(files)){
            throw new Exception(String.format("[ERROR] 目录%s下没有项目%s公式%s的csv文件！", dir.getAbsolutePath(), projectId, funcName));
        }
        List<String> bugIdList = new ArrayList<>(files.length);
        for (File file : files) {
            String fileName = file.getName();
            bugIdList.add(fileName.split("-")[1]);
        }
        return bugIdList;
    }

    private List<String> getBugIdListFromBuggyMethod(String projectId)throws Exception{
        File dir = new File("/home/yy/BuggyMethod/");
        File[] files = dir.listFiles(file -> file.getName().contains(projectId));
        if(ArrayUtils.isEmpty(files)){
            throw new Exception(String.format("[ERROR] 目录%s下没有项目%s的buggyMethod文件！", dir.getAbsolutePath(), projectId));
        }
        List<String> bugIdList = new ArrayList<>(files.length);
        for (File file : files) {
            String fileName = file.getName();
            bugIdList.add(fileName.split("-")[1].split("[.]")[0]);
        }
        return bugIdList;
    }

    private class MAR{
        private String projectId;
        private Map<String, BigDecimal> bugIdScoreMap;

        public MAR(String projectId){
            this.projectId = projectId;
            this.bugIdScoreMap = new HashMap<>();
        }

        public void put(String bugId, BigDecimal score){
            this.bugIdScoreMap.put(bugId, score);
        }

        private static final String HEAD = "projectid,bugid,MAR\r\n";

        public void output(File file)throws Exception{
            FileUtils.writeStringToFile(file, HEAD, "utf-8", false);
            StringBuilder data = new StringBuilder();
            final BigDecimal[] sumScore = {BigDecimal.ZERO};
            this.bugIdScoreMap.forEach((bugid, score) -> {
                data.append(projectId).append(",").append(bugid).append(",").append(score.toPlainString()).append("\r\n");
                sumScore[0] = sumScore[0].add(score);
            });
            String averageScore = sumScore[0].divide(new BigDecimal(this.bugIdScoreMap.size()), 8, RoundingMode.HALF_UP).toPlainString();
            data.append("Average,").append(averageScore);
            FileUtils.writeStringToFile(file, data.toString(), "utf-8", true);
        }
    }
}
