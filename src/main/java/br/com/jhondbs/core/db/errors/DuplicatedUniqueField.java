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
package br.com.jhondbs.core.db.errors;

/**
 * Error thrown if trying to save an entity that has a variable value that has
 * already been used by another that is saved in the database.<br><br>
 * Erro lançado caso tente salvar uma entidade que possui um valor de varíavel
 * que já tenha sido usada por outra que está salva no banco de dados.
 * @author jhonesconrado
 */
public class DuplicatedUniqueField extends Exception{

    public DuplicatedUniqueField() {
    }
    
    public DuplicatedUniqueField(String msg) {
        super(msg);
    }
    
    
}
