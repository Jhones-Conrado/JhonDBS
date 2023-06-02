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

import br.com.jhondbs.core.db.Keys;
import br.com.jhondbs.core.db.base.Entity;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.io.tools.BooleanLetter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhonessales
 */
public class Capsule {
    
    private static Properties dictionary;
    private StringBuilder capsule;
    
    private transient Object object;
    private transient List<Entity> entities;
    private transient BooleanLetter succes;

    public Capsule(Object obj) {
        if(obj != null){
            this.succes = new BooleanLetter(true);
            this.entities = new ArrayList<>();
            this.object = obj;
            this.capsule = new StringBuilder();
        }
    }
    
    private Capsule(String str){
        this.capsule = new StringBuilder();
        this.capsule.append(str);
    }
    
    public boolean make() throws Exception{
        make(entities, succes);
        if(succes.isBool()){
            entities.forEach(ente -> {
                File old = new File("db/"+ente.getClass().getName()
                        .replaceAll(".class", "")
                        .replaceAll("[.]", "/")
                        +"/"+String.valueOf(ente.getEnteId()));
                File neu = new File("db/"+ente.getClass().getName()
                        .replaceAll(".class", "")
                        .replaceAll("[.]", "/")
                        +"/new"+String.valueOf(ente.getEnteId()));
                if(neu.exists()){
                    fullDelete();
//                    if(old.exists()){
//                        old.delete();
//                    }
                    neu.renameTo(old);
                }
            });
        } else {
            entities.forEach(ente -> {
                File neu = new File(ente.getClass().getName()
                        .replaceAll(".class", "")
                        .replaceAll("[.]", "/")
                        +"/new"+String.valueOf(ente.getEnteId()));
                if(neu.exists()){
                    neu.delete();
                }
            });
        }
        return this.succes.isBool();
    }
    
    private boolean make(List<Entity> entities, BooleanLetter succes) throws Exception{
        startDictionary();
        if(this.object != null){
            if(Reflection.isInstance(this.object.getClass(), Entity.class)){
                Keys.gerarId((Entity) this.object);
                encapsuleObject(this.object, entities, succes);
                if(save()){
                    entities.add((Entity) this.object);
                }
            } else if(this.object.getClass().getName().startsWith("[")){
                encapsuleArray(this.object, entities, succes);
            } else if(Reflection.isInstance(this.object.getClass(), List.class)){
                encapsuleList(this.object, entities, succes);
            } else if(Reflection.isInstance(this.object.getClass(), Set.class)){
                encapsuleSet(this.object, entities, succes);
            } else if(Reflection.isInstance(this.object.getClass(), Properties.class)){
                encapsuleProperties((Properties) this.object, entities, succes);
            } else if(Reflection.isInstance(this.object.getClass(), Map.class)){
                encapsuleMap((Map) this.object, entities, succes);
            } else if(new Reflection().reflect().contains(this.object.getClass().getName())){
                // Project classes.
                encapsuleObject(this.object, entities, succes);
            } else if(dictionary.containsKey(this.object.getClass().getName())){
                // Supported special class.
                encapsuleSpecialCase(this.object, entities, succes);
            } else {
                throw new Exception("Class not supported: "+this.object.getClass().getName());
            }
        } else {
            this.succes.setBool(false);
        }
        return this.succes.isBool();
    }
    
    private boolean save(){
        if(Reflection.isInstance(this.object.getClass(), Entity.class)){
            File folder = new File("db/"+this.object.getClass().getName().replaceAll(".class", "").replaceAll("[.]", "/"));
            folder.mkdirs();
            if(folder.exists()){
                File newEntity = new File(folder.getPath()+"/new"+String.valueOf(((Entity) this.object).getEnteId()));
                try(BufferedWriter w = Files.newBufferedWriter(Paths.get(newEntity.getPath()), StandardCharsets.UTF_8)) {
                    w.write(toString());
                    w.flush();
                    return true;
                } catch (Exception e) {
                }
            }
        }
        return false;
    }
    
