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
package br.com.jhondbs.core.db.obj;

import br.com.jhondbs.core.db.interfaces.Entity;
import java.util.ArrayList;

/**
 *
 * @author jhones
 */
public class EntityList<T extends Entity> extends ArrayList<T> {
    
    public T get(String id) throws Exception {
        if (id == null) return null;
        for (T entity : this) {
            if (id.equals(entity.getId())) {
                return entity;
            }
        }
        return null;
    }
    
    public boolean remove(String id) throws Exception {
        if (id == null) return false;
        for (int i = 0; i < size(); i++) {
            if (id.equals(get(i).getId())) {
                remove(i);
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(String id) throws Exception {
        if(id == null) return false;
        for(Entity entity : this) {
            if(entity.getId().equals(id)) return true;
        }
        return false;
    }
}
