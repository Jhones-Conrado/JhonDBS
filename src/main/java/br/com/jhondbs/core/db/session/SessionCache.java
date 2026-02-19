/*
 * The MIT License
 *
 * Copyright 2026 jhones.
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
package br.com.jhondbs.core.db.session;

import br.com.jhondbs.core.db.capsule.Bottle;
import br.com.jhondbs.core.db.interfaces.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Armazena referências a entidades e mantém em memória para evitar sobrecarga
 * de IO.
 * Preserva tempo médio de vida.
 * @author jhones
 */
public class SessionCache {
    
    public static long LIFE = TimeUnit.MINUTES.toNanos(20);
    
    private long time = System.nanoTime();
    private Map<String, Entity> entities = new HashMap();
    private Map<Entity, Bottle> bottles = new HashMap<>();
    
    public SessionCache(){}
    
    public void addEntity(Bottle entity) throws Exception {
        if(!entities.containsKey(entity.entity.getId())) {
            touch();
            entities.putIfAbsent(entity.entity.getId(), entity.entity);
            bottles.put(entity.entity, entity);
        }
    }
    
    public void removeEntity(String id) {
        if(entities.containsKey(id)) {
            touch();
            Entity get = entities.get(id);
            entities.remove(id);
            bottles.remove(get);
        }
        
    }
    
    public void removeEntity(Entity entity) throws Exception {
        removeEntity(entity.getId());
    }
    
    public boolean contains(String id) {
        return entities.containsKey(id);
    }
    
    public boolean contains(Bottle bottle) {
        for(String id : bottle.bottles.keySet()) {
            if(contains(id)) return true;
        }
        return false;
    }
    
    public Entity get(String id) {
        if(!contains(id)) return null;
        touch();
        return entities.getOrDefault(id, null);
    }
    
    public Bottle getBottle(String id) {
        if(!contains(id)) return null;
        touch();
        return bottles.get(entities.getOrDefault(id, null));
    }
    
    public void absorb(SessionCache session) {
        if(session == this) return;
        this.entities.putAll(session.entities);
        session.entities.clear();
    }
    
    private void touch() {
        this.time = System.nanoTime();
    }
    
    public boolean isExpired() {
        return (time + LIFE) < System.nanoTime();
    }
    
    public void clear() {
        this.entities.clear();
        this.bottles.clear();
    }
    
}
