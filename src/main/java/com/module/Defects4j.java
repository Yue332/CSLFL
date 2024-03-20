package com.module;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;

import java.util.Arrays;

public class Defects4j extends Bean implements IProcessModule{
	public static String D4J_BASE_COMMAND = "@:D4J_HOME@/framework/bin/";
//	public static String D4J_CHECKOUT_COMMAND = "defects4j checkout -p @:PROJECT_ID@ -v @:BUG_ID@b -w @:WORK_DIR@";
	public static String D4J_CHECKOUT_COMMAND = "@:D4J_HOME@/framework/bin/defects4j checkout -p @:PROJECT_ID@ -v @:BUG_ID@b -w @:WORK_DIR@";
	public static String D4J_BUGID_INFO_COMMAND = "defects4j info -p @:PROJECT_ID@ -b @:BUG_ID@";
	
	public Defects4j(Configer config) {
		super(config);
	}
	
	public void checkout(Runtime runTime)throws Exception{

		String[] msg = Utils.executeCommandLine(runTime, D4J_CHECKOUT_COMMAND
				.replaceAll("@:D4J_HOME@", config.getConfig(ConfigUtils.PRO_D4J_HOME_KEY))
				.replaceAll("@:PROJECT_ID@", projectId)
				.replaceAll("@:BUG_ID@", bugId)
				.replaceAll("@:WORK_DIR@", projectPath));
		System.out.println("[DEBUG] " + Arrays.toString(msg));
//		String[] msg = Utils.executeCommandLine4D4j(runTime, D4J_CHECKOUT_COMMAND
//				.replaceAll("@:D4J_HOME@", config.getConfig(ConfigUtils.PRO_D4J_HOME_KEY))
//				.replaceAll("@:PROJECT_ID@", projectId)
//				.replaceAll("@:BUG_ID@", bugId)
//				.replaceAll("@:WORK_DIR@", projectPath));
//		System.out.println("[DEBUG] " + Arrays.toString(msg));
	}
	
	public String info(Runtime runTime)throws Exception{
		String[] msg = Utils.executeCommandLine(runTime, D4J_BUGID_INFO_COMMAND
				.replaceAll("@:PROJECT_ID@", projectId)
				.replaceAll("@:BUG_ID@", bugId));
		if(!"0".equals(msg[0])) {
			throw new Exception("[ERROR] execute defects4j checkout fail!");
		}
		return msg[1];
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		checkout(runTime);
		
	}

	@Override
	public void onPrepare(Runtime runTime) {
		// TODO Auto-generated method stub
		
	}
	
	
}
