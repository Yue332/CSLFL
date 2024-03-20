package com.utils.topn;

import com.utils.csv2bean.Csv2BeanUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2022/9/16
 **/
public class TopN<T, E> {
    private static final String HEAD = "projectid-bugid,function,Top-N,contained\r\n";
    public static final String BUGGY_METHOD_FILE = "/home/yy/BuggyMethod/@:PROJECTID@.@:BUGID@.buggy.methods";
    private static final String BUGGY_LINE_PATH = "";//TODO:
    public static final String FUNC_PACKAGE = "com.utils.cal.func.";

    private String projectId;
    private List<String> bugIdList;
    private String simpleFuncName;
    private int top;
    private Class<T> buggyClz;
    private Class<E> dataClz;

    private Map<String, List<E>> dataMap;

    private Map<String, List<T>> buggyMap;

    private Field buggyClzField = null;

    /**
     * buggy�з����������ֶ�
     */
    private Field buggyDimField = null;

    private Field dataClzField = null;

    private Field dataDimField = null;

    private Field sortField = null;

    private String dataFilePath;

    private String dimType = null;

    public TopN(String projectId, List<String> bugIdList, String simpleFuncName, Class<T> buggyClz, Class<E> dataClz,
                String dataFilePath, int top) {
        this.projectId = projectId;
        this.bugIdList = bugIdList;
        this.simpleFuncName = simpleFuncName;
        this.buggyClz = buggyClz;
        this.dataClz = dataClz;
        this.dataFilePath = dataFilePath;
        this.top = top;
    }

    public void onPrepare(){
        System.out.println("[INFO] ��ʼ��TopN������...");
        getBuggyDimension();
        getDataDimension();
        //TODO: У��data�е�dimField��buggy�е�dimField�Ƿ�һֱ �� �Ƿ�Ϊ��� �����Ƿ�Ϊ����

        this.dataMap = new HashMap<>(this.bugIdList.size());
        this.buggyMap = new HashMap<>(this.bugIdList.size());

        System.out.println("[INFO] ��ʼ����buggy�б�");
        buildBuggyList();
        System.out.println("[INFO] ����buggy�б����");

        System.out.println("[INFO] ��ʼ���������б�");
        buildDataList();
        System.out.println("[INFO] ���������б����");
        System.out.printf("[INFO] ��ʼ��TopN��������ɣ���Ŀ%s������bug%s����ʽ%s��top%s %n", projectId, bugIdList.toString(), simpleFuncName, top);
    }

