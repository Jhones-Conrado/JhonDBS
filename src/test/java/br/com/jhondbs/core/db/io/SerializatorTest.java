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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.objects.EnteA;
import tests.objects.EnteBase;
import tests.objects.HardObject;
import tests.objects.ObjA;
import tests.objects.ObjB;
import tests.objects.ObjMantemAbstrato;
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
        try {
            Serializator.serialize(obj);
            assert true;
        } catch (IllegalAccessException | IllegalArgumentException e) {
            assert false;
        } catch (DuplicatedUniqueField | EntIdBadImplementation ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of deserialize method, of class Serializator.
     */
    @Test
    public void testDeserialize() {
        System.out.println("deserialize");
        ObjTesteExtendido obj = new ObjTesteExtendido();
        try {
            Object d = Serializator.deserialize(Serializator.serialize(obj));
            assert d != null;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        } catch (Exception ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
    @Test
    public void testDeserializeAbstract(){
        System.out.println("deserialize abstract");
        
        ObjMantemAbstrato oba = new ObjMantemAbstrato();
        oba.item = new ObjA();
        
        ObjMantemAbstrato obb = new ObjMantemAbstrato();
        obb.item = new ObjB();
        
        try {
            Object da = Serializator.deserialize(Serializator.serialize(oba));
            Object db = Serializator.deserialize(Serializator.serialize(obb));
            
            ObjMantemAbstrato castA = ObjMantemAbstrato.class.cast(da);
            ObjMantemAbstrato castB = ObjMantemAbstrato.class.cast(db);
            
            if(castA.item.getName().equals("a") &&
                    castB.item.getName().equals("b") &&
                    castA.bonito == true &&
                    castB.bonito == true){
                assert true;
            } else {
                assert false;
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        } catch (Exception ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
    @Test
    public void testSubEntity(){
        System.out.println("test sub entity");
        
        EnteBase base = new EnteBase();
        EnteA a = new EnteA();
        a.name = "Jhones";
        base.a = a;
        
        try {
            base.save();
            EnteBase b = base.load(base.getEnteId());
            assert b.a.name.equals("Jhones");
        } catch (DuplicatedUniqueField | EntIdBadImplementation | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
    @Test
    public void testHardObject(){
        System.out.println("test HardObject serialization");
        HardObject o = new HardObject();
        try {
            if(o.save()){
                HardObject load = o.load(o.getEnteId());
                assert load.props.get("nome").equals("Jhones");
            } else {
                assert false;
            }
        } catch (DuplicatedUniqueField | EntIdBadImplementation | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
    @Test
    public void testListSerialize(){
        System.out.println("test List serialization");
        List<Object> list = new ArrayList<>();
        list.add("Texto 1");
        list.add(50);
        list.add(new EnteA());
        try {
            String json = Serializator.serialize(list);
            List<Object> desList = Serializator.deserialize(json);
            assert desList.size() == 3;
        } catch (IllegalArgumentException | IllegalAccessException | DuplicatedUniqueField | EntIdBadImplementation ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        } catch (InstantiationException ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        } catch (Exception ex) {
            Logger.getLogger(SerializatorTest.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
}
