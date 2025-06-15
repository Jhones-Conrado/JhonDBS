/*
 * The MIT License
 *
 * Copyright 2025 jhones.
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

import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.tools.Reflection;
import br.com.jhondbs.core.tools.StringTools;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mantém todos os métodos auxiliares de encapsulamento de objetos e entidades.
 * @author jhones
 */
public class Encapsulator {
    
    public static String encapsularField(Field field, Object object, Bottle bottle) throws Exception {
        field.setAccessible(true);
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(field.getName()).append(":");
        sb.append(encapsularObjeto(object, field.isAnnotationPresent(Cascate.class), bottle));
        sb.append("}");
        return sb.toString();
    }
    
    public static String encapsularObjeto(Object objeto, boolean cascate, Bottle bottle) throws Exception {
        if(objeto.getClass().isEnum()) {
            return encapsuleEnum((Enum) objeto);
        } else {
            if(Reflection.isPrimitive(objeto.getClass()) || Reflection.isNumerical(objeto.getClass()) || Reflection.isDate(objeto.getClass())) {
                return encapsulePrimitive(objeto);
            } else if(Reflection.isArrayMap(objeto)) {
                return encapsuleArray(objeto, cascate, bottle);
            } else if(Reflection.isInstance(objeto.getClass(), Entity.class)) {
                Entity ente = (Entity) objeto;
                if(!bottle.bottles.containsKey(ente.getId())) {
                    Assist.createBottle(ente, bottle.bottles, bottle.modoOperacional, bottle.TEMP_DB, bottle.entity, cascate);
                }
                bottle.putRef(ente);
                return encapsuleId(ente);
            } else if(objeto instanceof File file) {
                if(file.exists()) {
                    if(!bottle.files.containsKey(file.getName())) {
                        bottle.files.put(file.getName(), file);
                    }
                    return "{file:"+file.getName()+"}";
                } else {
                    throw new FileNotFoundException("Arquivo não encontrado: "+file);
                }
            } else if(objeto instanceof Image img) {
                byte[] bytes = ImageWorker.getImageData(img);
                String hash = StringTools.generateMD5Hash(bytes);
                if(!bottle.imgs.containsKey(hash)) {
                    bottle.imgs.put(hash, img);
                }
                return "{img:"+hash+"}";
            } else {
                // Objeto complexo que precisa ser serializado.
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append(String.valueOf(ClassDictionary.getIndex(objeto.getClass())));
                sb.append(":");
                        
                List<Field> fields = FieldsManager.getAllSerializebleFields(objeto.getClass());
                for(Field field : fields) {
                    field.setAccessible(true);
                    Object get = field.get(objeto);
                    if(get != null) {
                        sb.append(encapsularField(field, get, bottle));
                    }
                }
                sb.append("}");
                return sb.toString();
            }
        }
    }
    
    private static String encapsuleEnum(Enum e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(e.getClass())))
                .append(":")
                .append(e.toString())
                .append("}");
        return sb.toString();
    }
    
    private static String encapsulePrimitive(Object object) {
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
    
    private static String encapsuleId(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
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
    
    private static String encapsuleArray(Object object, boolean cascate, Bottle bottle) throws Exception {
        if(object == null) {
            return "{}";
        }
        
        int type = 0;
        StringBuilder sb = new StringBuilder();
        
        if(Reflection.isInstance(object.getClass(), List.class)) {
            List l = (List) object;
            if(l.isEmpty()) {
                return "{list:{}}";
            }
        } else if(Reflection.isInstance(object.getClass(), Set.class)) {
            type = 1;
            Set set = (Set) object;
            if(set.isEmpty()) {
                return "{list:{}}";
            }
        } else if(Reflection.isInstance(object.getClass(), Map.class)) {
            type = 2;
            Map m = (Map) object;
            if(m.isEmpty()) {
                return "{map:{}}";
            }
        } else {
            type = 3;
            ArrayList<Object> list = ArrayWorker.toArrayList(object);
            if(list.isEmpty()) {
                return "{list:{}}";
            }
        }
        
        if(type != 2 || object.getClass().getName().contains("[")) {
            sb.append("{")
                .append("list")
                .append(":");
        } else {
            sb.append("{")
                .append("map")
                .append(":");
        }
        
        if(type == 0 || type == 3) {
            List list = null;
            if(type == 3) {
                list = ArrayWorker.toArrayList(object);
            } else {
                list = (List) object;
            }
            if(!list.isEmpty()) {
                for(Object obj : list) {
                    sb.append(encapsularObjeto(obj, cascate, bottle));
                }
            }
        } else if(type == 1) {
          Set set = (Set) object;
          List list = new ArrayList();
          list.addAll(set);
          if(!list.isEmpty()) {
                for(Object obj : list) {
                    sb.append(encapsularObjeto(obj, cascate, bottle));
                }
            }
        } else if(Reflection.isInstance(object.getClass(), Map.class)) {
            Map map = (Map) object;
            Set keys = map.keySet();
            for(Object key : keys) {
                sb.append(encapsularObjeto(key, cascate, bottle));
                sb.append(encapsularObjeto(map.get(key), cascate, bottle));
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
}
