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

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.objects.EntidadePrincipal;
import tests.objects.SubEntidade;

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
    public void correr() throws Exception {
//        EntidadePrincipal e = new EntidadePrincipal("jhones"+String.valueOf(System.nanoTime()));
//        SubEntidade sub = new SubEntidade("COISA");
//        sub.dono = e;
//        e.enteb = sub;
//        e.file = new File("./medidas.svg");
//        Image img = ImageIO.read(new File("./img.jpg"));
//        e.image = img;
//        e.save();
//        
//        e = e.load(e.getId());
//        e.enteb.file = new File("./img.jpg");
//        e.file = null;
//        e.save();
//        
//        e = e.load(e.getId());
//        e.enteb.image = ImageIO.read(new File("./img2.png"));
//        e.save();
//        e = e.load(e.getId());
//        e.image = null;
//        e.save();
//        e.delete();
        
//        e.enteb.subDaSub = new SubEntidade("SUB DA SUB");
//        e.enteb.subDaSub.subDaSub = e.enteb;
//        e.save();
//        
//        e.delete();
    }
    
    private void soutNames(EntidadePrincipal e) {
        System.out.println("NOME -> "+e.name);
        System.out.println("-> "+e.enteb.type);
        System.out.println("-> "+e.character);
        System.out.println("-> "+e.curto);
        System.out.println("-> "+e.inteiro);
        System.out.println("-> "+e.longo);
        System.out.println("-> "+e.decimal);
        System.out.println("-> "+e.duplo);
        System.out.println("-> "+e.boleano);
        System.out.println("-> "+e.xbyte);
        System.out.println("-> "+e.bigdecimal);
        System.out.println("-> "+e.biginteger);
        System.out.println("-> "+e.endereco);
        System.out.println("-> lista size"+e.lista.size());
        System.out.println("-> setList size"+e.setList.size());
        System.out.println("-> mapa size"+e.mapa.size());
        System.out.println("-> "+e.date);
        System.out.println("-> "+e.calendar);
        System.out.println("-> "+e.localDate);
        System.out.println("-> "+e.time);
        System.out.println("-> "+e.dateTime);
        System.out.println("-> "+e.zonedDateTime);
        System.out.println("-> "+e.instant);
        System.out.println("-> "+e.period);
        System.out.println("-> "+e.enteb);
        System.out.println("-> "+e.subObjeto);
        System.out.println("-> "+e.file);
    }
    
}
