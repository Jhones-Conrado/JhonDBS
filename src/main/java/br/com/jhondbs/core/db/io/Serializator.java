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

import br.com.jhondbs.core.db.errors.DuplicatedUniqueField;
import br.com.jhondbs.core.db.errors.EntIdBadImplementation;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.*;
import br.com.jhondbs.core.db.base.Entity;
import com.google.gson.internal.LinkedTreeMap;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * ENGLISH<br>
 * Serializing and deserializing objects while maintaining their respective class
 * references makes it possible to serialize abstract fields.<br>
 * The whole process completely ignores the SerialVersionUID.
 * <br><br>
 * PORTUGUÊS<br>
 * Serializa e desserializa objetos, mantendo as respectivas referências de classe,
 * isso torna possível serializar campos abstratos. <br>
 * Todo o processo ignora completamente o SerialVersionUID.
 * @author jhonessales
 */
public class Serializator {
    
    private static final Gson gson = new Gson();
    
    /**
     * Serializes an object to a JSON map that maintains the class reference of
     * all objects and fields, ignoring the SerialVersionUID.<br><br>
     * Serializa um objeto para um mapa JSON que mantém a referência de classe de todos
     * os objetos e campos, ignorando o SerialVersionUID.
     * @param <T>
     * @param object
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     * @throws br.com.jhondbs.core.db.errors.DuplicatedUniqueField 
     * @throws br.com.jhondbs.core.db.errors.EntIdBadImplementation 
     */
    public static <T> String serialize(T object) throws IllegalArgumentException, IllegalAccessException, DuplicatedUniqueField, EntIdBadImplementation {
        
        if (object == null) {
            return null;
        }
        
        if(Reflection.isInstance(object.getClass(), Date.class)){
            return serializeDate((Date) object);
        } else if(Reflection.isInstance(object.getClass(), Calendar.class)){
            return serializeCalendar((Calendar) object);
        } else if(Reflection.isInstance(object.getClass(), Number.class)){
            return serializeNumber((Number) object);
        } else if(Reflection.isInstance(object.getClass(), String.class)){
            return serializeString((String) object);
        } else if(Reflection.isInstance(object.getClass(), Boolean.class)){
            return serializeBoolean((Boolean) object);
        } else if(Reflection.isInstance(object.getClass(), Byte.class)){
            return serializeByte((Byte) object);
        } else if(Reflection.isInstance(object.getClass(), Entity.class)){
            return serializeEntity((Entity) object);
        } else if(Reflection.isInstance(object.getClass(), List.class)){
            return serializeList((List) object);
        } else if(Reflection.isInstance(object.getClass(), Set.class)){
            return serializeSet((Set) object);
        } else if(Reflection.isInstance(object.getClass(), Properties.class)){
            return serializeProperties((Properties) object);
        } else {
            return serializeObject(object);
        }
    }
    
    /**
     * It receives a JSON of maps with objects, fields and their respective class
     * and type references and deserializes it, returning the created object.
     * <br><br>
     * Recebe um JSON de mapas com objetos, campos e suas respectivas referências
     * de classe e tipo e desserializa, retornando o objeto criado.
     * @param <T>
     * @param json
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException 
     */
    public static <T> T deserialize(String json) throws ClassNotFoundException, IllegalAccessException, InstantiationException, Exception {
        if(!json.isBlank()){
            
            Map<String, Object> jsonMap = gson.fromJson(json, Map.class);
            String clName = jsonMap.keySet().iterator().next();
            
            if(Reflection.isInstance(Class.forName(clName), Date.class)){
                return (T) deserializeDate(json);
            } else if(Reflection.isInstance(Class.forName(clName), Calendar.class)){
                return (T) deserializeCalendar(json);
            } else if(Reflection.isInstance(Class.forName(clName), Number.class)){
                return (T) deserializeNumber(json);
            } else if(Reflection.isInstance(Class.forName(clName), String.class)){
                return (T) deserializeString(json);
            } else if(Reflection.isInstance(Class.forName(clName), Boolean.class)){
                return (T) deserializeBoolean(json);
            } else if(Reflection.isInstance(Class.forName(clName), Byte.class)){
                return (T) deserializeByte(json);
            } else if(Reflection.isInstance(Class.forName(clName), Entity.class)){
                return (T) deserializeEntity(json);
            } else if(Reflection.isInstance(Class.forName(clName), List.class)){
                return (T) deserializeList(json);
            } else if(Reflection.isInstance(Class.forName(clName), Set.class)){
                return (T) deserializeSet(json);
            } else if(Reflection.isInstance(Class.forName(clName), Properties.class)){
                return (T) deserializeProperties(json);
            } else {
                return (T) deserializeObject(json);
            }
        }
        throw new Exception("Blank JSON");
    }
    
