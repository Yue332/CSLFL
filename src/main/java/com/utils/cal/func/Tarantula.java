package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Tarantula implements IAnalysisFunc {
	
	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
		List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
		double a = 0.0, b = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            if(0 == (matrix[i][1] + matrix[i][3]) || 0 == (matrix[i][0] + matrix[i][2]) || 0 == (matrix[i][1] + matrix[i][0])){
            	suspValue.add(i, new BigDecimal("0"));
            }else{
            	a = matrix[i][1] / (matrix[i][1] + matrix[i][3]);
                b = matrix[i][0] / (matrix[i][0] + matrix[i][2]);
                double temp = a /(a+b);  
                //list.add(i, new BigDecimal(temp));
                suspValue.add(i,new BigDecimal(temp));
            }
        }
        return suspValue;
	}

}
