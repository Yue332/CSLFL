package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Goodman implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
         
        for (int i = 0; i < matrix.length; i++) {
        	double temp = (2*matrix[i][1] - matrix[i][3] - matrix[i][0]) /
                    (2*matrix[i][1] + matrix[i][3] + matrix[i][0]);
            suspValue.add(i,new BigDecimal(temp));
        }
        return suspValue;
	}

}