    private void encapsuleList(Object list, List<Entity> entities, BooleanLetter succes){
        capsule.append("l");
        encapsuleMultiples((List) list, entities, succes);
    }
    
    private void encapsuleSet(Object set, List<Entity> entities, BooleanLetter succes){
        capsule.append("s");
        encapsuleMultiples(((Set) set).stream().toList(), entities, succes);
    }
    
    private void encapsuleArray(Object arr, List<Entity> entities, BooleanLetter succes){
        // Any other approach here will not work!
        capsule.append("[");
        List oblist = new ArrayList();
        try {
            if (arr instanceof byte[]){
                byte[] a = (byte[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            } else if (arr instanceof boolean[]) {
                boolean[] a = (boolean[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            } else if (arr instanceof short[]) {
                short[] a = (short[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            } else if (arr instanceof int[]) {
                int[] a = (int[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            } else if (arr instanceof long[]) {
                long[] a = (long[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            } else if (arr instanceof float[]) {
                float[] a = (float[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            } else if (arr instanceof double[]) {
                double[] a = (double[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            } else if (arr instanceof Object[]) {
                Object[] a = (Object[]) arr;
                for(int i = 0 ; i < a.length ; i++){
                    oblist.add(a[i]);
                }
            }
        } catch (Exception e) {
            // Trate a exceção, se necessário
            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, e);
        }
        encapsuleMultiples(oblist, entities, succes);
    }
    
    private void encapsuleMultiples(List list, List<Entity> entities, BooleanLetter succes){
        // ADICIONA TODOS OS ITENS EXTRAÍDOS À CAPSULA.
        capsule.append("[");
        list.forEach(o -> {
            capsule.append("{");
            if(Reflection.isInstance(o.getClass(), Entity.class)){
                Entity e = (Entity) o;
                Capsule cap = new Capsule(e);
                try {
                    capsule.append("e");
                    cap.make(entities, succes);
                    capsule.append(dictionary.getProperty(o.getClass().getName()));
                    capsule.append(":");
                    capsule.append(String.valueOf(e.getEnteId()));
                } catch (Exception ex) {
                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                    entities.add(e);
                    succes.setBool(false);
                }
            } else {
                try {
                    Capsule cap = new Capsule(o);
                    cap.make(entities, succes);
                    capsule.append(cap.toString());
                } catch (Exception ex) {
                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            capsule.append("}");
        });
        capsule.append("]");
    }
    
    private void encapsuleMap(Map map, List<Entity> entities, BooleanLetter succes){
        map.keySet().forEach(key -> {
            try {
                capsule.append("(");
                Capsule cap = new Capsule(key);
                cap.make();
                capsule.append(cap.toString());
                capsule.append(";");
                Capsule capValue = new Capsule(map.get(key));
                capValue.make(entities, succes);
                capsule.append(capValue.toString());
                capsule.append(")");
            } catch (Exception ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private void encapsuleProperties(Properties prop, List<Entity> entities, BooleanLetter succes){
        capsule.append("p");
        Map<String, String> map = new HashMap<>();
        Properties p = (Properties) prop;
        p.keySet().forEach(key -> {
            map.put((String) key, p.getProperty((String) key));
        });
        encapsuleMap(map, entities, succes);
    }
    
    private void encapsuleSpecialCase(Object obj, List<Entity> entities, BooleanLetter succes){
        // VERIFICA SE É UM PRIMITIVO OU NÚMERO.
        if(Reflection.isPrimitive(obj) || Reflection.isInstance(obj.getClass(), Number.class)){
            capsule.append(dictionary.get(obj.getClass().getName())).append(":");
            capsule.append(obj.toString());
        // VERIFICA SE É UMA DATA.
        } else if(Reflection.isInstance(obj.getClass(), Date.class)){
            capsule.append(dictionary.get(Date.class.getName())).append(":");
            capsule.append(((Date) obj).toGMTString());
        } else if(Reflection.isInstance(obj.getClass(), Calendar.class)){
            capsule.append(dictionary.get(Calendar.class.getName())).append(":");
            capsule.append(((Calendar) obj).getTime().toGMTString());
        } else {
            System.out.println("Special case error: Unsupported class.");
            System.out.println(obj.getClass());
        }
    }
    
    private void encapsuleObject(Object obj, List<Entity> entities, BooleanLetter succes) throws Exception{
        capsule.append(dictionary.get(obj.getClass().getName())).append(":");
        List<Field> fields = FieldsManager.getAllFields(obj);
        for(Field field : fields){
            if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())){
                field.setAccessible(true);
                if(field.get(obj) != null){
                    capsule.append("{");
                    capsule.append(field.getName());
                    capsule.append(":");
                    if(Reflection.isInstance(field.getType(), Entity.class)){
                        Entity inner = (Entity) field.get(obj);
                        Capsule cap = new Capsule(inner);
                        cap.make(entities, succes);
                        capsule.append("e");
                        capsule.append(dictionary.get(inner.getClass().getName()));
                        capsule.append(";");
                        capsule.append(String.valueOf(inner.getEnteId()));
                    } else {
                        Capsule cap = new Capsule(field.get(obj));
                        cap.make(entities, succes);
                        capsule.append(cap.toString());
                    }
                    capsule.append("}");
                }
            }
        }
    }
    
    public <T> T extract() throws ClassNotFoundException{
        String str = toString();
        if(str.startsWith("e")){
            // Entity.
            return extractEntity();
        } else if(str.startsWith("l") || str.startsWith("s") || str.startsWith("[")){
            // List
            return extractList();
        } else if(str.startsWith("(") || str.startsWith("p")){
            // Map
            return extractMap();
        } else if(str.contains(":{") && str.endsWith("}")){
            // Object.
            return extractObject();
        } else {
            return extractSpecialCase();
        }
    }
    
    private <T> T extractList(){
        List back = new ArrayList();
        List<String> strs = parseArrayString(toString());
        strs.forEach(str -> {
            try {
                back.add(new Capsule(str).extract());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        if(toString().startsWith("l")){
            return (T) back;
        } else if(toString().startsWith("s")){
            Set set = new HashSet();
            back.forEach(o -> {
                set.add(o);
            });
            return (T) set;
        } else {
            if(!back.isEmpty()){
                boolean unique = true;
                Class cl = back.get(0).getClass();
                for(Object i : back){
                    if(i.getClass() != cl){
                        unique = false;
                    }
                }
                if(unique){
                    Object o = back.get(0);
                    if(o instanceof Byte){
                        byte[] ar = new byte[back.size()];
                        for(int i = 0 ; i < back.size() ; i++){
                            ar[i] = (byte) back.get(i);
                        }
                        return (T) ar;
                    } else if(o instanceof Boolean){
                        boolean[] ar = new boolean[back.size()];
                        for(int i = 0 ; i < back.size() ; i++){
                            ar[i] = (boolean) back.get(i);
                        }
                        return (T) ar;
                    } else if(o instanceof Short){
                        short[] ar = new short[back.size()];
                        for(int i = 0 ; i < back.size() ; i++){
                            ar[i] = (short) back.get(i);
                        }
                        return (T) ar;
                    } else if(o instanceof Integer){
                        int[] ar = new int[back.size()];
                        for(int i = 0 ; i < back.size() ; i++){
                            ar[i] = (int) back.get(i);
                        }
                        return (T) ar;
                    } else if(o instanceof Long){
                        long[] ar = new long[back.size()];
                        for(int i = 0 ; i < back.size() ; i++){
                            ar[i] = (long) back.get(i);
                        }
                        return (T) ar;
                    } else if(o instanceof Float){
                        float[] ar = new float[back.size()];
                        for(int i = 0 ; i < back.size() ; i++){
                            ar[i] = (float) back.get(i);
                        }
                        return (T) ar;
                    } else if(o instanceof Double){
                        double[] ar = new double[back.size()];
                        for(int i = 0 ; i < back.size() ; i++){
                            ar[i] = (double) back.get(i);
                        }
                        return (T) ar;
                    }
                    
                }
            }
            return (T) back.toArray();
        }
    }
    
    private <T> T extractMap(){
        List<String> refil = new ArrayList<>();
        getSub(refil, toString(), '(', ')');
        Map map = new HashMap();
        refil.forEach(str -> {
            int separator = str.indexOf(";");
            String key = str.substring(0, separator);
            String value = str.substring(separator+1);
            try {
                map.put(new Capsule(key).extract(), new Capsule(value).extract());
            } catch (Exception e) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, e);
            }
        });
        if(toString().startsWith("p")){
            Properties properties = new Properties();
            map.keySet().forEach(key -> {
                properties.put(key, map.get(key));
            });
            return (T) properties;
        }
        return (T) map;
    }
    
    private <T> T extractPrimitive() throws ClassNotFoundException{
        String str = toString();
        String index = str.substring(0, str.indexOf(":"));
        String clName = (String) dictionary.keySet().stream().filter(key -> (dictionary.get(key).equals(index))).iterator().next();
        String value = str.substring(str.indexOf(":") + 1);
        Class<?> cl = Class.forName(clName);
        if(Reflection.isPrimitive(cl) 
                || Reflection.isInstance(cl, Number.class)
                || Reflection.isInstance(cl, Date.class)){
            try {
                Constructor<?> con = cl.getConstructor(String.class);
                return (T) con.newInstance(value);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    private <T extends Entity> T extractEntity(){
        String str = toString().substring(1);
        String index = str.substring(0, str.indexOf(":"));
        long id = Long.parseLong(str.substring(str.indexOf(":")+1));
        String clName = (String) dictionary.keySet().stream().filter(key -> (dictionary.get(key).equals(index))).iterator().next();
        try {
            File entity = new File("db/"+clName
                    .replaceAll(".class", "")
                    .replaceAll("[.]", "/")
                    +"/"+String.valueOf(id));
            try(BufferedReader r = Files.newBufferedReader(Paths.get(entity.getPath()))) {
                String line = r.readLine();
                Capsule cap = new Capsule(line);
                r.close();
                return cap.extract();
            }
        } catch (Exception ex) {
            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private <T> T extractObject(){
        try {
            String str = toString();
            String index = str.substring(0, str.indexOf(":"));
            String clName = (String) dictionary.keySet().stream().filter(key -> (dictionary.get(key).equals(index))).iterator().next();
            Object ins = new Reflection().getNewInstance(clName);
            List<String> refil = new ArrayList<>();
            getSub(refil, str, '{', '}');
            Map<String, Capsule> values = new HashMap<>();
            refil.forEach(field -> {
                String fieldName = field.substring(0, field.indexOf(":"));
                String capsuleString = field.substring(field.indexOf(":")+1, field.length());
                Capsule cap = new Capsule(capsuleString);
                values.put(fieldName, cap);
            });
            
            List<Field> fields = FieldsManager.getAllFields(ins);
            fields.forEach(field -> {
                if(values.containsKey(field.getName())){
                    field.setAccessible(true);
                    try {
                        field.set(ins, values.get(field.getName()).extract());
                    } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            return (T) ins;
        } catch (IOException ex) {
            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private <T> T extractSpecialCase(){
        try {
            T t = extractPrimitive();
            if(t == null){
                String str = toString();
                String index = str.substring(0, str.indexOf(":"));
                String clName = (String) dictionary.keySet().stream().filter(key -> (dictionary.get(key).equals(index))).iterator().next();
                String value = str.substring(str.indexOf(":") + 1);
                Class<?> cl = Class.forName(clName);
                if(Reflection.isInstance(cl, Calendar.class)){
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(value));
                    return (T) calendar;
                }
            } else {
                return t;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<String> parseArrayString(String str){
        List<String> back = new ArrayList<>();
        str = str.substring(2, str.length() -1);
        getSub(back, str, '{', '}');
        return back;
    }
    
    private void getSub(List<String> refil, String str, char openKey, char closeKey){
        if(str.contains(new String(new char[]{openKey})) && str.contains(new String(new char[]{closeKey}))){
            int opens = 1;
            int start = str.indexOf(openKey);
            int end = -1;
            for(int i = start+1 ; i < str.length() ; i++){
                if(str.charAt(i) == openKey){
                    opens++;
                } else if(str.charAt(i) == closeKey){
                    opens--;
                }
                if(opens == 0){
                    end = i+1;
                    break;
                }
            }
            refil.add(str.substring(start+1, end-1));
            if(str.length() > end){
                getSub(refil, str.substring(end), openKey, closeKey);
            }
        }
    }
    
    private void fullDelete(){
        try {
            Entity entity = (Entity) this.object;
            try(BufferedReader r = Files.newBufferedReader(Paths.get(new File("db/"+entity.getClass().getName()
                        .replaceAll(".class", "")
                        .replaceAll("[.]", "/")
                        +"/"+String.valueOf(entity.getEnteId())).getPath()))) {
                String line = r.readLine();
                Capsule capsule = new Capsule(line);
                fullDelete(capsule.extract());
            }
        } catch (Exception e) {
            List<Field> fields = FieldsManager.getAllFields(object);
        }
    }
    
    private void fullDelete(Entity entity){
        List<Field> fields = FieldsManager.getAllFields(entity);
        fields.forEach(field -> {
            if(Reflection.isInstance(field.getType(), Entity.class)){
                try {
                    field.setAccessible(true);
                    Entity e = (Entity) field.get(entity);
                    if(e != null){
                        fullDelete(e);
                    }
                } catch(Exception e){
                }
            } else if(Reflection.isInstance(field.getType(), List.class)){
                try {
                    field.setAccessible(true);
                    List list = (List) field.get(entity);
                    if(list != null){
                        list.forEach(obj -> {
                            if(Reflection.isInstance(obj.getClass(), Entity.class)){
                                fullDelete(((Entity) obj));
                            }
                        });
                    }
                } catch(Exception e){
                }
            } else if(Reflection.isInstance(field.getType(), Map.class)){
                try {
                    field.setAccessible(true);
                    Map map = (Map) field.get(entity);
                    if(map != null){
                        map.keySet().forEach(key -> {
                            if(Reflection.isInstance(map.get(key).getClass(), Entity.class)){
                                fullDelete(((Entity) map.get(key)));
                            }
                        });
                    }
                } catch(Exception e){
                }
            } else if(field.getType().getClass().getName().startsWith("[")){
                try {
                    field.setAccessible(true);
                    Object[] array = (Object[]) field.get(entity);
                    if(array != null){
                        Arrays.asList(array).forEach(obj -> {
                            if(Reflection.isInstance(obj.getClass(), Entity.class)){
                                fullDelete(((Entity) obj));
                            }
                        });
                    }
                } catch(Exception e){
                }
            }
        });
        File ente = new File("db/"+entity.getClass().getName()
            .replaceAll(".class", "")
            .replaceAll("[.]", "/")
            +"/"+String.valueOf(entity.getEnteId()));
        if(ente.exists()){
            ente.delete();
        }
    }
    
    @Override
    public String toString() {
        return capsule.toString();
    }
    
    private void setObject(Object object){
        this.object = object;
    }
    
    private void startDictionary() throws IOException{
        if(dictionary == null){
            File dic = new File("db/dictionary.dic");
            if(dic.exists()){
                dictionary = new Properties();
                dictionary.load(new FileInputStream(dic));
            } else {
                dictionary = new Properties();
                dictionary.put(Boolean.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Byte.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Short.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Integer.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Long.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Double.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(String.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(BigInteger.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(BigDecimal.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Date.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(GregorianCalendar.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(List.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Map.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Set.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Properties.class.getName(), String.valueOf(dictionary.size()));
                dictionary.put(Calendar.class.getName(), String.valueOf(dictionary.size()));
            }
            List<String> all = new Reflection().allImplementsNotAbstract(Object.class);
            all.forEach(cl -> {
                if(!dictionary.containsKey(cl)){
                    dictionary.put(cl, String.valueOf(dictionary.size()));
                }
            });
            new File("db").mkdirs();
            dictionary.store(new FileOutputStream(dic), "JhonDBS Class Dictionary");
        }
    }
    
}
