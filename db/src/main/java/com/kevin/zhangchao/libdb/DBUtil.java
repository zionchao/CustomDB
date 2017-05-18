package com.kevin.zhangchao.libdb;

import android.database.sqlite.SQLiteDatabase;

import com.kevin.zhangchao.libdb.Utilities.TextUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

class DBUtil {

    public static final String PK1 = "pk1";
    public static final String PK2 = "pk2";

    //  String sql="create table h1(id TEXT primary key NOT NULL , name TEXT, age TEXT, company TEXT, skills TEXT)";
    public static void createTable(SQLiteDatabase db, Class<?> clz) {
//        StringBuilder builder=new StringBuilder();
//        Field[] fields=clz.getDeclaredFields();
//        for (int i=0;i<fields.length;i++){
//            Field field=fields[i];
//            builder.append(getOneColumnStmt(field));
//            if (i!=fields.length-1)
//                builder.append(",");
//        }
//        String sql="create table "+getTableName(clz)+"("+builder+")";
//        db.execSQL(sql);
        ArrayList<String> stmts=getCreateTableStmt(clz);
        for (String stmt:stmts) {
            db.execSQL(stmt);
        }
    }

    public static ArrayList<String> getCreateTableStmt(Class<?> clz){
        StringBuilder mColumnStmts=new StringBuilder();
        ArrayList<String> stmts=new ArrayList<>();
        if (clz.isAnnotationPresent(Table.class)){
            Field[] fields=clz.getDeclaredFields();
            for (int i=0;i<fields.length;i++){
                Field field=fields[i];
                if (field.isAnnotationPresent(Column.class)){
                    if (field.getAnnotation(Column.class).type()==Column.ColumnType.TMANY){
                        stmts.add("create table "+getAsscociationTableName(clz,field.getName())+ "(" + PK1 + " TEXT, " + PK2
                                + " TEXT)");
                    }
                    mColumnStmts.append(getOneColumnStmt(field));
                    mColumnStmts.append(",");
                }

            }
            if (mColumnStmts.length()>0){
                mColumnStmts.delete(mColumnStmts.length()-2,mColumnStmts.length());
            }
            stmts.add("create table "+getTableName(clz)+"("+mColumnStmts+")");
        }
        return stmts;
    }

    public static String getAsscociationTableName(Class<?> clz, String association) {
        return getIDColumnName(clz)+"_"+association;
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
                    //一对多，新建一张表
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
//        String sql="drop table if exists  "+getTableName(clz);
//        db.execSQL(sql);
        ArrayList<String> stmts=getDropTableStatms(clz);
        for (String stmt : stmts) {
            db.execSQL(stmt);
        }
    }

    public static ArrayList<String> getDropTableStatms(Class<?> clz){
        ArrayList<String> stmts=new ArrayList<>();
        if (clz.isAnnotationPresent(Table.class)){
            Field[] fields=clz.getDeclaredFields();
            for (int i=0;i<fields.length;i++){
                Field field=fields[i];
                if (field.isAnnotationPresent(Column.class)){
                    if (field.getAnnotation(Column.class).type()==Column.ColumnType.TMANY){
                        stmts.add("drop table if exists "+getAsscociationTableName(clz,field.getName()));
                    }
                }
            }
            stmts.add("drop table if exists "+getTableName(clz));
        }
        return stmts;
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

    public static ArrayList<Field> getForeignFields(Field[] mColumnFields) {
        Column column=null;
        ArrayList<Field> fields=new ArrayList<>();
        for (Field field: mColumnFields) {
            column=field.getAnnotation(Column.class);
            if (column.type()== Column.ColumnType.TMANY)
                fields.add(field);
        }
        return fields;
    }
}
