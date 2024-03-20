package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Ample implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
		List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
        
        for (int i = 0; i < matrix.length; i++) {
            //实际情况下测试用例不可能全通过/失败
        	
        	double temp = matrix[i][1] / (matrix[i][1]+matrix[i][3]) - matrix[i][0] / (matrix[i][0]+matrix[i][2]);
        	            	
            suspValue.add(i,new BigDecimal(temp));
        }
        return suspValue;
	}

}
