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
package br.com.jhondbs.core.db.io.capsule;

import br.com.jhondbs.core.db.base.Represent;
import br.com.jhondbs.core.db.io.Reflection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
public class ClassDictionary {
    
    private static Properties dictionary;
    
    public static int getIndex(Class clazz){
        if(get().containsKey(clazz.getName())){
            return Integer.valueOf(get().getProperty(clazz.getName()));
        }
        return -1;
    }
    
    public static Class fromIndex(int index){
        Set<Object> keySet = get().keySet();
        for(Object key : keySet){
            int i = Integer.parseInt(get().getProperty((String) key).toString());
            if(i == index){
                try {
                    try {
                        return Class.forName((String) key);
                    } catch (Exception e) {
                        return Thread.currentThread().getContextClassLoader().loadClass((String) key);
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ClassDictionary.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(ClassDictionary.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
    
    public static Properties get(){
        if(dictionary == null){
            try {
                startDictionary();
            } catch (IOException ex) {
                Logger.getLogger(ClassDictionary.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return dictionary;
    }
    
    /**
     * Initializes the database class dictionary if it does not already exist.
     * @throws IOException 
     */
    private static void startDictionary() throws IOException{
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
                dictionary.put(Character.class.getName(), String.valueOf(dictionary.size()));
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
            List<String> all = Reflection.allImplementsNotAbstract(Object.class);
            all.forEach(cl -> {
                if(!dictionary.containsKey(cl)){
                    dictionary.put(cl, String.valueOf(dictionary.size()));
                }
            });
            new File("db").mkdirs();
            dictionary.store(new FileOutputStream(dic), "JhonDBS Class Dictionary");
        }
    }
    
    public static boolean isArrayMap(Object object){
        return
                Reflection.isInstance(object.getClass(), List.class) ||
                Reflection.isInstance(object.getClass(), Set.class) ||
                Reflection.isInstance(object.getClass(), Map.class) ||
                object.getClass().getName().contains("[");
    }
    
    public static boolean isDate(Object object){
        return 
                Reflection.isInstance(object.getClass(), Date.class) ||
                Reflection.isInstance(object.getClass(), Calendar.class);
    }
    
}
