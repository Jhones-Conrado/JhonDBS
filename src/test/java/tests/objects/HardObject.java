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

import br.com.jhondbs.core.db.base.padrao.DefaultEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author jhonessales
 */
public class HardObject extends DefaultEntity {
    
    private BigDecimal money;
    private List<Object> textList;
    private final String name;
    private Date date;
    private GregorianCalendar calendar;
    private Set set;
    private Properties props;

    public HardObject() {
        money = new BigDecimal(50);
        textList = new ArrayList<>();
        name = "Jhones";
        date = new Date();
        calendar = new GregorianCalendar();
        
        EnteA ente = new EnteA();
        ente.name = "João HardObject";
        
        textList.add("casa");
        textList.add("sapato");
        textList.add(55);
        textList.add(33.002);
        textList.add("boneca");
        textList.add("mato");
        textList.add(ente);
        textList.add(new Date());
        
        set = new HashSet();
        set.add(5);
        set.add(10);
        set.add(11.3);
        set.add("Jhones");
        set.add(5);
        
        props = new Properties();
        props.put("nome", "Jhones");
        
    }
    
    public String getName(){
        return name;
    }
    
    public BigDecimal getMoney(){
        return money;
    }
    
    public List<Object> getList(){
        return textList;
    }
    
}
