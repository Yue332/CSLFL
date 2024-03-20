package com.finalmodule.cost_slfl;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description: 将PassTestReduction生成的新matrix文件处理后生成修改后的matrix文件
 * Author: zhangziyi
 * Date: 2024/2/28
 **/
public class GenerateChangeMatrixFile extends FinalBean implements IFinalProcessModule {

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        File newMatrixPath = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix"));
        String projectBasePath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        String projectId = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        List<String> bugIdList = Arrays.asList(config.getBugIdArr());

        if (!newMatrixPath.exists()) {
            throw new Exception(String.format("目录%s不存在，请检查！", newMatrixPath.getAbsolutePath()));
        }
        List<File> matrixNewList = getMatrixNewFileList(newMatrixPath, projectId);
//        List<File> matrixNewList = Arrays.stream(Objects.requireNonNull(newMatrixPath.listFiles((dir, name) -> name.endsWith("_matrix_new")))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(matrixNewList)) {
            throw new Exception(String.format("目录%s下未找到文件名末尾为_matrix_new的文件", newMatrixPath.getAbsolutePath()));
        }
        //按照规则生成新的matrix文件
        matrixNewList.forEach(file -> {
            String[] fileNameArray = file.getName().split("_");
            String bugId = fileNameArray[1];
            dealOneFile(file, null, projectId, bugId);
        });

        //

        File removeIndexFile = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", projectId, "remove" + projectId + ".txt"));
        List<String> removeBugList = FileUtils.readLines(removeIndexFile, "utf-8").stream().map(line -> {
            //projectId_bugId,blabla
            String[] split = line.split(",")[0].split("-");
            return split[1];
        }).collect(Collectors.toList());


        List<Pair<String, File>> oldMatrixFileList = bugIdList.stream().filter(bugId -> !removeBugList.contains(bugId)).map(bugId -> {
            File oldMatrixFile = new File(String.join(File.separator, projectBasePath, projectId + "_" + bugId, "gzoltar_output", projectId, bugId, "matrix"));
            if (!oldMatrixFile.exists()) {
                throw new RuntimeException(String.format("文件%s不存在！", oldMatrixFile.getAbsolutePath()));
            }
            return Pair.of(bugId, oldMatrixFile);
        }).collect(Collectors.toList());
        oldMatrixFileList.forEach(pair -> dealOneFile(pair.getValue(), "old", projectId, pair.getKey()));
    }

    private List<File> getMatrixNewFileList(File basePath, String projectId) {
        File removedCasePath = new File(String.join(File.separator, basePath.getAbsolutePath(),projectId, "removedCase"));
        if (!removedCasePath.exists()) {
            throw new RuntimeException(String.format("目录%s不存在！", removedCasePath.getAbsolutePath()));
        }

        return Arrays.asList(Objects.requireNonNull(removedCasePath.listFiles(file -> file.getName().endsWith("_matrix_new"))));

//        Arrays.stream(Objects.requireNonNull(basePath.listFiles(File::isDirectory))).forEach(dir -> {
//            File file = new File(String.join(File.separator, dir.getAbsolutePath(), "removedCase"));
//            if (!file.exists()) {
//                throw new RuntimeException(String.format("目录%s不存在！", file.getAbsolutePath()));
//            }
//            resultList.addAll(Arrays.stream(Objects.requireNonNull(file.listFiles((f, n) -> n.endsWith("_matrix_new")))).collect(Collectors.toList()));
//        });
//        return resultList;
    }


    @SneakyThrows
    private void dealOneFile(File file, String type, String projectId, String bugId) {
        System.out.printf("[INFO] 开始处理文件%s%n", file.getAbsolutePath());
        List<String> martixList = FileUtils.readLines(file, StandardCharsets.UTF_8);
        List<Integer> failTestIndexList = martixList.stream().filter(row -> row.endsWith("-")).map(martixList::indexOf).collect(Collectors.toList());
        failTestIndexList.forEach(i -> martixList.set(i, martixList.get(i).replace("-", "+")));

        for (int i = 0; i < martixList.size(); i++) {
            if (failTestIndexList.contains(i)) {
                continue;
            }
            List<String> replaceMatrixList = new ArrayList<>(martixList);
            replaceMatrixList.set(i, replaceMatrixList.get(i).replace("+", "-"));
            File outputFile = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", "change", projectId + "_" + bugId, "matrix_change_" + (StringUtils.isBlank(type) ? "" : type + "_") + (i + 1)));
            outputFile.delete();
            FileUtils.writeStringToFile(outputFile, String.join("\r\n", replaceMatrixList), StandardCharsets.UTF_8, false);
            System.out.printf("[INFO] 已生成文件%s，将第%s个成功的测试用例替换为失败%n", outputFile.getAbsolutePath(), (i + 1));
        }
        System.out.printf("[INFO] 文件%s生成修改matrix文件完成", file.getAbsolutePath());
    }
}
