package com.utils.csv2bean;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: zhangziyi
 * Date: 2022/9/17
 **/
public class Csv2BeanUtil {

    public static <T> List<T> convertCsv2Bean(File csvFile, Class<T> beanClass)throws Exception{
        List<String> list = org.apache.commons.io.FileUtils.readLines(csvFile, "utf-8");
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>(0);
        }
        List<T> retList = new ArrayList<>(list.size() - 1);

        String[] head = list.get(0).split(",", -1);
        for (int i = 0; i < head.length; i++) {
            head[i] = head[i].trim();
        }
        list.remove(0);
        Constructor<T> constructor = beanClass.getConstructor( null);
        Field[] fields = beanClass.getDeclaredFields();
        for (String line : list) {
            String[] lineArr = line.split(",", -1);
            T obj = (T) constructor.newInstance( null);
            for (Field field : fields) {
                field.setAccessible(true);
                Csv2BeanAnno anno = field.getAnnotation(Csv2BeanAnno.class);
                String fieldName = field.getName().trim();
                Method setMethod = beanClass.getDeclaredMethod("set" + upperFirst(fieldName), field.getType());
                Object value;
                if(ObjectUtils.isEmpty(anno)){
                    int idx = ArrayUtils.indexOf(head, fieldName);
                    value = getValue(lineArr[idx], line, field.getType(), null);
                }else{
                    if(anno.ignore()){
                        continue;
                    }
                    value = getValueByAnno(head, lineArr, line, fieldName, field.getType(), anno);
                }
                setMethod.invoke(obj, value);
            }
            retList.add(obj);

        }

        return retList;
    }

    private static Object getValueByAnno(String[] head, String[] lineArr, String line, String fieldName, Class fieldType, Csv2BeanAnno anno)throws Exception{
        if(anno.line()){
            return line;
        }
        Object objValue;
        Class dealClass = anno.dealFunc();
        if(dealClass == Object.class){
            dealClass = null;
        }
        String csvValue = anno.value();
        if(StringUtils.isNotBlank(csvValue)){
            int idx = ArrayUtils.indexOf(head, csvValue);
            objValue = getValue(lineArr[idx], line, fieldType, dealClass);
        }else{
            int idx = ArrayUtils.indexOf(head, fieldName);
            objValue = getValue(lineArr[idx], line, fieldType, dealClass);
        }
        return objValue;
    }

    private static Object getValue(String value, String line, Class fieldType, Class dealClass)throws Exception{
        if(ObjectUtils.isNotEmpty(dealClass)){
            ICsv2BeanDealFunc processor = ((ICsv2BeanDealFunc) dealClass.newInstance());
            return processor.deal(value, line);
        }
        if(fieldType == int.class){
            return StringUtils.isBlank(value) ? 0 : Integer.parseInt(value);
        }else if (fieldType == BigDecimal.class){
            return StringUtils.isBlank(value) ? BigDecimal.ZERO : new BigDecimal(value);
        }else if (fieldType == boolean.class){
            return !StringUtils.isBlank(value) && Boolean.parseBoolean(value);
        }else{
            return value;
        }
    }

    public static String upperFirst(String word){
        if(StringUtils.isBlank(word)){
            return word;
        }
        if(word.length() == 1){
            return word.toUpperCase();
        }
        String first = word.substring(0, 1).toUpperCase();
        return first + word.substring(1);
    }
}
