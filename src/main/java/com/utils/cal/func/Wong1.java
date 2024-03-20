package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Wong1 implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
         
        for (int i = 0; i < matrix.length; i++) {
        	double temp = matrix[i][1];
            suspValue.add(i,new BigDecimal(temp));
        }
        return suspValue;
	}

}
