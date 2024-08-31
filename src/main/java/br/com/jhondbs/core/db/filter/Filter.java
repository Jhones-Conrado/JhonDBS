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

import java.util.ArrayList;
import java.util.List;
import br.com.jhondbs.core.db.interfaces.Entity;

/**
 * A class that can store filtering settings and be saved for future repetitive uses.
 * Uma classe que pode armazenar configurações de filtragem e ser salva para
 * futuros usos repetitivos.
 * @author jhonesconrado
 */
public class Filter implements Entity{
    
    private long id = -1l;
    
    /**
     * Tells the filter whether to consider all filtering items or just one.<br>
     * Avisa ao filtro se deve considerar todos os itens de filtragem ou somente um.
     */
    public boolean all;
    
    /**
     * List of filters that will be applied in the filtering.<br>
     * Lista de filtros que serão aplicados na filtragem.
     */
    public List<FilterCondition> filters;
    
    /**
     * Creates a new filter with a default value of true for "all tests" and an
     * empty list of filters.<br>
     * Cria um novo filtro com valor padrão de verdadeiro para "todos os testes"
     * e uma lista vazia de filters.
     */
    public Filter(){
        this.all = true;
        this.filters = new ArrayList<>();
    }
    
    /**
     * Creates a new filter that takes a value of true or false for the "all tests"
     * requirement and has an initially empty list of filters.<br>
     * Cria um novo filtro que recebe um valor de verdadeiro ou falso para a exigência
     * de "todos os testes" e possui uma lista de filters inicialmente vazia.
     * @param all Verdadeiro para indicar que a entidade precisa passar em all
     * os testes da lista para ser aprovada. Falso para caso a entidade precise
     * ser aprovada em somente um dos testes.
     */
    public Filter(boolean all){
        this.all = all;
        this.filters = new ArrayList<>();
    }
    
    /**
     * Creates a new filter that takes a value of true or false for the "all tests"
     * requirement and has an initially empty list of filters.<br>
     * Cria um novo filtro que recebe um valor de verdadeiro ou falso para a exigência
     * de "all os testes" e uma lista de filtros.
     * @param all Verdadeiro para indicar que a entidade precisa passar em all
     * os testes da lista para ser aprovada. Falso para caso a entidade precise
     * ser aprovada em somente um dos testes.
     * @param filters Lista de itens de filtragem que serão usados no filtro.
     */
    public Filter(boolean all, List<FilterCondition> filters) {
        this.all = all;
        this.filters = filters;
    }
    
    /**
     * Adds a new filter item to the filtering list.<br>
     * Adiciona um novo item de filtro na lista de filtragem.
     * @param filter A ser adicionado na lista de requisitos.
     */
    public void addCondition(FilterCondition filter){
        if(!filters.contains(filter)){
            filters.add(filter);
        }
    }
    
    /**
     * Removes a filter item from the filtering list.<br>
     * Remove um item de filtro da lista de filtragem.
     * @param filtro A ser removido da lista de requisitos.
     */
    public void removeItem(FilterCondition filtro){
        if(filters.contains(filtro)){
            filters.remove(filtro);
        }
    }
    
    /**
     * Receives an entity and checks if it passes the validation test.<br>
     * Recebe uma entidade e verifica se passa no teste de validação.
     * @param entity Entity to be filtered.<br>
     * Entity a ser filtrada.
     * @return 
     */
    public boolean filter(Entity entity){
        boolean approved = false;
        for(FilterCondition f : filters){
            if(all){
                approved = true;
                if(!f.filter(entity)){
                    return false;
                }
            } else {
                approved = false;
                if(f.filter(entity)){
                    return true;
                }
            }
        }
        return approved;
    }
    
//    @Override
//    public long getEnteId() {
//        return id;
//    }
//
//    @Override
//    public void onSetId(long id) {
//        this.id = id;
//    }
    
    /**
     * Returns the number of tests in the filter.<br>
     * Retorna o número de testes no filtro.
     * @return 
     */
    public int getFilterCount(){
        return filters.size();
    }
    
    /**
     * Converte uma lista de itens de filtragem em um objeto filtro configurado
     * com a exigência de all os testes serem verdadeiros para aprovar uma entidade.
     * @param list Lista de itens de filtragem.
     * @return Objeto Filter com a lista de filtragem.
     */
    public static Filter listToFilter(List<FilterCondition> list){
        return new Filter(true, list);
    }
    
}
