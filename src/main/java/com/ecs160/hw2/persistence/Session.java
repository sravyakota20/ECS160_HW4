package com.ecs160.hw2.persistence;

import com.ecs160.hw2.Post;
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
        } else {
        throw new IllegalArgumentException("Class is not persistable: " + obj.getClass().getName());
        }
    }

    public void persistAll() {
        for (Object obj : sessionObjects) {
            persistObject(obj);
        }
    }

    private void persistObject(Object obj) {
        Class<?> clazz = obj.getClass();
        String postKey = "post:" + getId(obj);

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(PersistableField.class) || field.isAnnotationPresent(PersistableId.class)) {
                    jedis.hset(postKey, field.getName(), String.valueOf(field.get(obj)));
                } else if (field.isAnnotationPresent(PersistableListField.class)) {
                    List<?> list = (List<?>) field.get(obj);
                    if (list != null) {
                        String ids = String.join(",", list.stream().map(item -> String.valueOf(getId(item))).toArray(String[]::new));
                        jedis.hset(postKey, "replyIds", ids);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private int getId(Object obj) {
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(PersistableId.class)) {
                    field.setAccessible(true);
                    return (int) field.get(obj);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("No @PersistableId found in " + obj.getClass().getName());
    }

    public Object load(Object obj) {
        String postKey = "post:" + getId(obj);
        Map<String, String> data = jedis.hgetAll(postKey);

        if (data.isEmpty()) return null;

        try {
            Class<?> clazz = obj.getClass();
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PersistableField.class) || field.isAnnotationPresent(PersistableId.class)) {
                    field.set(instance, castValue(field.getType(), data.get(field.getName())));
                } else if (field.isAnnotationPresent(PersistableListField.class)) {
                    List<Post> replies = new ArrayList<>();
                    if (data.containsKey("replyIds")) {
                        for (String id : data.get("replyIds").split(",")) {
                            if (!id.isEmpty()) {
                                Post reply = new Post();
                                reply.setPostId(Integer.parseInt(id));
                                replies.add(reply);
                            }
                        }
                    }
                    field.set(instance, replies);
                }
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

