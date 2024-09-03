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
package tests.objects;

import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.db.interfaces.Unique;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jhonessales
 */
public class EnteA implements Entity{
    
    private String id;
    
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
    
    public Map<String, ObjetoA> mapa = new HashMap<>();
    
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
    
    public EnteA() {
        this.name = null;
    }

    public EnteA(String name) {
        this.name = name;
    }

    public EnteA(String name, Endereco endereco) {
        this.name = name;
        this.endereco = endereco;
    }
    
}
