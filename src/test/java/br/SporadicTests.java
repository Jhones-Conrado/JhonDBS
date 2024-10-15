/*
 * The MIT License
 *
 * Copyright 2024 jhones.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br;

import br.com.jhondbs.core.db.obj.ColdEntity;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.objects.Cold;
import tests.objects.EnteA;
import tests.objects.EnteB;

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
        
//        EnteA a = new EnteA();
//        a.loadAll();
        
        
        
//        EnteB b = new EnteB("Subentidade");
//        b.dono = a;
//        a.enteb = b;
//        
//        Cold cold = new Cold();
//        a.cold = new ColdEntity(cold);
//        
//        try {
//            if(a.save()) {
//                System.out.println("salvou");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        EnteA ab = new EnteA("Carlos");
//        ab.enteb = b;
//        ab.save();
        
        assert true;
    }
}
