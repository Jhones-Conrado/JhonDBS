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
package br.com.jhondbs.core.db;

import br.com.jhondbs.core.db.errors.EntIdBadImplementation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.jhondbs.core.db.base.Entity;

/**
 * Responsible for managing the unique keys of each class.<br>
 * This means that whenever an entity needs a new ID to be saved in the database,
 * it will request this ID for this class.
 * <br><br>
 * Responsável por gerênciar as chaves únicas de cada classe.<br>
 * Isso significa que sempre que uma entidade precisar de um ID novo para poder
 * ser salva no banco de dados, ela requisitará esse ID para esta classe.
 * @author jhonesconrado
 */
public class Keys {
    
    private static Map<String, Long> keys;
    
    /**
     * Starts the Keys instance, looking for a previously saved configuration file
     * and loading it if it exists.
     * <br><br>
     * Inicia a instância de Keys, procurando se existe algum arquivo de configurações
     * previamente salvo e carregando se existir.
     */
    private Keys() {
        if(keys == null){
            File key = new File("db/keys");
            if(key.exists()){
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(key));
                    keys = (Map<String, Long>) in.readObject();
                    in.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                keys = new HashMap<>();
            }
        }
    }
    
    /**
     * Saves the key configuration file to the database.<br>
     * Salva o arquivo de configuração de chaves no banco de dados.
     */
    private void save(){
        try {
            new File("db").mkdirs();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("db/keys")));
            out.writeObject(keys);
            out.flush();
            while(!new File("db/keys").exists()){}
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * It checks if the entity still doesn't have an ID defined and, if necessary,
     * generates a new ID and tries to configure it in the Entity.
     * <br><br>
     * Verifica se a entidade ainda não possui ID definido e se precisar, gera um
     * novo ID e tenta configurá-lo na Entity.
     * @param e Entity a ser testada para ID.
     * @throws EntIdBadImplementation Caso o método tente adicionar o novo ID
     * e a entidade não modifique o resultado do método getId. Ou seja, a classe
     * implementou de forma errada a interface Entity.
     */
    public synchronized void gerarIdLocal(Entity e) throws EntIdBadImplementation, Exception{
        if(e.getEnteId() < 0){
            if(keys.containsKey(e.getClass().getName())){
                analiseId(e);
            } else {
                keys.put(e.getClass().getName(), 0l);
                analiseId(e);
            }
            save();
        }
    }
    
    /**
     * Checks if an ID record already exists for the entity class, generates a 
     * new ID and adds it to the entity.
     * <br><br>
     * Analisa se já existe um registro de IDs para a classe da entidade, gera um
     * novo ID e adiciona à entidade.
     * @param e Que receberá o novo ID.
     * @throws EntIdBadImplementation 
     */
    private void analiseId(Entity e) throws EntIdBadImplementation, Exception{
        e.setEnteId(keys.get(e.getClass().getName()));
        if(e.getEnteId() < 0){
            throw new EntIdBadImplementation();
        }
        keys.put(e.getClass().getName(), keys.get(e.getClass().getName())+1);
    }
    
    /**
     * It checks if the entity still doesn't have an ID defined and, if necessary,
     * generates a new ID and tries to configure it in the Entity.
     * <br><br>
     * Verifica se a entidade ainda não possui ID definido e se precisar, gera um
     * novo ID e tenta configurá-lo na Entity.
     * @param e Entity a ser testada para ID.
     * @throws EntIdBadImplementation Caso o método tente adicionar o novo ID
     * e a entidade não modifique o resultado do método getId. Ou seja, a classe
     * implementou de forma errada a interface Entity.
     */
    public static void gerarId(Entity e) throws EntIdBadImplementation, Exception{
        new Keys().gerarIdLocal(e);
    }
    
}
