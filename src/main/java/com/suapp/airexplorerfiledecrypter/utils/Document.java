package com.suapp.airexplorerfiledecrypter.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Document extends ConcurrentHashMap<String, Object> {

    public static String NULL_VAL = "";

    public Document() {
        super();
    }
    
    public Document(String key, Object value) {
        super();
        put(key, value);
    }

    public Document(Map<String, Object> map) {
        super();
        Document map2 = new Document();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                value = NULL_VAL;//getDefaultValue(value);
            }

            if(value != null)
                map2.put(key, value);
        }
        super.putAll(map2);
    }
    
    public Document append(String key, Object value) {
        put(key, value);
        return this;
    }

    @Override
    public Object get(Object key) {
        Object o;
        if (key == null) {
            o = null;
        } else {
            o = super.get(key);
        }

        return o;
    }

    @Override
    public Object put(String key, Object value
    ) {
        if (key == null) {
            return null;
        }
        Object o;
        if (value == null) {
            o = NULL_VAL;//getDefaultValue(value);
        } else {
            o = value;
        }

        Object r = super.put(key, (Object) o);
        return r;
    }

    public static boolean isNull(Object o) {
        if (o == null) {
            return true;
        }

        if (o instanceof String) {
            if (((String) o).equals(Document.NULL_VAL)) {
                return true;
            }
        }

        return false;
    }

    public String getStringValue(String key) {
        if (key == null) {
            return "";
        }

        Object o;
        o = (Object) this.get(key);
        if (o == null) {
            return "";
        }

        if (o instanceof String) {
            return (String) o;
        }

        if (o instanceof Number) {
            return ((Number) o).toString();
        }

        return "";
    }
    
    public <T> T get(String key, Class<T> type)
    {
        return (T) get(key);
    }

    public <T> T getOrDefault(String key, Class<T> type, T defaultValue)
    {
        if(get(key) != null)
            try
            {
                return type.cast(get(key)) ;
            } catch (Exception e)
            {
            }
            
        return defaultValue;
    }
    
    
}

