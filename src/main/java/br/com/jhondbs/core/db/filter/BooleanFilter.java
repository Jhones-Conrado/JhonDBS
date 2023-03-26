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

import br.com.jhondbs.core.db.base.Entidade;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.errors.AttributeNotFind;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Um filtro boleano que verificará um determinado campo de uma entidade.
 * @author jhonessales
 */
public class BooleanFilter implements ItemFilter{
    
    private String field;
    private boolean value;

    public BooleanFilter(String field, boolean value) {
        this.field = field;
        this.value = value;
    }
    
    /**
     * Vai verificar se a entidade possui um campo com o nome definido na criação
     * do filtro e também se esse valor passa no teste, de acordo com o método de
     * filtragem definido na criação do filtro.
     * @param e Entidade a ser filtrada.
     * @return Verdadeiro para caso tenha passado no teste.
     */
    @Override
    public boolean filtrar(Entidade e) {
        try {
            Object val = FieldsManager.getValueFrom(field, e);
            return (boolean) val == value;
        } catch (IllegalArgumentException | IllegalAccessException  ex) {
            Logger.getLogger(BooleanFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(BooleanFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public String getField(){
        return this.field;
    }
    
    public boolean getValue(){
        return this.value;
    }
    
}
