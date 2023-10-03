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
package br.com.jhondbs.core.db.io.capsule.capsulators;

import br.com.jhondbs.core.db.base.Entity;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.base.Represent;
import br.com.jhondbs.core.db.io.IO;
import br.com.jhondbs.core.db.io.Reflection;
import br.com.jhondbs.core.db.io.capsule.Capsule;
import br.com.jhondbs.core.db.io.capsule.ClassDictionary;
import br.com.jhondbs.core.db.io.letters.BooleanLetter;
import br.com.jhondbs.core.tools.StringTools;
import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhonessales
 */
public class CapsulateObject {
    
    public static String encapsulate(Object object, List<Entity> entities, BooleanLetter letter, Entity superente, Entity root){
        String r = null;
        if((r=encapPrimitive(object)) != null){
            return r;
        } else if((r = encapDate(object)) != null){
            return r;
        } else {
            return encapObject(object, entities, letter, superente, root);
        }
    }
    
    private static String encapPrimitive(Object object){
        if(Reflection.isPrimitive(object) || Reflection.isInstance(object.getClass(), Number.class)){
            if(Reflection.isInstance(object.getClass(), Number.class)){
                return encapNumber(object);
            } else if(Reflection.isInstance(object.getClass(), Boolean.class)){
                return encapBoolean(object);
            } else if(Reflection.isInstance(object.getClass(), Byte.class)){
                return encapByte(object);
            } else if(Reflection.isInstance(object.getClass(), String.class)){
                return encapString(object);
            } else if(Reflection.isInstance(object.getClass(), Character.class)){
                return encapChar(object);
            }
        }
        return null;
    }
    
    private static String encapNumber(Object object){
        if(Reflection.isInstance(object.getClass(), Number.class)){
            if(Reflection.isInstance(object.getClass(), Short.class)){
                return encapShort(object);
            } else if(Reflection.isInstance(object.getClass(), Integer.class)){
                return encapInteger(object);
            } else if(Reflection.isInstance(object.getClass(), Long.class)){
                return encapLong(object);
            } else if(Reflection.isInstance(object.getClass(), Float.class)){
                return encapFloat(object);
            } else if(Reflection.isInstance(object.getClass(), Double.class)){
                return encapDouble(object);
            } else if(Reflection.isInstance(object.getClass(), BigInteger.class)){
                return encapBigInteger(object);
            } else if(Reflection.isInstance(object.getClass(), BigDecimal.class)){
                return encapBigDecimal(object);
            }
        }
        return null;
    }
    
    private static String encapBoolean(Object object){
        return "{"+ClassDictionary.getIndex(Boolean.class)+":"+object.toString()+"}";
    }
    
    private static String encapByte(Object object){
        return "{"+ClassDictionary.getIndex(Byte.class)+":"+object.toString()+"}";
    }
    
//    ############### ENCAPSULAMENTO DE NÚMEROS
    
    private static String encapShort(Object object){
        return "{"+ClassDictionary.getIndex(Short.class)+":"+object.toString()+"}";
    }
    
    private static String encapInteger(Object object){
        return "{"+ClassDictionary.getIndex(Integer.class)+":"+object.toString()+"}";
    }
    
    private static String encapLong(Object object){
        return "{"+ClassDictionary.getIndex(Long.class)+":"+object.toString()+"}";
    }
    
    private static String encapFloat(Object object){
        return "{"+ClassDictionary.getIndex(Float.class)+":"+object.toString()+"}";
    }
    
    private static String encapDouble(Object object){
        return "{"+ClassDictionary.getIndex(Double.class)+":"+object.toString()+"}";
    }
    
    private static String encapBigInteger(Object object){
        return "{"+ClassDictionary.getIndex(BigInteger.class)+":"+object.toString()+"}";
    }
    
    private static String encapBigDecimal(Object object){
        return "{"+ClassDictionary.getIndex(BigDecimal.class)+":"+object.toString()+"}";
    }
    
    public static String encapChar(Object object){
        return "{"+ClassDictionary.getIndex(Character.class)+":"+object.toString()+"}";
    }
    
    public static String encapString(Object object){
        return "{"+ClassDictionary.getIndex(String.class)+":"+object.toString()+"}";
    }
    
//    ############## ENCAPSULAMENTO DE DATAS.
    
    public static String encapDate(Object object){
        if(Reflection.isInstance(object.getClass(), Date.class)){
            return "{"+ClassDictionary.getIndex(object.getClass())+":"+((Date) object).toGMTString()+"}";
        } else if(Reflection.isInstance(object.getClass(), Calendar.class)){
            return "{"+ClassDictionary.getIndex(object.getClass())+":"+((Calendar) object).getTime().toGMTString()+"}";
        }
        return null;
    }
    
