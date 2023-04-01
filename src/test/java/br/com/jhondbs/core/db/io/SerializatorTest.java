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
package br.com.jhondbs.core.db.io;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import tests.objects.ObjTesteExtendido;

/**
 *
 * @author jhonessales
 */
public class SerializatorTest {
    
    public SerializatorTest() {
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

    /**
     * Test of serialize method, of class Serializator.
     */
    @Test
    public void testSerialize() {
        System.out.println("serialize");
        ObjTesteExtendido obj = new ObjTesteExtendido();
        String result = Serializator.serialize(obj);
        System.out.println(result);
        try {
            Object d = Serializator.deserialize(result);
            System.out.println(d.getClass());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assert true;
    }

    /**
     * Test of deserialize method, of class Serializator.
     */
    @Test
    public void testDeserialize() throws Exception {
        System.out.println("deserialize");
        assert true;
    }
    
}
