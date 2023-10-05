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
package br.com.jhondbs.core.db.io.capsule.descapsulators;

import br.com.jhondbs.core.db.base.Entity;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.io.Reflection;
import br.com.jhondbs.core.db.io.capsule.Capsule;
import br.com.jhondbs.core.db.io.capsule.ClassDictionary;
import br.com.jhondbs.core.tools.StringTools;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
public class DescapsulateObject {
    
    public static <T> T extract(String msg){
        if(msg.startsWith("{") && msg.endsWith("}") && msg.contains(":")){
            int pontos = msg.indexOf(":");
            String index = msg.substring(1, pontos);
            if(StringTools.isNumericalString(index)){
                Class clazz = ClassDictionary.fromIndex(Integer.parseInt(index));
                if(Reflection.isNumerical(clazz)){
                    return (T) extractNumber(clazz, msg);
                } else if(Reflection.isInstance(clazz, String.class)){
                    return (T) msg.substring(msg.indexOf(":")+1, msg.length()-1);
                } else if(Reflection.isInstance(clazz, boolean.class) || Reflection.isInstance(clazz, Boolean.class)){
                    return (T) extractBoolean(msg);
                } else if(Reflection.isInstance(clazz, byte.class) || Reflection.isInstance(clazz, Byte.class)){
                    return (T) extractByte(msg);
                } else if(Reflection.isInstance(clazz, Date.class) || Reflection.isInstance(clazz, Calendar.class)){
                    return (T) extractDate(clazz, msg);
                } else {
                    return (T) extractObject(clazz, msg);
                }
            } else {
                if(index.equals("l")){
                    return (T) extractList(msg);
                } else if(index.equals("m")){
                    return (T) extractMap(msg);
                }
            }
        }
        return null;
    }
    
    private static Object extractNumber(Class clazz, String msg){
        String number = msg.substring(msg.indexOf(":")+1, msg.length()-1);
        if(Reflection.isInstance(clazz, short.class) || Reflection.isInstance(clazz, Short.class)){
            if(number.contains(".")){
                number = number.substring(0, number.indexOf("."));
            }
            short s = new Short(number);
            return s;
        } else if(Reflection.isInstance(clazz, int.class) || Reflection.isInstance(clazz, Integer.class)){
            if(number.contains(".")){
                number = number.substring(0, number.indexOf("."));
            }
            int i = new Integer(number);
            return i;
        } else if(Reflection.isInstance(clazz, long.class) || Reflection.isInstance(clazz, Long.class)){
            if(number.contains(".")){
                number = number.substring(0, number.indexOf("."));
            }
            long l = new Long(number);
            return l;
        } else if(Reflection.isInstance(clazz, float.class) || Reflection.isInstance(clazz, Float.class)){
            float f = new Float(number);
            return f;
        } else if(Reflection.isInstance(clazz, double.class) || Reflection.isInstance(clazz, Double.class)){
            double d = new Double(number);
            return d;
        } else if(Reflection.isInstance(clazz, BigInteger.class)){
            if(number.contains(".")){
                number = number.substring(0, number.indexOf("."));
            }
            BigInteger b = new BigInteger(number);
            return b;
        } else if(Reflection.isInstance(clazz, BigDecimal.class)){
            BigDecimal b = new BigDecimal(number);
            return b;
        }
        return null;
    }
    
    private static Object extractBoolean(String msg){
        String value = msg.substring(msg.indexOf(":")+1, msg.length()-1);
        return new Boolean(value);
    }
    
    private static Object extractByte(String msg){
        String value = msg.substring(msg.indexOf(":")+1, msg.length()-1);
        return new Byte(value);
    }
    
    private static Object extractDate(Class clazz, String msg){
        String value = msg.substring(msg.indexOf(":")+1, msg.length()-1);
        Date d = new Date(value);
        if(Reflection.isInstance(clazz, Date.class)){
            return d;
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c;
        }
    }
    
