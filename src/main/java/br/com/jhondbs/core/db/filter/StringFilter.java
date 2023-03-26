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

import br.com.jhondbs.core.db.base.Entidade;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.errors.AttributeNotFind;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
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
    
    private final int metodo;
    private final String campo;
    private final String parametro;
    private final boolean ignore_case;
    
    /**
     * Cria um filtro que verificará se o campo da entidade tem o valor IGUAL ao
     * definido na criação do filtro.
     * @param field Nome da variável que será verificada.
     * @param value Valor que será pesquisado na variável.
     */
    public StringFilter(String field, String value) {
        this.metodo = 0;
        this.campo = field;
        this.parametro = value;
        this.ignore_case = false;
    }
    
    /**
     * Cria um filtro que verificará se o campo da entidade tem o valor IGUAL ao
     * definido na criação do filtro.
     * @param field Nome da variável que será verificada.
     * @param value Valor que será pesquisado na variável.
     * @param ignore_case Se deve ignorar a diferença entre maiúscula e minúscula.
     */
    public StringFilter(String field, String value, boolean ignore_case) {
        this.metodo = 0;
        this.campo = field;
        if(ignore_case){
            this.parametro = value.toUpperCase();
        } else {
            this.parametro = value;
        }
        this.ignore_case = ignore_case;
    }
    
    /**
     * Cria um filtro que verificará se o campo da entidade tem um valor que
     * coincida com o parâmetro da criação do filtro.Podendo ser POSSUI, COMEÇA
     * ou TERMINA.
     * @param metodo Usado pra a filtragem, podendo ser uma busca dos tipos
     * Começa com, Termina com ou Contém.
     * @param field Nome da variável que será verificada.
     * @param value Texto que será usado como base de busca.
     */
    public StringFilter(int metodo, String field, String value) {
        this.metodo = metodo;
        this.campo = field;
        this.parametro = value;
        this.ignore_case = false;
    }
    
    /**
     * Cria um filtro que verificará se o campo da entidade tem um valor que
     * coincida com o parâmetro da criação do filtro.Podendo ser POSSUI, COMEÇA
     * ou TERMINA.
     * @param metodo Usado pra a filtragem, podendo ser uma busca dos tipos
     * Começa com, Termina com ou Contém.
     * @param field Nome da variável que será verificada.
     * @param value Texto que será usado como base de busca.
     * @param ignore_case Se deve ignorar a diferença entre maiúscula e minúscula.
     */
    public StringFilter(int metodo, String field, String value, boolean ignore_case) {
        this.metodo = metodo;
        this.campo = field;
        if(ignore_case){
            this.parametro = value.toUpperCase();
        } else {
            this.parametro = value;
        }
        this.ignore_case = ignore_case;
    }
    
    /**
     * Busca nos campos da entidade se ela contém um campo com o nome específicado
     * e se o valor desse campo passa na filtragem definida na criação do filtro.
     * @param e Entidade a ser filtrada.
     * @return Verdadeiro caso tenha passado no teste.
     */
    @Override
    public boolean filtrar(Entidade e) {
        try {
            String valor = (String) FieldsManager.getValueFrom(campo, e);
            if(valor != null){
                if(this.ignore_case){
                    valor = valor.toUpperCase();
                }
                switch (metodo) {
                    case IGUAL:
                        return valor.equals(parametro);
                    case POSSUI:
                        return valor.contains(parametro);
                    case COMECA:
                        return valor.startsWith(parametro);
                    case TERMINA:
                        return valor.endsWith(parametro);
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
     * Retorna o número do método que o filtro foi criado.
     * @return Metodo.
     */
    public int getMetodo(){
        return metodo;
    }
    
    public String getField(){
        return this.campo;
    }
    
    /**
     * Retorna o parâmetro de busca que o filtro foi criado.
     * @return Parametro.
     */
    public String getParametro(){
        return parametro;
    }
    
    public boolean isIgnoreCase(){
        return ignore_case;
    }
    
}
