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
import br.com.jhondbs.core.db.base.Represent;
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
 * <h2>ENGLISH</h2>
 * Responsible for encapsulating objects, transforming them into a String with all
 * the information of its variables.
 * Encapsulation has a recursive call that keeps all entities separate from each other,
 * forming branches within the database.
 * Not all native Java classes (that are in bootstrap) are supported, however all
 * classes created in your project are supported.
 * Among the native Java classes that are supported for wrapping in the current version are:
 * - byte - boolean - shorts - int - long - float - double - String - Date - calendar - map
 * - List - Set - Properties - BigInteger - BigDecimal
 * and there is also support for the arrays of the mentioned classes.
 * Support for other classes will be added in future releases.
 * <br><br>
 * <h2>PORTUGUÊS</h2>
 * Responsável pelo encapsulamento de objetos, transformando-os em uma String com
 * todas as informações de suas variáveis.
 * O encapsulamento possui chamada recursiva que mantém todas as entidades separadas
 * umas das outras, formando ramificações dentro do banco de dados.
 * Nem todas as classes nativas do Java (que estejam no bootstrap) são suportadas,
 * porém todas as classes criadas em seu projeto são suportadas.
 * Dentre as classes nativas do Java que são suportadas para encapsulamento na
 * versão atual estão:
 * - byte - boolean - short - int - long - float - double - String - Date - Calendar
 * - Map - List - Set - Properties - BigInteger - BigDecimal
 * e há também o suporte para os arrays das classes citadas.
 * O suporte para outras classes será adicionado em futuras versões.
 * 
 * @author jhonessales
 */
public class Capsule {
    
    /**
     * Holds references to supported classes.
     */
    private static Properties dictionary;
    
    /**
     * Capsule that holds object information.
     */
    private StringBuilder capsule;
    
    /**
     * Object that will be encapsulated.
     */
    private transient Object object;
    
    /**
     * List of entities that went through the encapsulation process.
     * It is used at the end of the encapsulation to identify which files should
     * be renamed or deleted, which guarantees that any updates to the database
     * occur atomically.
     */
    private transient List<Entity> entities;
    
    /**
     * Maintains encapsulation state. If any part of the wrapper returns an error,
     * the value of the letter is changed to false, preventing incomplete
     * informationfrom being saved to the database.
     */
    private transient BooleanLetter succes;
    
    /**
     * Creates a new capsule and prepares an object to be encapsulated.
     * @param obj to be encapsulated.
     */
    public Capsule(Object obj) {
        if(obj != null){
            this.succes = new BooleanLetter(true);
            this.entities = new ArrayList<>();
            this.object = obj;
            this.capsule = new StringBuilder();
            try {
                startDictionary();
            } catch (IOException e) {
            }
        }
    }
    
    /**
     * Creates a new capsule from a String usually extracted from the database,
     * allowing the process of extracting objects from texts.
     * @param str Object encapsulated in text format.
     */
    public Capsule(String str){
        this.capsule = new StringBuilder();
        this.capsule.append(str);
        try {
            startDictionary();
        } catch (IOException e) {
        }
    }
    
