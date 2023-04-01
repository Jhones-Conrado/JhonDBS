/*
 * Copyright (C) 2023 jhonessales
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.jhondbs.core.db.io;

import com.google.gson.Gson;
import java.util.*;

/**
 * Serializa e desserializa objetos.
 * @author jhonessales
 */
public class Serializator {
    

    public static <T> String serialize(T object) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("class", object.getClass().getName());

        if (object instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                jsonMap.put(entry.getKey().toString(), entry.getValue());
            }
        } else {
            for (java.lang.reflect.Field field : getAllFields(object.getClass())) {
                try {
                    field.setAccessible(true);
                    jsonMap.put(field.getName(), field.get(object));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error serializing object: " + e.getMessage(), e);
                }
            }
        }

        return new Gson().toJson(jsonMap);
    }

    public static <T> T deserialize(String json) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Map<String, Object> jsonMap = new Gson().fromJson(json, Map.class);
        Class<?> clazz = Class.forName(jsonMap.get("class").toString());
        Object object = clazz.newInstance();

        if (object instanceof Map) {
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                if (!entry.getKey().equals("class")) {
                    ((Map<String, Object>) object).put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            for (java.lang.reflect.Field field : getAllFields(clazz)) {
                if (jsonMap.containsKey(field.getName())) {
                    try {
                        field.setAccessible(true);
                        if(field.getType() == Short.TYPE){
                            System.out.println("Short TYPE");
                            Object getx = jsonMap.get(field.getName());
                            String of = String.valueOf(getx);
                            if(of.contains(".")){
                                of = of.substring(0, of.indexOf("."));
                            }
                            short get = Short.parseShort(of);
                            field.set(object, get);
                        } else if(field.getType() == Integer.TYPE){
                            System.out.println("Integer TYPE");
                            Object getx = jsonMap.get(field.getName());
                            String of = String.valueOf(getx);
                            if(of.contains(".")){
                                of = of.substring(0, of.indexOf("."));
                            }
                            int get = Integer.parseInt(of);
                            field.set(object, get);
                        } else if(field.getType() == Long.TYPE){
                            System.out.println("Long TYPE");
                            Object getx = jsonMap.get(field.getName());
                            String of = String.valueOf(getx);
                            if(of.contains(".")){
                                of = of.substring(0, of.indexOf("."));
                            }
                            long get = Long.parseLong(of);
                            field.set(object, get);
                        } else if(field.getType() == Float.TYPE){
                            System.out.println("Float TYPE");
                            Object getx = jsonMap.get(field.getName());
                            String of = String.valueOf(getx);
                            float get = Float.parseFloat(of);
                            field.set(object, get);
                        } else if(field.getType() == Double.TYPE){
                            System.out.println("Double TYPE");
                            Double get = (Double) jsonMap.get(field.getName());
                            field.set(object, get);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error deserializing object: " + e.getMessage(), e);
                    }
                }
            }
        }

        return (T) object;
    }

    private static List<java.lang.reflect.Field> getAllFields(Class<?> clazz) {
        List<java.lang.reflect.Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            fields.addAll(getAllFields(superClass));
        }
        return fields;
    }
    
}
