package com.netmarch.monitorcenter.util;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class BeanMap implements Map<String,Object>{
    private Map<String,Object> map;
    private BeanMap(){}
    public BeanMap(Map<String,Object> map){
        this.map = map;
    }
    public <T> T get(String key,Class<T> clas){
        Object value = this.map.get(key);
        if (value!=null){
            return TypeUtil.cast(value,clas);
        }
        return null;
    }
    public String getString(String key){
        return get(key,String.class);
    }
    public Integer getInteger(String key){
        return get(key,Integer.class);
    }
    public Long getLong(String key){
        return get(key,Long.class);
    }
    public Double getDouble(String key){
        return get(key,Double.class);
    }
    public Date getDate(String key){
        return get(key,Date.class);
    }
    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return this.map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return this.map.put(key,value);
    }

    @Override
    public Object remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(Map m) {
        this.map.putAll(m);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }


    @Override
    public boolean equals(Object o) {
        return this.map.equals(o);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
}
