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

import br.com.jhondbs.core.db.capsule.Capsule;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.objects.Endereco;
import tests.objects.EnteA;
import tests.objects.EnteB;
import tests.objects.MeuEnum;
import tests.objects.ObjetoA;

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
        
        Endereco endereco = new Endereco("SDO A", 429, "Jabuti", "Itaitinga", "Ceará", "Brasil");
        EnteA ente = new EnteA("Jhones Sales", endereco);
        
        for(int i = 0 ; i < 5 ; i++) {
            ente.lista.add(new ObjetoA("Objeto"+String.valueOf(i)));
        }
        
        ente.mapa.put("Maria", new ObjetoA("Maria Pessoa"));
        ente.mapa.put("João", new ObjetoA("João Pessoa"));
        ente.mapa.put("Ricardo", new ObjetoA("Ricardo Pessoa"));
        ente.mapa.put("Lene", new ObjetoA("Lene Pessoa"));

        ente.enteb = new EnteB("Carro");
        ente.enteb.dono = ente;
        ente.em = MeuEnum.USUARIO;
        
        Capsule capsule = new Capsule(ente);
        capsule.start();
        capsule.flush();
        System.out.println("Salvo:");
        System.out.println(capsule.getCapsule());
        System.out.println("");
        
        Capsule cap2 = new Capsule(capsule.getCapsule());
        EnteA ee = cap2.recover();
        
        System.out.println(ee.em);
        
        ee.enteb = null;
        
//        ee.save();
        
//        Filter filter = new Filter(true);
//        StringFilter stringFilter = new StringFilter("name", "Jhones");
//        NumberFilter numberFilter = new NumberFilter("age", NumberFilter.GREATER, 17);
//        BooleanFilter booleanFilter = new BooleanFilter("client", true);
//        
//        filter.addCondition(stringFilter);
//        filter.addCondition(numberFilter);
//        filter.addCondition(booleanFilter);
//        
//        ente.loadAll(filter);
        
        
//        System.out.println("NOME: "+FieldsManager.getValueFrom("name", ente));
        
//        EnteA recovered = capsule.recover();
//        System.out.println(recovered.name);
        
        assert true;
    }
}
