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
package br.com.jhondbs.core.db.io;

import br.com.jhondbs.core.db.UniqueAnalyser;
import br.com.jhondbs.core.db.filter.Filter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.stream.Collectors;
import br.com.jhondbs.core.db.filter.ItemFilter;
import br.com.jhondbs.core.db.base.Entity;

/**
 * Responsible for saving, deleting and loading database entities.<br><br>
 * Responsável por salvar, deletar e carregar entidades do banco de dados.
 * @author jhonesconrado
 */
public class IO {
    
    /**
     * ENGLISH<br>
     * It saves an entity in the database, passing it through filters that
     * guarantee the proper functioning of the system.How to verify if the unique values are available.<br><br>
     * PORTUGUÊS<br>
     * Salva uma entidade no banco de dados passando a mesma por filters que garantem
     * o bom funcionamento do sistema.Como verificar se os valores unicos estão disponíveis.
     * @param entity Entity a ser salva no banco de dados.
     * @return Verdadeiro para caso a entidade tenha sido salva. Falso em caso de erro.
     * @throws java.lang.Exception
     */
    public static boolean save(Entity entity) throws Exception {
        if(new UniqueAnalyser().analise(entity)){ //Precisa passar no teste de campos únicos.
            Capsule capsule = new Capsule(entity);
            return capsule.make();
        }
        return false;
    }
    
