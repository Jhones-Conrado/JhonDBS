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
package br.com.jhondbs.core.db.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * It serves to annotate the fields of entities that cannot be duplicated among
 * other entities of the same type.<br>
 * Serve para anotar os campos das entidades que não podem ser duplicados entre
 * as outras entidades de mesmo tipo.
 * @author jhonesconrado
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {
    
}
