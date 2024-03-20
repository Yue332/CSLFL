package com.finalmodule.cost_slfl;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import com.utils.SuspValueBean;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2024/3/17
 **/
public class CostCompute extends FinalBean implements IFinalProcessModule {

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectBasePath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);

        String projectId = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        List<String> bugIdList = Arrays.stream(config.getBugIdArr()).collect(Collectors.toList());


        // /home/yy/newMatrix/[projectId]/[projectId]_[bugId]_suspValue_change_[number].csv
        File changeSuspValuePath = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", projectId));
        if (!changeSuspValuePath.exists()) {
            throw new Exception(String.format("δ�ҵ�Ŀ¼%s", changeSuspValuePath.getAbsolutePath()));
        }
        List<File> pathList = Arrays.stream(Objects.requireNonNull(new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", projectId))
                .listFiles(file -> file.isFile() && file.getName().endsWith(".csv") && file.getName().startsWith(projectId)))).collect(Collectors.toList());
        for (String bugId : bugIdList) {
            System.out.println(String.format("[INFO] ��ʼ����%s", bugId));
            List<File> changeSuspValueFileList = pathList.stream().filter(path -> path.isFile() && path.getName().startsWith(projectId + "_" + bugId + "_suspValue")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(changeSuspValueFileList)) {
                System.out.println(String.format("[INFO] Ŀ¼%s�в�����suspValue�ļ���������bug", String.join(File.separator, changeSuspValuePath.getAbsolutePath(), projectId + "_" + bugId)));
                continue;
            }
            //buggyLine
            File buggyLineFile = new File(String.join(File.separator, projectBasePath, "get_buggy_lines_" + projectId, projectId + "-" + bugId + ".buggy.lines"));
            if (!buggyLineFile.exists()) {
                throw new Exception(String.format("buggyLine�ļ�%s�����ڣ�", buggyLineFile.getAbsolutePath()));
            }
            List<String> tmpList = FileUtils.readLines(buggyLineFile, "utf-8");
            List<BuggyLine> buggyLineBeanList = BuggyLine.getBuggyLineList(tmpList);

            List<Pair<File, Integer>> changeList = findBuggyLineByChangeSuspValue(changeSuspValueFileList, buggyLineBeanList);
            Pair<File, Integer> old = findBuggyLineByOldSuspValue(projectId, bugId, buggyLineBeanList);

            File outputFile = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", "change", "cost", "cost" + projectId + "_" + bugId));

            List<Integer> data = new ArrayList<>();
            for (Pair<File, Integer> pair : changeList) {
                data.add(Math.abs(pair.getRight() - old.getRight()));
            }
            FileUtils.writeStringToFile(outputFile, StringUtils.join(data, ","), "utf-8", false);
        }
    }

    @SneakyThrows
    private static List<Pair<File, Integer>> findBuggyLineByChangeSuspValue(List<File> changeSuspValueFileList, List<BuggyLine> buggyLineList) {
        List<Pair<File, Integer>> resultList = new ArrayList<>();
        for (File changeSuspValueFile : changeSuspValueFileList) {
            List<String> changeSuspValueList = FileUtils.readLines(changeSuspValueFile, "utf-8");
            changeSuspValueList.remove(0);
            //������ֵ��������
            List<SuspValueBean> suspValueBeanList = SuspValueBean.getBeanList(changeSuspValueList, 0).stream().sorted().collect(Collectors.toList());
            boolean isMatch = false;
            for (int i = 0; i < suspValueBeanList.size(); i++) {
                SuspValueBean suspValueBean = suspValueBeanList.get(i);
                if (buggyLineList.stream().anyMatch(buggyLine -> buggyLine.getClz().equals(suspValueBean.getClz()) && buggyLine.getLineNumber().equals(String.valueOf(suspValueBean.getLineNumber())))) {
                    resultList.add(Pair.of(changeSuspValueFile, i));
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                //û�ҵ� ����
                throw new RuntimeException(String.format("�ļ�%s���Ҳ�����buggyLine��Ӧ����䣡", changeSuspValueFile.getAbsolutePath()));
            }
        }
        return resultList;
    }

    @SneakyThrows
    private Pair<File, Integer> findBuggyLineByOldSuspValue(String projectId, String bugId, List<BuggyLine> buggyLineList) {
        File oldSuspValueFile = new File(String.join(File.separator, config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY),
                projectId + "_" + bugId, "gzoltar_output", projectId, bugId, projectId + "-" + bugId + "-suspValue.csv"));
        if (!oldSuspValueFile.exists()) {
            throw new RuntimeException(String.format("�ļ�%s������", oldSuspValueFile.getAbsolutePath()));
        }
        List<String> oldSuspValueStrList = FileUtils.readLines(oldSuspValueFile, "utf-8");
        String head = oldSuspValueStrList.remove(0);
        List<String> headList = Arrays.stream(head.split(",")).collect(Collectors.toList())
                .stream().map(String::trim).collect(Collectors.toList());
        headList.remove(0);
        int funcIndex = headList.indexOf("Ochiai");
        if (funcIndex == -1) {
            throw new RuntimeException(String.format("�ļ�%s��û��Ochiai��Ӧ�Ŀ���ֵ", oldSuspValueFile.getAbsolutePath()));
        }

        List<SuspValueBean> suspValueBeanList = SuspValueBean.getBeanList(oldSuspValueStrList, funcIndex).stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < suspValueBeanList.size(); i++) {
            SuspValueBean suspValueBean = suspValueBeanList.get(i);
            if (buggyLineList.stream().anyMatch(buggyLine -> buggyLine.getClz().equals(suspValueBean.getClz()) && buggyLine.getLineNumber().equals(String.valueOf(suspValueBean.getLineNumber())))) {
                return Pair.of(oldSuspValueFile, i);
            }
        }
        //û�ҵ� ����
        throw new RuntimeException(String.format("�ļ�%s���Ҳ�����buggyLine��Ӧ����䣡", oldSuspValueFile.getAbsolutePath()));
    }

    @SneakyThrows
    public static void main(String[] args) {
        List<File> changeSuspValueFileList = Collections.singletonList(new File("C:\\Users\\zhangziyi\\Desktop\\Chart_2_suspValue_change_206.csv"));
        List<String> tmpList = FileUtils.readLines(new File("C:\\Users\\zhangziyi\\Desktop\\Chart-2.buggy.lines"), "utf-8");
        List<BuggyLine> buggyLineBeanList = BuggyLine.getBuggyLineList(tmpList);
        findBuggyLineByChangeSuspValue(changeSuspValueFileList, buggyLineBeanList);
    }
}
