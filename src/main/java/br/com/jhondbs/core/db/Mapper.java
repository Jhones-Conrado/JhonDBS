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
package br.com.jhondbs.core.db;

import br.com.jhondbs.core.db.capsule.Ref;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.FieldsManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 *
 * @author jhones
 */
public class Mapper {
    
    private static WeakHashMap<Entity, List<Ref>> map = new WeakHashMap<>();
    public static WeakHashMap<Object, Map<Field, Ref>> coldMap = new WeakHashMap<Object, Map<Field, Ref>>();
    
    public static void add(Entity entity, String str) {
        if(!str.isBlank()) {
            add(entity, Arrays.asList(str.split("::"))
                    .stream()
                    .filter(s -> !s.isBlank())
                    .toList());
        }
    }
    
    public static void add(Entity entity, List<String> refs) {
        List<Ref> list = new ArrayList<>();
        for(String str : refs) {
            list.add(new Ref(str));
        }
        map.put(entity, list);
    }
    
    public static void add(Entity entity, Set<Ref> refs) {
        List<Ref> list = new ArrayList<>();
        list.addAll(refs);
        map.put(entity, list);
    }
    
    public static List<Ref> get(Entity entity) {
        return map.getOrDefault(entity, new ArrayList<>());
    }
    
    public static void addCold(Object object, Field field, Ref ref) {
        coldMap.putIfAbsent(object, new HashMap<>());
        coldMap.get(object).putIfAbsent(field, ref);
    }
    
    public static Ref getCold(Object object, Field field) {
        Map<Field, Ref> m = coldMap.getOrDefault(object, new HashMap<>());
        return m.getOrDefault(field, null);
    }
    
    public static Ref getCold(Object object, String field) {
        Field get = FieldsManager.getAllFields(object)
                .stream().filter(f -> f.getName().equals(field))
                .findFirst()
                .get();
        Map<Field, Ref> m = coldMap.getOrDefault(object, new HashMap<>());
        return m.getOrDefault(get, null);
    }
    
    public static <T extends Entity> T loadCold(Object object, Field field) throws Exception {
        return (T) getCold(object, field).recover();
    }
    
    public static <T extends Entity> T loadCold(Object object, String field) throws Exception {
        return (T) getCold(object, field).recover();
    }
    
}
