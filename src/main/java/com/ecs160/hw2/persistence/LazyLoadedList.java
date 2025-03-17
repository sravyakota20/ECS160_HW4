    package com.ecs160.hw2.persistence;

    import java.lang.reflect.Field;
    import java.util.*;
    import redis.clients.jedis.Jedis;

    public class LazyLoadedList extends AbstractList<Object> {
        private List<String> ids;
        private List<Object> loadedObjects;
        private Jedis jedis;
        private String className;

        public LazyLoadedList(List<String> ids, Jedis jedis, String className) {
            this.ids = ids;
            this.jedis = jedis;
            this.className = className;
            this.loadedObjects = null; // ✅ Objects are not loaded yet
        }

        private void loadObjects() {
            if (loadedObjects == null) {
                System.out.println("Lazy-loading objects for: " + className); // Debugging
                loadedObjects = new ArrayList<>();
                try {
                    Class<?> listClass = Class.forName(className);
                    for (String id : ids) {
                        if (!id.trim().isEmpty()) {
                            Object obj = listClass.getDeclaredConstructor().newInstance();
                            Map<String, String> data = jedis.hgetAll(id);
                            for (Field field : listClass.getDeclaredFields()) {
                                field.setAccessible(true);
                                String value = data.get(field.getName());
                                if (value != null) {
                                    field.set(obj, convertValue(field.getType(), value));
                                }
                            }
                            loadedObjects.add(obj);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Object get(int index) {
            loadObjects(); // ✅ Load objects only when accessed
            return loadedObjects.get(index);
        }

        @Override
        public int size() {
            return ids.size();
        }

        private Object convertValue(Class<?> type, String value) {
            if (type == Integer.class || type == int.class) {
                return Integer.parseInt(value);
            } else if (type == String.class) {
                return value;
            }
            return null; // Modify this for additional types if needed
        }
    }
