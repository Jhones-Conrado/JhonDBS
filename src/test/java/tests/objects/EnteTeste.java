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
package tests.objects;

import br.com.jhondbs.core.db.base.Entidade;

/**
 *
 * @author jhonessales
 */
public class EnteTeste implements Entidade{
    
    public long enteId;
    
    public int idade;
    public String nome;
    public boolean rico;

    public EnteTeste() {
        this.enteId = -1l;
    }
    
    public EnteTeste(int idade, String nome, boolean rico) {
        this.enteId = -1l;
        this.idade = idade;
        this.nome = nome;
        this.rico = rico;
    }
    
    @Override
    public long getEnteId() {
        return this.enteId;
    }

    @Override
    public void onSetId(long id) {
        this.enteId = id;
    }
    
}
