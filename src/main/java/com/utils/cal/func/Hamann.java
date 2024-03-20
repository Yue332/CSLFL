package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Hamann implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
        double temp = 0.0, a =0.0, b = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            a = matrix[i][1] + matrix[i][2] - matrix[i][0] - matrix[i][3];
            b = matrix[i][0] + matrix[i][1] + matrix[i][2] + matrix[i][3];
            temp = a / b;
            suspValue.add(i,new BigDecimal(temp));
        }
        return suspValue;
	}

}
