package com.finalmodule.cost_slfl;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.module.CalculateValueAndGenCSV;
import com.utils.ConfigUtils;
import com.utils.Utils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2024/2/28
 **/
public class GenerateChangeMatrixSuspValueCSV extends FinalBean implements IFinalProcessModule {

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectBasePath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        File changeMatrixPath = new File(String.join(File.separator, System.getProperty("user.home"), "newMatrix", "change"));
        if (!changeMatrixPath.exists()) {
            throw new Exception(String.format("未找到目录%s", changeMatrixPath.getAbsolutePath()));
        }
        List<File> pathList = Arrays.stream(Objects.requireNonNull(changeMatrixPath.listFiles(File::isDirectory))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(pathList)) {
            throw new Exception(String.format("目录%s下没有生成的修改后matrix目录", changeMatrixPath.getAbsolutePath()));
        }
        CalculateValueAndGenCSV processService = new CalculateValueAndGenCSV(config);

        pathList.forEach(path -> {
            String[] projectInfoArray = path.getName().split("_");
            String projectId = projectInfoArray[0];
            String bugId = projectInfoArray[1];

            List<File> matrixFileList = Arrays.stream(Objects.requireNonNull(path.listFiles((dir, name) -> name.startsWith("matrix_change_")))).collect(Collectors.toList());
            File spectra = new File(Utils.getStringWithFileSeparator(projectBasePath, projectId + "_" + bugId, "gzoltar_output", projectId, bugId, "spectra"));
            processService.setProjectId(projectId);
            processService.setBugId(bugId);
            processService.setProjectPath(config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bugId + File.separator);
            processService.onPrepare(runTime);
            processService.setCsvName(String.join(File.separator, System.getProperty("user.home"), "newMatrix", projectId, projectId + "_" + bugId + "_suspValue"));
            matrixFileList.forEach(matrix -> {
                String[] split = matrix.getName().split("_");
                String type = "change_" + split[split.length - 1];
                try {
                    processService.readDatasAndCalculate(spectra, matrix, type);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
