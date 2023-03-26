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

import br.com.jhondbs.core.db.base.Entidade;
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

/**
 * Responsável por gerênciar as chaves únicas de cada classe.
 * Isso significa que sempre que uma entidade precisar de um ID novo para poder
 * ser salva no banco de dados, ela requisitará esse ID para esta classe.
 * @author jhonesconrado
 */
public class Keys {
    
    private static Keys instance;
    private static Map<String, Long> keys;
    
    /**
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
     * Verifica se a entidade ainda não possui ID definido e se precisar, gera um
     * novo ID e tenta configurá-lo na Entidade.
     * @param e Entidade a ser testada para ID.
     * @throws EntIdBadImplementation Caso o método tente adicionar o novo ID
     * e a entidade não modifique o resultado do método getId. Ou seja, a classe
     * implementou de forma errada a interface Entidade.
     */
    public synchronized void gerarIdLocal(Entidade e) throws EntIdBadImplementation{
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
     * Analisa se já existe um registro de IDs para a classe da entidade, gera um
     * novo ID e adiciona à entidade.
     * @param e Que receberá o novo ID.
     * @throws EntIdBadImplementation 
     */
    private void analiseId(Entidade e) throws EntIdBadImplementation{
        e.setEnteId(keys.get(e.getClass().getName()));
        if(e.getEnteId() < 0){
            throw new EntIdBadImplementation();
        }
        keys.put(e.getClass().getName(), keys.get(e.getClass().getName())+1);
    }
    
    /**
     * Verifica se a entidade ainda não possui ID definido e se precisar, gera um
     * novo ID e tenta configurá-lo na Entidade.
     * @param e Entidade a ser testada para ID.
     * @throws EntIdBadImplementation Caso o método tente adicionar o novo ID
     * e a entidade não modifique o resultado do método getId. Ou seja, a classe
     * implementou de forma errada a interface Entidade.
     */
    public static void gerarId(Entidade e) throws EntIdBadImplementation{
        new Keys().gerarIdLocal(e);
    }
    
}
