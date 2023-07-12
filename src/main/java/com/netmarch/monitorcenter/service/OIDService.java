package com.netmarch.monitorcenter.service;

import jnetman.snmp.Table;
import org.snmp4j.smi.OID;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/***
 * @author daiqk
 */
public interface OIDService {

    /**
     * 返回单个实体，请参考超级方法
     *
     * @see OIDService#readList(Table, String[], Class, Class[], List, Predicate, Consumer)
     */
    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue,final Consumer<? super T> consumer);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue,final Predicate<? super T> filter);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue,final Consumer<? super T> consumer,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue,final Predicate<? super T> filter,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue,final Predicate<? super T> filter,final Consumer<? super T> consumer);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue,final Predicate<? super T> filter,final Consumer<? super T> consumer,final UnaryOperator<? super T> operator);


    /***
     * 返回集合，请参考超级方法{@link #readObject}
     * @see OIDService#readList(Table, String[], Class, Class[], List, Predicate, Consumer)
     */
    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,final UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,final Consumer<? super List<T>> consumer);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,final Predicate<? super T> filter);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,final Consumer<? super List<T>> consumer,final UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,final Predicate<? super T> filter,final UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,final Predicate<? super T> filter,final Consumer<? super List<T>> consumer);

    /***
     * 在可遍历表格里根据names提取出对应的值
     * @param table 结果集表
     * @param fieldNames 实体的字段名/oid名称
     * @param clazz 待返回类名
     * @param declaredFieldType 每个name的数据类型
     * @param defaultValue 失败默认返回值
     * @param filter 过滤器(断言代码)
     * @param consumer 数据产出后的后续操作
     * @param operator 应用于对象的操作
     * @param <T> 期望返回的实体类数组
     * @return
     */
    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator);


    <R,T> List<R> readTable(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,
                            Predicate<? super T> filter, Consumer<? super List<T>> consumer, Function<? super T, ? extends R> mapper);


    //=================================不需要输入字段类型(反射获得)的方法=======================================


    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue,final Consumer<? super T> consumer);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue,final Predicate<? super T> filter);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue,final Consumer<? super T> consumer,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue,final Predicate<? super T> filter,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue,final Predicate<? super T> filter,final Consumer<? super T> consumer);

    <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue,final Predicate<? super T> filter,final Consumer<? super T> consumer,final UnaryOperator<? super T> operator);


    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue,final UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue,final Consumer<? super List<T>> consumer);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue,final Predicate<? super T> filter);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue,final Consumer<? super List<T>> consumer,final UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue,final Predicate<? super T> filter,final UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue,final Predicate<? super T> filter,final Consumer<? super List<T>> consumer);

    /**
     * 请参考超级方法(不需要封送字段数据类型的方法)
     *
     * @see OIDService#readList(Table, String[], Class, List, Predicate, Consumer, UnaryOperator)
     */
    <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue,final Predicate<? super T> filter,final Consumer<? super List<T>> consumer,final UnaryOperator<? super T> operator);



    //=================================根据oid前缀匹配，并将数值设置到实体上============================================
    @Deprecated
    <T> List<T> readList(Table table, OID[] oids,String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue);



    <T> T readObject(Table table, OID[] oids,String[] fieldNames, Class clazz, T defaultValue);

    <T> T readObject(Table table, OID[] oids,String[] fieldNames, Class clazz, T defaultValue,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, OID[] oids,String[] fieldNames, Class clazz, T defaultValue,final Consumer<? super T> consumer);

    <T> T readObject(Table table, OID[] oids,String[] fieldNames, Class clazz, T defaultValue,final Predicate<? super T> filter);

    <T> T readObject(Table table, OID[] oids,String[] fieldNames, Class clazz, T defaultValue,final Consumer<? super T> consumer,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, OID[] oids,String[] fieldNames, Class clazz, T defaultValue,final Predicate<? super T> filter,final UnaryOperator<? super T> operator);

    <T> T readObject(Table table, OID[] oids,String[] fieldNames, Class clazz, T defaultValue,final Predicate<? super T> filter,final Consumer<? super T> consumer);

    <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter, Consumer<? super T> consumer,UnaryOperator<? super T> operator);



    <T> List<T> readList(Table table, OID[] oids,String[] fieldNames, Class clazz, List<T> defaultValue);

    <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Consumer<? super List<T>> consumer);

    <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter);

    <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue,  UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, UnaryOperator<? super T> operator);

    <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer);

    <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator);


    /***
     * 根据磁盘类型oid翻译成对应的可阅读、具有丰富语义的字符
     * @param oidType
     * @return
     */
    String translateStorageType(String oidType);
}