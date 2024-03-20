package com.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzy
 * @date 2021-04-04 20:09
 */
public class BuggyLine {
    private String line;
    private String clz;
    private String lineNumber;
    private String other;

    public BuggyLine(String line){
        this.line = line;
        String[] tmpArr = line.split("#", -1);
        this.clz = tmpArr[0].replace(".java", "").replace("/", ".");
        this.lineNumber = tmpArr[1];
        this.other = tmpArr[2];
    }

    public String getLine() {
        return line;
    }

    public String getClz() {
        return clz;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public String getOther() {
        return other;
    }

    public String getElement(){
        return this.clz + "#" + this.lineNumber;
    }

    public static List<BuggyLine> getBuggyLineList(List<String> lines){
        List<BuggyLine> beanList = new ArrayList<>(lines.size());
        lines.forEach(str -> beanList.add(new BuggyLine(str)));
        return beanList;
    }

    public static List<BuggyLine> getBuggyLineList(String projectPath, String project, String bug)throws Exception{
        File file = new File(projectPath + File.separator + "get_buggy_lines_" + project +
                File.separator + project + "-" + bug + ".buggy.lines");
        List<String> list = FileUtils.readLines(file, "utf-8");
        return getBuggyLineList(list);
    }

    public static List<String> getAllElements(List<BuggyLine> beanList){
        List<String> list = new ArrayList<>();
        beanList.forEach(bean -> list.add(bean.getElement()));
        return list;
    }

    public static List<String> getAllElements(String projectPath, String project, String bug)throws Exception{
        List<BuggyLine> list = getBuggyLineList(projectPath, project, bug);
        return getAllElements(list);
    }

    public static Map<String, Integer> getTotalAndContained(List<String> buggyLineList, List<String> elements){
        Map<String, Integer> retMap = new HashMap<>(2);
        int totalLines = 0;
        int contained = 0;
        for (String buggyLine : buggyLineList) {
            for (String element : elements) {
                if (buggyLine.equals(element)) {
                    contained = 1;
                    totalLines++;
                }
            }
        }
        retMap.put("TOTAL", totalLines);
        retMap.put("CONTAINED", contained);
        return retMap;
    }
}
