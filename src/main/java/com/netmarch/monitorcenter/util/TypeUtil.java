package com.netmarch.monitorcenter.util;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Types;
import java.util.Date;

public class TypeUtil {
    public static final Class JAVA_DATE = Date.class;
    public static final Class JAVA_STRING = String.class;
    public static final Class JAVA_NUMBER = Number.class;
    /**
     *
     * @param obj
     * @param clazz 目标数据类型
     * @param <T>
     * @return
     */
    public static <T> T cast(@NotNull Object obj,@NotNull Class<T> clazz){
        try {
            // 目标类型 与 待转 类型一样，直接返回
            if(obj.getClass().equals(clazz)){
                return (T) obj;
            }
            if (isDate(obj)&&isDate(clazz)){
               return (T) newDate(((Date)obj).getTime(),clazz);
            }else if (isDate(obj)){
                  if (isString(clazz)){
                    return (T) DateUtil.parseDateToStr((Date)obj,DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS);
                  }else if (isNumber(clazz)){
                     return (T) (Long)((Date)obj).getTime();
                  }
            } else if (isDate(clazz)){
                if (isString(obj.getClass())){
                    return (T) DateUtil.parseStrToDate((String) obj,DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS);
                }else if (isNumber(obj.getClass())){
                    Long time = new Long(obj.toString());
                    return (T) newDate(time,clazz);
                }
            } else if (isNumber(clazz)){
                    return newNumber(obj,clazz);
            } else if (isString(clazz)){
                return (T) obj.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) obj.toString();
    }
    private static  <T> T  newNumber(@NotNull Object num,@NotNull Class<T> dateClazz){
        try {
            if (!isNumber(dateClazz)){
                throw new IllegalArgumentException("dateClazz param of method newNumber is wrong");
            }
            Class[] parameterTypes= {String.class};
            Constructor constructor = dateClazz.getConstructor(parameterTypes);
            return (T) constructor.newInstance(num.toString());

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static  <T> T  newDate(Long time,Class<T> dateClazz){
        try {
            if (!isDate(dateClazz)){
                throw new IllegalArgumentException("dateClazz param of method newDate is wrong");
            }
            Constructor constructor = dateClazz.getDeclaredConstructor(long.class);
            return (T) constructor.newInstance(time);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean isString(@NotNull Object obj){
        return isType(JAVA_STRING,obj);
    }
    public static boolean isNumber(@NotNull Object obj){
        return isType(JAVA_NUMBER,obj);
    }
    public static boolean isDate(@NotNull Object obj){
        return isType(JAVA_DATE,obj);
    }
    public static boolean isType(@NotNull Class clazz,@NotNull Object obj){
          if (obj instanceof Class){
             return  clazz.isAssignableFrom((Class)obj);
          }else {
             return  clazz.isAssignableFrom(obj.getClass());
          }
    }
    /**
     * Translates a data type from an integer (java.sql.Types value) to a string
     * that represents the corresponding class.
     *
     * REFER: https://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/jdbc/getstart/mapping.doc.html
     *
     * @param type
     *            The java.sql.Types value to convert to its corresponding class.
     * @return The class that corresponds to the given java.sql.Types
     *         value, or Object.class if the type has no known mapping.
     */
    public static Class<?> parseSqlTypeToClass(int type) {
        Class<?> result =  null;
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                result = String.class;
                break;

            case Types.NUMERIC:
            case Types.DECIMAL:
                result = java.math.BigDecimal.class;
                break;

            case Types.BIT:
                result = Boolean.class;
                break;

            case Types.TINYINT:
                result = Byte.class;
                break;

            case Types.SMALLINT:
                result = Short.class;
                break;

            case Types.INTEGER:
                result = Integer.class;
                break;

            case Types.BIGINT:
                result = Long.class;
                break;

            case Types.REAL:
            case Types.FLOAT:
                result = Float.class;
                break;

            case Types.DOUBLE:
                result = Double.class;
                break;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                result = Byte[].class;
                break;

            case Types.DATE:
                result = java.sql.Date.class;
                break;

            case Types.TIME:
                result = java.sql.Time.class;
                break;

            case Types.TIMESTAMP:
                result = java.sql.Timestamp.class;
                break;
            default: result = Object.class;
        }

        return result;
    }
    public static String parseSqlTypeToDbType(int type){
        String result =  null;
        switch (type) {
            case Types.CHAR:
                result = "CHAR";break;
            case Types.LONGVARCHAR:
                result = "LONGVARCHAR";break;
            case Types.NUMERIC:
                result = "NUMERIC";break;
            case Types.DECIMAL:
                result = "DECIMAL";break;
            case Types.BIT:
                result = "BIT";break;
            case Types.TINYINT:
                result = "TINYINT"; break;
            case Types.SMALLINT:
                result = "SMALLINT";break;
            case Types.INTEGER:
                result = "INTEGER";break;
            case Types.BIGINT:
                result = "BIGINT";break;
            case Types.REAL:
                result = "REAL";break;
            case Types.FLOAT:
                result = "FLOAT";break;
            case Types.DOUBLE:
                result = "DOUBLE";break;
            case Types.BINARY:
                result = "BINARY";break;
            case Types.VARBINARY:
                result = "VARBINARY";break;
            case Types.LONGVARBINARY:
                result = "LONGVARBINARY";break;
            case Types.DATE:
                result = "DATE";break;
            case Types.TIME:
                result = "TIME";break;
            case Types.TIMESTAMP:
                result = "TIMESTAMP";break;
            default: result = "VARCHAR";break;
        }

        return result;
    }
}
