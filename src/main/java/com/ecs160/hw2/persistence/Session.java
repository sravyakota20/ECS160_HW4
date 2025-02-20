package com.ecs160.hw2.persistence;

import redis.clients.jedis.Jedis;
import java.lang.reflect.Field;
import java.util.*;

public class Session {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private Jedis jedis;
    private List<Object> sessionObjects;

    public Session() {
        this.jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        this.sessionObjects = new ArrayList<>();
    }

    public void add(Object obj) {
        if (obj.getClass().isAnnotationPresent(Persistable.class)) {
            sessionObjects.add(obj);
        }
    }

    public void persistAll() {
        for (Object obj : sessionObjects) {
            persistObject(obj);
        }
    }

    private void persistObject(Object obj) {
        String key = getPrimaryKeyValue(obj);
        Map<String, String> fields = new HashMap<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(PersistableField.class)) {
                    fields.put(field.getName(), field.get(obj).toString());
                } else if (field.isAnnotationPresent(PersistableListField.class)) {
                    List<?> list = (List<?>) field.get(obj);
                    fields.put(field.getName(), serializeList(list));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        jedis.hmset(key, fields);
    }

    public Object load(Object obj) {
        String key = getPrimaryKeyValue(obj);
        Map<String, String> storedData = jedis.hgetAll(key);
        
        if (storedData.isEmpty()) return null;
        
        try {
            Object loadedObj = obj.getClass().getDeclaredConstructor().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PersistableField.class)) {
                    field.set(loadedObj, castValue(field.getType(), storedData.get(field.getName())));
                } else if (field.isAnnotationPresent(PersistableListField.class)) {
                    field.set(loadedObj, deserializeList(field.getType(), storedData.get(field.getName())));
                }
            }
            return loadedObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPrimaryKeyValue(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PersistableId.class)) {
                try {
                    field.setAccessible(true);
                    return "object:" + field.get(obj).toString();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String serializeList(List<?> list) {
        return String.join(",", list.stream().map(Object::toString).toArray(String[]::new));
    }

    private List<Object> deserializeList(Class<?> type, String data) {
        List<Object> list = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            String[] items = data.split(",");
            for (String item : items) {
                try {
                    Object obj = type.getDeclaredConstructor().newInstance();
                    Field idField = type.getDeclaredField("postId");
                    idField.setAccessible(true);
                    idField.set(obj, Integer.parseInt(item));
                    list.add(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    private Object castValue(Class<?> type, String value) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == String.class) {
            return value;
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }
}

