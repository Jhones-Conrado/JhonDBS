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
package br.com.jhondbs.core.db.interfaces;

import br.com.jhondbs.core.db.capsule.Capsule;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException;
import br.com.jhondbs.core.db.filter.Filter;
import java.io.Serializable;
import java.util.List;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import br.com.jhondbs.core.db.filter.GenericFilterCondition;

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
    default String getId() throws Exception{
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
    default String setId(String enteId) throws Exception{
        if(getId() == null || getId().isBlank() && !enteId.isBlank()){
            infunction.setid(this, enteId);
        } else if(getId() == null || getId().isBlank()) {
            infunction.makeId(this);
        }
        return getId();
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
        Capsule capsule = new Capsule(this);
        capsule.start();
        return capsule.flush();
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
    default <T extends Entity> T load(String id) throws Exception{
        Capsule capsule = new Capsule(this.getClass(), id);
        return capsule.recover();
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
    default <T extends Entity> List<T> loadAll() throws Exception{
        return Capsule.loadAll(this.getClass());
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
    default <T extends Entity> List<T> loadAll(Filter filter) throws Exception{
        return Capsule.loadAll(this.getClass(), filter);
    }
    
    /**
     * Returns a list of the IDs of all saved entities.
     * Retorna uma lista com os IDs de todas as entidades salvas.
     * @return 
     */
    default List<String> getAllIds(){
        return Capsule.getAllIds(this.getClass());
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
        Capsule cap = new Capsule(this);
        return cap.delete();
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
    default boolean deleteCascate() throws Exception{
        Capsule cap = new Capsule(this);
        return cap.deleteCascate();
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
        return FieldsManager.getValueFrom(fieldName, this);
    }
    
    default <T extends Entity> List<T> findByFieldValue(String fieldname, Object value) throws Exception{
        GenericFilterCondition condition = new GenericFilterCondition(fieldname, value.toString(), false);
        Filter filter = new Filter();
        filter.addCondition(condition);
        return Capsule.loadAll(this.getClass(), filter);
    }
    
    default <T extends Entity> List<T> findByFieldValueIgnoreCase(String fieldname, Object value) throws Exception{
        GenericFilterCondition condition = new GenericFilterCondition(fieldname, value.toString(), true);
        Filter filter = new Filter();
        filter.addCondition(condition);
        return Capsule.loadAll(this.getClass(), filter);
    }
    
    class infunction {
        
        private static String makeId(Entity entity) throws Exception {
            UUID uuid = UUID.randomUUID();
            setid(entity, uuid.toString());
            return uuid.toString();
        }
        
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
        private static void setid(Entity entity, String id) throws Exception{
            if(entity != null){
                List<Field> ids = FieldsManager.getAllFields(entity).parallelStream()
                        .filter(field -> (field.getType().getName().contains("java.lang.String")))
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
                        FieldsManager.setValue(selected.getName(), entity, id);
//                        selected.setAccessible(true);
//                        selected.set(entity, id);
                    } else {
                        throw new Exception("The entity does not have a String type variable for the ID.");
                    }
                } else {
                    throw new Exception("The entity does not have a String type variable for the ID.");
                }
            } else {
                throw new NullPointerException("Entidade nula");
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
        private static String getid(Entity entity) throws Exception{
            if(entity != null){
                List<Field> ids = FieldsManager.getAllFields(entity).parallelStream()
                        .filter(field -> (field.getType().getName().contains("String")))
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
                        String id = FieldsManager.getValueFrom(selected.getName(), entity);
                        if(id == null || id.isBlank()) {
                            return makeId(entity);
                        } else {
                            return id;
                        }
                    } else {
                        throw new Exception("The entity does not have a String type variable for the ID.");
                    }
                }
                throw new NullPointerException("Entidade não possui um campo tipo String para o ID.");
            }
            throw new Exception("The entity does not have a String type variable for the ID.");
        }
        
    }
    
}
