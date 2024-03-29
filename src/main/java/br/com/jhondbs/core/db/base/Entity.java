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

import br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.db.io.IO;
import java.io.Serializable;
import java.util.List;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.jhondbs.core.db.filter.ItemFilter;
import java.util.Arrays;

/**
 * ENGLISH<br>
 * Responsible for transforming a class into an entity with CRUD functions.
 * A variable must be placed in the object so that it can store its ID.
 * The setId and getId methods must implement on this mentioned variable.<br><br>
 * It is STRONGLY recommended that the ID variable created in the object that
 * implements this class has the value -1l by default!
 * This will ensure that the database work properly.<br>
 * The program will check the entity ID when saving it in the database and will
 * use the value -1l as a parameter for verification.<br>
 * It means that an entity that has an ID less than 0 will receive a new ID
 * generated at the moment.
 * <br><br>
 * PORTUGUÊS<br>
 * Responsável por transformar uma classe em uma entidade com funções de CRUD.
 * Uma variável deve ser posta no objeto para que possa armazenar o ID do mesmo.
 * Os métodos setId e getId devem implementar sobre essa variável citada.<br><br>
 * É FORTEMENTE recomendado que a variável ID criada no objeto que implemente
 * esta classe, tenha o valor -1l por padrão! Isso garantirá que o banco de dados
 * funcione da forma correta.<br>
 * O programa irá verificar o ID da entidade na hora de salvá-la no banco de dados
 * e usará o valor -1l como parâmetro para verificação.<br>
 * Significa que uma entidade que tiver um ID menor que 0, receberá um novo ID
 * gerado no momento.
 * @author jhonesconrado
 */
public interface Entity extends Serializable, Cloneable{
    
    public static final long serialVersionUID = 1l;
    
    /**
     * It should be implemented in order to retrieve the entity ID from some variable.<br><br>
     * Deverá ser implementado de forma a resgatar de alguma variável o ID da entidade.
     * @return ID da entidade.
     * @throws java.lang.Exception
     */
    default long getEnteId() throws Exception{
        return infunction.getid(this);
    }
    
    /**
     * ENGLISH<br>
     * Applies the ID to the entity.<br> 
     * An entity should only be able to have its ID changed if it is equal to -1l!
     * That is, after an entity receives an ID, it should no longer be exchangeable.<br><br>
     * PORTUGUÊS<br>
     * Aplica o ID para a entidade.<br>
     * Uma entidade só deverá poder ter o seu ID alterado caso este seja igual a
     * -1l! Ou seja, após uma entidade receber um ID, este não deverá mais poder
     * ser trocado.
     * @param enteId Novo ID para a entidade.
     * @throws java.lang.Exception
     */
    default void setEnteId(long enteId) throws Exception{
        if(getEnteId() == -1l){
            infunction.setid(this, enteId);
        }
    }
    
    /**
     * ENGLISH<br>
     * THIS METHOD MUST NOT BE USED DIRECTLY! CALL METHOD setID!</br>
     * It must be implemented in a way to store the value in an ID variable.
     * <br><br>
     * PORTUGUÊS<br>
     * ESTE MÉTODO NÃO DEVE SER USADO DE FORMA DIRETA! CHAME O MÉTODO setID!</br>
     * Deve ser implementado de forma a armazenar o valor em uma variável de ID.
     * @param id Novo ID.
     */
//    void onSetId(long id);
    
    /**
     * ENGLISH<br>
     * Save this entity to the database, ensuring that no values annotated with 
     * @throws java.lang.Exception
     * @throws br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException
     * @unique are duplicated.
     * <br><br>
     * PORTUGUÊS<br>
     * Salva essa entidade no bando de dados, garantindo que nenhum valor anotado com @unique
     * seja duplicado.
     * @return True if the entity was successfully saved.
     * False if there were errors.
     * the getId and onSetId methods.
     */
    default boolean save() throws Exception, DuplicatedUniqueFieldException {
        return IO.save(this);
    }
    
    /**
     * Loads an entity from the database based on its ID.
     * Carrega uma entidade do banco de dados a partir do seu ID.
     * @param <T> Entity to search for by its type. <br>
     * Entity para se buscar pelo seu tipo.
     * @param id of the entity to be fetched.<br>
     * Da entidade a ser buscada.
     * @return Entity found in the database. Null for not found.<br>
     * Entity encontrada no banco de dados. Nulo para não encontrada.
     */
    default <T extends Entity> T load(long id){
        return (T) IO.load(this, id);
    }
    
