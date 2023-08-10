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
package br;

import br.com.jhondbs.core.webServer.WebServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.objects.EnteComRepresent;

/**
 *
 * @author jhonessales
 */
public class SporadicTests {
    
    public SporadicTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void hello() throws IOException, Exception {
        System.out.println("sporadic test");
        
        EnteComRepresent e = new EnteComRepresent();
        
        System.out.println("######### ID");
        System.out.println("- "+e.getEnteId());
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        e.setEnteId(5);
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println("- new "+e.getEnteId());
        
        assert e.getEnteId() == 5l;
        
//        try {
//            e.setOwner(e);
//            e.save();
//            assert true;
//        } catch (Exception ex) {
//            Logger.getLogger(SporadicTests.class.getName()).log(Level.SEVERE, null, ex);
//            assert false;
//        }
    }
}
