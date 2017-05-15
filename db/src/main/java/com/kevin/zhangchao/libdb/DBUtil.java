package com.kevin.zhangchao.libdb;

import com.kevin.zhangchao.libdb.Utilities.TextUtil;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

class DBUtil {

    public static String getTableName(Class<?> clz) {
        if (clz.isAnnotationPresent(Table.class)){
            String name=clz.getAnnotation(Table.class).name();
            if (TextUtil.isValidate(name)){
                return name;
            }else{
                return clz.getSimpleName();
            }
        }
        throw  new IllegalArgumentException("the class " + clz.getSimpleName() + " can't map to the table");
    }
}
