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
package tests;

import tests.objects.EnteTeste;
import br.com.jhondbs.core.db.filter.NumberFilter;

/**
 * Testes dos filtros de números.
 * @author jhonessales
 */
public class NumberFilterTest {
    
    public void testNumberEquals(){
        EnteTeste e = new EnteTeste();
        e.idade = 18;
        NumberFilter f = new NumberFilter("idade", NumberFilter.IGUAL, 18d);
        assert f.filtrar(e);
    }
    
    public void testNumberSmaller(){
        EnteTeste e = new EnteTeste();
        e.idade = 15;
        NumberFilter f = new NumberFilter("idade", NumberFilter.MENOR, 18d);
        assert f.filtrar(e);
    }
    
    public void testNumberBigger(){
        EnteTeste e = new EnteTeste();
        e.idade = 25;
        NumberFilter f = new NumberFilter("idade", NumberFilter.MAIOR, 18d);
        assert f.filtrar(e);
    }
    
    public void testNumberBetween(){
        EnteTeste e = new EnteTeste();
        e.idade = 18;
        NumberFilter f = new NumberFilter("idade", 15, 30d);
        assert f.filtrar(e);
    }
    
}
