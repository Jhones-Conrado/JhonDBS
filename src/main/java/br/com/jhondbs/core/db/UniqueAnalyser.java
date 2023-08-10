/*
 * Copyright (C) 2022 jhonessales
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

import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.io.IO;
import java.lang.reflect.Field;
import java.util.List;
import br.com.jhondbs.core.db.base.Entity;
import br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException;

/**
 * Analisa se uma entidade está apta para ser salva. Ou seja, se não possui
 * nenhuma variável unica duplicada.
 * @author jhonessales
 */
public class UniqueAnalyser {
    

    public UniqueAnalyser() {}
    
    /**
     * Analyzes if the entity has any field annotated as Unique where the value
     * has already been used by another entity of the same type.<br><br>
     * Analisa se a entidade possui algum campo anotado como Unique onde o valor
     * já tenha sido usado por outra entidade de mesmo tipo.
     * @param entity
     * @return True se estiver apto a ser salvo, False se estiver com um campo já usado.
     * @throws br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException
     */
    public boolean analise(Entity entity) throws DuplicatedUniqueFieldException, Exception{
        List<Field> unicos = FieldsManager.getFieldsUnique(entity);
        if(!unicos.isEmpty()){
            for(Long l : IO.loadAllOnlyIds(entity)){
                if(l != entity.getEnteId()){
                    Entity load = entity.load(l);
                    for(Field f : unicos){
                        f.setAccessible(true);
                        try {
                            if(f.get(entity) == null){
                                return true;
                            }
                            if(f.get(entity).equals(f.get(load))){
                                throw new DuplicatedUniqueFieldException(f.getName());
                            }
                        } catch (IllegalArgumentException | IllegalAccessException ex) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
}
