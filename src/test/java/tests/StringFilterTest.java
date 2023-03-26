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

import br.com.jhondbs.core.db.filter.StringFilter;
import tests.objects.EnteTeste;

/**
 * Testa os métodos da classe StringFilter.
 * @author jhonessales
 */
public class StringFilterTest {
    
    public void testContains(){
        EnteTeste e = new EnteTeste();
        e.nome = "Jhones Conrado";
        StringFilter f = new StringFilter(StringFilter.POSSUI, "nome", "Jhones");
        assert f.filtrar(e);
    }
    
    public void testStarts(){
        EnteTeste e = new EnteTeste();
        e.nome = "Jhones Conrado";
        StringFilter f = new StringFilter(StringFilter.COMECA, "nome", "Jho");
        assert f.filtrar(e);
    }
    
    public void testEnds(){
        EnteTeste e = new EnteTeste();
        e.nome = "Jhones Conrado";
        StringFilter f = new StringFilter(StringFilter.TERMINA, "nome", "do");
        assert f.filtrar(e);
    }
    
    public void testEquals(){
        EnteTeste e = new EnteTeste();
        e.nome = "Jhones Conrado";
        StringFilter f = new StringFilter(StringFilter.IGUAL, "nome", "Jhones Conrado");
        assert f.filtrar(e);
    }
    
    public void testContainsIgnoreCase(){
        EnteTeste e = new EnteTeste();
        e.nome = "JHones Conrado";
        StringFilter f = new StringFilter(StringFilter.POSSUI, "nome", "Jho", true);
        assert f.filtrar(e);
    }
    
    public void testStartsIgnoreCase(){
        EnteTeste e = new EnteTeste();
        e.nome = "JhOnes Conrado";
        StringFilter f = new StringFilter(StringFilter.COMECA, "nome", "Jho", true);
        assert f.filtrar(e);
    }
    
    public void testEndsIgnoreCase(){
        EnteTeste e = new EnteTeste();
        e.nome = "Jhones ConradO";
        StringFilter f = new StringFilter(StringFilter.TERMINA, "nome", "do", true);
        assert f.filtrar(e);
    }
    
    public void testEqualsIgnoreCase(){
        EnteTeste e = new EnteTeste();
        e.nome = "JhONes CoNrAdo";
        StringFilter f = new StringFilter(StringFilter.IGUAL, "nome", "Jhones Conrado", true);
        assert f.filtrar(e);
    }
    
}
