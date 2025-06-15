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
package br.com.jhondbs.core.db.capsule;

import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;

/**
 * Criado específicamente para facilitar o referenciamento de entidades.
 * Mantém o índice de classe e o id de uma entidade.
 * @author jhones
 */
public class Ref extends Pairing<String, Integer>{

    public Ref() {
    }
    
    public Ref(Class clazz, String id) {
        super(id, ClassDictionary.getIndex(clazz));
    }
    
    public Ref(String key, Integer value) {
        super(key, value);
    }
    
    public Ref(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        super(entity.getId(), ClassDictionary.getIndex(entity.getClass()));
    }

    public Ref(String pair) {
        String[] split = pair.split(":");
        String k = split[1].substring(0, split[1].length());
        if(k.endsWith("}")) {
            k = k.substring(0, k.length()-1);
        }
        setKey(k);
        setValue(Integer.valueOf(split[0].replaceAll("[^0-9]", "")));
    }
    
    public Class recoverClass() {
        return ClassDictionary.fromIndex(getValue());
    }
    
    public Entity recover() throws Exception{
        Bottle bottle = new Bottle.BottleBuilder()
                .entityClass(recoverClass())
                .id(getKey())
                .modoOperacional(Bottle.ROOT_STAGE)
                .build();
//        bottle.cleanFolders();
        return bottle.entity;
    }
    
    @Override
    public String toString() {
        return getValue()+":"+getKey();
    }
    
}
