package com.finalmodule.cost_slfl;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.utils.ConfigUtils;
import com.utils.Utils;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import soot.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2024/2/19
 **/
public class PassTestReduction extends FinalBean implements IFinalProcessModule {

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String[] bugIdArr = super.config.getBugIdArr();
        String projectId = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);

        for (String bugId : bugIdArr) {
            List<String> matrixList = Utils.getMatrixList(projectPath, projectId, bugId);
            //��ʧ�ܵĲ�������
            List<String> failMatixList = matrixList.stream().filter(row -> row.endsWith("-")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(failMatixList)) {
                continue;
            }
            List<String> spectraList = Utils.getSpectraList(projectPath, projectId, bugId);
            //���Ӧ�ķ��������к�
            List<Pair<String, List<Integer>>> methodDeclareList = findMethodDeclare(spectraList, projectPath, projectId, bugId);

            //��Ҫɾ����matrix���±��б�
            List<Integer> removeIndexList = new ArrayList<>();

            List<List<String>> failMartixFlagList = failMatixList.stream().map(row -> Arrays.stream(row.replace("-", "").split(" ")).collect(Collectors.toList())).collect(Collectors.toList());
            for (int i = 0; i < matrixList.size(); i++) {
                String matrixRow = matrixList.get(i);
                if (matrixRow.endsWith("-")) {
                    continue;
                }

                List<String> successMartixFlag = Arrays.stream(matrixRow.replace("+", "").split(" ")).collect(Collectors.toList());
                //�ɹ������������ǵ��������
                int count = successMartixFlag.size();
                //δ��ʧ�ܲ����������ǵ��������
                int notMatchCoverCount = 0;
                for (int j = 0; j < successMartixFlag.size(); j++) {
                    String flag = successMartixFlag.get(j);
                    //��Ҫ��������� 1.û�ߵģ�flag = 0����2.����soot�жϵĹ������
                    if (isSkipStatement(j, flag, projectPath, projectId, bugId, spectraList, methodDeclareList)) {
                        count--;
                        continue;
                    }
                    int index = j;
                    if (failMartixFlagList.stream().noneMatch(failMartixFlag -> "1".equals(failMartixFlag.get(index)))) {
                        notMatchCoverCount++;
                    }
                }
                if (count == notMatchCoverCount) {
                    removeIndexList.add(i);
                }
            }

            if (CollectionUtils.isNotEmpty(removeIndexList)) {

                File removeIndexFile = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", projectId, "remove" + projectId + ".txt"));
                FileUtils.writeStringToFile(removeIndexFile, String.join(",", projectId + "-" + bugId, Integer.toString(removeIndexList.size()), removeIndexList.toString()) + "\r\n", StandardCharsets.UTF_8, true);

                File outputFile = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", projectId, "removedCase", projectId + "_" + bugId + "_matrix_new"));
                outputFile.delete();

                removeIndexList.forEach(matrixList::remove);
                matrixList.forEach(row -> {
                    try {
                        FileUtils.writeStringToFile(outputFile, row + "\r\n", StandardCharsets.UTF_8, true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }



            G.reset();
        }
    }


    /**
     * ͨ��java parser�ҷ�������������
     */
    @SneakyThrows
    public static List<Pair<String, List<Integer>>> findMethodDeclare(List<String> spectraList, String projectPath, String projectId, String bugId) {
        String basePath = Utils.getStringWithFileSeparator(projectPath, projectId + "_" + bugId,
                Utils.getSourcePathByProjectID(projectId));
        List<File> javaFileList = getAllJavaFile(basePath);
        if (CollectionUtils.isEmpty(javaFileList)) {
            throw new RuntimeException(String.format("Ŀ¼%s��δ�ҵ�java�ļ�", basePath));
        }

        //����spectra���õ�spectra�ļ��е���ķ����������  return �����������������к�
        return spectraList.stream().map(row -> {
            String[] rowArray = row.split("#");
            return rowArray[0];
        }).distinct().map(className -> {
            String realClassName = className;
            //�ڲ���
            if(className.contains("$")) {
                realClassName = className.substring(0, className.indexOf("$"));
            }

            String classFileName = realClassName.replace(".", File.separator) + ".java";

            File javaFile = javaFileList.stream().filter(file -> file.getAbsolutePath().contains(classFileName))
                    .findAny().orElseThrow(() -> new RuntimeException(String.format("δ�ҵ��ļ�%s", classFileName)));

//            File javaFile = new File(Utils.getStringWithFileSeparator(projectPath, projectId + "_" + bugId,
//                    Utils.getSourcePathByProjectID(projectId)), realClassName.replace(".", File.separator) + ".java");
//            if (!javaFile.exists()) {
//                javaFile = new File(Utils.getStringWithFileSeparator(projectPath, projectId + "_" + bugId,
//                    Utils.getSourcePathByProjectID(projectId), "main", "java", realClassName.replace(".", File.separator) + ".java"));
//            }
//            if (!javaFile.exists()) {
//                throw new RuntimeException(String.format("���ļ�%s������", javaFile.getAbsolutePath()));
//            }
            try {
                List<Integer> resultList = new ArrayList<>();
                CompilationUnit cu = StaticJavaParser.parse(javaFile);
                cu.accept(new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(ConstructorDeclaration n, Object arg) {
                        n.getBegin().ifPresent(position -> resultList.add(position.line));
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        n.getBegin().ifPresent(position -> resultList.add(position.line));
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(FieldDeclaration n, Object arg) {
                        //final���ε��ֶ�
                        if (n.getModifiers().stream().anyMatch(row -> row.getKeyword().equals(Modifier.Keyword.FINAL))) {
                            n.getRange().ifPresent(range -> {
                                resultList.add(range.begin.line);
                                if (range.getLineCount() > 1) {
                                    for (int addLine = range.begin.line + 1; addLine <= range.end.line; addLine ++) {
                                        resultList.add(addLine);
                                    }
                                }
                            });
                        }
                        super.visit(n, arg);
                    }
                }, null);
                return Pair.of(className, resultList);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

    }

    public static boolean isSkipStatement(int index, String flag, String projectPath, String projectId, String bugId,
                                          List<String> spectraList, List<Pair<String, List<Integer>>> methodDeclareList) throws Exception {
        if ("0".equals(flag)) {
            return true;
        }
        String spectraRow = spectraList.get(index);
        String[] spectraArray = spectraRow.split("#");
        if (methodDeclareList.stream().anyMatch(row -> row.getLeft().equals(spectraArray[0]) && row.getRight().contains(Integer.parseInt(spectraArray[1])))) {
            return true;
        }
        return false;
    }


    public static List<File> getAllJavaFile(String basePath) {
        File base = new File(basePath);
        List<File> resultList = new ArrayList<>();
        putJavaFileToList(base, resultList);
        return resultList;
    }

    private static void putJavaFileToList(File path, List<File> resultList) {
        if (path.isFile() && !path.getName().endsWith(".java")) {
            return;
        }
        File[] files = path.listFiles(file -> file.isFile() && file.getName().endsWith(".java"));
        if (ArrayUtils.isNotEmpty(files)) {
            resultList.addAll(Arrays.stream(files).collect(Collectors.toList()));
        }
        File[] paths = path.listFiles(File::isDirectory);
        if (ArrayUtils.isNotEmpty(paths)) {
            Arrays.stream(paths).forEach(p -> putJavaFileToList(p, resultList));
        }
    }
}
