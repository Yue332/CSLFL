package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Ochiai implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
        for (int i = 0; i < matrix.length; i++) {
            if(0 == (matrix[i][1] + matrix[i][0]) || 0 == (matrix[i][1] + matrix[i][3])){
                suspValue.add(i, new BigDecimal("0"));
            }else{
                BigDecimal temp = new BigDecimal(String.valueOf(matrix[i][1]))
                .divide(new BigDecimal(
                		String.valueOf(
                				Math.sqrt((matrix[i][1] + matrix[i][0])*( matrix[i][1] + matrix[i][3])))),
                		scale, roundingMode);
                suspValue.add(i,temp);
            }
        }
        return suspValue;
	}

}