    /**
     * ENGLISH<br>
     * Loads an entity saved in the database, receiving the entity type and its ID.
     * A null object will be returned if the entity is not found.
     * <br><br>
     * PORTUGUÊS<br>
     * Carrega uma entidade salva no banco de dados, recebendo para isso o tipo
     * da entidade e seu ID. Um objeto nulo será retornado se a entidade não for
     * encontrada.
     * @param entity Entity que servirá para localizar o tipo a ser buscado.
     * @param id Da entidade a ser buscada no banco de dados.
     * @return Entity carregada do banco de dados. Nulo caso não encontre.
     */
    public static Object load(Entity entity, long id){
        File file = new File(getDBFolderWithID(entity, id));
        //Método antigo utilizando JSON
        if(file.exists()){
            try {
                try (BufferedReader r = Files.newBufferedReader(Paths.get(file.getPath()))) {
                    Capsule capsule = new Capsule(r.readLine());
                    return capsule.extract();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    /**
     * ENGLISH<br>
     * Loads all entities of the current type.
     * Be careful when using this method as the high number of entities can 
     * consume a lot of memory.<br><br>
     * PORTUGUÊS<br>
     * Carrega todas as entidades do tipo atual. Ter cuidado
     * ao usar esse método pois o alto número de entidades pode consumir muita memória.
     * @param entity Entity a ser listada.
     * @return Lista de entidades do tipo solicitado.
     */
    public static List<Entity> loadAll(Entity entity){
        return entity.loadAllOnlyIds().stream().map((id) -> {
            return (Entity) entity.load(id);
        }).collect(Collectors.toList());
    }
    
    /**
     * Returns a filtered list of entities of the given type.<br><br>
     * Retorna uma lista filtrada de entidades de determinado tipo.
     * @param entity Entity que será listado o tipo.
     * @param filters Lista de filters que será aplicado na busca.
     * @param all Precisa passar em all os filters, ou somente um basta?
     * Verdadeiro para a necessidade de passar em all, falso para somente um
     * item ser suficiente.
     * @return Lista das entidades aprovadas no teste.
     */
    public static List<Entity> loadAll(Entity entity, List<ItemFilter> filters, boolean all){
        Filter filter = new Filter(all);
        filter.filters = filters;
        return loadAll(entity, filter);
    }
    
    /**
     * Returns a filtered list of entities of the given type.<br>
     * Retorna uma lista filtrada de entidades de determinado tipo.
     * @param entity Entity que será listado o tipo.
     * @param filter Objeto de filtragem que será usado para validação.
     * @return Lista de entidades que passaram no teste de validação.
     */
    public static List<Entity> loadAll(Entity entity, Filter filter){
        return entity.loadAllOnlyIds().stream().map((id) -> {
            return (Entity) entity.load(id);
        }).filter((ente) -> filter.filter(ente)).collect(Collectors.toList());
    }
    
    /**
     * Returns the list of entity type IDs. It saves memory space in relation to
     * the loadAll method, which brings instances of all objects already saved.
     * <br><br>
     * Retorna a lista de IDs do tipo de entidade. Economiza espaço em memória em
     * relação ao método loadAll que trás as instâncias de todos os objetos já salvos.
     * @param entity
     * @return 
     */
    public static List<Long> loadAllOnlyIds(Entity entity){
        List<Long> list = new ArrayList<>();
        File file = new File(getDBFolder(entity));
        if(file.exists()){
            Arrays.asList(file.list()).forEach(n -> {
                try {
                    list.add(Long.valueOf(n));
                } catch (NumberFormatException ex) {
                }
            });
        }
        return list;
    }
    
    /**
     * Delete an entity.<br>
     * Deleta uma entidade.
     * @param entity Entity a ser deletada.
     * @return 
     */
    public static boolean delete(Entity entity){
        File ente = new File(getDBFolderWithID(entity));
        if(ente.exists()){
            return ente.delete();
        }
        return false;
    }
    
    /**
     * Deletes all entities from a list.<br>
     * Deleta todas as entidades de uma lista.
     * @param entities Lista de entidades a serem apagadas.
     */
    public static void delete(List<Entity> entities){
        entities.forEach(e -> IO.delete(e));
    }
    
    /**
     * ENGLISH<br>
     * Deletes the entity and all sub entities that are values of its fields.<br>
     * In other words: If the entity has a field that stores the reference to
     * another entity and so on, then recursive calls to the fullDelete method
     * will be made so that all child entities are deleted.<br><br>
     * PORTUGUÊS<br>
     * Deleta a entidade e todas as sub entidades que sejam valores de seus campos.<br>
     * Em outras palavras: Se a entidade possuir um campo que armazene a referência
     * para outra entidade e assim por diante, então serão feitas chamadas recursivas
     * ao método fullDelete para que todas as entidades filho sejam deletadas.
     * @param entity
     */
    public static void fullDelete(Entity entity){
        Capsule capsule = new Capsule(entity);
        capsule.fullDelete();
    }
    
    /**
     * ENGLISH<br>
     * Receives a list of entities that should REMAIN in the database, deleting
     * any entity that is not on that list!<br><br>
     * PORTUGUÊS<br>
     * Recebe uma lista de entidades que deverão PERMANECER no banco de dados,
     * apagando qualquer entidade que não esteja nessa lista!
     * @param entidades Lista de entidades que PERMANECERÃO após a execução do
     * método.
     */
    public static void deleteInverse(List<Entity> entidades){
        if(!entidades.isEmpty()){ //Verifica se há entidades no filtro.
            List<Long> all = entidades.get(0).loadAllOnlyIds();
            List<Long> collect = entidades.stream().map((t) -> t.getEnteId()).collect(Collectors.toList());
            List<Long> toDelete = all.stream().filter((t) -> !collect.contains(t)).collect(Collectors.toList());
            toDelete.forEach(l -> {
                entidades.get(0).load(l).delete();
            });
        }
    }
    
    /**
     * ENGLISH<br>
     * Empty the directory by deleting everything inside and then delete the
     * directory.
     * Makes a recursive call that deletes all sub-files and sub-directories until it
     * clears the directory which will then be deleted.
     * <br><br>
     * PORTUGUÊS<br>
     * Esvazia o diretório apagando tudo que estiver dentro e depois apaga o diretório.
     * Faz uma chamada recursiva que apaga todos os sub-arquivos e sub-diretórios
     * até limpar o diretório que será então excluído.
     * @param directory Diretório que será esvaziado e deletado.
     */
    public static void deleteDirectory(File directory){
        if(directory.isDirectory()){
            for(File sub : directory.listFiles()){
                if(sub.isDirectory()){
                    deleteDirectory(sub);
                }
                sub.delete();
            }
            directory.delete();
        }
    }
    
    public static String getDBFolder(Object object){
        return "db/"+object.getClass().getName().replaceAll(".class", "")
                .replaceAll("[.]", "/");
    }

    public static String getDBFolderWithID(Entity entity){
        return getDBFolder(entity)+"/"+String.valueOf(entity.getEnteId());
    }
    
    public static String getDBFolderWithID(Entity entity, long id){
        return getDBFolder(entity)+"/"+String.valueOf(id);
    }
    
}
