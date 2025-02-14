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
package tests.objects;

import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.db.interfaces.Unique;
import br.com.jhondbs.core.db.obj.ColdEntity;
import br.com.jhondbs.core.tools.Reflection;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jhonessales
 */
public class EnteA implements Entity{
    
    private String id;
    
    @Unique
    public final String name;
    
    public MeuEnum em;
    
    public char character = 'x';
    public short curto = 5;
    public int inteiro = 9;
    public long longo = 30l;
    public float decimal = 4.2f;
    public double duplo = 7.3;
    public boolean boleano = true;
    public byte xbyte = 0x15;
    public BigDecimal bigdecimal = new BigDecimal(25.30);
    public BigInteger biginteger = new BigInteger("100");
    
    public Endereco endereco;
    
    public List<ObjetoA> lista = new ArrayList<>();
    
    public Set<Object> setList = new HashSet<>();
    
    public Map<String, Object> mapa = new HashMap<>();
    
    public Date date = new Date();
    public Calendar calendar = GregorianCalendar.getInstance();
    
    public LocalDate localDate = LocalDate.now();
    public LocalTime time = LocalTime.now();
    public LocalDateTime dateTime = LocalDateTime.now();
    public ZonedDateTime zonedDateTime = ZonedDateTime.now();
    public Instant instant = Instant.now();
    public Period period = Period.between(localDate, localDate.plusMonths(4));
    
    @Cascate
    public EnteB enteb;
    
    public ObjetoA subObjeto = new ObjetoA("sub objeto");
    
    public ColdEntity cold;
    
    public File file;
    
    public EnteA() {
        this.name = null;
    }

    public EnteA(String name) {
        this.name = name;
        String[] a = {"carro", "bike", "moto", "casa"};
        setList.addAll(Arrays.asList(a));
        
        lista.add(new ObjetoA("tomate"));
        lista.add(new ObjetoA("cereja"));
        lista.add(new ObjetoA("alface"));
        lista.add(new ObjetoA("coentro"));
        
        System.out.println("SUB ESTÁ SAINDO COMO LISTA?");
        System.out.println(Reflection.isArrayMap(subObjeto));
    }

    public EnteA(String name, Endereco endereco) {
        this.name = name;
        this.endereco = endereco;
    }
    
}
