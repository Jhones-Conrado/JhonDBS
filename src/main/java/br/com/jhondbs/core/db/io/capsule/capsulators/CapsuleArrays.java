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
import br.com.jhondbs.core.db.io.Reflection;
import br.com.jhondbs.core.db.io.capsule.Capsule;
import br.com.jhondbs.core.db.io.letters.BooleanLetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author jhonessales
 */
public class CapsuleArrays {
    
    public static String encapMultiples(Object object, List<Entity> entities, BooleanLetter letter, Entity superente, Entity root){
        if(Reflection.isInstance(object.getClass(), List.class)){
            return encapList(object, entities, letter, superente, root);
        } else if(Reflection.isInstance(object.getClass(), Set.class)){
            return encapList(List.copyOf((Set) object), entities, letter, superente, root);
        } else if(Reflection.isInstance(object.getClass(), Properties.class)){
            Map map = new HashMap();
            Properties props = (Properties) object;
            props.keySet().forEach(key -> {
                map.put(key, props.get(key));
            });
            return encapMap(map, entities, letter, superente, root);
        } else if(Reflection.isInstance(object.getClass(), Map.class)){
            return encapMap(object, entities, letter, superente, root);
        }
        return null;
    }
    
    public static String encapList(Object object, List<Entity> entities, BooleanLetter letter, Entity superente, Entity root){
        StringBuilder sb = new StringBuilder();
        sb.append("{l");
        sb.append(":[");
        
        List list = (List) object;
        
        list.forEach(obj -> {
            sb.append(new Capsule(obj, entities, letter, superente, root).make());
        });
        
        sb.append("]}");
        return sb.toString();
    }
    
    public static String encapMap(Object object, List<Entity> entities, BooleanLetter letter, Entity superente, Entity root){
        StringBuilder sb = new StringBuilder();
        Map map = (Map) object;
        sb.append("{");
        sb.append("m:[");
        map.keySet().forEach(key -> {
            sb.append("(");
            Capsule ckey = new Capsule(key, entities, letter, superente, root);
            Capsule cvalue = new Capsule(map.get(key), entities, letter, superente, root);
            sb.append(ckey.make());
            sb.append(":");
            sb.append(cvalue.make());
            sb.append(")");
        });
        sb.append("]}");
        return sb.toString();
    }
    
}
