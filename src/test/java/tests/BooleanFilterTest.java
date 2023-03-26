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

import br.com.jhondbs.core.db.filter.BooleanFilter;
import tests.objects.EnteTeste;

/**
 * Testa a classe de filtragem booleana.
 * @author jhonessales
 */
public class BooleanFilterTest {
    
    public void testBooleanFilter(){
        EnteTeste e = new EnteTeste();
        e.rico = true;
        BooleanFilter f = new BooleanFilter("rico", true);
        assert f.filtrar(e);
    }
    
}
