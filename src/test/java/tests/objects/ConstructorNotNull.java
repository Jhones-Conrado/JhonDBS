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
package tests.objects;

/**
 *
 * @author jhonessales
 */
public class ConstructorNotNull {
    
    private String name;
    private int age;
    private float height;
    private Object obj;

    public ConstructorNotNull(String name, int age, float height, Object obj) {
        this.name = name;
        this.age = age;
        this.height = height;
        this.obj = obj;
    }
    
}