    /**
     * Starts the object encapsulation process.
     * @return True if the wrapping operation succeeds. False for any other error.
     * @throws Exception Generic exception for any error during the encapsulation process.
     */
    public boolean make() throws Exception{
        make(entities, succes);
        if(succes.isBool()){
            entities.forEach(ente -> {
                try {
                    File old = new File(IO.getDBFolderWithID(ente));
                    File neu = new File(IO.getDBFolder(ente)
                            +"/new"+String.valueOf(ente.getEnteId()));
                    if(neu.exists()){
                        fullDelete();
                        neu.renameTo(old);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } else {
            entities.forEach(ente -> {
                try {
                    File neu = new File(IO.getDBFolder(ente)
                            +"/new"+String.valueOf(ente.getEnteId()));
                    if(neu.exists()){
                        neu.delete();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
        return this.succes.isBool();
    }
    
    /**
     * Recursive function of the "make" method.
     * @param entities
     * @param succes
     * @return
     * @throws Exception 
     */
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
    
    /**
     * Saves the encapsulated text to a file in the database.
     * @return Status of the saving process.
     */
    private boolean save() throws Exception{
        if(Reflection.isInstance(this.object.getClass(), Entity.class)){
            File folder = new File(IO.getDBFolder(object));
            folder.mkdirs();
            if(folder.exists()){
                File newEntity = new File(IO.getDBFolderWithID((Entity) this.object));
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
    
    /**
     * Prepare a list for the encapsulation process.
     * @param list List to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     */
    private void encapsuleList(Object list, List<Entity> entities, BooleanLetter succes){
        capsule.append("l");
        encapsuleMultiples((List) list, entities, succes);
    }
    
    /**
     * Prepare a set for the encapsulation process.
     * @param set Set to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     */
    private void encapsuleSet(Object set, List<Entity> entities, BooleanLetter succes){
        capsule.append("s");
        encapsuleMultiples(((Set) set).stream().toList(), entities, succes);
    }
    
    /**
     * Encapsulates an array.
     * @param arr Array of objects to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     */
    private void encapsuleArray(Object arr, List<Entity> entities, BooleanLetter succes){
        /**
         * Do not change how array type checking happens.
         * Despite seeming inefficient and difficult to maintain,
         * this was the only way that worked as expected.
         */
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
            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, e);
            succes.setBool(false);
        }
        encapsuleMultiples(oblist, entities, succes);
    }
    
    /**
     * Performs the encapsulation of multiple objects through a list.
     * @param list List to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     */
    private void encapsuleMultiples(List list, List<Entity> entities, BooleanLetter succes){
        capsule.append("[");
        list.forEach(o -> {
            capsule.append("{");
            if(Reflection.isInstance(o.getClass(), Entity.class)){
                // If it is an entity.
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
                    succes.setBool(false);
                }
            }
            capsule.append("}");
        });
        capsule.append("]");
    }
    
    /**
     * Prepare a map for the encapsulation process.
     * @param map Map to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     */
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
                succes.setBool(false);
            }
        });
    }
    
    /**
     * Prepare a properties for the encapsulation process.
     * @param prop Properties to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     */
    private void encapsuleProperties(Properties prop, List<Entity> entities, BooleanLetter succes){
        capsule.append("p");
        Map<String, String> map = new HashMap<>();
        Properties p = (Properties) prop;
        p.keySet().forEach(key -> {
            map.put((String) key, p.getProperty((String) key));
        });
        encapsuleMap(map, entities, succes);
    }
    
    /**
     * Attempts to encapsulate a special case.
     * @param obj Object to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     */
    private void encapsuleSpecialCase(Object obj, List<Entity> entities, BooleanLetter succes) throws Exception{
        // CHECK IF IT IS A PRIMITIVE OR NUMBER.
        if(Reflection.isPrimitive(obj) || Reflection.isInstance(obj.getClass(), Number.class)){
            capsule.append(dictionary.get(obj.getClass().getName())).append(":");
            capsule.append(obj.toString());
        // CHECK IF IT IS A DATE.
        } else if(Reflection.isInstance(obj.getClass(), Date.class)){
            capsule.append(dictionary.get(Date.class.getName())).append(":");
            capsule.append(((Date) obj).toGMTString());
        // CHECK IF IT IS A CALENDAR.
        } else if(Reflection.isInstance(obj.getClass(), Calendar.class)){
            capsule.append(dictionary.get(Calendar.class.getName())).append(":");
            capsule.append(((Calendar) obj).getTime().toGMTString());
        } else if(Reflection.isInstance(obj.getClass(), Represent.class)){
            encapsuleObject(obj, entities, succes);
        } else {
            System.out.println("Special case error: Unsupported class.");
            System.out.println(obj.getClass());
            succes.setBool(false);
        }
    }
    
    /**
     * Performs the encapsulation process of a complex object.
     * @param obj Object to be encapsulated.
     * @param entities List that keeps the reference of entities saved during the process.
     * @param succes Letter that maintains the success state of the encapsulation.
     * @throws Exception Generic exception for any error that may happen.
     */
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
                        capsule.append(":");
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
    
    /**
     * Starts the process of extracting an object.
     * @param <T> Type of object to extract.
     * @return Extracted object.
     * @throws ClassNotFoundException 
     */
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
    
    /**
     * Starts the process of extracting an list.
     * @param <T> Type of object to extract.
     * @return Extracted list.
     */
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
                    // Please don't change that part of checks,
                    // it was the only way that worked as expected.
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
    
    /**
     * Starts the process of extracting an map.
     * @param <T> Type of object to extract.
     * @return Extracted map.
     */
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
            } catch (ClassNotFoundException e) {
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
    
    /**
     * Starts the process of extracting an primitive.
     * @param <T> Type of object to extract.
     * @return Extracted primitive.
     * @throws ClassNotFoundException 
     */
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
    
    /**
     * Starts the process of extracting an entity.
     * @param <T> Type of object to extract.
     * @return Extracted entity.
     */
    private <T extends Entity> T extractEntity(){
        try {
            String str = toString().substring(1);
            String index = str.substring(0, str.indexOf(":"));
            long id = Long.parseLong(str.substring(str.indexOf(":") + 1));
            String clName = (String) dictionary.keySet().stream().filter(key -> (dictionary.get(key).equals(index))).iterator().next();
            try {
                File entity = new File("db/" + clName
                        .replaceAll(".class", "")
                        .replaceAll("[.]", "/")
                        + "/" + String.valueOf(id));
                try (BufferedReader r = Files.newBufferedReader(Paths.get(entity.getPath()))) {
                    String line = r.readLine();
                    Capsule cap = new Capsule(line);
                    r.close();
                    return cap.extract();
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NumberFormatException numberFormatException) {
        }
        return null;
    }
    
    /**
     * Starts the process of extracting an object.
     * @param <T> Type of object to extract.
     * @return Extracted object.
     */
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
    
    /**
     * Starts the process of extracting an special object case.
     * @param <T> Type of object to extract.
     * @return Extracted object.
     */
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
    
    /**
     * Separates the built-in objects from a list.
     * @param str 
     * @return 
     */
    private List<String> parseArrayString(String str){
        List<String> back = new ArrayList<>();
        str = str.substring(2, str.length() -1);
        getSub(back, str, '{', '}');
        return back;
    }
    
    /**
     * It receives a text and the identification of an opening and closing character
     * of the block and then divides the String into identified blocks, allocating
     * them in a list of Strings.
     * @param refil List that will receive the divided blocks.
     * @param str String with the blocks to be separated.
     * @param openKey Block opening character.
     * @param closeKey Block closing character.
     */
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
    
    /**
     * Method that will completely delete the object's branch.
     * Including all sub entities of the branch.
     */
    public void fullDelete(){
        try {
            Entity entity = (Entity) this.object;
            try(BufferedReader r = Files.newBufferedReader(Paths.get(new File(IO.getDBFolderWithID(entity)).getPath()))) {
                String line = r.readLine();
                Capsule capsule = new Capsule(line);
                fullDelete(capsule.extract());
            } catch (Exception ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassCastException e){
            fullDeleteObj(object);
        }
    }
    
    /**
     * Method that will completely delete the object's branch.
     * Including all sub entities of the branch.
     */
    private void fullDeleteObj(Object object){
        List<Field> fields = FieldsManager.getAllFields(object);
        fields.forEach(field -> {
            try {
                if(!Reflection.isPrimitive(field.getType())){
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if(Reflection.isInstance(value.getClass(), Entity.class)){
                        fullDelete((Entity) value);
                    } else {
                        fullDeleteObj(value);
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
            } catch (Exception ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    /**
     * Method that will completely delete the object's branch.
     * Including all sub entities of the branch.
     * @param entity Entity that opens the branch to be deleted.
     */
    private void fullDelete(Entity entity) throws Exception{
        List<Field> fields = FieldsManager.getAllFields(entity);
        fields.forEach(field -> {
            if(Reflection.isInstance(field.getType(), Entity.class)){
                try {
                    field.setAccessible(true);
                    Entity e = (Entity) field.get(entity);
                    if(e != null){
                        fullDelete(e);
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                } catch (Exception ex) {
                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if(Reflection.isInstance(field.getType(), List.class)){
                try {
                    field.setAccessible(true);
                    List list = (List) field.get(entity);
                    if(list != null){
                        list.forEach(obj -> {
                            if(Reflection.isInstance(obj.getClass(), Entity.class)){
                                try {
                                    fullDelete(((Entity) obj));
                                } catch (Exception ex) {
                                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                }
            } else if(Reflection.isInstance(field.getType(), Map.class)){
                try {
                    field.setAccessible(true);
                    Map map = (Map) field.get(entity);
                    if(map != null){
                        map.keySet().forEach(key -> {
                            if(Reflection.isInstance(map.get(key).getClass(), Entity.class)){
                                try {
                                    fullDelete(((Entity) map.get(key)));
                                } catch (Exception ex) {
                                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                }
            } else if(field.getType().getClass().getName().startsWith("[")){
                try {
                    field.setAccessible(true);
                    Object[] array = (Object[]) field.get(entity);
                    if(array != null){
                        Arrays.asList(array).forEach(obj -> {
                            if(Reflection.isInstance(obj.getClass(), Entity.class)){
                                try {
                                    fullDelete(((Entity) obj));
                                } catch (Exception ex) {
                                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                }
            }
        });
        File ente = new File(IO.getDBFolderWithID(entity));
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
    
    /**
     * Initializes the database class dictionary if it does not already exist.
     * @throws IOException 
     */
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
                dictionary.put(Represent.class.getName(), String.valueOf(dictionary.size()));
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
