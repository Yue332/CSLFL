package com.module;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;

public class Gzoltar extends Bean implements IProcessModule{
	public Gzoltar(Configer config) {
		super(config);
	}
	public static String COMMAND1 = "cd @:GZOLTAR_HOME@/gzoltar/";
	public static String COMMAND2 = "./job.sh --project @:PROJECT_ID@ --bug @:BUG_ID@ --output_dir @:OUTPUT_DIR@ --tool @:TOOL@";
	
	private String outputDir;
	private String tool;
	
	public void executeGzoltarJob(Runtime runTime)throws Exception{
		String[] msg = Utils.executeCommandLine(runTime, COMMAND1.replaceAll("@:GZOLTAR_HOME@", config.getConfig(ConfigUtils.PRO_FAULT_LOCALIZATION_DATA_HOME_KEY)),
				COMMAND2.replaceAll("@:PROJECT_ID@", projectId)
//				.replaceAll("@:PROJECT_PATH@", projectPath)
				.replaceAll("@:BUG_ID@", bugId)
				.replaceAll("@:OUTPUT_DIR@", this.outputDir)
				.replaceAll("@:TOOL@", this.tool));
		if(!"0".equals(msg[0])) {
			//gzoltar 执行最后压缩命令时一定会报错，这里只是提示出来
			System.out.println("[WARNING] gzoltar job.sh execute fail!");
//			throw new Exception("[ERROR] gzoltar job.sh execute fail!");
		}
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		executeGzoltarJob(runTime);
	}

	@Override
	public void onPrepare(Runtime runTime) {
		this.tool = config.getConfig(ConfigUtils.PRO_TOOL_KEY);
		this.outputDir = projectPath + "gzoltar_output/";
	}
	
	
}
