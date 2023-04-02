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
package br.com.jhondbs.core.db.base;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.Test;
import tests.objects.ObjTeste;
import tests.objects.ObjTesteExtendido;

/**
 *
 * @author jhonessales
 */
public class FieldsManagerTest {
    
    @Test
    public void getValueFromPublicTest(){
        System.out.println("getValueFrom public");
        ObjTeste o = new ObjTeste();
        try {
            Object v = FieldsManager.getValueFrom("name", o);
            System.out.println(v);
            Assert.assertNotNull(v);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(FieldsManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void getValueFromPrivateTest(){
        System.out.println("getValueFrom private");
        ObjTeste o = new ObjTeste();
        try {
            Object v = FieldsManager.getValueFrom("idade", o);
            System.out.println(v);
            Assert.assertNotNull(v);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(FieldsManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void getValueFromPrivateExtendTest(){
        System.out.println("getValueFrom private extend");
        ObjTesteExtendido o = new ObjTesteExtendido();
        try {
            Object v = FieldsManager.getValueFrom("idade", o);
            System.out.println(v);
            Assert.assertNotNull(v);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(FieldsManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void getValueFromPrivateExtend2Test(){
        System.out.println("getValueFrom private extend 2");
        ObjTesteExtendido o = new ObjTesteExtendido();
        try {
            Object v = FieldsManager.getValueFrom("altura", o);
            System.out.println(v);
            Assert.assertNotNull(v);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(FieldsManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
