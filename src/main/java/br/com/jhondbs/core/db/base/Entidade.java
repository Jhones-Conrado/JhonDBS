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
package br.com.jhondbs.core.db.base;

import br.com.jhondbs.core.db.errors.DuplicatedUniqueField;
import br.com.jhondbs.core.db.errors.EntIdBadImplementation;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.db.io.IO;
import java.io.Serializable;
import java.util.List;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.jhondbs.core.db.filter.ItemFilter;

/**
 * Responsável por transformar uma classe em uma entidade com funções de CRUD.
 * Uma variável deve ser posta no objeto para que possa armazenar o ID do mesmo.
 * Os métodos setId e getId devem implementar sobre essa variável citada.</br>
 * É FORTEMENTE recomendado que a variável ID criada no objeto que implemente
 * esta classe, tenha o valor -1l por padrão! Isso garantirá que o banco de dados
 * funcione da forma correta.</br>
 * O programa irá verificar o ID da entidade na hora de salvá-la no banco de dados
 * e usará o valore -1l como parâmetro para verificação.</br>
 * Significa que uma entidade que tiver um ID menor que 0, receberá um novo ID
 * gerado no momento.
 * @author jhonesconrado
 */
public interface Entidade extends Serializable, Cloneable{
    
    public static final long serialVersionUID = 1l;
    
    /**
     * Deverá ser implementado de forma a resgatar de alguma variável o ID da entidade.
     * @return ID da entidade.
     */
    long getEnteId();
    
    /**
     * Aplica o ID para a entidade.</br>
     * Uma entidade só deverá poder ter o seu ID alterado caso este seja igual a
     * -1l! Ou seja, após uma entidade receber um ID, este não deverá mais poder
     * ser trocado.
     * @param enteId Novo ID para a entidade.
     */
    default void setEnteId(long enteId){
        if(getEnteId() == -1l){
            onSetId(enteId);
        }
    }
    
    /**
     * ESTE MÉTODO NÃO DEVE SER USADO DE FORMA DIRETA! CHAME O MÉTODO setID!</br>
     * Deve ser implementado de forma a armazenar o valor em uma variável de ID.
     * @param id Novo ID.
     */
    void onSetId(long id);
    
    /**
     * Salva essa entidade no bando de dados, garantindo que nenhum valor anotado com @unique
     * seja duplicado.
     * @return Verdadeiro para caso a entidade tenha sido salva com sucesso.
     * Falso se houver acontecido erros.
     * @throws DuplicatedUniqueField Caso algum campo único esteja com um valor já usado. 
     * @throws EntIdBadImplementation Caso a classe tenha implementado de forma incorreta
     * os métodos getId e onSetId.
     */
    default boolean save() throws DuplicatedUniqueField, EntIdBadImplementation{
        return IO.save(this);
    }
    
    /**
     * Carrega uma entidade a partir do banco de dados recebendo o seu ID.
     * @param <T> Entidade para se buscar pelo seu tipo.
     * @param id Da entidade a ser buscada.
     * @return Entidade encontrada no banco de dados. Nulo para não encontrada.
     */
    default <T extends Entidade> T load(long id){
        return (T) IO.load(this, id);
    }
    
    /**
     * Carrega todas as entidades do tipo atual. Ter cuidado
     * ao usar esse método pois o alto número de entidades pode consumir muita memória.
     * @param <T> Tipo da entidade que será retornada na lista.
     * @return Lista com as entidades do banco de dados.
     */
    default <T extends Entidade> List<T> loadAll(){
        return (List<T>) IO.loadAll(this);
    }
    
    /**
     * Retorna uma lista filtrada de entidades.
     * @param <T> Tipo da entidade da lista.
     * @param filtros Lista de filtros que será aplicado na busca.
     * @param todos Precisa passar em todos os filtros, ou somente um basta?
     * Verdadeiro para a necessidade de passar em todos, falso para somente um
     * item ser suficiente.
     * @return Lista das entidades aprovadas no teste.
     */
    default <T extends Entidade> List<T> loadAll(List<ItemFilter> filtros, boolean todos){
        return (List<T>) IO.loadAll(this, filtros, todos);
    }
    
    /**
     * Retorna uma lista filtrada de entidades.
     * @param <T> Tipo da entidade da lista.
     * @param filtro Filter que será aplicado na busca por entidades.
     * @return Lista com as entidades que passaram no teste.
     */
    default <T extends Entidade> List<T> loadAll(Filter filtro){
        return (List<T>) IO.loadAll(this, filtro);
    }
    
    /**
     * Retorna uma lista com os IDs de todas as entidades salvas.
     * @return 
     */
    default List<Long> loadAllOnlyIds(){
        return IO.loadAllOnlyIds(this);
    }
    
    /**
     * Deleta essa entidade do banco de dados, liberando os seus campos unicos para
     * que outras entidades possam usar os valores.
     * @return Verdadeiro para deletado com sucesso.
     */
    default boolean delete(){
        return IO.delete(this);
    }
    
    /**
     * Deleta todas as entidades de uma lista.
     * @param entidades Lista de entidades a serem apagadas.
     */
    default void delete(List<Entidade> entidades){
        IO.delete(entidades);
    }
    
    /**
     * Recebe uma lista de entidades que deverão PERMANECER no banco de dados,
     * apagando qualquer entidade que não esteja nessa lista!
     * @param entidades Lista de entidades que PERMANECERÃO após a execução do
     * método.
     */
    default void deleteInverse(List<Entidade> entidades){
        IO.deleteInverse(entidades);
    }
    
    /**
     * Verifica se um outro objeto é igual a este.
     * @param obj A ser comparado.
     * @return Verdadeiro para caso de igualdade.
     */
    default boolean igual(Object obj){
        List<Field> af = FieldsManager.getFields(this);
        List<Field> bf = FieldsManager.getFields(obj);
        if(af.size() == bf.size()){
            for(Field f : af){
                f.setAccessible(true);
                try {
                    if(f.get(this).equals(f.get(obj))){
                        return true;
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Entidade.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }
    
}
