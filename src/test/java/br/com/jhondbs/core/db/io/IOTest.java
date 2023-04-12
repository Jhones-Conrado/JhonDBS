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

import br.com.jhondbs.core.db.errors.DuplicatedUniqueField;
import br.com.jhondbs.core.db.errors.EntIdBadImplementation;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.objects.EnteTeste;
import br.com.jhondbs.core.db.base.Entity;
import tests.objects.OFDDad;
import tests.objects.OFDSon;

/**
 *
 * @author jhonessales
 */
public class IOTest {
    
    public IOTest() {
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
     * Test of save method, of class IO.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        Entity e = new EnteTeste();
        try {
            assert IO.save(e);
        } catch (DuplicatedUniqueField | EntIdBadImplementation | IllegalArgumentException | IllegalAccessException ex) {
            assert false;
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of load method, of class IO.
     */
    @Test
    public void testLoad() {
        System.out.println("load");
        Entity e = new EnteTeste();
        try {
            IO.save(e);
            Entity load = (Entity) IO.load(e, e.getEnteId());
            assert load != null;
        } catch (DuplicatedUniqueField | EntIdBadImplementation | IllegalArgumentException | IllegalAccessException ex) {
            assert false;
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

//    /**
//     * Test of loadAll method, of class IO.
//     */
//    @Test
//    public void testLoadAll_Entidade() {
//        System.out.println("loadAll");
//        Entity e = null;
//        List<Entidade> expResult = null;
//        List<Entidade> result = IO.loadAll(e);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of loadAll method, of class IO.
//     */
//    @Test
//    public void testLoadAll_3args() {
//        System.out.println("loadAll");
//        Entity e = null;
//        List<ItemFilter> filtros = null;
//        boolean todos = false;
//        List<Entidade> expResult = null;
//        List<Entidade> result = IO.loadAll(e, filtros, todos);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of loadAll method, of class IO.
//     */
//    @Test
//    public void testLoadAll_Entidade_Filter() {
//        System.out.println("loadAll");
//        Entity e = null;
//        Filter filtro = null;
//        List<Entidade> expResult = null;
//        List<Entidade> result = IO.loadAll(e, filtro);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of loadAllOnlyIds method, of class IO.
//     */
//    @Test
//    public void testLoadAllOnlyIds() {
//        System.out.println("loadAllOnlyIds");
//        Entity e = null;
//        List<Long> expResult = null;
//        List<Long> result = IO.loadAllOnlyIds(e);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of delete method, of class IO.
//     */
//    @Test
//    public void testDelete_Entidade() {
//        System.out.println("delete");
//        Entity e = null;
//        boolean expResult = false;
//        boolean result = IO.delete(e);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
    /**
     * Test of fullDelete method, of class IO.
     */
    @Test
    public void testFullDelete(){
        OFDDad obj = new OFDDad();
        long prevId = obj.getEnteId();
        long prevSonId = obj.son.getEnteId();
        
        try {
            obj.save();
            
            if(obj.getEnteId() == prevId || obj.son.getEnteId() == prevSonId){
                assert false;
            } else {
                prevId = obj.getEnteId();
                prevSonId = obj.son.getEnteId();
                
                OFDSon son = new OFDSon().load(prevSonId);
                if(son != null){
                    obj.fullDelete();
                    son = new OFDSon().load(prevSonId);
                    if(son == null){
                        assert true;
                    } else {
                        assert false;
                    }
                } else {
                    assert false;
                }
            }
            
        } catch (DuplicatedUniqueField | EntIdBadImplementation | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
        
        OFDDad dad = new OFDDad();
        try {
            dad.save();
            long sonId = dad.son.getEnteId();
            
            dad.delete();
            
            OFDSon son2 = new OFDSon().load(sonId);
            if(son2 != null){
                assert true;
            } else {
                assert false;
            }
            
        } catch (DuplicatedUniqueField | EntIdBadImplementation | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
    
//    /**
//     * Test of delete method, of class IO.
//     */
//    @Test
//    public void testDelete_List() {
//        System.out.println("delete");
//        List<Entidade> entidades = null;
//        IO.delete(entidades);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of deleteInverse method, of class IO.
//     */
//    @Test
//    public void testDeleteInverse() {
//        System.out.println("deleteInverse");
//        List<Entidade> entidades = null;
//        IO.deleteInverse(entidades);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of deleteDirectory method, of class IO.
//     */
//    @Test
//    public void testDeleteDiretorio() {
//        System.out.println("deleteDirectory");
//        File diretorio = null;
//        IO.deleteDirectory(diretorio);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
