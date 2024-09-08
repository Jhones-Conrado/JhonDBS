/*
 * The MIT License
 *
 * Copyright 2024 Jhones Sales.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.com.jhondbs.core.db.capsule;

import br.com.jhondbs.core.db.FileManager;
import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.tools.Reflection;
import br.com.jhondbs.core.tools.ClassDictionary;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsável por transformar um objeto em texto para armazenar no banco de dados e vice-versa.
 * @author Jhones Conrado
 */
public class Capsule {
    
    /**
     * Diretório raiz do banco de dados.
     */
    private static final String rootDirectory = "./db/";
    
    private Set<String> toLock = new HashSet<>();
    
    
    private Set<Entity> serializados = new HashSet<>();
    private Map<String, Capsule> capsules = new HashMap<>();
    private final Map<String, Entity> recovereds = new HashMap<>();
    private Entity entity;
    
    private String str;
    
    private Set<String> referencias = new HashSet<>();
    private Set<String> fields = new HashSet<>();
    
    private ClassLoader loader;
    
    public Capsule(Entity entity) throws Exception {
        this.entity = entity;
        loadRefs();
    }
    
    public Capsule(Class entityClass, String id) throws Exception {
        this.entity = load(entityClass, id);
        loadRefs();
    }
    
    public Capsule(Class entityClass, String id, ClassLoader loader) throws Exception {
        this.loader = loader;
        this.entity = load(entityClass, id, loader);
        loadRefs();
    }
    
    private Capsule(Entity entity, Set<Entity> serializados, Map<String, Capsule> capsules) throws Exception {
        this.entity = entity;
        this.serializados = serializados;
        this.capsules = capsules;
        loadRefs();
    }
    
    public Capsule(String str) throws Exception {
        this.str = str;
    }
    
    public String start() throws IOException, Exception {
        if(this.entity == null) {
            throw new NullPointerException("Entidade nula para serialização.");
        }
        this.entity.getId();
        serializados.add(entity);
        Encapsule(entity);
        capsules.put(entity.getId(), this);
        return entity.getId();
    }
    
    private void putRef(Entity entity) throws Exception {
        referencias.add(String.valueOf(ClassDictionary.getIndex(entity.getClass()))+":"+entity.getId());
    }
    
    private void removeRef(Entity entity) throws Exception {
        referencias.remove(String.valueOf(ClassDictionary.getIndex(entity.getClass()))+":"+entity.getId());
    }
    
