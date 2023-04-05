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
package br.com.jhondbs.core.db;

import br.com.jhondbs.core.db.filter.ItemFilter;
import br.com.jhondbs.core.db.filter.StringFilter;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import tests.objects.EnteTeste;
import br.com.jhondbs.core.db.base.Entity;

/**
 * Testa as funções da API do banco de dados.
 * @author jhonessales
 */
public class DBAPITest {
    
    @Test
    public void testStringIgual(){
        System.out.println("String Igual");
        String cmd = "ti nome Jhones";
        StringFilter f = new StringFilter("nome", "Jhones");
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringContem(){
        System.out.println("String Contém");
        String cmd = "tp nome Jhones";
        StringFilter f = new StringFilter(StringFilter.POSSUI, "nome", "Jhones");
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringComeca(){
        System.out.println("String Começa");
        String cmd = "tc nome Jhones";
        StringFilter f = new StringFilter(StringFilter.COMECA, "nome", "Jhones");
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringTermina(){
        String cmd = "tt nome Jhones";
        StringFilter f = new StringFilter(StringFilter.TERMINA, "nome", "Jhones");
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringComecalIgnoreCase(){
        String cmd = "tci nome JHONES";
        StringFilter f = new StringFilter(StringFilter.COMECA, "nome", "Jhones", true);
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringTerminaIgnoreCase(){
        String cmd = "tti nome JHONES";
        StringFilter f = new StringFilter(StringFilter.TERMINA, "nome", "Jhones", true);
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringPossuiIgnoreCase(){
        String cmd = "tpi nome JHONES";
        StringFilter f = new StringFilter(StringFilter.POSSUI, "nome", "Jhones", true);
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringIgualIgnoreCase(){
        String cmd = "tii nome JHONES";
        StringFilter f = new StringFilter(StringFilter.IGUAL, "nome", "Jhones", true);
        assert DBAPI.toString(f).equals(cmd);
    }
    
    @Test
    public void testStringIgualInverse(){
        try {
            String cmd = "ti nome Jhones";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testStringContemInverse(){
        try {
            String cmd = "tp nome Jhones";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testStringComecaInverse(){
        try {
            String cmd = "tc nome Jhones";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testStringTerminaInverse(){
        try {
            String cmd = "tt nome Jhones";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testStringComecalIgnoreCaseInverse(){
        try {
            String cmd = "tci nome JHONES";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testStringTerminaIgnoreCaseInverse(){
        try {
            String cmd = "tti nome JHONES";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testStringPossuiIgnoreCaseInverse(){
        try {
            String cmd = "tpi nome JHONES";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testStringIgualIgnoreCaseInverse(){
        try {
            String cmd = "tii nome JHONES";
            ItemFilter f = DBAPI.toFilter(cmd);
            assert DBAPI.toString(f).equals(cmd);
        } catch (Exception ex) {
            assert false;
        }
    }
    
    @Test
    public void testLoadAllByCMD(){
        try {
            
            EnteTeste a = new EnteTeste(27, "Jhones", true);
            EnteTeste b = new EnteTeste(53, "Carla", true);
            EnteTeste c = new EnteTeste(30, "Maria", false);
            
            a.save();
            b.save();
            c.save();
            
            /**
             * Recupera uma lista de todos os objetos do tipo EnteTeste salvos no
             * banco de dados, filtrados para somente os que o nome contém a letra
             * 'a'.
             */
            List<Entity> byf = DBAPI.getByFilter("EnteTeste tpi nome a");
            assert byf.size() == 2;
        } catch (Exception ex) {
            Logger.getLogger(DBAPITest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
}