    public void calculate(File outputFile)throws Exception{
        long startTime = System.currentTimeMillis();
        System.out.println("[INFO] ��ʼ����topN");
        FileUtils.writeStringToFile(outputFile, HEAD, "utf-8", false);
        StringBuilder data = new StringBuilder();
        for (String bugId : bugIdList) {
            List<E> dataList = dataMap.get(bugId);
            dataList.sort((o1, o2) -> {
                try {
                    return ((BigDecimal) this.sortField.get(o2)).compareTo(((BigDecimal) this.sortField.get(o1)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
            List<T> buggyList = buggyMap.get(bugId);
            int realTop = this.top == -1 ? dataList.size() : Math.min(dataList.size(), top);
            List<E> topNList = dataList.subList(0, realTop);
            AtomicInteger contained = new AtomicInteger(0);
            buggyList.forEach(buggyBean -> {
                try {
                    String buggyClz = ((String) this.buggyClzField.get(buggyBean));
                    String buggyDim = ((String) this.buggyDimField.get(buggyBean));
                    topNList.forEach(dataBean -> {
                        try {
                            String dataClz = ((String) this.dataClzField.get(dataBean));
                            String dataDim = ((String) this.dataDimField.get(dataBean));
                            if(buggyClz.equals(dataClz) && buggyDim.equals(dataDim)){
                                contained.set(1);
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
            data.append(projectId).append("-").append(bugId).append(",").append(simpleFuncName).append(",")
                    .append(this.top).append(",").append(contained.get()).append("\r\n");
        }
        FileUtils.writeStringToFile(outputFile, data.toString(), "utf-8", true);
        long endTime = System.currentTimeMillis();
        System.out.printf("[INFO] topN������ɣ���ʱ[%s]ms��������ļ�%s", (endTime - startTime), outputFile.getAbsolutePath());
    }



    private void getBuggyDimension(){
        System.out.println("[INFO] Ѱ��Buggy��ά�ȣ���� �� ������");
        Field[] fields = buggyClz.getDeclaredFields();
        if(ArrayUtils.isEmpty(fields)){
            throw new RuntimeException(String.format("��%s���κ��ֶΣ�������ֶΣ�", buggyClz.getName()));
        }
        for (Field field : fields) {
            TopNDimension anno = field.getAnnotation(TopNDimension.class);
            if(ObjectUtils.isNotEmpty(anno)){
                if (TOPNDimensionEnum.TOPN_DIMENSION_CLASS.equals(anno.value())) {
                    this.buggyClzField = field;
                    this.buggyClzField.setAccessible(true);
                }else{
                    this.buggyDimField = field;
                    this.buggyDimField.setAccessible(true);
                    this.dimType = anno.value().getCode();
                }
            }
        }
        System.out.printf("[INFO] Buggyά��Ѱ����ɣ�Ϊ%s%n", this.dimType);
//        Optional.ofNullable(this.buggyClzField).orElseThrow(() -> new RuntimeException("��%s��Ҫ"));
    }

    private void getDataDimension(){
        System.out.println("[INFO] Ѱ�����ݶ���");
        Field[] fields = dataClz.getDeclaredFields();
        if(ArrayUtils.isEmpty(fields)){
            throw new RuntimeException(String.format("��%s���κ��ֶΣ�������ֶΣ�", dataClz.getName()));
        }
        for (Field field : fields) {
            TopNDimension anno = field.getAnnotation(TopNDimension.class);
            if(ObjectUtils.isNotEmpty(anno)){
                if(anno.sortField()){
                    this.sortField = field;
                    this.sortField.setAccessible(true);
                    continue;
                }
                if (TOPNDimensionEnum.TOPN_DIMENSION_CLASS.equals(anno.value())) {
                    this.dataClzField = field;
                    this.dataClzField.setAccessible(true);
                }else{
                    this.dataDimField = field;
                    this.dataDimField.setAccessible(true);
                }
            }
        }
        System.out.println("[INFO] Ѱ�����ݶ������");
    }

    private void buildDataList(){
        this.bugIdList.forEach(bugid -> {
            File file = new File(dataFilePath.replace("@:PROJECTID@", projectId)
                    .replace("@:BUGID@", bugid)
                    .replace("@:FUNC@", simpleFuncName));
            if (!file.exists()) {
                throw new RuntimeException(String.format("�ļ�%s�����ڣ����飡", file.getAbsolutePath()));
            }
            try {
                dataMap.put(bugid, Csv2BeanUtil.convertCsv2Bean(file, dataClz));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void buildBuggyList(){
        if (TOPNDimensionEnum.TOPN_DIMENSION_METHOD.getCode().equals(this.dimType)) {
            buildBuggyMethodList();
        }else{
            buildBuggyLineList();
        }
    }

    private void buildBuggyMethodList(){
        this.bugIdList.forEach(bugid -> {
            File file = new File(BUGGY_METHOD_FILE.replace("@:PROJECTID@", projectId).replace("@:BUGID@", bugid));
            if (!file.exists()) {
                throw new RuntimeException(String.format("�ļ�%s�����ڣ����飡", file.getAbsolutePath()));
            }
            try {
                buggyMap.put(bugid, Csv2BeanUtil.convertCsv2Bean(file, buggyClz));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void buildBuggyLineList(){
        throw new RuntimeException("�ݲ�֧��buggyline");
    }


    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<String> getBugIdList() {
        return bugIdList;
    }

    public void setBugIdList(List<String> bugIdList) {
        this.bugIdList = bugIdList;
    }

    public String getSimpleFuncName() {
        return simpleFuncName;
    }

    public void setSimpleFuncName(String simpleFuncName) {
        this.simpleFuncName = simpleFuncName;
    }

    public Class<T> getBuggyClz() {
        return buggyClz;
    }

    public void setBuggyClz(Class<T> buggyClz) {
        this.buggyClz = buggyClz;
    }

    public Class<E> getDataClz() {
        return dataClz;
    }

    public void setDataClz(Class<E> dataClz) {
        this.dataClz = dataClz;
    }
}
