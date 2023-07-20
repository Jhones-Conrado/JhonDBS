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
package br.com.jhondbs.core.db.base.padrao;

import br.com.jhondbs.core.db.base.Entity;

/**
 * ENGLISH <br>
 * An entity with preconfigured ID methods to be used on entities
 * that need to be saved. If your class doesn't need to extend any other class,
 * it is recommended that your entity class extend this. <br>
 * PORTUGUÊS <br>
 * Uma entidade com os métodos de ID pre-configurados para ser usada em entidades
 * que precisem ser salvas. Caso sua classe não precise extender nenhuma outra,
 * é recomendado que a sua classe de entidade extenda esta.
 * @author jhonesconrado
 */
public abstract class DefaultEntity implements Entity {
    
    /**
     * Entity ID that will represent the object in the database. 
     * ID de entidade que representará o objeto no banco de dados.
     */
    private long enteId = -1l;
    
    /**
     * The entity id of the class responsible for its identification in the database.
     * O id de entidade da classe responsável por sua identificação no banco de
     * dados.
     * @return ID de entidade.
     */
    @Override
    public long getEnteId() {
        return enteId;
    }
    
    /**
     * The entity id of the class responsible for its identification in the database.
     * O id de entidade da classe responsável por sua identificação no banco de
     * dados.
     * @param id ID Number for be saved in the database.
     * de dados.
     */
    @Override
    public void onSetId(long id) {
        this.enteId = id;
    }
    
    /**
     * ENGLISH<br>
     * Creates a clone of the current object.
     * <br><br>
     * PORTUGUÊS<br>
     * Cria um clone do objeto atual.
     * @return Clone of the current object.
     * @throws CloneNotSupportedException 
     */
    @Override
    public DefaultEntity clone() throws CloneNotSupportedException {
        return (DefaultEntity) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == this.getClass()){
            Entity e = (Entity) obj;
            return e.getEnteId() == getEnteId();
        }
        return false;
    }
    
}
