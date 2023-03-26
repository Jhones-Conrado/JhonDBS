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

import br.com.jhondbs.core.db.base.Entidade;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.io.IO;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Analisa se uma entidade está apta para ser salva. Ou seja, se não possui
 * nenhuma variável unica duplicada.
 * @author jhonessales
 */
public class UniqueAnalyser {
    

    public UniqueAnalyser() {}
    
    /**
     * Analisa se a entidade possui algum campo anotado como Unique onde o valor
     * já tenha sido usado por outra entidade.
     * @param e
     * @return True se estiver apto a ser salvo, False se estiver com um campo já usado.
     */
    public boolean analise(Entidade e){
        List<Field> unicos = FieldsManager.getFieldsUnique(e);
        if(!unicos.isEmpty()){
            for(Long l : IO.loadAllOnlyIds(e)){
                if(l != e.getEnteId()){
                    Entidade load = e.load(l);
                    for(Field f : unicos){
                        f.setAccessible(true);
                        try {
                            if(f.get(e) == null){
                                return true;
                            }
                            if(f.get(e).equals(f.get(load))){
                                return false;
                            }
                        } catch (IllegalArgumentException | IllegalAccessException ex) {
                            return false;
//                            Logger.getLogger(Unique2.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        return true;
    }
    
}
