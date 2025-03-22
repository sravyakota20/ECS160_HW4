package com.ecs160.hw4.persistence;

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
        sessionObjects.add(obj);
    }

    public void persistAll() {
        for (Object obj : sessionObjects) {
            persistObject(obj);
        }
        sessionObjects.clear();
    }

    private void persistObject(Object obj) {
        Class<?> clazz = obj.getClass();
        if (!clazz.isAnnotationPresent(Persistable.class)) {
            return;
        }

        String key = null;
        Map<String, String> fieldsToPersist = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(PersistableId.class)) {
                    key = String.valueOf(field.get(obj));  // Extract ID field value
                    // System.out.println("ID: " + key);
                } else if (field.isAnnotationPresent(PersistableField.class)) {
                    Object value = field.get(obj);
                    if (value != null) {
                        fieldsToPersist.put(field.getName(), String.valueOf(value));
                        // System.out.println("Adding Field: " + field.getName() + ", item: " + String.valueOf(value));
                    }
                } else if (field.isAnnotationPresent(PersistableListField.class)) {
                    PersistableListField listField = field.getAnnotation(PersistableListField.class);
                    List<?> list = (List<?>) field.get(obj);
                    if (list != null) {
                        List<String> idList = new ArrayList<>();
                        for (Object item : list) {
                            // ✅ Find the correct ID field dynamically
                            Field idField = item.getClass().getDeclaredField(getPersistableIdFieldName(item.getClass()));
                            idField.setAccessible(true);
                            String itemId = String.valueOf(idField.get(item));
                            idList.add(itemId);

                            // ✅ Store each reply (or list item) separately in Redis
                            persistObject(item);
                        }
                        // System.out.println("Adding List: " + field.getName() + ", items: " + String.join(",", idList));
                        fieldsToPersist.put(field.getName(), String.join(",", idList));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (key != null) {
            //System.out.println("Persisting the object: Key: " + key);
            jedis.hmset(key, fieldsToPersist);  // ✅ Store the main object in Redis
        }
    }
    
    public Object load(Object obj) {
        try {
            Class<?> clazz = obj.getClass();
            Field idField = clazz.getDeclaredField(getPersistableIdFieldName(clazz));
            idField.setAccessible(true);
            String key = String.valueOf(idField.get(obj));

            Map<String, String> data = jedis.hgetAll(key);
            if (data.isEmpty()) return null;

            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String value = data.get(field.getName());

                if (value != null) {
                    Class<?> fieldType = field.getType();

                    if (field.isAnnotationPresent(PersistableField.class)) {
                        // Handle primitive and String types
                        field.set(instance, convertValue(fieldType, value));
                    } if (field.isAnnotationPresent(PersistableListField.class)) {
                        String fieldValue = data.get(field.getName()); // Comma-separated IDs
                        if (fieldValue != null && !fieldValue.isEmpty()) {
                            List<String> idList = Arrays.asList(fieldValue.split(","));

                            if (field.isAnnotationPresent(LazyLoad.class)) {
                                // ✅ Use LazyLoadedList (Without Generics)
                                field.set(instance, new LazyLoadedList(idList, jedis, field.getAnnotation(PersistableListField.class).className()));
                            } else {
                                // ✅ Fully load objects immediately
                                List<Object> list = new ArrayList<>();
                                Class<?> listClass = Class.forName(field.getAnnotation(PersistableListField.class).className());
                                for (String id : idList) {
                                    if (!id.trim().isEmpty()) {
                                        Object listItem = listClass.getDeclaredConstructor().newInstance();
                                        Map<String, String> objectData = jedis.hgetAll(id);
                                        for (Field objectField : listClass.getDeclaredFields()) {
                                            objectField.setAccessible(true);
                                            String objectValue = objectData.get(objectField.getName());
                                            if (objectValue != null) {
                                                objectField.set(listItem, convertValue(objectField.getType(), objectValue));
                                            }
                                        }
                                        list.add(listItem);
                                    }
                                }
                                field.set(instance, list);
                            }
                        }
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getAllKeys() {
        return new ArrayList<>(jedis.keys("*")); // Fetch all stored object keys
    }

    private String getPersistableIdFieldName(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(PersistableId.class)) {
                return field.getName();
            }
        }
        throw new RuntimeException("No @PersistableId field found in class: " + clazz.getName());
    }

    private Object convertValue(Class<?> type, String value) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == String.class) {
            return value;
        }
        return null;
    }
}
