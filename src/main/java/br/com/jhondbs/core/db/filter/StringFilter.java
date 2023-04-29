/*
 * Copyright (C) 2022 jhonesconrado
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
 * A textual filter that will check the field of an entity.<br><br>
 * Um filtro textual que vai veriricar o campo de uma entidade.
 * @author jhonesconrado
 */
public class StringFilter implements ItemFilter{
    
    public static final int IGUAL = 0;
    public static final int POSSUI = 1;
    public static final int COMECA = 2;
    public static final int TERMINA = 3;
    
    public static final int EQUALS = 0;
    public static final int CONTAINS = 1;
    public static final int STARTS = 2;
    public static final int ENDS = 3;
    
    private final int method;
    private final String field;
    private final String parameter;
    private final boolean ignore_case;
    
    /**
     * Creates a filter that will check if the entity's field has the value EQUAL
     * to the one defined when creating the filter.
     * <br><br>
     * Cria um filtro que verificará se o campo da entidade tem o valor IGUAL ao
     * definido na criação do filtro.
     * @param field Name of the variable to be checked.<br>
     * Nome da variável que será verificada.
     * @param value Value that will be searched in the variable.<br>
     * Valor que será pesquisado na variável.
     */
    public StringFilter(String field, String value) {
        this.method = 0;
        this.field = field;
        this.parameter = value;
        this.ignore_case = false;
    }
    
    /**
     * Creates a filter that will check if the entity's field has the value EQUAL
     * to the one defined when creating the filter.<br><br>
     * 
     * Cria um filtro que verificará se o campo da entidade tem o valor IGUAL ao
     * definido na criação do filtro.
     * @param field Name of the variable to be checked.<br>
     * Nome da variável que será verificada.
     * @param value Value that will be searched in the variable.<br>
     * Valor que será pesquisado na variável.
     * @param ignore_case Whether to ignore the case difference.<br>
     * Se deve ignorar a diferença entre maiúscula e minúscula.
     */
    public StringFilter(String field, String value, boolean ignore_case) {
        this.method = 0;
        this.field = field;
        if(ignore_case){
            this.parameter = value.toUpperCase();
        } else {
            this.parameter = value;
        }
        this.ignore_case = ignore_case;
    }
    
    /**
     * Creates a filter that will check if the entity's field has a value that
     * matches the filter's creation parameter. It can be HAS, STARTS or ENDS.
     * <br><br>
     * Cria um filtro que verificará se o campo da entidade tem um valor que
     * coincida com o parâmetro da criação do filtro.Podendo ser POSSUI, COMEÇA
     * ou TERMINA.
     * @param method Used for filtering, it can be a search of the types
     * Starts with, Ends with or Contains.<br><br>
     * Usado pra a filtragem, podendo ser uma busca dos tipos
     * Começa com, Termina com ou Contém.
     * @param field Name of the variable to be checked.<br>
     * Nome da variável que será verificada.
     * @param value Value that will be searched in the variable.<br>
     * Valor que será pesquisado na variável.
     */
    public StringFilter(int method, String field, String value) {
        this.method = method;
        this.field = field;
        this.parameter = value;
        this.ignore_case = false;
    }
    
    /**
     * Creates a filter that will check if the entity's field has a value that
     * matches the filter's creation parameter. It can be HAS, STARTS or ENDS.
     * <br><br>
     * Cria um filtro que verificará se o campo da entidade tem um valor que
     * coincida com o parâmetro da criação do filtro.Podendo ser POSSUI, COMEÇA
     * ou TERMINA.
     * @param method Used for filtering, it can be a search of the types
     * Starts with, Ends with or Contains.<br><br>
     * Usado pra a filtragem, podendo ser uma busca dos tipos
     * Começa com, Termina com ou Contém.
     * @param field Name of the variable to be checked.<br>
     * Nome da variável que será verificada.
     * @param value Value that will be searched in the variable.<br>
     * Valor que será pesquisado na variável.
     * @param ignore_case Whether to ignore the case difference.<br>
     * Se deve ignorar a diferença entre maiúscula e minúscula.
     */
    public StringFilter(int method, String field, String value, boolean ignore_case) {
        this.method = method;
        this.field = field;
        if(ignore_case){
            this.parameter = value.toUpperCase();
        } else {
            this.parameter = value;
        }
        this.ignore_case = ignore_case;
    }
    
    /**
     * Searches the entity's fields if it contains a field with the specified name
     * and if the value of that field passes the filtering defined when creating
     * the filter.
     * <br><br>
     * Busca nos campos da entidade se ela contém um campo com o nome específicado
     * e se o valor desse campo passa na filtragem definida na criação do filtro.
     * @param entity Entity to be filtered.<br>
     * Entity a ser filtrada.
     * @return True if it passed the test.<br>
     * Verdadeiro caso tenha passado no teste.
     */
    @Override
    public boolean filter(Entity entity) {
        try {
            String value = (String) FieldsManager.getValueFrom(field, entity);
            if(value != null){
                if(this.ignore_case){
                    value = value.toUpperCase();
                }
                switch (method) {
                    case IGUAL:
                        return value.equals(parameter);
                    case POSSUI:
                        return value.contains(parameter);
                    case COMECA:
                        return value.startsWith(parameter);
                    case TERMINA:
                        return value.endsWith(parameter);
                    default:
                        break;
                }
            }
            
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException  ex) {
            Logger.getLogger(StringFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Returns the number of the method that the filter was created.<br>
     * Retorna o número do método que o filtro foi criado.
     * @return Metodo.
     */
    public int getMetodo(){
        return method;
    }
    
    /**
     * Returns the name of the field defined at creation.<br>
     * Retorna o nome do campo definido na criação.
     * @return 
     */
    public String getField(){
        return this.field;
    }
    
    /**
     * Returns the search parameter that the filter was created with.<br>
     * Retorna o parâmetro de busca que o filtro foi criado.
     * @return Parameter.
     */
    public String getParameter(){
        return parameter;
    }
    
    public boolean isIgnoreCase(){
        return ignore_case;
    }
    
}
