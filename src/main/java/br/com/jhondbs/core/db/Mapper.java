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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 *
 * @author jhones
 */
public class Mapper {
    
    private static WeakHashMap<Entity, List<Ref>> map = new WeakHashMap<>();
    
    public static void add(Entity entity, String str) {
        add(entity, Arrays.asList(str.split("::")));
    }
    
    public static void add(Entity entity, List<String> refs) {
        List<Ref> list = new ArrayList<>();
        for(String str : refs) {
            String[] split = str.split(":");
            Ref ref = new Ref(split[1], Integer.valueOf(split[0]));
            list.add(ref);
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
    
}
