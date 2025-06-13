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

import br.com.jhondbs.core.db.Mapper;
import br.com.jhondbs.core.db.obj.ColdEntity;
import br.com.jhondbs.core.db.obj.ColdImage;
import br.com.jhondbs.core.tools.FieldsManager;
import java.awt.image.BufferedImage;
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
    public void ColdImage() throws Exception {
        correr();
        System.gc();
        System.out.println("MAP POS DELETE: "+Mapper.coldMap.size());
    }
    
    public void correr() throws Exception {
        EntidadePrincipal e = new EntidadePrincipal("jhones"+String.valueOf(System.nanoTime()));
        SubEntidade sub = new SubEntidade("COISA");
        e.coldEntity = new ColdEntity(sub);
        
        BufferedImage img = ImageIO.read(new File("./img.png"));
        e.image = new ColdImage(img);
//        e.save();
        
//        e = e.load(e.getId());
        
//        e.delete();
    }

//    @Test
//    public void salvarSemArquivo() throws IOException, Exception {
//        System.out.println("TESTE DE SALVAMENTO DE ENTIDADE SEM ARQUIVO");
//        EntidadePrincipal a = new EntidadePrincipal("Jhones"+String.valueOf(System.nanoTime()));
//        // Sub entidade recursiva
//        a.enteb = new SubEntidade("Carro");
//        a.enteb.dono = a;
//        a.save();
//        assert true;
//    }
//    
//    @Test
//    public void salvarELerSemArquivo() throws IOException, Exception {
//        System.out.println("TESTE DE SALVAR E LER SEM ARQUIVO");
//        EntidadePrincipal a = new EntidadePrincipal("Jhones"+String.valueOf(System.nanoTime()));
//        // Sub entidade recursiva
//        a.enteb = new SubEntidade("Carro");
//        a.enteb.dono = a;
//        a.save();
//        //Load
//        EntidadePrincipal a2 = a.load(a.getId());
//        System.out.println(a2.enteb.dono.name);
//        assert true;
//    }
//    
//    @Test
//    public void salvarLerEDeletarSemArquivo() throws IOException, Exception {
//        System.out.println("TESTE DE SALVAR, LER E DELETAR SEM ARQUIVO");
//        EntidadePrincipal a = new EntidadePrincipal("Jhones"+String.valueOf(System.nanoTime()));
//        // Sub entidade recursiva
//        a.enteb = new SubEntidade("Carro");
//        a.enteb.dono = a;
//        a.save();
//        //Load
//        EntidadePrincipal a2 = a.load(a.getId());
//        System.out.println(a2.enteb.dono.name);
//        a2.delete();
//        assert true;
//    }
//    
//    
//    @Test
//    public void salvarComArquivo() throws IOException, Exception {
//        System.out.println("TESTE DE SALVAMENTO DE ENTIDADE COM ARQUIVO");
//        EntidadePrincipal a = new EntidadePrincipal("Jhones"+String.valueOf(System.nanoTime()));
//        // Sub entidade recursiva
//        a.enteb = new SubEntidade("Carro");
//        a.enteb.dono = a;
//        // Arquivo
//        a.file = new File("./medidas.svg");
//        a.save();
//        assert true;
//    }
//    
//    @Test
//    public void salvarELerComArquivo() throws IOException, Exception {
//        System.out.println("TESTE DE SALVAR E LER COM ARQUIVO");
//        EntidadePrincipal a = new EntidadePrincipal("Jhones"+String.valueOf(System.nanoTime()));
//        // Sub entidade recursiva
//        a.enteb = new SubEntidade("Carro");
//        a.enteb.dono = a;
//        // Arquivo
//        a.file = new File("./medidas.svg");
//        a.save();
//        //Load
//        EntidadePrincipal a2 = a.load(a.getId());
//        System.out.println(a2.enteb.dono.name);
//        assert true;
//    }
//    
//    @Test
//    public void salvarLerEDeletarComArquivo() throws IOException, Exception {
//        System.out.println("TESTE DE SALVAR, LER E DELETAR COM ARQUIVO");
//        EntidadePrincipal a = new EntidadePrincipal("Jhones"+String.valueOf(System.nanoTime()));
//        // Sub entidade recursiva
//        a.enteb = new SubEntidade("Carro");
//        a.enteb.dono = a;
//        // Arquivo
//        a.file = new File("./medidas.svg");
//        a.save();
//        //Load
//        EntidadePrincipal a2 = a.load(a.getId());
//        System.out.println(a2.enteb.dono.name);
//        a2.delete();
//        assert true;
//    }
    
//    @Test
//    public void backupArquivoQuebrado() throws IOException, Exception {
//        System.out.println("TESTE DE SALVAR, LER E DELETAR COM ARQUIVO");
//        EntidadePrincipal a = new EntidadePrincipal("Jhones"+String.valueOf(System.nanoTime()));
//        // Sub entidade recursiva
//        a.enteb = new SubEntidade("Carro");
//        a.enteb.dono = a;
//        // Arquivo
//        a.file = new File("./medidas.svg");
//        a.save();
//        //Load
//        EntidadePrincipal a2 = a.load(a.getId());
//        a2.file.delete();
//        a2.delete();
//        assert true;
//    }
    
}