    private String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(entity.getClass())))
                .append(":");
        for(String s : fields) {
            sb.append(s);
        }
        sb.append("}");
        
        sb.append("ref::");
        for(String s : referencias) {
            sb.append(s).append("::");
        }
        return sb.toString();
    }

    private void loadRefs() throws Exception {
        File file = new File(getPath(entity));
        if(file.exists()) {
            String line = Files.readString(file.toPath());
            if(line.contains("ref::")) {
                line = line.substring(line.indexOf("ref::")+"ref::".length());
                if(line.contains("::")) {
                    String[] split = line.split("::");
                    referencias.addAll(Arrays.asList(split));
                }
            }
        }
    }
    
    private Set<String> loadRefs(Entity entity) throws Exception {
        Set<String> ids = new HashSet<>();
        File file = new File(getPath(entity));
        if(file.exists()) {
            String line = Files.readString(file.toPath());
            if(line.contains("ref::")) {
                line = line.substring(line.indexOf("ref::")+"ref::".length());
                if(line.contains("::")) {
                    String[] split = line.split("::");
                    ids.addAll(Arrays.asList(split));
                }
            }
        }
        return ids;
    }
    
    private void writeNewState() throws Exception {
        for(Capsule c : capsules.values()) {
            File file = new File(getNewPath(c.entity));
            try(BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                w.write(c.build());
                w.flush();
            }
        }
    }
    
    private void makeBackUp() throws Exception {
        for(Capsule c : capsules.values()) {
            File file = new File(getPath(c.entity));
            if(file.exists()) {
                file.renameTo(new File(getOldPath(c.entity)));
            }
        }
    }
    
    private void applyChanges() throws Exception {
        for(Capsule c : capsules.values()) {
            File file = new File(getNewPath(c.entity));
            if(file.exists()) {
                file.renameTo(new File(getPath(c.entity)));
            }
        }
    }
    
    private void clearDb() throws Exception {
        for(Capsule c : capsules.values()) {
            File file = new File(getNewPath(c.entity));
            if(file.exists()) {
                file.delete();
            }
            File f = new File(getOldPath(c.entity));
            if(f.exists()) {
                f.delete();
            }
        }
    }
    
    private void recoverBackUp() throws Exception {
        for(Capsule c : capsules.values()) {
            File file = new File(getPath(c.entity));
            File f = new File(getOldPath(c.entity));
            if(file.exists() && f.exists()) {
                file.delete();
                f.renameTo(file);
            }
        }
    }
    
    private void clearRefs() throws Exception {
        File file = new File(getPath(entity));
        if(file.exists()) {
            
        }
    }
    
    private void lock() throws Exception {
        if(toLock.isEmpty()) {
            Set<String> idsToLock = new HashSet<>();
            Map<String, Entity> recu = recursiveEntities();
            idsToLock.addAll(recu.keySet());

            for(Entity e : recu.values()) {
                Set<String> refs = loadRefs(e);
                for(String ref : refs) {
                    idsToLock.add(ref.substring(ref.indexOf(":")+1));
                }
            }
        }
        List<String> list = toLock.stream().toList();
        FileManager.getInstance().lockWrite(list);
    }
    
    private void unlock() {
        List<String> list = toLock.stream().toList();
        FileManager.getInstance().unlockWrite(list);
    }
    
    public boolean flush() throws DuplicatedUniqueFieldException, Exception {
        initDb();
        
        try {
            lock();
            writeNewState();
            makeBackUp();
            applyChanges();
            clearDb();
            return true;
        } catch (Exception e) {
            recoverBackUp();
            clearDb();
            return false;
        } finally {
            unlock();
        }
    }
    
    private String Encapsule(Entity entity) throws IOException, Exception {
        if(!uniqueFieldTest(entity)) {
            throw new DuplicatedUniqueFieldException("Entidade com campo único duplicado: "+entity);
        } else {
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(entity.getClass())))
                .append(":");
        List<Field> fields = FieldsManager.getAllFields(entity.getClass());
        for(Field field : fields) {
            if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())){
                field.setAccessible(true);
                Object value = FieldsManager.getValue(field, entity);
                if(value != null) {
                    if(ClassDictionary.getIndex(value.getClass()) != -1 || Reflection.isArrayMap(field.getType())) {
                        StringBuilder fBuilder = new StringBuilder();
                        sb.append("{")
                                .append(field.getName())
                                .append(":");
                        
                        fBuilder.append("{")
                                .append(field.getName())
                                .append(":");
                        if(value.getClass().isEnum()) {
                            sb.append(encapsuleEnum((Enum) value));
                            fBuilder.append(encapsuleEnum((Enum) value));
                        } else {
                            if(Reflection.isPrimitive(field.getType()) || Reflection.isNumerical(field.getType()) || Reflection.isDate(field.getType())) {
                                sb.append(encapsulePrimitive(value));
                                fBuilder.append(encapsulePrimitive(value));
                            } else if(Reflection.isArrayMap(field.getType())) {
                                if(Reflection.isInstance(value.getClass(), List.class)) {
                                    List l = (List) value;
                                    if(!l.isEmpty()) {
                                        String s = encapsuleArray(value, entity);
                                        sb.append(s);
                                        fBuilder.append(s);
                                    } else {
                                        String indice = String.valueOf(ClassDictionary.getIndex(List.class));
                                        sb.append("{list:{}}");
                                        fBuilder.append("{list:{}}");
                                    }
                                } else if(Reflection.isInstance(value.getClass(), Map.class)) {
                                    Map m = (Map) value;
                                    if(!m.isEmpty()) {
                                        String s = encapsuleArray(value, entity);
                                        sb.append(s);
                                        fBuilder.append(s);
                                    } else {
                                        String indice = String.valueOf(ClassDictionary.getIndex(Map.class));
                                        sb.append("{map:{}}");
                                        fBuilder.append("{map:{}}");
                                    }
                                }
                            } else if(Reflection.isInstance(field.getType(), Entity.class)) {
                                Entity ente = (Entity) value;
                                if(!serializados.contains(ente)) {
                                    Capsule capsule = new Capsule(ente, serializados, capsules);
                                    capsule.putRef(entity);
                                    try {
                                        capsule.start();
                                    } catch (Exception ex) {
                                        throw ex;
                                    }
                                }
                                sb.append(encapsuleId(ente));
                                fBuilder.append(encapsuleId(ente));
                            } else {
                                String s = encapsuleObject(value, entity);
                                sb.append(s);
                                fBuilder.append(s);
                            }
                        }
                        sb.append("}");
                        fBuilder.append("}");
                        this.fields.add(fBuilder.toString());
                    }
                }
            }
        }
        sb.append("}");
        str = sb.toString();
        return entity.getId();
    }
    
    private String encapsuleEnum(Enum e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(e.getClass())))
                .append(":")
                .append(e.toString())
                .append("}");
        return sb.toString();
    }
    
    private String encapsulePrimitive(Object object) {
        if(object == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(object.getClass())))
                .append(":")
                .append(object.toString())
                .append("}");
        return sb.toString();
    }
    
    private String encapsuleObject(Object object, Entity ref) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(object.getClass())))
                .append(":");

        List<Field> fields = FieldsManager.getAllFields(object.getClass());
        for(Field field : fields) {
            if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())){
                field.setAccessible(true);
                Object value = FieldsManager.getValue(field, object);
                if(value != null) {
                    sb.append("{")
                            .append(field.getName())
                            .append(":");
                    if(value.getClass().isEnum()) {
                        sb.append(encapsuleEnum((Enum) value));
                    }else if(Reflection.isPrimitive(field.getType()) || Reflection.isNumerical(field.getType()) || Reflection.isDate(field.getType())) {
                        sb.append(encapsulePrimitive(value));
                    } else if(Reflection.isArrayMap(field.getType())) {
                        sb.append(encapsuleArray(value, ref));
                    } else if(Reflection.isInstance(field.getType(), Entity.class)) {
                        Entity ente = (Entity) value;
                        if(!serializados.contains(ente)) {
                            Capsule capsule = new Capsule(ente, serializados, capsules);
                            capsule.putRef(ref);
                            capsule.start();
                        }
                        sb.append(encapsuleId(ente));
                    } else {
                        sb.append(encapsuleObject(value, ref));
                    }
                    sb.append("}");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Encapsula um array que pode ser do tipo lista ou mapa.
     * @param object Lista ou mapa para encapsular.
     * @return String da lista ou mapa encapsulado.
     */
    private String encapsuleArray(Object object, Entity ref) throws Exception {
        if(object == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        if(Reflection.isInstance(object.getClass(), List.class) || Reflection.isInstance(object.getClass(), Set.class) || object.getClass().getName().contains("[")) {
            sb.append("{")
                .append("list")
                .append(":");
        } else if (Reflection.isInstance(object.getClass(), Map.class)) {
            sb.append("{")
                .append("map")
                .append(":");
        }
        if(Reflection.isInstance(object.getClass(), List.class) || Reflection.isInstance(object.getClass(), Set.class) || object.getClass().getName().contains("[")) {
            List list = asList(object);
            for(Object obj : list) {
                if(Reflection.isPrimitive(obj) || Reflection.isNumerical(obj.getClass()) || Reflection.isDate(obj.getClass())) {
                    sb.append(encapsulePrimitive(obj));
                } else if(Reflection.isArrayMap(obj)) {
                    sb.append(encapsuleArray(obj, ref));
                } else if(Reflection.isInstance(obj.getClass(), Entity.class)) {
                    Entity ente = (Entity) obj;
                    if(!serializados.contains(ente)) {
                        Capsule capsule = new Capsule(ente, serializados, capsules);
                        capsule.putRef(ref);
                        capsule.start();
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsuleObject(obj, ref));
                }
            }
        } else if(Reflection.isInstance(object.getClass(), Map.class)) {
            Map map = (Map) object;
            Set keys = map.keySet();
            for(Object key : keys) {
                if(Reflection.isPrimitive(key) || Reflection.isNumerical(key.getClass()) || Reflection.isDate(key.getClass())) {
                    sb.append(encapsulePrimitive(key));
                } else if(Reflection.isArrayMap(key)) {
                    sb.append(encapsuleArray(key, ref));
                } else if(Reflection.isInstance(key.getClass(), Entity.class)) {
                    Entity ente = (Entity) key;
                    if(!serializados.contains(ente)) {
                        Capsule capsule = new Capsule(ente, serializados, capsules);
                        capsule.putRef(ref);
                        capsule.start();
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsuleObject(key, ref));
                }
                if(Reflection.isPrimitive(map.get(key)) || Reflection.isNumerical(map.get(key).getClass()) || Reflection.isDate(map.get(key).getClass())) {
                    sb.append(encapsulePrimitive(map.get(key)));
                } else if(Reflection.isArrayMap(map.get(key))) {
                    sb.append(encapsuleArray(map.get(key), ref));
                } else if(Reflection.isInstance(map.get(key).getClass(), Entity.class)) {
                    Entity ente = (Entity) map.get(key);
                    if(!serializados.contains(ente)) {
                        Capsule capsule = new Capsule(ente, serializados, capsules);
                        capsule.putRef(ref);
                        capsule.start();
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsuleObject(map.get(key), ref));
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    private String encapsuleId(Entity entity) throws Exception {
        if(entity == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(entity.getClass())))
                .append(":")
                .append(entity.getId())
                .append("}");
        return sb.toString();
    }
    
    public String getCapsule() {
        return this.str;
    }
    
    private List asList(Object object) {
        
        try {
            return (List) object;
        } catch (Exception e) {
            try {
                return Arrays.asList(object);
            } catch (Exception ex) {
            }
        }
        return new ArrayList();
    }
    
    public <T extends Entity> T recover() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ParseException, Exception {
        if(this.entity != null) {
            return (T) this.entity;
        }
        T ente = (T) recover(str, recovereds, this.getClass().getClassLoader());
        this.entity = ente;
        loadRefs();
        return ente;
    }
    
    public <T extends Entity> T recover(ClassLoader loader) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ParseException, Exception {
        if(this.entity != null) {
            return (T) this.entity;
        }
        this.entity = recover(str, recovereds, loader);
        loadRefs();
        return (T) this.entity;
    }
    
    private <T> T recover(String str, Map<String, Entity> recovereds, ClassLoader loader) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, Exception {
        Class<?> type = getType(str);
        Entity root = (Entity) Reflection.getNewInstance(type, loader);
        List<String> fields = getFields(str);
        Field fieldId = FieldsManager.getFieldId(root.getClass());
        for (String field : fields) {
            if (recoverFieldName(field).contains(fieldId.getName())) {
                injectField(root, field, recovereds, loader);
                fields.remove(field);
                recovereds.put(root.getId(), root);
                break;
            }
        }
        for (String field : fields) {
            injectField(root, field, recovereds, loader);
        }
        return (T) root;
    }

    private Map<String, Object> recoverOnlyFields(Class entityClass, String id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String capsule = Files.readString(Paths.get(getPath(entityClass, id)));
        List<String> capsules = getFields(capsule);
        for(String field : capsules) {
            map.put(recoverFieldName(field), recoverValueCapsule(recoverValueCapsule(field)));
        }
        return map;
    }
    
    private Map<String, String> recoverOnlyUniqueFields(Class entityClass, String id) throws Exception {
        Map<String, String> map = new HashMap<>();
        String capsule = Files.readString(Paths.get(getPath(entityClass, id)));
        List<String> fieldsCapsules = getFields(capsule);
        List<Field> fields = FieldsManager.getFieldsUnique(FieldsManager.getAllFields(entityClass));
        List<String> achados = new ArrayList<>();
        for(Field field : fields) {
            for(String fieldCapsule : fieldsCapsules) {
                if(field.getName().equals(recoverFieldName(fieldCapsule))) {
                    achados.add(fieldCapsule);
                }
            }
        }
        for(String fieldCapsule : achados) {
            map.put(recoverFieldName(fieldCapsule), recoverValueCapsule(recoverValueCapsule(fieldCapsule)));
        }
        return map;
    }
    
    private void injectField(Object object, String fieldCapsule, Map<String, Entity> recovereds, ClassLoader loader) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ParseException, Exception {
        String name = recoverFieldName(fieldCapsule);
        String valueCapsule = recoverValueCapsule(fieldCapsule);
        Class<?> type = Class.forName(getType(valueCapsule).getName(), true, loader);
        if (type.isEnum()) {
            @SuppressWarnings("unchecked")
            Enum<?> enumValue = Enum.valueOf((Class<Enum>) type, recoverValueCapsule(valueCapsule));
            FieldsManager.setValue(name, object, enumValue);
        } else {
            String value = recoverValueCapsule(valueCapsule);
            Object recoveredValue = recoverFromCapsule(valueCapsule, recovereds, loader);
            FieldsManager.setValue(name, object, recoveredValue);
        }
    }
    
    private <T> T recoverFromCapsule(String capsule, Map<String, Entity> recovereds, ClassLoader loader) throws Exception {
        Class<?> type = Class.forName(getType(capsule).getName(), true, loader);
        String value = recoverValueCapsule(capsule);
        if (Reflection.isPrimitive(type) || Reflection.isNumerical(type)) {
            return (T) parsePrimitiveFromString(value, type, loader);
        } 
        else if (Reflection.isDate(type)) {
            if (Reflection.isInstance(type, Date.class)) {
                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                Class<?> formatterClass = Class.forName(formatter.getClass().getName(), true, loader);
                Constructor<?> constructor = formatterClass.getConstructor(String.class, Locale.class);
                SimpleDateFormat form = (SimpleDateFormat) constructor.newInstance("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                return (T) form.parse(value);
            } 
            else if (Reflection.isInstance(type, Calendar.class)) {
                return (T) parseCalendarFromString(value, loader);
            } 
            else if (Reflection.isInstance(type, Temporal.class)) {
                return (T) parseDateTimeFromString(value, type, loader);
            } 
            else if (Reflection.isInstance(type, Period.class)) {
                return (T) parsePeriodFromString(value, loader);
            }
        } 
        else if (Reflection.isInstance(type, Entity.class)) {
            if (recovereds.containsKey(value)) {
                return (T) recovereds.get(value);
            } else {
                String path = getPath(type, value);
                return (T) recover(Files.readString(Paths.get(path)), recovereds, loader);
            }
        } 
        else if (Reflection.isInstance(type, List.class)) {
            return (T) parseListFromString(capsule, recovereds, loader);
        } 
        else if (Reflection.isInstance(type, Map.class)) {
            return (T) parseMapFromString(value, recovereds, loader);
        } 
        else {
            if (type.isEnum()) {
                return (T) Enum.valueOf((Class<Enum>) type, value);
            }
            Object o = Reflection.getNewInstance(type, loader);
            List<String> fields = getFields(capsule);
            for (String field : fields) {
                injectField(o, field, recovereds, loader);
            }
            return (T) o;
        }
        throw new Exception("Capsula não identificada.");
    }

    private String recoverFieldName(String fieldCapsule) {
        if(fieldCapsule != null && !fieldCapsule.isBlank() && fieldCapsule.contains(":")) {
            int end = fieldCapsule.indexOf(":");
            return fieldCapsule.substring(1, end);
        }
        throw new NullPointerException("String de capsula com erro ou nula.");
    }
    
    private String recoverValueCapsule(String fieldCapsule) {
        if(fieldCapsule != null && !fieldCapsule.isBlank() && fieldCapsule.contains(":")) {
            int init = fieldCapsule.indexOf(":") + 1;
            return fieldCapsule.substring(init, fieldCapsule.length()-1);
        }
        throw new NullPointerException("String de capsula com erro ou nula.");
    }
    
    private Class getType(String str) {
        int pointer = str.indexOf(":");
        String type = str.substring(1, pointer);
        return switch (type) {
            case "list" -> List.class;
            case "map" -> Map.class;
            default -> ClassDictionary.fromIndex(Integer.parseInt(type));
        };
    }
    
    private List<String> getFields(String str) {
        if(str == null || str.isBlank()) {
            return null;
        }
        List<String> fields = new ArrayList<>();
        if(str.equals("{}")) {
            return fields;
        }
        int init = str.indexOf(":") + 1;
        String split = str.substring(init);
        while(split.contains("{") && split.contains("}")) {
            int a = split.indexOf("{");
            int b = a+1;
            int count = 1;
            while(count != 0 || b > split.length()) {
                if(split.charAt(b) == '{') {
                    count++;
                } else if(split.charAt(b) == '}') {
                    count--;
                }
                b++;
            }
            if(count == 0) {
                fields.add(split.substring(a, b));
                split = split.substring(b);
            } else {
                return null;
            }
        }
        return fields;
    }
    
    private List parseListFromString(String str, Map<String, Entity> recovereds, ClassLoader loader) throws Exception {
        List list = new ArrayList();
        List<String> fields = getFields(str);
        for(String field : fields) {
            if(!field.equals("{}")) {
                Class type = getType(field);
                list.add(recoverFromCapsule(field, recovereds, loader));
            }
        }
        return list;
    }
    
    private Map parseMapFromString(String str, Map<String, Entity> recovereds, ClassLoader loader) throws Exception {
        Map<Object, Object> map = new HashMap<>();
        List<String> fields = getFields(str);
        if (fields == null || fields.isEmpty()) {
            return map;
        }
        if(fields.size() >= 2) {
            for (int i = 0; i < fields.size() - 1; i += 2) {
                Object key = recoverFromCapsule(fields.get(i), recovereds, loader);
                Object value = recoverFromCapsule(fields.get(i + 1), recovereds, loader);
                map.put(key, value);
            }
        }
        return map;
    }
    
    private Calendar parseCalendarFromString(String calendarString, ClassLoader loader) throws Exception {
        Pattern patternYear = Pattern.compile("YEAR=(\\d+)");
        Pattern patternMonth = Pattern.compile("MONTH=(\\d+)");
        Pattern patternDayOfMonth = Pattern.compile("DAY_OF_MONTH=(\\d+)");
        Pattern patternHour = Pattern.compile("HOUR_OF_DAY=(\\d+)");
        Pattern patternMinute = Pattern.compile("MINUTE=(\\d+)");
        Pattern patternSecond = Pattern.compile("SECOND=(\\d+)");
        Pattern patternMillisecond = Pattern.compile("MILLISECOND=(\\d+)");
        Pattern patternZone = Pattern.compile("id=\"([^\"]+)\"");
        int year = extractValue(patternYear, calendarString, loader);
        int month = extractValue(patternMonth, calendarString, loader);
        int dayOfMonth = extractValue(patternDayOfMonth, calendarString, loader);
        int hour = extractValue(patternHour, calendarString, loader);
        int minute = extractValue(patternMinute, calendarString, loader);
        int second = extractValue(patternSecond, calendarString, loader);
        int millisecond = extractValue(patternMillisecond, calendarString, loader);
        String timeZoneID = extractString(patternZone, calendarString, loader);
        Class<?> calendarClass = Class.forName("java.util.GregorianCalendar", true, loader);
        Class<?> timeZoneClass = Class.forName("java.util.TimeZone", true, loader);
        Calendar calendar = (Calendar) calendarClass.getDeclaredConstructor().newInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        Object timeZone = timeZoneClass.getMethod("getTimeZone", String.class).invoke(null, timeZoneID);
        calendar.setTimeZone((java.util.TimeZone) timeZone);
        return calendar;
    }

    private Object parseDateTimeFromString(String dateTimeString, Class<?> type, ClassLoader loader) throws Exception {
        switch (type.getName()) {
            case "java.time.LocalDate" -> {
                try {
                    Class<?> localDateClass = Class.forName("java.time.LocalDate", true, loader);
                    return localDateClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.LocalTime" -> {
                try {
                    Class<?> localTimeClass = Class.forName("java.time.LocalTime", true, loader);
                    return localTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.LocalDateTime" -> {
                try {
                    Class<?> localDateTimeClass = Class.forName("java.time.LocalDateTime", true, loader);
                    return localDateTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.ZonedDateTime" -> {
                try {
                    Class<?> zonedDateTimeClass = Class.forName("java.time.ZonedDateTime", true, loader);
                    return zonedDateTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.Instant" -> {
                try {
                    Class<?> instantClass = Class.forName("java.time.Instant", true, loader);
                    return instantClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Formato de data/tempo desconhecido: " + dateTimeString);
    }

    private Period parsePeriodFromString(String periodString, ClassLoader loader) {
        try {
            Class<?> periodClass = Class.forName("java.time.Period", true, loader);
            Object period = periodClass.getMethod("parse", CharSequence.class).invoke(null, periodString);
            return (Period) period;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de período desconhecido: " + periodString, e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao tentar parsear o período: " + periodString, e);
        }
    }

    private Object parsePrimitiveFromString(String input, Class<?> type, ClassLoader loader) throws Exception {
        if (type == String.class) {
            return input; // A string já é o valor
        }
        Class<?> charClass = Class.forName("java.lang.Character", true, loader);
        Class<?> shortClass = Class.forName("java.lang.Short", true, loader);
        Class<?> intClass = Class.forName("java.lang.Integer", true, loader);
        Class<?> longClass = Class.forName("java.lang.Long", true, loader);
        Class<?> floatClass = Class.forName("java.lang.Float", true, loader);
        Class<?> doubleClass = Class.forName("java.lang.Double", true, loader);
        Class<?> booleanClass = Class.forName("java.lang.Boolean", true, loader);
        Class<?> byteClass = Class.forName("java.lang.Byte", true, loader);
        Class<?> bigDecimalClass = Class.forName("java.math.BigDecimal", true, loader);
        Class<?> bigIntegerClass = Class.forName("java.math.BigInteger", true, loader);
        if (type == char.class || type == charClass) {
            if (input.length() == 1) {
                return input.charAt(0); // Converte a string para char se tiver um único caractere
            } else {
                throw new IllegalArgumentException("Formato inválido para char: " + input);
            }
        } else if (type == short.class || type == shortClass) {
            return shortClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == int.class || type == intClass) {
            return intClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == long.class || type == longClass) {
            return longClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == float.class || type == floatClass) {
            return floatClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == double.class || type == doubleClass) {
            return doubleClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == boolean.class || type == booleanClass) {
            return booleanClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == byte.class || type == byteClass) {
            return byteClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == bigDecimalClass) {
            return bigDecimalClass.getConstructor(String.class).newInstance(input);
        } else if (type == bigIntegerClass) {
            return bigIntegerClass.getConstructor(String.class).newInstance(input);
        } else {
            throw new IllegalArgumentException("Tipo não suportado: " + type.getSimpleName());
        }
    }

    private int extractValue(Pattern pattern, String text, ClassLoader loader) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                Class<?> integerClass = Class.forName("java.lang.Integer", true, loader);
                return (int) integerClass.getMethod("parseInt", String.class).invoke(null, matcher.group(1));
            } catch (Exception e) {
                throw new RuntimeException("Erro ao tentar parsear o valor: " + matcher.group(1), e);
            }
        }
        return 0; // valor padrão se o campo não for encontrado
    }

    private String extractString(Pattern pattern, String text, ClassLoader loader) {
        try {
            Class<?> matcherClass = Class.forName("java.util.regex.Matcher", true, loader);
            Matcher matcher = pattern.matcher(text);
            if ((boolean) matcherClass.getMethod("find").invoke(matcher)) {
                return (String) matcherClass.getMethod("group", int.class).invoke(matcher, 1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao tentar extrair a string: " + text, e);
        }
        return "GMT"; // valor padrão se o campo não for encontrado
    }

    private void initDb() {
        List<String> all = Reflection.allImplementsNotAbstract(Entity.class);
        for(String path : all) {
            File file = new File(rootDirectory+path.replaceAll(".class", "").replaceAll("[.]", "/"));
            file.mkdirs();
            System.out.println("Diretório criado -> "+file.getPath());
        }
    }
    
    private boolean uniqueFieldTest(Entity entity) throws Exception {
        if(entity == null) {
            throw new NullPointerException("Entidade nula para teste de unicidade.");
        }
        List<Field> unicos = FieldsManager.getAllFieldsUniques(entity);
        if(unicos.isEmpty()) {
            return true;
        }
        String[] listaDeIds = new File(rootDirectory+entity.getClass().getName().replaceAll(".class", "").replaceAll("[.]", "/")).list();
        if(listaDeIds != null && listaDeIds.length > 0) {
            for(String id : listaDeIds) {
                if(!id.equals(entity.getId())) {
                    Map<String, String> camposParaAnalisar = recoverOnlyUniqueFields(entity.getClass(), id);
                    if(!unicos.stream().noneMatch(unico -> {
                        if(!camposParaAnalisar.containsKey(unico.getName())) {
                            return false;
                        } else {
                            try {
                                return FieldsManager.getValue(unico, entity).toString().equals(camposParaAnalisar.get(unico.getName()));
                            } catch (Exception ex) {
                                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                                return true;
                            }
                        }
                    })) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public <T extends Entity> T load(Class entityClass, String id) throws Exception {
        List<String> list = new ArrayList<>();
        list.add(id);
        FileManager.getInstance().lockRead(list);
        try {
            String path = getPath(entityClass, id);
            File file = new File(path);
            if(file.exists()) {
                if(this.loader != null) {
                    return (T) recover(Files.readString(Paths.get(path)), recovereds, loader);
                }
                return (T) recover(Files.readString(Paths.get(path)), recovereds, this.getClass().getClassLoader());
            } else {
                throw new FileNotFoundException("Arquivo de entidade não encontrado.");
            }
        } finally {
            FileManager.getInstance().unlockRead(list);
        }
    }
    
    public <T extends Entity> T load(Class entityClass, String id, ClassLoader loader) throws Exception {
        List<String> list = new ArrayList<>();
        list.add(id);
        FileManager.getInstance().lockRead(list);
        try {
            String path = getPath(entityClass, id);
            return (T) recover(Files.readString(Paths.get(path)), recovereds, loader);
        } finally {
            FileManager.getInstance().unlockRead(list);
        }
    }
    
    private Map<String, Entity> recursiveCascadeEntities() throws Exception {
        Map<String, Entity> map = new HashMap<>();
        return recursiveCascadeEntities(map);
    }
    
    private Map<String, Entity> recursiveCascadeEntities(Entity entity) throws Exception {
        Map<String, Entity> map = new HashMap<>();
        return recursiveCascadeEntities(map, entity);
    }
    
    private Map<String, Entity> recursiveCascadeEntities(Map<String, Entity> map) throws Exception {
        return recursiveCascadeEntities(map, entity);
    }
    
    private Map<String, Entity> recursiveCascadeEntities(Map<String, Entity> map, Entity entity) throws Exception {
        if(entity == null) {
            throw new NullPointerException("Capsula com entidade nula para buscar.");
        }
        if(!map.containsKey(entity.getId())) {
            map.put(entity.getId(), entity);
            List<Field> fields = FieldsManager.getAllFields(entity.getClass()).stream()
                    .filter(field -> {
                        if(field.isAnnotationPresent(Cascate.class)) {
                            Class type = field.getType();
                            return
                                    Reflection.isInstance(type, Entity.class) ||
                                    Reflection.isArrayMap(type);
                        } else {
                            return false;
                        }
                    })
                    .toList();
            for(Field field : fields) {
                if(Reflection.isInstance(field.getType(), Entity.class)) {
                    Entity ente = FieldsManager.getValue(field, entity);
                    if(ente != null) {
                        Capsule cap = new Capsule(ente);
                        cap.recursiveEntities(map);
                    }
                } else if(Reflection.isInstance(field.getType(), List.class)) {
                    List list = FieldsManager.getValue(field, entity);
                    if(list != null && !list.isEmpty()) {
                        for(Object o : list) {
                            if(Reflection.isInstance(o.getClass(), Entity.class)) {
                                Capsule cap = new Capsule((Entity) o);
                                cap.recursiveEntities(map);
                            }
                        }
                    }
                } else if(Reflection.isInstance(field.getType(), Map.class)) {
                    Map mapa = FieldsManager.getValue(field, entity);
                    if(mapa != null && !mapa.isEmpty()) {
                        Set keys = mapa.keySet();

                        for(Object key : keys) {
                            Object get = mapa.get(key);
                            if(Reflection.isInstance(get.getClass(), Entity.class)) {
                                Capsule cap = new Capsule((Entity) get);
                                cap.recursiveEntities(map);
                            }
                        }
                    }
                }
            }
        }
        return map;
    }
    
    private Map<String, Entity> recursiveEntities() throws Exception {
        Map<String, Entity> map = new HashMap<>();
        return recursiveEntities(map);
    }
    
    private Map<String, Entity> recursiveEntities(Entity entity) throws Exception {
        Map<String, Entity> map = new HashMap<>();
        return recursiveEntities(map, entity);
    }
    
    private Map<String, Entity> recursiveEntities(Map<String, Entity> map) throws Exception {
        return recursiveEntities(map, this.entity);
    }
    
    private Map<String, Entity> recursiveEntities(Map<String, Entity> map, Entity entity) throws Exception {
        if(entity == null) {
            throw new NullPointerException("Capsula com entidade nula para buscar.");
        }
        if(!map.containsKey(entity.getId())) {
            map.put(entity.getId(), entity);
            List<Field> fields = FieldsManager.getAllFields(entity.getClass()).stream()
                    .filter(field -> {
                        Class type = field.getType();
                        return
                                Reflection.isInstance(type, Entity.class) ||
                                Reflection.isArrayMap(type);
                    })
                    .toList();
            for(Field field : fields) {
                if(Reflection.isInstance(field.getType(), Entity.class)) {
                    Entity ente = FieldsManager.getValue(field, entity);
                    Capsule cap = new Capsule(ente);
                    cap.recursiveEntities(map);
                } else if(Reflection.isInstance(field.getType(), List.class)) {
                    List list = FieldsManager.getValue(field, entity);
                    for(Object o : list) {
                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                            Capsule cap = new Capsule((Entity) o);
                            cap.recursiveEntities(map);
                        }
                    }
                } else if(Reflection.isInstance(field.getType(), Map.class)) {
                    Map mapa = FieldsManager.getValue(field, entity);
                    Set keys = mapa.keySet();
                    for(Object key : keys) {
                        Object get = mapa.get(key);
                        if(Reflection.isInstance(get.getClass(), Entity.class)) {
                            Capsule cap = new Capsule((Entity) get);
                            cap.recursiveEntities(map);
                        }
                    }
                }
            }
        }
        return map;
    }
    
    private String getFolder(Class entityClass) {
        return rootDirectory+entityClass.getName().replaceAll(".class", "").replaceAll("[.]", "/");
    }
    
    private String buildPath(Class<?> entityClass, String suffix) {
    return rootDirectory + entityClass.getName().replace('.', '/') + suffix;
}

    private String getPath(Entity entity) throws Exception {
        return buildPath(entity.getClass(), "/" + entity.getId());
    }

    private String getOldPath(Entity entity) throws Exception {
        return buildPath(entity.getClass(), "/" + entity.getId() + "old");
    }

    private String getNewPath(Entity entity) throws Exception {
        return buildPath(entity.getClass(), "/" + entity.getId() + "new");
    }

    private String getPath(Class entityClass, String id) throws Exception {
        return rootDirectory+entityClass.getName().replaceAll(".class", "").replaceAll("[.]", "/")+"/"+id;
    }
    
    private boolean renameToOld(Entity entity) throws Exception {
        File file = new File(getPath(entity));
        if(file.exists()) {
            return file.renameTo(new File(getOldPath(entity)));
        }
        return true;
    }
    
    private boolean renameFromOld(Entity entity) throws Exception {
        File file = new File(getOldPath(entity));
        if(file.exists()) {
            file.renameTo(new File(getPath(entity)));
            return true;
        }
        return false;
    }
    
    private boolean renameToNew(Entity entity) throws Exception {
        File file = new File(getPath(entity));
        if(file.exists()) {
            file.renameTo(new File(getNewPath(entity)));
            return true;
        }
        return false;
    }
    
    private boolean renameFromNew(Entity entity) throws Exception {
        File file = new File(getNewPath(entity));
        if(file.exists()) {
            return file.renameTo(new File(getPath(entity)));
        }
        return false;
    }
    
    private boolean recoverOld(List<Entity> entities) {
        entities.stream()
                .allMatch(ente -> {
            try {
                return renameFromOld(ente);
            } catch (Exception ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        });
        return false;
    }
    
    private boolean parseOld(List<Entity> entities) {
        entities.stream()
                .allMatch(ente -> {
            try {
                return renameToOld(ente);
            } catch (Exception ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        });
        return false;
    }
    
    private void clearInDB(Entity entity) throws Exception {
        File old = new File(getOldPath(entity));
        File newer = new File(getNewPath(entity));
        if(old.exists()) {
            old.delete();
        }
        if (newer.exists()) {
            newer.delete();
        }
    }

    public static <T extends Entity> List<T> loadAll(Class clazz) throws Exception{
        return loadAll(clazz, null);
    }
    
    public static <T extends Entity> List<T> loadAll(Class clazz, Filter filter) throws Exception{
        List<T> list = new ArrayList<>();
        Capsule cap = new Capsule("");
        File folder = new File(cap.getFolder(clazz));
        if(folder.exists()) {
            List<String> ids = Arrays.asList(folder.list());
            if(ids != null) {
                for(String id : ids) {
                    Capsule capsule = new Capsule(clazz, id);
                    T ente = capsule.recover();
                    if(filter != null) {
                        if(filter.filter(ente)) {
                            list.add(ente);
                        }
                    } else {
                        list.add(ente);
                    }
                }
            }
        }
        return list;
    }
    
    public static List<String> getAllIds(Class clazz) throws Exception {
        Capsule cap = new Capsule("");
        File folder = new File(cap.getFolder(clazz));
        if(folder.exists()) {
            String[] list = folder.list();
            if(list != null) {
                return Arrays.asList(list);
            }
        }
        return new ArrayList<>();
    }
    
}