    public static String encapObject(Object object, List<Entity> entities, BooleanLetter letter, Entity superente, Entity root){
        boolean hassuperente = false;
        boolean hasroot = false;
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(ClassDictionary.getIndex(object.getClass()));
        sb.append(":");
        List<Field> fields = FieldsManager.getAllFields(object);
        for(Field field : fields){
            field.setAccessible(true);
            sb.append("{");
            try {
                String fieldName = field.getName();
                
                if(Reflection.isInstance(field.getType(), Represent.class)){
                    if(fieldName.equalsIgnoreCase("superente")){
                        hassuperente = true;
                    } else if(fieldName.equalsIgnoreCase("root")){
                        hasroot = true;
                    }
                }
                
                sb.append(fieldName).append(":");
                Object value = field.get(object);
                Capsule cap = null;
                if(Reflection.isInstance(object.getClass(), Entity.class)){
                    cap = new Capsule(value, entities, letter, (Entity) object, root);
                } else {
                    cap = new Capsule(value, entities, letter, superente, root);
                }
                sb.append(cap.make());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(CapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
            }
            sb.append("}");
        }
        
        
        if(Reflection.isInstance(object.getClass(), Entity.class)){
            if(!hassuperente){
                if(superente != null){
                    addThisSuperente(superente, sb);
                } else {
                    addOldSuperente(sb, (Entity) object);
                }
            }
            if(!hasroot){
                if(root != null){
                    if(object.getClass().getName().equals(root.getClass().getName())){
                        Entity eObject = (Entity) object;
                        try {
                            if(root.getEnteId() == eObject.getEnteId()){
                                String path = IO.getDBFolderWithID(eObject);
                                File file = new File(path);
                                if(file.exists()){
                                    addOldRoot(sb, eObject);
                                } else {
                                    addThisRoot(root, sb);
                                }
                            } else {
                                addThisRoot(root, sb);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(CapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        addThisRoot(root, sb);
                    }
                } else {
                    addOldRoot(sb, (Entity) object);
                }
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private static void addThisRoot(Entity root, StringBuilder sb){
        try {
            Represent r = new Represent(root);
            Capsule c = new Capsule(r);
            sb.append("{root:");
            sb.append(c.make());
            sb.append("}");
        } catch (Exception ex) {
            Logger.getLogger(CapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void addOldRoot(StringBuilder sb, Entity root){
        try {
            String path = IO.getDBFolderWithID(root);
            File file = new File(path);
            if(file.exists()){
                String line = String.join("\n", Files.readAllLines(Paths.get(path)));
                String key = "{root:";
                List<String> list = new ArrayList<>();
                StringTools.splitMsg(list, line.substring(1), "{", "}");
                list.forEach(str -> {
                    if(str.startsWith(key)){
                        sb.append(str);
                    }
                });
            }
        } catch (Exception ex) {
            Logger.getLogger(CapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void addThisSuperente(Entity superente, StringBuilder sb){
        try {
            Represent r = new Represent(superente);
            Capsule c = new Capsule(r);
            sb.append("{superente:");
            sb.append(c.make());
            sb.append("}");
        } catch (Exception ex) {
            Logger.getLogger(CapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void addOldSuperente(StringBuilder sb, Entity superente){
        try {
            String path = IO.getDBFolderWithID(superente);
            File file = new File(path);
            if(file.exists()){
                String line = String.join("\n", Files.readAllLines(Paths.get(path)));
                String key = "{superente:";
                List<String> list = new ArrayList<>();
                StringTools.splitMsg(list, line.substring(1), "{", "}");
                list.forEach(str -> {
                    if(str.startsWith(key)){
                        sb.append(str);
                    }
                });
            }
        } catch (Exception ex) {
            Logger.getLogger(CapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Represent tryGetSuperente(Entity entity){
        try {
            Capsule cap = new Capsule(tryGetSuperenteAsString(entity));
            return cap.extract();
        } catch (Exception e) {
        }
        return null;
    }
    
    public static String tryGetSuperenteAsString(Entity entity){
        try {
            String path = IO.getDBFolderWithID(entity);
            File file = new File(path);
            if(file.exists()){
                try(BufferedReader r = Files.newBufferedReader(Paths.get(path))) {
                    String line = r.readLine();
                    if(line.toLowerCase().contains("superente:")){
                        int init = line.toLowerCase().indexOf("superente:");
                        init--;
                        int end = init+1;
                        int key = 1;
                        while(key > 0){
                            if(line.charAt(end) == '{'){
                                key++;
                            } else if(line.charAt(end) == '}'){
                                key--;
                            }
                            end++;
                            if(end > line.length()){
                                break;
                            }
                        }
                        return line.substring(init, end);
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
