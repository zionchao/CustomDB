package com.kevin.zhangchao.libdb;

import android.database.sqlite.SQLiteDatabase;

import com.kevin.zhangchao.libdb.Utilities.TextUtil;

import java.lang.reflect.Field;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

class DBUtil {

//  String sql="create table h1(id TEXT primary key NOT NULL , name TEXT, age TEXT, company TEXT, skills TEXT)";
    public static void createTable(SQLiteDatabase db, Class<?> clz) {
        StringBuilder builder=new StringBuilder();
        Field[] fields=clz.getDeclaredFields();
        for (int i=0;i<fields.length;i++){
            Field field=fields[i];
            builder.append(getOneColumnStmt(field));
            if (i!=fields.length-1)
                builder.append(",");
        }
        String sql="create table "+getTableName(clz)+"("+builder+")";
        db.execSQL(sql);
    }

    public static String getOneColumnStmt(Field field){
        String name=null;
        if (field.isAnnotationPresent(Column.class)){
            Column column=field.getAnnotation(Column.class);
            name=column.name();
            if (!TextUtil.isValidate(name))
                name="["+field.getName()+"]";
            else
                name="["+name+"]";
            String type=null;
            Class<?> clzType=field.getType();
            if (clzType==String.class){
                type=" TEXT ";
            }else if (clzType==Integer.class||clzType==int.class){
                type=" integer ";
            }else{
                Column.ColumnType columnType=column.type();
                if (columnType== Column.ColumnType.TONE){
                    type=" TEXT ";
                }else if (columnType== Column.ColumnType.TMANY){

                }else if(columnType== Column.ColumnType.SERIALIZABLE){
                    type=" BLOB ";
                }
            }
            name+=type;
            if (column.id()){
               name+=" primary key ";
            }
            return name;
        }
        return  "";
    }

    public static void dropTable(SQLiteDatabase db, Class<?> clz) {
        String sql="drop table if exists  "+getTableName(clz);
        db.execSQL(sql);
    }


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


    public static String getColumnName(Field filed) {
        Column column=filed.getAnnotation(Column.class);
        String name=column.name();
        if (!TextUtil.isValidate(name))
            name=filed.getName();
        return name;
    }


    public static String getIDColumnName(Class<?> clz) {
        if (clz.isAnnotationPresent(Table.class)){
            Field[] fields=clz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)){
                    Column column=field.getAnnotation(Column.class);
                    if (column.id()){
                        String name=column.name();
                        if (!TextUtil.isValidate(name))
                            name=field.getName();
                        return name;
                    }
                }
            }
        }
        return null;
    }
}
