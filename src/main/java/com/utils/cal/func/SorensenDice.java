package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SorensenDice implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
        double temp = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            if(0 == (2*matrix[i][1] + matrix[i][0] + matrix[i][3])){
                suspValue.add(i,new BigDecimal("0"));
            }else{
                temp = 2*matrix[i][1] / (2*matrix[i][1] + matrix[i][0] + matrix[i][3]);
                suspValue.add(i,new BigDecimal(temp));
            }
        }
        return suspValue;
	}

}
