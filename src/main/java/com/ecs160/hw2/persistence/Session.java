package com.ecs160.hw2;

import com.ecs160.hw2.persistence.Persistable;
import com.ecs160.hw2.persistence.PersistableField;
import com.ecs160.hw2.persistence.PersistableId;
import com.ecs160.hw2.persistence.PersistableListField;
import com.ecs160.hw2.persistence.LazyLoad;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import redis.clients.jedis.Jedis;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
                    String replyIdsStr = data.get("replyIds");
                    boolean isLazy = field.isAnnotationPresent(LazyLoad.class);
                    if (isLazy) {
                        List<Post> lazyProxy = createLazyLoadingProxy(replyIdsStr);
                        field.set(instance, lazyProxy);
                    } else {
                        List<Post> replies = new ArrayList<>();
                        if (replyIdsStr != null) {
                            for (String id : replyIdsStr.split(",")) {
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
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Post> createLazyLoadingProxy(String replyIdsStr) {
        final List<Post> delegate = new ArrayList<>();
        final String[] replyIds = (replyIdsStr == null || replyIdsStr.isEmpty()) ? new String[0] : replyIdsStr.split(",");
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[]{List.class});
        Class<?> proxyClass = factory.createClass();
        List<Post> proxyInstance = null;
        try {
            proxyInstance = (List<Post>) proxyClass.getDeclaredConstructor().newInstance();
            ((Proxy) proxyInstance).setHandler(new LazyListMethodHandler(delegate, replyIds, this.jedis));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proxyInstance;
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

    public Object loadLazy(Object obj) {
        // Optionally implement separate lazy load logic here
        return load(obj);
    }

    public void close() {
        jedis.close();
    }

    // ---------------------------
    // Lazy Loading Proxy Handler
    // ---------------------------
    private static class LazyListMethodHandler implements MethodHandler {
        private List<Post> delegate;
        private String[] replyIds;
        private boolean loaded = false;
        private Jedis jedis;

        public LazyListMethodHandler(List<Post> delegate, String[] replyIds, Jedis jedis) {
            this.delegate = delegate;
            this.replyIds = replyIds;
            this.jedis = jedis;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (!loaded) {
                // Load full reply objects from Redis for each reply id
                for (String id : replyIds) {
                    if (!id.isEmpty()) {
                        int replyId = Integer.parseInt(id);
                        String key = "post:" + replyId;
                        Map<String, String> data = jedis.hgetAll(key);
                        if (!data.isEmpty()) {
                            Post fullReply = new Post();
                            fullReply.setPostId(replyId);
                            fullReply.setRecord(new Record(data.get("postContent")));
                            fullReply.setCreatedAt(data.get("createdAt"));
                            // Optionally, load additional fields if necessary
                            delegate.add(fullReply);
                        }
                    }
                }
                loaded = true;
            }
            return thisMethod.invoke(delegate, args);
        }
    }
}
