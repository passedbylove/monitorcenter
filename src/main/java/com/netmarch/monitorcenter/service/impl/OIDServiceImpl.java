package com.netmarch.monitorcenter.service.impl;

import com.netmarch.monitorcenter.bean.SnmpFileSystem;
import com.netmarch.monitorcenter.service.MibService;
import com.netmarch.monitorcenter.service.OIDService;
import jnetman.snmp.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OIDServiceImpl implements OIDService {

    @Autowired
    private MibService mibService;


    /**
     * 返回单个实体，请参考超级方法
     *
     * @param table
     * @param fieldNames
     * @param clazz
     * @param declaredFieldType
     * @param defaultValue
     * @see OIDService#readList(Table, OID[], String[], Class, List, Predicate, UnaryOperator) 
     */
    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue) {
        return readObject(table,fieldNames,clazz,declaredFieldType,defaultValue,null,null,null);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue, UnaryOperator<? super T> operator) {
        return readObject(table,fieldNames,clazz,declaredFieldType,defaultValue, null, null,operator);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue, Consumer<? super T> consumer) {
        return readObject(table,fieldNames,clazz,declaredFieldType,defaultValue,null,consumer,null);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue, Predicate<? super T> filter) {
        return readObject(table,fieldNames,clazz,declaredFieldType,defaultValue,filter,null,null);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue, Consumer<? super T> consumer, UnaryOperator<? super T> operator) {
        return readObject(table,fieldNames,clazz,declaredFieldType,defaultValue,null,consumer,operator);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue, Predicate<? super T> filter, UnaryOperator<? super T> operator) {
        return readObject(table,fieldNames,clazz,declaredFieldType,defaultValue,filter,null,operator);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue, Predicate<? super T> filter, Consumer<? super T> consumer) {
        return readObject(table,fieldNames,clazz,declaredFieldType,defaultValue,filter,consumer,null);
    }

    /***
     * 以上所有方法全部委托给此方法处理
     */
    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, T defaultValue, Predicate<? super T> filter, Consumer<? super T> consumer,final UnaryOperator<? super T> operator) {

        Optional<T> result = readList(table, fieldNames, clazz, declaredFieldType,Arrays.asList(defaultValue)).stream().findFirst();
        return handleSingleObject(result,filter,consumer,operator);
    }

    /***
     * 单个对象的断言、预转换、后续操作
     * @param result
     * @param filter
     * @param consumer
     * @param operator
     * @param <T>
     * @return
     */
    <T> T handleSingleObject(Optional<T> result, Predicate<? super T> filter, Consumer<? super T> consumer,final UnaryOperator<? super T> operator)
    {
        if(!result.isPresent())
        {
            throw new NullPointerException("表结果集为空或者解析失败");
        }

        T instance = result.get();

        if(operator != null) {
            operator.apply(instance);
        }


        if(filter != null)
        {
            if(!filter.test(instance)) {
                instance = null;
            }
        }

        if(consumer != null) {
            consumer.accept(instance);
        }
        return instance;
    }

    /***
     * 返回集合，请参考超级方法{@link #readList}
     * @see OIDService#readList(Table, String[], Class, Class[], List, Predicate, Consumer)
     * @param table
     * @param fieldNames
     * @param clazz
     * @param declaredFieldType
     * @param defaultValue
     */
    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue) {
        return readList(table,fieldNames,clazz,declaredFieldType,defaultValue,null,null,null);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, UnaryOperator<? super T> operator) {
        return readList(table,fieldNames,clazz,declaredFieldType,defaultValue,null,null,operator);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, Consumer<? super List<T>> consumer) {
        return readList(table,fieldNames,clazz,declaredFieldType,defaultValue,null,consumer,null);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, Predicate<? super T> filter) {
        return readList(table,fieldNames,clazz,declaredFieldType,defaultValue,filter,null,null);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator) {
        return readList(table,fieldNames,clazz,declaredFieldType,defaultValue,null,consumer,operator);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, Predicate<? super T> filter, UnaryOperator<? super T> operator) {
        return readList(table,fieldNames,clazz,declaredFieldType,defaultValue,filter,null,operator);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer) {
        return readList(table,fieldNames,clazz,declaredFieldType,defaultValue,filter,consumer,null);
    }

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
     * @return
     */
    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator) {
        if(table == null || table.isEmpty()) {
            return defaultValue;
        }

        if(ArrayUtils.isEmpty(fieldNames)||ArrayUtils.isEmpty(declaredFieldType))
        {
            log.error("字段名长度和字段数据类型长度不能为null");
            throw new IllegalArgumentException("字段名长度和字段数据类型长度不能为null");
        }

        if(!ArrayUtils.isSameLength(fieldNames, declaredFieldType))
        {
            throw new IllegalArgumentException("字段名个数与字段数据类型数组长度不一致");
        }

        Map<OID, Table.Row> rowMapping = table.getMap();

        if(MapUtils.isEmpty(rowMapping)) {
            return defaultValue;
        }

        log.debug("待查询oid名称:{}",String.join(",", fieldNames));

        OID [] oids = mibService.translateOID(fieldNames);

        String [] oidInDottedString = Arrays.asList(oids).stream().map(OID::toDottedString).toArray(size->new String[size]);

        List<T> list = new LinkedList<T>();

        for(Table.Row row : rowMapping.values()) {

            Set<OID> keys = row.getMap().keySet();

            try {
                T instance = (T)clazz.newInstance();

                for (OID key : keys) {
                    String dotString = key.toDottedString();

                    for (int i = 0; i < oidInDottedString.length; i++) {

                        String oidString = oidInDottedString[i];

                        if (dotString.contains(oidString)) {
                            String fieldName = fieldNames[i];

                            Variable value = row.getVariable(key);

                            Class declaredType = declaredFieldType[i];

                            setProperty(instance,declaredType,fieldName,value);

                        }
                    }
                }

                list.add(instance);

            }catch (IllegalAccessException|InstantiationException e)
            {
                log.error("提取属性失败:{}",e);
            }
        }

        if(operator != null && CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(instance->{operator.apply(instance);});
        }

        if(filter != null && CollectionUtils.isNotEmpty(list)) {
            list = list.stream().filter(filter).collect(Collectors.toList());
        }

        if(consumer != null && CollectionUtils.isNotEmpty(list)) {
            consumer.accept(list);
        }

        return list;
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue) {
        return readObject(table,fieldNames,clazz,defaultValue,null,null,null);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue, UnaryOperator<? super T> operator) {
        return readObject(table,fieldNames,clazz,defaultValue,null,null,operator);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue, Consumer<? super T> consumer) {
        return readObject(table,fieldNames,clazz,defaultValue,null,consumer,null);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter) {
        return readObject(table,fieldNames,clazz,defaultValue,null,null,null);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue, Consumer<? super T> consumer, UnaryOperator<? super T> operator) {
        return readObject(table,fieldNames,clazz,defaultValue,null,consumer,operator);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter, UnaryOperator<? super T> operator) {
        return readObject(table,fieldNames,clazz,defaultValue,filter,null,operator);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter, Consumer<? super T> consumer) {
        return readObject(table,fieldNames,clazz,defaultValue,filter,consumer,null);
    }

    @Override
    public <T> T readObject(Table table, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter, Consumer<? super T> consumer, UnaryOperator<? super T> operator) {

        if (table == null || table.isEmpty()) {
            return defaultValue;
        }
        Optional<T> result = readList(table, fieldNames, clazz, Arrays.asList(defaultValue)/*, null, null, null*/).stream().findFirst();

        return handleSingleObject(result,filter,consumer,operator);

    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue) {
        return readList(table,fieldNames,clazz,defaultValue, null,null,null);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue, UnaryOperator<? super T> operator) {
        return readList(table,fieldNames,clazz,defaultValue,null,null,operator);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue, Consumer<? super List<T>> consumer) {
        return readList(table,fieldNames,clazz,defaultValue,null,consumer,null);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter) {
        return readList(table,fieldNames,clazz,defaultValue,filter,null,null);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator) {
        return readList(table,fieldNames,clazz,defaultValue,null,consumer,operator);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, UnaryOperator<? super T> operator) {
        return readList(table,fieldNames,clazz,defaultValue,filter,null,operator);
    }

    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer) {
        return readList(table,fieldNames,clazz,defaultValue,filter,consumer,null);
    }


    @Override
    public <R,T> List<R> readTable(Table table, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue,
                            Predicate<? super T> filter, Consumer<? super List<T>> consumer, Function<? super T, ? extends R> mapper)
    {
        List<T> list = readList(table, fieldNames, clazz, declaredFieldType, defaultValue, filter, consumer);
        return list.stream().map(item -> {
            R target = mapper.apply(item);
            return target;
        }).collect(Collectors.toList());
    }


    @Override
    public <T> List<T> readList(Table table, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator) {
        //Class [] declaredFieldType = getDeclaredFieldType(fieldNames,clazz);

        return readList(table,fieldNames,clazz,getDeclaredFieldType(fieldNames,clazz),defaultValue,filter,consumer,operator);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, Class[] declaredFieldType, List<T> defaultValue) {
        throw new NotImplementedException("暂时不考虑/建议客户端使用自定义的字段类型来调用此方法");
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz, T defaultValue) {
        return readObject(table,oids,fieldNames,clazz,defaultValue,null,null,null);
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz, T defaultValue, UnaryOperator<? super T> operator) {
        return readObject(table,oids,fieldNames,clazz,defaultValue,null,null,operator);
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz, T defaultValue, Consumer<? super T> consumer) {
        return readObject(table,oids,fieldNames,clazz,defaultValue,null,consumer,null);
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz,T defaultValue, Predicate<? super T> filter) {
        return readObject(table,oids,fieldNames,clazz,defaultValue,filter,null,null);
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz,T defaultValue, Consumer<? super T> consumer, UnaryOperator<? super T> operator) {
        return readObject(table,oids,fieldNames,clazz,defaultValue,null,consumer,operator);
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter, UnaryOperator<? super T> operator) {
        return readObject(table,oids,fieldNames,clazz,defaultValue,filter,null,operator);
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter, Consumer<? super T> consumer) {
        return readObject(table,oids,fieldNames,clazz,defaultValue,filter,consumer,null);
    }

    @Override
    public <T> T readObject(Table table, OID[] oids, String[] fieldNames, Class clazz, T defaultValue, Predicate<? super T> filter, Consumer<? super T> consumer,UnaryOperator<? super T> operator) {

        if (table == null || table.isEmpty()) {
            return defaultValue;
        }

        Optional<T> result = readList(table, fieldNames, clazz, Arrays.asList(defaultValue)).stream().findFirst();

        return handleSingleObject(result,filter,consumer,operator);

    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue) {
        return readList(table,oids,fieldNames,clazz,defaultValue,null,null,null);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Consumer<? super List<T>> consumer) {
        return readList(table,oids,fieldNames,clazz,defaultValue,null,consumer,null);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter) {
        return readList(table,oids,fieldNames,clazz,defaultValue,filter,null,null);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, UnaryOperator<? super T> operator) {
        return readList(table,oids,fieldNames,clazz,defaultValue,null,null,operator);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator) {
        return readList(table,oids,fieldNames,clazz,defaultValue,null,consumer,operator);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, UnaryOperator<? super T> operator) {
        return readList(table,oids,fieldNames,clazz,defaultValue,filter,null,operator);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer) {
        return readList(table,oids,fieldNames,clazz,defaultValue,filter,consumer,null);
    }

    @Override
    public <T> List<T> readList(Table table, OID[] oids, String[] fieldNames, Class clazz, List<T> defaultValue, Predicate<? super T> filter, Consumer<? super List<T>> consumer, UnaryOperator<? super T> operator) {
        if (table == null || table.isEmpty()) {
            return defaultValue;
        }

        Class[] declaredFieldType = getDeclaredFieldType(fieldNames, clazz);

        if(ArrayUtils.isEmpty(fieldNames)||ArrayUtils.isEmpty(declaredFieldType))
        {
            log.error("字段名长度和字段数据类型长度不能为null");
            throw new IllegalArgumentException("字段名长度和字段数据类型长度不能为null");
        }

        if(!ArrayUtils.isSameLength(fieldNames, declaredFieldType))
        {
            throw new IllegalArgumentException("字段名个数与字段数据类型数组长度不一致");
        }

        Map<OID, Table.Row> rowMapping = table.getMap();

        if(MapUtils.isEmpty(rowMapping)) {
            return defaultValue;
        }

        log.debug("待查询oid名称:{}",String.join(",", fieldNames));

        String [] oidInDottedString = Arrays.asList(oids).stream().map(OID::toDottedString).toArray(size->new String[size]);

        List<T> list = new LinkedList<T>();

        for(Table.Row row : rowMapping.values()) {

            Set<OID> keys = row.getMap().keySet();

            try {
                T instance = (T)clazz.newInstance();

                for (OID key : keys) {
                    String dotString = key.toDottedString();

                    for (int i = 0; i < oidInDottedString.length; i++) {

                        String oidString = oidInDottedString[i];

                        if (dotString.contains(oidString)) {

                            String fieldName = fieldNames[i];

                            Variable value = row.getVariable(key);

                            Class declaredType = declaredFieldType[i];

                            setProperty(instance,declaredType,fieldName,value);

                        }
                    }
                }

                list.add(instance);

            }catch (IllegalAccessException|InstantiationException e)
            {
                log.error("提取属性失败:{}",e);
            }
        }

        if(operator != null && CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(instance->{operator.apply(instance);});
        }

        if(filter != null && CollectionUtils.isNotEmpty(list)) {
            list = list.stream().filter(filter).collect(Collectors.toList());
        }

        if(consumer != null && CollectionUtils.isNotEmpty(list)) {
            consumer.accept(list);
        }

        return list;
    }


    /***
     * {@inheritDoc}
     * @param oidType
     * @return
     */
    @Override
    public String translateStorageType(String oidType)
    {
        String[] labels = mibService.translate2Label(new OID(oidType));

        if (ArrayUtils.isNotEmpty(labels)) {
            String storageType = labels[0];
            return storageType;
        } else {
            log.error("未查询到相应的存储类型:{}", oidType);
        }
        return null;
    }

    void matchField(Field[] fields, String fieldName, final Consumer<? super Class<?>> consumer)
    {
        Optional<Field> matched = Arrays.stream(fields).filter(field -> field.getName().equals(fieldName)).findFirst();
        if(matched.isPresent())
        {
            Class<?> type = matched.get().getType();
            consumer.accept(type);
        }
    }


    Class[] getDeclaredFieldType(String []fieldNames, Class clazz)
    {
        //通过字段名称反射获得申明的数据类型
        Field[] fields = FieldUtils.getAllFields(clazz);
        List<Class<?>> types = new LinkedList<>();
        Arrays.stream(fieldNames).forEach(
                fieldName->{
                    matchField(fields,fieldName,types::add);
                }
        );

        Class [] declaredFieldType = types.toArray(new Class<?>[]{});

        return declaredFieldType;
    }

    void setProperty(Object target, Class fieldType, String fieldName, Variable value)
    {

        String declaredFileTypeName = fieldType.getSimpleName();
        try {
            Object converted = null;

            if ("Integer".equals(declaredFileTypeName)) {
                converted = Integer.valueOf(value.toInt());
            }

            if ("Long".equals(declaredFileTypeName)) {
                converted = Long.valueOf(value.toLong());
            }

            if ("String".equals(declaredFileTypeName)) {
                converted = value.toString();
            }

            if("Float".equals(declaredFileTypeName)) {
                converted = Float.valueOf(value.toString());
            }

            PropertyUtils.setProperty(target,fieldName,converted);

        }catch (Exception ex)
        {
            log.error("setter注入属性失败,属性名:{}",fieldName,ex);
        }
    }

}