    private static String serializeDate(Date date){
        Map<String, Long> map = new HashMap<>();
        map.put(date.getClass().getName(), date.getTime());
        return gson.toJson(map);
    }
    
    private static Date deserializeDate(String json) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        Map map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        Double time = (Double) map.get(className);
        Date instance = (Date) Class.forName(className).newInstance();
        instance.setTime(time.longValue());
        return instance;
    }
    
    private static String serializeCalendar(Calendar calendar){
        Map<String, Long> map = new HashMap<>();
        map.put(calendar.getClass().getName(), calendar.getTimeInMillis());
        return gson.toJson(map);
    }
    
    private static Calendar deserializeCalendar(String json) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        Map map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        Double time = (Double) map.get(className);
        Calendar instance = (Calendar) Class.forName(className).newInstance();
        instance.setTimeInMillis(time.longValue());
        return instance;
    }
    
    private static String serializeNumber(Number number){
        Map<String, String> map = new HashMap<>();
        map.put(number.getClass().getName(), number.toString());
        return gson.toJson(map);
    }
    
    private static Number deserializeNumber(String json) throws ClassNotFoundException{
        Map map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        String textNumber = (String) map.get(className);
        
        Class<?> clazz = Class.forName(className);
        
        if(Reflection.isInstance(clazz, BigInteger.class)){
            return new BigInteger(textNumber);
        } else if(Reflection.isInstance(clazz, BigDecimal.class)){
            return new BigDecimal(textNumber);
        }
        
        Double number = Double.valueOf(textNumber);
        if(Reflection.isInstance(clazz, Short.class)){
            return number.shortValue();
        } else if(Reflection.isInstance(clazz, Integer.class)){
            return number.intValue();
        } else if(Reflection.isInstance(clazz, Long.class)){
            return number.longValue();
        } else if(Reflection.isInstance(clazz, Float.class)){
            return number.floatValue();
        }
        return number;
    }
    
    private static String serializeString(String string){
        Map<String, String> map = new HashMap<>();
        map.put(string.getClass().getName(), string);
        return gson.toJson(map);
    }
    
    private static String deserializeString(String json){
        Map map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        return (String) map.get(className);
    }
    
    private static String serializeBoolean(Boolean bool){
        Map<String, Boolean> map = new HashMap<>();
        map.put(bool.getClass().getName(), bool);
        return gson.toJson(map);
    }
    
    private static Boolean deserializeBoolean(String json){
        Map map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        return (Boolean) map.get(className);
    }
    
    private static String serializeByte(Byte b){
        Map<String, Byte> map = new HashMap<>();
        map.put(b.getClass().getName(), b);
        return gson.toJson(map);
    }
    
    private static Byte deserializeByte(String json){
        Map map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        return (Byte) map.get(className);
    }
    
    private static String serializeEntity(Entity entity) throws IllegalArgumentException, IllegalAccessException, DuplicatedUniqueField, EntIdBadImplementation{
        return serializeObject(entity);
    }
    
    private static Entity deserializeEntity(String json) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception{
        return deserializeObject(json);
    }
    
    private static String serializeObject(Object obj) throws IllegalArgumentException, IllegalAccessException, DuplicatedUniqueField, EntIdBadImplementation{
        Map<String, Object> mapObj = new LinkedHashMap<>();
        Map<String, Object> map = new LinkedHashMap<>();
        for(Field field : getAllFields(obj.getClass())){
            if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())){
                field.setAccessible(true);
                if(Reflection.isInstance(field.getType(), Entity.class)){
                    Entity fieldEnte = (Entity) field.get(obj);
                    fieldEnte.save();
                    map.put(field.getName(), fieldEnte.getEnteId());
                } else {
                    map.put(field.getName(), serialize(field.get(obj)));
                }
            }
        }
        mapObj.put(obj.getClass().getName(), map);
        return gson.toJson(mapObj);
    }
    
    private static <T> T deserializeObject(String json) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception{
        Map map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        Class<?> entityClass = Class.forName(className);
        T instance = (T) new Reflection().getNewInstance(entityClass);
        Map fields = (Map) map.get(className);
        List<Field> allFields = getAllFields(entityClass);
        for(Field field : allFields){
            field.setAccessible(true);
            if(fields.containsKey(field.getName())){
                if(Reflection.isInstance(field.getType(), Entity.class)){
                    Entity e = (Entity) new Reflection().getNewInstance(field.getType());
                    Entity load = e.load(((Number) fields.get(field.getName())).longValue());
                    field.set(instance, load);
                } else if(Reflection.isInstance(field.getType(), List.class)){
                    field.set(instance, deserializeList((String) fields.get(field.getName())));
                } else if(Reflection.isInstance(field.getType(), Set.class)){
                    field.set(instance, deserializeSet((String) fields.get(field.getName())));
                } else {
                    field.set(instance, deserialize((String) fields.get(field.getName())));
                }
            }
        }
        return (T) instance;
    }
    
    private static String serializeList(List list) throws IllegalArgumentException, IllegalAccessException, DuplicatedUniqueField, EntIdBadImplementation{
        List<String> backList = new ArrayList<>();
        for(Object object : list){
            if(Reflection.isInstance(object.getClass(), Entity.class)){
                Entity castEntity = (Entity) object;
                castEntity.save();
                Map<String, Long> castEntityMap = new LinkedHashMap<>();
                castEntityMap.put(castEntity.getClass().getName(), castEntity.getEnteId());
                backList.add("->"+gson.toJson(castEntityMap));
            } else {
                backList.add(serialize(object));
            }
        }
        Map<String, List<String>> backMap = new HashMap<>();
        backMap.put(list.getClass().getName(), backList);
        return gson.toJson(backMap);
    }
    
    private static List<Object> deserializeList(List<String> list) throws IllegalAccessException, InstantiationException, Exception{
        List<Object> backList = new ArrayList<>();
        for(String s : list){
            if(s.startsWith("->")){
                Map<String, Long> mat = gson.fromJson(s.substring(2), Map.class);
                String clName = mat.keySet().iterator().next();
                Class<?> forName = Class.forName(clName);
                Long id = ((Number) mat.get(clName)).longValue();
                Entity load = (Entity) IO.load((Entity) new Reflection().getNewInstance(forName), id);
                backList.add(load);
            } else {
                backList.add(deserialize(s));
            }
        }
        return backList;
    }
    
    private static List<Object> deserializeList(String json) throws InstantiationException, Exception{
        Map<String, List<String>> map = gson.fromJson(json, Map.class);
        String className = (String) map.keySet().iterator().next();
        List<String> list = map.get(className);
        return deserializeList(list);
    }
    
    private static String serializeSet(Set set) throws DuplicatedUniqueField, EntIdBadImplementation, IllegalArgumentException, IllegalAccessException{
        return serializeList(Arrays.asList(set.toArray()));
    }
    
    private static Set deserializeSet(String json) throws Exception{
        return new HashSet(deserializeList(json));
    }
    
    private static String serializeProperties(Properties properties){
        Map<String, Properties> map = new HashMap<>();
        map.put(properties.getClass().getName(), properties);
        return gson.toJson(map);
    }
    
    private static Properties deserializeProperties(String json){
        Map<String, Object> map = gson.fromJson(json, Map.class);
        LinkedTreeMap<Object, Object> tree = (LinkedTreeMap<Object, Object>) map.get(map.keySet().iterator().next());
        Properties back = new Properties();
        tree.keySet().forEach(key -> {
            back.put(key, tree.get(key));
        });
        return back;
    }
    
    /**
     * Ensures that all fields of the class and its superclasses are returned.
     * Garante que todos os campos da classe e suas superclasses sejam retornados.
     * @param clazz
     * @return 
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while(clazz != null){
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
    
}
