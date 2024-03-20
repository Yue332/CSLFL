package com.module;

import com.utils.Bean;
import com.utils.Configer;
import com.utils.Utils;
import com.utils.cal.IAnalysisFunc;
import com.utils.cal.TFuncRegister;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;


public class CalculateValueAndGenCSV extends Bean implements IProcessModule {
	protected String spectrumPath;
	protected String sprctraPath;

	@Setter
	private String csvName;

	public CalculateValueAndGenCSV(Configer config) {
		super(config);
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		File path = new File(spectrumPath);
		if(!path.exists()) {
			throw new Exception("[ERROR] 未找到【"+spectrumPath+"】，请将gzoltar输出的频谱及语句(spectra文件和matrix)复制到该目录下");
		}
		File[] matrixList = path.listFiles((dir, name) -> name.startsWith("matrix"));
		File spectra = new File(sprctraPath);
		for(File matrix : matrixList) {
			String type = Utils.getType(matrix.getName());
			readDatasAndCalculate(spectra, matrix, type);
		}
	}



	public void readDatasAndCalculate(File spectra, File matrix, String type) throws Exception {
		System.out.println("开始读取频谱、语句文件并计算["+projectId+"]bugID["+bugId+"]的怀疑值");
        List<String> codeLines = FileUtils.readLines(spectra, "UTF-8");
        List<String> testExecResults = FileUtils.readLines(matrix, "UTF-8");
        System.out.println("读取完成");
        System.out.println("将读取信息写入二维数组");
        //矩阵信息写入二维数组
        String[][] testExecResults2 = new String [testExecResults.size()][codeLines.size()+1];
        for (int i = 0; i < testExecResults.size(); i++) {
            String[] temps = testExecResults.get(i).split(" ");
            for (int j = 0; j < temps.length; j++) {
                testExecResults2[i][j] = temps[j];
            }
        }
//        n11:覆盖语句   + 通过
//        n10:覆盖语句   + 失败
//        n01:未覆盖语句 + 通过
//        n00:未覆盖语句 + 失败
	    double[][] testExecInfo = new double[codeLines.size()][4];
	    for (int i = 0; i < codeLines.size(); i++) {
	        double n11 = 0, n10 = 0, n01 = 0, n00 = 0;
			for (String[] strings : testExecResults2) {
				double temp = Integer.parseInt(strings[i]);
				String flag = strings[codeLines.size()];
				if (1 == temp && flag.equals("+")) {
					n11++;
				} else if (1 == temp && flag.equals("-")) {
					n10++;
				} else if (0 == temp && flag.equals("+")) {
					n01++;
				} else if (0 == temp && flag.equals("-")) {
					n00++;
				}
			}
	        testExecInfo[i][0]=n11;
	        testExecInfo[i][1]=n10;
	        testExecInfo[i][2]=n01;
	        testExecInfo[i][3]=n00;
	    }
	    System.out.println("[INFO] 写入完成，开始计算怀疑值");
	    calculateValue(codeLines, testExecInfo, type);
	}

	//计算怀疑值并生成csv文件
	public void calculateValue(List<String> codeLines, double[][] ints, String type) throws Exception {
		ArrayList<List<BigDecimal>> suspValues = new ArrayList<>();
		LinkedHashMap<String, IAnalysisFunc> funcMap = TFuncRegister.getRegistClass(config);

        String[] titleLine = new String[funcMap.size() + 1];
        titleLine[0] = "element";

        int idx = 1;
        Iterator<Entry<String, IAnalysisFunc>> entries = funcMap.entrySet().iterator();
        StringBuilder funcBuilder = new StringBuilder();
        while (entries.hasNext()) {
			Entry<String, IAnalysisFunc> entry = entries.next();
			String clzName = entry.getKey();
			IAnalysisFunc func = entry.getValue();

			titleLine[idx] = clzName;
			funcBuilder.append(clzName).append(",");
			idx ++;
			try {
				suspValues.add(func.onProcess(ints));
			}catch (Exception e) {
				throw new Exception("执行方法["+clzName+"]异常！" + e.getMessage());
			}
		}
        System.out.println("[INFO] 使用公式：" + funcBuilder.substring(0, funcBuilder.length() - 1).toString());
        System.out.println("[INFO] 开始写入CSV文件");

        String csvName = this.csvName + ("".equals(type) ? "" : "_" + type) + ".csv";
        File dest = new File(csvName);
        FileUtils.writeStringToFile(dest, Arrays.toString(titleLine).replace("[", "").replace("]", "") + "\r\n", false);
        for (int i = 0; i < codeLines.size(); i++) {
            for (int j = 0; j < suspValues.size(); j++) {
                BigDecimal result = suspValues.get(j).get(i);
                if(j == 0){
                	String splitFlag = suspValues.size() == 1 ? "\r\n" : ",";
                    FileUtils.writeStringToFile(dest, codeLines.get(i) + ",", true);
                    FileUtils.writeStringToFile(dest, result.toPlainString() + splitFlag, true);
                }else if (j == (suspValues.size()-1)){
                    FileUtils.writeStringToFile(dest, result.toPlainString() + "\r\n", true);
                }else{
                    FileUtils.writeStringToFile(dest, result.toPlainString() + ",", true);
                }
            }
        }
        System.out.println("[INFO] CSV文件生成完成！");
	}

	@Override
	public void onPrepare(Runtime runTime) {
		spectrumPath = super.projectPath + File.separator + "gzoltar_output" + File.separator + super.projectId + File.separator + super.bugId;
		sprctraPath = spectrumPath + File.separator + "spectra";
		if (StringUtils.isBlank(csvName)) {
			csvName = spectrumPath + File.separator + super.projectId + "-" + super.bugId + "-" + "suspValue";
		}
	}
}