    /**
     * ENGLISH<br>
     * Loads all entities of the current type.
     * Be careful when using this method as the high number of entities can 
     * consume a lot of memory.<br><br>
     * PORTUGUÊS<br>
     * Carrega todas as entidades do tipo atual. Ter cuidado
     * ao usar esse método pois o alto número de entidades pode consumir muita memória.
     * @param <T> Entity type that will be returned in the list.<br>
     * Tipo da entidade que será retornada na lista.
     * @return List of entities of this type in the database.<br>
     * Lista com as entidades desse tipo no banco de dados.
     */
    default <T extends Entity> List<T> loadAll(){
        return (List<T>) IO.loadAll(this);
    }
    
    /**
     * Returns a filtered list of entities.<br>
     * Retorna uma lista filtrada de entidades.
     * @param <T> Entity type that will be returned in the list.<br>
     * Tipo da entidade que será retornada na lista.
     * @param filters List of filters that will be applied in the search.<br>
     * Lista de filtros que será aplicado na busca.
     * @param all Does this need to pass all filters, or is just one enough?<br>
     * True for the need to pass all, false for only one item being enough.<br><br>
     * Precisa passar em todos os filtros, ou somente um basta?
     * Verdadeiro para a necessidade de passar em todos, falso para somente um
     * item ser suficiente.
     * @return List of entities that passed the test.<br>
     * Lista das entidades aprovadas no teste.
     */
    default <T extends Entity> List<T> loadAll(List<ItemFilter> filters, boolean all){
        return (List<T>) IO.loadAll(this, filters, all);
    }
    
    /**
     * Returns a filtered list of entities.<br>
     * Retorna uma lista filtrada de entidades.
     * @param <T> List entity type.<br>
     * Tipo da entidade da lista.
     * @param filter Filter that will be applied in the search for entities.<br>
     * Filtro que será aplicado na busca por entidades.
     * @return List of entities that passed the test.<br>
     * Lista com as entidades que passaram no teste.
     */
    default <T extends Entity> List<T> loadAll(Filter filter){
        return (List<T>) IO.loadAll(this, filter);
    }
    
    /**
     * Returns a list of the IDs of all saved entities.
     * Retorna uma lista com os IDs de todas as entidades salvas.
     * @return 
     */
    default List<Long> loadAllOnlyIds(){
        return IO.loadAllOnlyIds(this);
    }
    
    /**
     * ENGLISH<br>
     * Deletes this entity from the database, releasing its unique fields so that
     * other entities can use the values.<br><br>
     * PORTUGUÊS<br>
     * Deleta essa entidade do banco de dados, liberando os seus campos unicos para
     * que outras entidades possam usar os valores.
     * @return True for successfully deleted.<br>
     * Verdadeiro para deletado com sucesso.
     * @throws java.lang.Exception
     */
    default boolean delete() throws Exception{
        return IO.delete(this);
    }
    
    /**
     * Deletes all entities from a list.
     * Deleta todas as entidades de uma lista.
     * @param entities List of entities to be deleted.<br>
     * Lista de entidades a serem apagadas.
     */
    default void delete(List<Entity> entities){
        IO.delete(entities);
    }
    
    /**
     * ENGLISH<br>
     * Deletes the entity and all sub entities that are values of its fields.<br>
     * In other words: If the entity has a field that stores the reference to
     * another entity and so on, then recursive calls to the fullDelete method
     * will be made so that all child entities are deleted.
     * <br><br>
     * PORTUGUÊS<br>
     * Deleta a entidade e todas as sub entidades que sejam valores de seus campos.<br>
     * Em outras palavras: Se a entidade possuir um campo que armazene a referência
     * para outra entidade e assim por diante, então serão feitas chamadas recursivas
     * ao método fullDelete para que todas as entidades filho sejam deletadas.
     */
    default void fullDelete(){
        IO.fullDelete(this);
    }
    
    /**
     * ENGLISH<br>
     * Receives a list of entities that should REMAIN in the database, deleting
     * any entity that is not on that list!<br><br>
     * PORTUGUÊS<br>
     * Recebe uma lista de entidades que deverão PERMANECER no banco de dados,
     * apagando qualquer entidade que não esteja nessa lista!
     * @param entities List of entities that WILL REMAIN after method execution.<br>
     * Lista de entidades que PERMANECERÃO após a execução do método.
     */
    default void deleteInverse(List<Entity> entities){
        IO.deleteInverse(entities);
    }
    
