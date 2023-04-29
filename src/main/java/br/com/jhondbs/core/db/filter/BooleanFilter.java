/*
 * Copyright (C) 2022 jhonessales
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
package br.com.jhondbs.core.db.filter;

import br.com.jhondbs.core.db.base.FieldsManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.jhondbs.core.db.base.Entity;

/**
 * A boolean filter that checks a given field of an entity.<br>
 * Um filtro boleano que verificará um determinado campo de uma entidade.
 * @author jhonessales
 */
public class BooleanFilter implements ItemFilter{
    
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