    private static Object extractList(String msg){
        int start = msg.indexOf("[")+1;
        int end = msg.lastIndexOf("]");
        
        String itensTxt = msg.substring(start, end);
        
        List<String> itens = new ArrayList<>();
        StringTools.splitMsg(itens, itensTxt, "{", "}");
        
        List<Object> objetos = new ArrayList<>();
        for(Object o : itens){
            objetos.add(new Capsule((String) o).extract());
        }
        
        return objetos;
    }
    
    private static Object extractMap(String msg){
        String itens = msg.substring(msg.indexOf("[")+1, msg.lastIndexOf("]"));
        List<String> chaveValor = new ArrayList<>();
        StringTools.splitMsg(chaveValor, itens, "(", ")");
        
        Map map = new HashMap();
        
        chaveValor.forEach(s -> {
            List<String> kv = new ArrayList<>();
            StringTools.splitMsg(kv, s, "{", "}");
            if(!kv.isEmpty()){
                Object key = new Capsule(kv.get(0)).extract();
                Object value = null;
                if(kv.size() > 1){
                    value = new Capsule(kv.get(1)).extract();
                }
                map.put(key, value);
            }
        });
        
        return map;
    }
    
    private static Object extractObject(Class clazz, String msg){
        int point = msg.indexOf(":");
        try {
            String nextPoint = msg.substring(point+1, point+2);
            if(StringTools.isNumericalString(nextPoint)){
                Entity et = (Entity) clazz.newInstance();
                long id = Long.parseLong(msg.substring(point+1, msg.length()-1));
                return et.load(id);
            }
        } catch (Exception e) {
        }
        
        try {
            Object ins = Reflection.getNewInstance(clazz);
            
            List<String> fieldtxt = new ArrayList<>();
            StringTools.splitMsg(fieldtxt, msg.substring(msg.indexOf(":")+1), "{", "}");
            
            Map<String, String> fieldMap = new HashMap<>();
            
            fieldtxt.forEach(str -> {
                String name = str.substring(1, str.indexOf(":"));
                fieldMap.put(name, str.substring(name.length()+2, str.length()-1));
            });
            
            List<Field> fields = FieldsManager.getAllFields(ins);
            
            for(Field field : fields){
                String fname = field.getName();
                if(fieldMap.containsKey(fname)){
                    field.setAccessible(true);
                    
                    String strValue = fieldMap.get(fname);
                    if(field.getType().isArray()){
                        List value = new Capsule(strValue).extract();
                        field.set(ins, value.toArray());
                    } else if(Reflection.isInstance(field.getType(), Set.class)){
                        List value = new Capsule(strValue).extract();
                        field.set(ins, new HashSet<>(value));
                    } else if(Reflection.isInstance(field.getType(), Properties.class)){
                        Map value = new Capsule(strValue).extract();
                        Properties p = new Properties();
                        p.putAll(value);
                        field.set(ins, p);
                    } else {
                        String index = strValue.substring(1, strValue.indexOf(":"));
                        if(StringTools.isNumericalString(index)){
                            Class cl = ClassDictionary.fromIndex(Integer.parseInt(index));
                            if(Reflection.isInstance(cl, Entity.class)){
                                String content = strValue.substring(strValue.indexOf(":")+1);
                                if(content.startsWith("{")){
                                    Capsule cap = new Capsule(content);
                                    field.set(ins, cap.extract());
                                } else {
                                    content = content.substring(0, content.length()-1);
                                    Entity entity = (Entity) cl.newInstance();
                                    Entity loaded = entity.load(Integer.parseInt(content));
                                    field.set(ins, loaded);
                                }
                            } else {
                                Capsule cap = new Capsule(strValue);
                                field.set(ins, cap.extract());
                            }
                        } else {
                            Capsule cap = new Capsule(strValue);
                            field.set(ins, cap.extract());
                        }
                    }
                }
            }
            return ins;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DescapsulateObject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