    /**
     * Checks if another object is equal to this one.<br>
     * Verifica se um outro objeto é igual a este.
     * @param obj To be compared.<br>
     * A ser comparado.
     * @return True for equality case.<br>
     * Verdadeiro para caso de igualdade.
     */
    default boolean igual(Object obj){
        List<Field> af = FieldsManager.getFields(this);
        List<Field> bf = FieldsManager.getFields(obj);
        if(af.size() == bf.size()){
            for(Field f : af){
                try {
                    f.setAccessible(true);
                    if(!f.get(this).equals(f.get(obj))){
                        return false;
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
    default void setSuperente(Entity superente) throws NoSuchFieldError{
        infunction.setSuperEnte(this, superente);
    }
    
    default Represent getSuperente() throws  NoSuchFieldError{
        return infunction.getSuperEnte(this);
    }
    
    /**
     * Checks if another object is equal to this one.<br>
     * Verifica se um outro objeto é igual a este.
     * @param obj To be compared.<br>
     * A ser comparado.
     * @return True for equality case.<br>
     * Verdadeiro para caso de igualdade.
     */
    default boolean isEquals(Object obj){
        return igual(obj);
    }
    
    /**
     * ENGLISH<br>
     * Search in the entity's fields to find a field with the same name passed as
     * a method parameter, returning its value in the form of an Object.<br><br>
     * PORTUGUÊS<br>
     * Busca nos campos da entidade se encontra algum campo com mesmo nome passado
     * como parâmetro do método, retornando o seu valor na forma de um Object.
     * @param <T>
     * @param fieldName Name of the parameter to be searched.<br>
     * Nome do parâmetro a ser buscado.
     * @return Requested field object.<br>
     * Objeto do campo solicitado.
     * @throws java.lang.NoSuchFieldException
     * @throws IllegalArgumentException.
     * @throws IllegalAccessException If, for some reason, access to the 
     * variable's value has not been obtained.<br>
     * Caso, por algum motivo, não tenha sido obtido acesso ao valor da variável.
     */
    default <T> T getValueFrom(String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException{
        Field field = null;
        Class clazz = this.getClass();
        
        while(field == null && clazz != Object.class){
            List<Field> list = Arrays.asList(clazz.getDeclaredFields());
            
            for(Field f : list){
                if(f.getName().endsWith(fieldName)){
                    Object cast = clazz.cast(this);
                    if(cast != null){
                        try {
                            f.setAccessible(true);
                            return (T) f.get(cast);
                        } catch (IllegalArgumentException | IllegalAccessException illegalArgumentException) {
                        }
                    }
                }
            }
            
            clazz = clazz.getSuperclass();
        }
        return null;
    }
    
    default <T> T findByFieldValue(String fieldname, Object value){
        if(FieldsManager.isFieldPresent(this.getClass(), fieldname)){
            for(Long id : loadAllOnlyIds()){
                Entity e = load(id);
                try {
                    if(e.getValueFrom(fieldname).equals(value)){
                        return (T) e;
                    }
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
                    Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
    
    class infunction {
        
        /**
         * It will look for all variables of type "long" and among these, it 
         * will look for those that have either the word "ent" or that are equal 
         * to "id".
         * The ID value will preferably be from the "enteid" variable, secondarily 
         * from the "id" variable if the first one is not found and thirdly from 
         * the first long type variable with the word "id" in the name, if the 
         * first two are not found.
         * @param entity
         * @param id
         * @throws Exception 
         */
        private static void setid(Entity entity, long id) throws Exception{
            if(entity != null){
                List<Field> ids = FieldsManager.getAllFields(entity).parallelStream()
                        .filter(field -> (field.getGenericType() == Long.TYPE))
                        .filter(field -> (field.getName().toUpperCase().contains("ID")))
                        .toList();
                if(!ids.isEmpty()){
                    Field selected = null;
                    List<Field> toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("enteid")))
                            .toList();
                    if(toList.size() == 1){
                        selected = toList.get(0);
                    } else {
                        toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("id")))
                            .toList();
                        if(toList.size() == 1){
                            selected = toList.get(0);
                        } else {
                            selected = ids.get(0);
                        }
                    }
                    if(selected != null){
                        selected.setAccessible(true);
                        try {
                            Class clazz = entity.getClass();
                            Field fin = null;
                            while(true){
                                try {
                                    fin = clazz.getDeclaredField(selected.getName());
                                    Object cast = clazz.cast(entity);
                                    fin.setAccessible(true);
                                    fin.setLong(cast, id);
                                    break;
                                } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                                    clazz = clazz.getSuperclass();
                                    if(clazz == Object.class){
                                        break;
                                    }
                                }
                            }
                        } catch (IllegalArgumentException illegalArgumentException) {
                            System.out.println(illegalArgumentException);
                        }
                    } else {
                        throw new Exception("The entity does not have a long type variable for the ID.");
                    }
                }
            }
        }
        
        /**
         * It will look for all variables of type "long" and among these, it 
         * will look for those that have either the word "ent" or that are equal 
         * to "id".
         * The ID value will preferably be from the "enteid" variable, secondarily 
         * from the "id" variable if the first one is not found and thirdly from 
         * the first long type variable with the word "id" in the name, if the 
         * first two are not found.
         * The value "-1l" will be returned by default if no ID variable is found.
         * @param entity
         * @return
         * @throws Exception 
         */
        private static long getid(Entity entity) throws Exception{
            if(entity != null){
                List<Field> ids = FieldsManager.getAllFields(entity).parallelStream()
                        .filter(field -> (field.getGenericType() == Long.TYPE))
                        .filter(field -> (field.getName().toUpperCase().contains("ID")))
                        .toList();
                if(!ids.isEmpty()){
                    Field selected = null;
                    List<Field> toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("enteid")))
                            .toList();
                    if(toList.size() == 1){
                        selected = toList.get(0);
                    } else {
                        toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("id")))
                            .toList();
                        if(toList.size() == 1){
                            selected = toList.get(0);
                        } else {
                            selected = ids.get(0);
                        }
                    }
                    if(selected != null){
                        selected.setAccessible(true);
                        try {
                            Class clazz = entity.getClass();
                            Field fin = null;
                            while(true){
                                try {
                                    fin = clazz.getDeclaredField(selected.getName());
                                    Object cast = clazz.cast(entity);
                                    fin.setAccessible(true);
                                    return fin.getLong(cast);
                                } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                                    clazz = clazz.getSuperclass();
                                    if(clazz == Object.class){
                                        break;
                                    }
                                }
                            }
                        } catch (IllegalArgumentException illegalArgumentException) {
                            System.out.println(illegalArgumentException);
                        }
                        selected.getLong(entity);
                    } else {
                        throw new Exception("The entity does not have a long type variable for the ID.");
                    }
                }
            }
            throw new Exception("The entity does not have a long type variable for the ID.");
        }
        
        private static void setSuperEnte(Entity son, Entity superente) throws NoSuchFieldError{
            if(son != null && superente != null){
                List<Field> fieldlist = FieldsManager.getAllFields(son).parallelStream()
                        .filter(field -> (field.getGenericType() == Represent.class))
                        .filter(field -> (field.getName().equalsIgnoreCase("superente")))
                        .toList();
                if(fieldlist.size() == 1){
                    Field selected = fieldlist.get(0);
                    selected.setAccessible(true);
                    try {
                        Class clazz = son.getClass();
                        Field fin = null;
                        while(true){
                            try {
                                fin = clazz.getDeclaredField(selected.getName());
                                Object cast = clazz.cast(son);
                                fin.setAccessible(true);
                                fin.set(cast, new Represent(superente));
                                break;
                            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                                if(clazz == Object.class){
                                    break;
                                }
                                clazz = clazz.getSuperclass();
                                if(clazz == Object.class){
                                    break;
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (IllegalArgumentException illegalArgumentException) {
                        System.out.println(illegalArgumentException);
                    }
                } else {
                    throw new NoSuchFieldError("Field not exists.");
                }
            }
        }
        
        private static Represent getSuperEnte(Entity son) throws NoSuchFieldError{
            if(son != null){
                List<Field> fieldlist = FieldsManager.getAllFields(son).parallelStream()
                        .filter(field -> (field.getGenericType() == Represent.class))
                        .filter(field -> (field.getName().equalsIgnoreCase("superente")))
                        .toList();
                if(fieldlist.size() == 1){
                    Field selected = fieldlist.get(0);
                    try {
                        Class clazz = son.getClass();
                        Field fin = null;
                        while(true){
                            try {
                                fin = clazz.getDeclaredField(selected.getName());
                                Object cast = clazz.cast(son);
                                fin.setAccessible(true);
                                fin.get(cast);
                                break;
                            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                                if(clazz == Object.class){
                                    break;
                                }
                                clazz = clazz.getSuperclass();
                                if(clazz == Object.class){
                                    break;
                                }
                            }
                        }
                    } catch (IllegalArgumentException illegalArgumentException) {
                        System.out.println(illegalArgumentException);
                    }
                } else {
                    throw new NoSuchFieldError();
                }
            }
            return null;
        }
        
    }
    
}
