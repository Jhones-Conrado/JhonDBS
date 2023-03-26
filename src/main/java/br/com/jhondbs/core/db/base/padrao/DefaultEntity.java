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

import br.com.jhondbs.core.db.base.Entidade;

/**
 * Uma entidade com os métodos de ID pre-configurados, ideal para poupar o programador
 * do trabalho repetitivo. Caso sua classe não precise extender nenhuma outra,
 * é recomendado que a sua classe de entidade extenda esta.
 * @author jhonesconrado
 */
public abstract class DefaultEntity implements Entidade {
    
    /**
     * Id de entidade que representará o objeto no banco de dados.
     */
    private long enteId = -1l;
    
    /**
     * O id de entidade da classe responsável por sua identificação no banco de
     * dados.
     * @return ID de entidade.
     */
    @Override
    public long getEnteId() {
        return enteId;
    }
    
    /**
     * O id de entidade da classe responsável por sua identificação no banco de
     * dados.
     * @param id Id de entidade responsável por identificar o objeto no banco
     * de dados.
     */
    @Override
    public void onSetId(long id) {
        this.enteId = id;
    }
    
    /**
     * Cria um clone do objeto atual.
     * @return Clone do objeto atual.
     * @throws CloneNotSupportedException 
     */
    @Override
    public DefaultEntity clone() throws CloneNotSupportedException {
        return (DefaultEntity) super.clone();
    }
    
    
    
}
