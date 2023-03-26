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
 * Erro lançado em caso de um atribute não ter sido encontrado em uma determinada
 * classe.
 * @author jhonesconrado
 */
public class AttributeNotFind extends Exception {

    /**
     * Creates a new instance of <code>AtributoNaoEncontrado</code> without
     * detail message.
     */
    public AttributeNotFind() {
    }

    /**
     * Constructs an instance of <code>AtributoNaoEncontrado</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public AttributeNotFind(String msg) {
        super(msg);
    }
}
