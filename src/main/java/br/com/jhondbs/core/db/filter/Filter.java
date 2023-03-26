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
import java.util.ArrayList;
import java.util.List;

/**
 * Uma classe que pode armazenar configurações de filtragem e ser salva para
 * futuros usos repetitivos.
 * @author jhonesconrado
 */
public class Filter implements Entidade{
    
    private long id = -1l;
    
    /**
     * Avisa ao filtro se deve consedirar todos os itens de filtragem ou somente um.
     */
    public boolean all;
    
    /**
     * Lista de filtros que serão aplicados na filtragem.
     */
    public List<ItemFilter> filters;
    
    /**
     * Cria um novo filtro com valor padrão de verdadeiro para "todos os testes"
     * e uma lista vazia de filters.
     */
    public Filter(){
        this.all = true;
        this.filters = new ArrayList<>();
    }
    
    /**
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
     * Cria um novo filtro que recebe um valor de verdadeiro ou falso para a exigência
     * de "all os testes" e uma lista de filters.
     * @param all Verdadeiro para indicar que a entidade precisa passar em all
     * os testes da lista para ser aprovada. Falso para caso a entidade precise
     * ser aprovada em somente um dos testes.
     * @param filters Lista de itens de filtragem que serão usados no filtro.
     */
    public Filter(boolean all, List<ItemFilter> filters) {
        this.all = all;
        this.filters = filters;
    }
    
    /**
     * Adiciona um novo item de filtro na lista de filtragem.
     * @param filter A ser adicionado na lista de requisitos.
     */
    public void addItem(ItemFilter filter){
        if(!filters.contains(filter)){
            filters.add(filter);
        }
    }
    
    /**
     * Remove um item de filtro da lista de filtragem.
     * @param filtro A ser removido da lista de requisitos.
     */
    public void removeItem(ItemFilter filtro){
        if(filters.contains(filtro)){
            filters.remove(filtro);
        }
    }
    
    /**
     * Recebe uma entidade e verifica se passa no teste de validação.
     * @param e Entidade a ser filtrada.
     * @return Verdadeiro ou Falso para a validação da entidade.
     */
    public boolean filter(Entidade e){
        boolean passou = false;
        for(ItemFilter f : filters){
            if(all){
                passou = true;
                if(!f.filtrar(e)){
                    return false;
                }
            } else {
                passou = false;
                if(f.filtrar(e)){
                    return true;
                }
            }
        }
        return passou;
    }
    
    @Override
    public long getEnteId() {
        return id;
    }

    @Override
    public void onSetId(long id) {
        this.id = id;
    }
    
    public int getFiltroCount(){
        return filters.size();
    }
    
    /**
     * Converte uma lista de itens de filtragem em um objeto filtro configurado
     * com a exigência de all os testes serem verdadeiros para aprovar uma entidade.
     * @param lista Lista de itens de filtragem.
     * @return Objeto Filter com a lista de filtragem.
     */
    public static Filter listToFiltro(List<ItemFilter> lista){
        return new Filter(true, lista);
    }
    
}
