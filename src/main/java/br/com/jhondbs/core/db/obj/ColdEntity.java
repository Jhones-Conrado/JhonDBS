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

import br.com.jhondbs.core.db.capsule.Bottle;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;

/**
 *
 * @author jhones
 */
public class ColdEntity {
    
    private Entity entity;
    
    private int index;
    private String id;
    
    private ClassLoader loader;
    
    public ColdEntity() {
    }
    
    public ColdEntity(Entity entity) throws Exception {
        this.entity = entity;
        this.index = ClassDictionary.getIndex(entity.getClass());
        this.id = entity.getId();
    }
    
    public void set(Entity entity) throws Exception {
        this.entity = entity;
        this.index = ClassDictionary.getIndex(entity.getClass());
        this.id = entity.getId();
    }
    
    public <T extends Entity>T get() throws Exception {
        if(this.entity == null) {
            Bottle bottle = new Bottle(ClassDictionary.fromIndex(index), id, Bottle.ROOT_STAGE);
            this.entity = bottle.entity;
        }
        return (T) this.entity;
    }
    
}
