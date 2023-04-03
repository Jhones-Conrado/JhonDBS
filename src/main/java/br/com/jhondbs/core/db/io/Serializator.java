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

import br.com.jhondbs.core.db.base.Entidade;
import br.com.jhondbs.core.db.errors.DuplicatedUniqueField;
import br.com.jhondbs.core.db.errors.EntIdBadImplementation;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializa e desserializa objetos, mantendo as respectivas referências de classe,
 * isso torna possível serializar campos abstratos. <br/>
 * Todo o processo ignora completamente o SerialVersionUID.
 * @author jhonessales
 */
public class Serializator {
    
    private static final Gson gson = new Gson();
    
    public static <T> String serialize(T object) throws IllegalArgumentException, IllegalAccessException {
        
        if (object == null) {
            return null;
        }
        
        Map<String, Object> mapObj = new LinkedHashMap<>();
        Map<String, Object> map = new LinkedHashMap<>();
        
        Object ins = null;
        
        for(Field field : getAllFields(object.getClass())){
            if(isPrimitive(field)){
                map.put(field.getName(), toMap(field, object));
            } else {
                try {
                    ins = field.getType().newInstance();
                    if(ins instanceof Entidade){
                        Entidade get = (Entidade) field.get(object);
                        get.save();
                        map.put(field.getName(), get.getEnteId());
                    } else {
                        map.put(field.getName(), serialize(field.get(object)));
                    }
                } catch (InstantiationException | DuplicatedUniqueField | EntIdBadImplementation ex) {
//                    Logger.getLogger(Serializator.class.getName()).log(Level.SEVERE, null, ex);
                    map.put(field.getName(), serialize(field.get(object)));
                }
            }
        }
        
        mapObj.put(object.getClass().getName(), map);
        return gson.toJson(mapObj);
    }

    public static <T> T deserialize(String json) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Map<String, Object> jsonMap = gson.fromJson(json, Map.class);
        String clName = (String) Arrays.asList(jsonMap.keySet().toArray()).get(0);
        
        Class<?> forName = Class.forName(clName);
        Object instance = forName.newInstance();
        
        Map<String, Object> inner = (Map<String, Object>) jsonMap.get(clName);
        
        for(Field field : getAllFields(forName)){
            field.setAccessible(true);
            if(isPrimitive(field)){
                Map map = (Map) inner.get(field.getName());
                
                //Verificações necessárias para não dar erro na conversão de tipos numéricos.
                if(field.getType() == Short.TYPE){
                    String a = String.valueOf(map.get(field.getType().getName()));
                    if(a.contains(".")){
                        a = a.substring(0, a.indexOf("."));
                    }
                    short get = Short.parseShort(a);
                    field.set(instance, get);
                } else if(field.getType() == Integer.TYPE){
                    String a = String.valueOf(map.get(field.getType().getName()));
                    if(a.contains(".")){
                        a = a.substring(0, a.indexOf("."));
                    }
                    int get = Integer.parseInt(a);
                    field.set(instance, get);
                } else if(field.getType() == Long.TYPE){
                    String a = String.valueOf(map.get(field.getType().getName()));
                    if(a.contains(".")){
                        a = a.substring(0, a.indexOf("."));
                    }
                    long get = Long.parseLong(a);
                    field.set(instance, get);
                } else if(field.getType() == Float.TYPE){
                    String a = String.valueOf(map.get(field.getType().getName()));
                    float get = Float.parseFloat(a);
                    field.set(instance, get);
                } else if(field.getType() == Double.TYPE){
                    String a = String.valueOf(map.get(field.getType().getName()));
                    double get = Double.parseDouble(a);
                    field.set(instance, get);
                } else {
                    field.set(instance, map.get(field.getType().getName()));
                }
                
            } else {
                
                try {
                    Object ins = field.getType().newInstance();
                    if(ins instanceof Entidade){
                        Entidade e = (Entidade) ins;
                        String get = String.valueOf(inner.get(field.getName()));
                        long id = Long.parseLong(get.substring(0, get.indexOf(".")));
                        field.set(instance, e.load(id));
                    } else {
                        field.set(instance, deserialize((String) inner.get(field.getName())));
                    }
                } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException e) {
                    field.set(instance, deserialize((String) inner.get(field.getName())));
                }
            }
        }
        
        return (T) instance;
    }
    
    private static Map toMap(Field field, Object obj){
        Map map = new LinkedHashMap();
        field.setAccessible(true);
        
        if(isPrimitive(field)){
            try {
                map.put(field.getType().getName(), field.get(obj));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Serializator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            map.put(field.getType().getName(), toMap(field, obj));
        }
        return map;
    }
    
    private static boolean isPrimitive(Field field){
        return !(field.getType() != Short.TYPE &&
            field.getType() != Integer.TYPE &&
            field.getType() != Long.TYPE &&
            field.getType() != Byte.TYPE &&
            field.getType() != Float.TYPE &&
            field.getType() != Double.TYPE &&
            field.getType() != Boolean.TYPE &&
            field.getType() != String.class);
    }
    
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while(clazz != null){
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
    
}
