/*
 * Copyright (C) 2023 jhonessales
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

import br.com.jhondbs.core.db.io.Reflection;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;

/**
 * Mantém uma representação de uma entidade, evitando salvamentos desnecessários
 * para classes que mantém muitas sub-entidades.
 * @author jhonessales
 */
public class Represent implements Serializable{
    
    private String classPath;
    private long enteId;

    public Represent() {
    }

    public Represent(String classPath, long enteId) throws Exception {
        if(classPath == null || classPath.isBlank()){
            throw new Exception("ClassPath can't be blank or null.");
        }
        this.classPath = classPath;
        this.enteId = enteId;
    }
    
    public Represent(Entity entity) throws Exception{
        if(entity == null){
            throw new Exception("Entity is null");
        }
        this.classPath = entity.getClass().getName();
        this.enteId = entity.getEnteId();
    }
    
    public Entity get() throws IOException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception{
        if(classPath != null){
            return ((Entity) new Reflection().getNewInstance(classPath)).load(enteId);
        }
        throw new Exception("ClassPath is null");
    }
    
}
