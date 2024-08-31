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
 * A textual filter that will check the field of an entity.<br><br>
 * Um filtro textual que vai veriricar o campo de uma entidade.
 * @author jhonesconrado
 */
public class StringFilter implements FilterCondition{
    
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
