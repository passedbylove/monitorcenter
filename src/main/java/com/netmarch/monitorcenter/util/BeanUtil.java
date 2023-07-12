package com.netmarch.monitorcenter.util;

import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.persistence.Transient;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 对于类的属性、方法操作的工具类
 *
 * @author wuyanglee on 2016-06-16 下午13:59
 */
public class BeanUtil {

    /**
     * get,set方法的最小长度 - 1
     */
    private static final int SET_START = "set".length();

    /**
     * is 方法最小长度 - 1
     */
    private static final int IS_START = "is".length();

    public static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat(
            "yyyy-MM-dd");

    /**
     * 正则表达式校验
     *
     * @author jemi 2016年7月6日
     */
    public static class Regexp {
        public static final Pattern DIGITS = Pattern.compile("^\\d+$");
        public static final Pattern NUMBER = Pattern
                .compile("^-?(?:\\d+|\\d{1,3}(?:,\\d{3})+)?(?:\\.\\d+)?$");

        public static final Pattern COMP_TAX_CODE = Pattern
                .compile("^[0-9a-zA-Z]+$");
        public static final Pattern VEND_TAX_CODE = Pattern
                .compile("^([0-9]|DK|X|L)+$");
    }

    /**
     * 把source的属性的值，拷贝给target同名属性
     *
     * @param source
     * @param target
     */
    @SuppressWarnings("rawtypes")
    public static void copy(Object source, Object target) {
        try {
            // 得到source类的所有方法
            /**
             * 反射中getMethods 与 getDeclaredMethods 的区别 public Method[]
             * getMethods()返回某个类的所有公用（public）方法包括其继承类的公用方法,当然也包括它所实现接口的方法。
             * public Method[] getDeclaredMethods()对象表示的类或接口声明的所有方法：
             * 包括公共、保护、默认（包）访问和私有方法，但不包括继承的方。当然也包括它所实现接口的方法。
             */
            Method[] methods = source.getClass().getDeclaredMethods();
            for (Method method : methods) {
                // 得到get方法
                if (isGetter(method)) {
                    // 得到get方法的返回值类型
                    Class<?> returnType = method.getReturnType();
                    // 得到source对象get方法的返回值
                    Object result = method.invoke(source);
                    // 不拷贝source值==null的属性
                    if (null == result) {
                        continue;
                    }
                    // 空集合不拷贝
                    if (result instanceof Collection) {
                        Collection collection = (Collection) result;
                        if (collection.size() <= 0) {
                            continue;
                        }
                    }
                    // 瞬时属性不拷贝
                    if (method.isAnnotationPresent(Transient.class)) {
                        continue;
                    }

                    // 通过get方法得到set方法名字
                    String setterName = getter2Setter(method.getName());
                    // 得到set方法,set方法的参数类型==get方法的返回值类型
                    Method setter = getMethod(source.getClass(), setterName,
                            returnType);
                    // 把soure对象get方法返回值，拷贝给对象的target对象的set方法
                    setter.invoke(target, result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到指定的方法
     *
     * @param clz        方法的类型
     * @param methodName 方法的名字
     * @param paramTypes 方法的参数列表
     * @return
     */
    public static Method getMethod(Class<?> clz, String methodName,
                                   Class<?>... paramTypes) {
        Method method = null;
        try {
            method = clz.getMethod(methodName, paramTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return method;
    }

    /**
     * getter方法名转换成setter方法名.
     *
     * @param methodName getter方法名
     * @return setter方法名isUserName setUserName
     */
    public static String getter2Setter(String methodName) {
        if (methodName.startsWith("get")) {
            return "s" + methodName.substring(1);
        } else if (methodName.startsWith("is")) {
            return "set" + methodName.substring(2);
        } else {
            throw new IllegalArgumentException(
                    "method not start with get or is.");
        }
    }

    /**
     * 判断方法名是否符合setter. 1.方法名是3个字符以上 2.方法名以set开头 3.方法只有一个参数 满足这三个条件，就被认为是setter
     *
     * @param method 方法
     * @return boolean
     */
    public static boolean isSetter(Method method) {
        String name = method.getName();
        boolean hasOneParam = method.getParameterTypes().length == 1;
        boolean startsWithGet = (name.length() > SET_START)
                && name.startsWith("set");

        return startsWithGet && hasOneParam;
    }

    /**
     * 判断方法名是否符合getter. 1.方法名以get开头，并在3个字符以上 2.方法名以is开头，并在2个字符以上111 3.方法没有参数
     * 满足这三个条件，就被认为是getter
     *
     * @param method 方法
     * @return boolean
     */
    public static boolean isGetter(Method method) {
        String name = method.getName();
        // 方法没有参数
        boolean hasNoParam = method.getParameterTypes().length == 0;
        // 方法有返回值
        boolean noResultType = method.getReturnType() != void.class;
        // 方法方法名已get开头，大于3位
        boolean startsWithGet = (name.length() > SET_START)
                && name.startsWith("get");
        // 方法以is开头，大于2位
        boolean startsWithIs = (name.length() > IS_START)
                && name.startsWith("is");
        // 不是getClass方法
        boolean notGetClass = !name.equals("getClass");

        return hasNoParam && noResultType && notGetClass
                && (startsWithGet || startsWithIs);
    }

    /**
     * 比较两个对象是否相等，如果都为空也认为相等
     */
    public static boolean equals(Object obj1, Object obj2) {
        if ((obj1 == null) && (obj2 == null)) {
            return true;
        }
        if ((obj1 != null) && obj1.equals(obj2)) {
            return true;
        }
        return false;
    }

    /**
     * 是否为有效数字
     */
    public static boolean isDigits(Object obj) {
        return Regexp.DIGITS.matcher(toString(obj)).find();
    }

    /**
     * 是否为公司税号
     */
    public static boolean isCompanyTaxCode(Object obj) {
        return Regexp.COMP_TAX_CODE.matcher(toString(obj)).find();
    }

    /**
     * 判断对象是否为空，支持 String, Collection, Map, Object[]
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).isEmpty();
        }
        if (obj instanceof Collection<?>) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        }
        return false;
    }


    public static Date addDate(Date date, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, num);
        return c.getTime();
    }

    public static Date toDate(String dateStr) {
        try {
            return YYYY_MM_DD.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatToYYYYMMDD(Date date) {
        try {
            return YYYY_MM_DD.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date toDate(String dateStr, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatDate(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static String leftZeroNumber(int number, int len) {
        return String.format("%0" + len + "d", number);
    }

    public static String toString(Object obj) {
        return toString(obj, null);
    }

    public static String toString(Object obj, String def) {
        return obj == null ? def : obj.toString();
    }


    /**
     * 是否数字
     */
    public static boolean isNumber(Object obj) {
        return Regexp.NUMBER.matcher(toString(obj)).find();
    }

    //将javabean实体类转为map类型，然后返回一个map类型的值
    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = new HashMap<String, Object>(0);
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (!"class".equals(name)) {
                    params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 将javabean转为map
     * @param bean
     * @param mp
     * @param <T>
     * @param <K>
     * @param <V>
     * @return
     */
    public static <T, K, V> Map<String, Object> bean2Map(T bean, Map<String, Object> mp) {
        if (bean == null) {
            return null;
        }

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                if (!key.equals("class")) {
                    //java提供的获取get方法
                    Method getter = property.getReadMethod();
                    mp.put(key, getter.invoke(bean));
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mp;
    }

    /**
     * 将map转为javabean
     * @param mp
     * @param beanCls
     * @param <T>
     * @param <K>
     * @param <V>
     * @return
     * @throws Exception
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static <T, K, V> T map2Bean(Map<K, V> mp, Class<T> beanCls)
            throws Exception, IllegalArgumentException, InvocationTargetException {
        T t = null;
        try {
             BeanInfo beanInfo = Introspector.getBeanInfo(beanCls);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            t = beanCls.newInstance();

            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                if (mp.containsKey(key)) {
                    Object value = mp.get(key);
                    Method setter = property.getWriteMethod();// Java中提供了用来访问某个属性的
                    setter.invoke(t, value);
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return t;
    }
    /**
     *判断Map中所需字段为空
     */
    public static String validaRequestBody(Map<String,String> input,List<String> needList){
        String lackMsg="";
        String msg="";
//        String msg="";
        for (String need:needList) {
            String contain = input.get(need);
            if (isEmpty(contain)) {
                lackMsg=lackMsg+need+",";
            }
        }
        if(!lackMsg.equals("")){
             msg = lackMsg.substring(0, lackMsg.length() - 1);
        }

        return msg;
    }
}
