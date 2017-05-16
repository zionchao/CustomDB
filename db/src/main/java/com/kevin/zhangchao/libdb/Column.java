package com.kevin.zhangchao.libdb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhangchao_a on 2017/5/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    boolean id() default false;
    String name() default "";
    ColumnType type() default ColumnType.UNKOWN;
    boolean autoRefresh() default false;

    public enum ColumnType{
        TONE,TMANY,SERIALIZABLE,UNKOWN
    }
}
