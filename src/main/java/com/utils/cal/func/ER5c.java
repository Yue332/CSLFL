package com.utils.cal.func;

import com.utils.cal.IAnalysisFunc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ER5c implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
        for (int i = 0; i < matrix.length; i++) {
            if( matrix[i][1] < (matrix[i][1]+matrix[i][3])){
                suspValue.add(i, new BigDecimal("0"));
            }else if( matrix[i][1] == (matrix[i][1]+matrix[i][3])){
                suspValue.add(i,new BigDecimal("1"));
            }else{
                suspValue.add(i, BigDecimal.ZERO);
            }
        }
        return suspValue;
	}

}
