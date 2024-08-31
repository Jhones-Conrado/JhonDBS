/*
 * The MIT License
 *
 * Copyright 2024 Jhones Sales.
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
package br.com.jhondbs.core.db.filter;

import br.com.jhondbs.core.tools.FieldsManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.jhondbs.core.db.interfaces.Entity;

/**
 * A boolean filter that checks a given field of an entity.<br>
 * Um filtro boleano que verificará um determinado campo de uma entidade.
 * @author jhonessales
 */
public class BooleanFilter implements FilterCondition{
    
    /**
     * Field name.
     */
    private String field;
    
    /**
     * Expected field value.
     */
    private boolean value;

    public BooleanFilter(String field, boolean value) {
        this.field = field;
        this.value = value;
    }
    
    /**
     * ENGLISH<br>
     * It will check if the entity has a field with the name defined when creating
     * the filter and also if this value passes the test, according to the
     * filtering method defined when creating the filter.<br><br>
     * PORTUGUÊS<br>
     * Vai verificar se a entidade possui um campo com o nome definido na criação
     * do filtro e também se esse valor passa no teste, de acordo com o método de
     * filtragem definido na criação do filtro.
     * @param entity Entity a ser filtrada.
     * @return Verdadeiro para caso tenha passado no teste.
     */
    @Override
    public boolean filter(Entity entity) {
        try {
            Object val = FieldsManager.getValueFrom(field, entity);
            return (boolean) val == value;
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException  ex) {
            Logger.getLogger(BooleanFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * the name of the field defined at creation.
     * @return 
     */
    public String getField(){
        return this.field;
    }
    
    /**
     * the value of the field defined at creation
     * @return 
     */
    public boolean getValue(){
        return this.value;
    }
    
}
