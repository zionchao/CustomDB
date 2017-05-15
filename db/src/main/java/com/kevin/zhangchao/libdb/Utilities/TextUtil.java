package com.kevin.zhangchao.libdb.Utilities;

import java.util.ArrayList;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

public class TextUtil {
    public static boolean isValidate(String content){
        if(content != null && !"".equals(content.trim())){
            return true;
        }
        return false;
    }

    public static boolean isValidate(ArrayList list){
        if (list != null && list.size() >0) {
            return true;
        }
        return false;
    }
}
