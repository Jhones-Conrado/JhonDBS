/*
 * The MIT License
 *
 * Copyright 2024 jhones.
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
package br.com.jhondbs.core.db.obj;

import br.com.jhondbs.core.db.Mapper;
import br.com.jhondbs.core.db.capsule.Assist;
import br.com.jhondbs.core.db.capsule.Reader;
import br.com.jhondbs.core.db.interfaces.Cold;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 *
 * @author jhones
 */
public class ColdEntity {
    
    private transient String innerId = UUID.randomUUID().toString();
    
    @Cold
    private Entity entity;
    
    public String id;
    public int index;
    
    private Map<String, Object> map = new HashMap<>();
    
    public ColdEntity() {
    }
    
    public ColdEntity(Entity entity) throws Exception {
        this.entity = entity;
        this.id = entity.getId();
        this.index = ClassDictionary.getIndex(entity.getClass());
    }
    
    public void set(Entity entity) throws Exception {
        this.entity = entity;
        this.id = entity.getId();
        this.index = ClassDictionary.getIndex(entity.getClass());
    }
    
    public <T extends Entity>T get() throws Exception {
        if(this.entity == null) {
            this.entity = Mapper.loadCold(this, "entity");
        }
        return (T) this.entity;
    }
    
    public String getField(String name) throws IOException {
        load();
        return (String) map.getOrDefault(name, "");
    }
    
    public void load() throws FileNotFoundException, IOException {
        if(map.isEmpty()) {
            String path = Assist.getRootPath(id, index);
            Properties p = new Properties();
            p.load(new FileInputStream(new File(path)));
            String fieldsStr = p.getProperty("fields");
            List<String> capsules = Reader.splitCapsules(fieldsStr);
            for(String str : capsules) {
                Map<String, String> map = Reader.splitCapsuleAsKeyValueMap(str);
                String field = map.keySet().stream().findFirst().get();
                Map<String, String> map2 = Reader.splitCapsuleAsKeyValueMap(map.values().stream().findFirst().get());
                String value = map2.values().stream().findFirst().get();
                map.put(field, value);
            }
        }
    }

//    @Override
//    public boolean equals(Object obj) {
//        if(obj.getClass().isAssignableFrom(this.getClass())) {
//            ColdEntity c = (ColdEntity) obj;
//            return c.innerId.equals(this.innerId);
//        }
//        
//        if(obj.getClass().isAssignableFrom(Entity.class)) {
//            Entity e = (Entity) obj;
//            if(ClassDictionary.getIndex(e.getClass()) == this.index) {
//                try {
//                    return e.getId().equals(this.id);
//                } catch (Exception ex) {
//                }
//            }
//        }
//
//        if(!this.getClass().isAssignableFrom(obj.getClass())) return false;
//
//        ColdEntity c = (ColdEntity) obj;
//        return this.id.equals(c.id);
//    }
//
//    @Override
//    public int hashCode() {
//        if(id == null || id.isBlank()) {
//            int hash = 0;
//            for (char c : innerId.toCharArray()) {
//                hash = 31 * hash + c; // Algoritmo baseado em String.hashCode()
//            }
//            return hash;
//        } else {
//            int hash = 0;
//            for (char c : id.toCharArray()) {
//                hash = 31 * hash + c; // Algoritmo baseado em String.hashCode()
//            }
//            return hash;
//        }
//    }
    
}
