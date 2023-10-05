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

import br.com.jhondbs.core.db.base.Entity;
import br.com.jhondbs.core.db.base.padrao.DefaultEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author jhonessales
 */
public class ObjetoMulti extends DefaultEntity {
    
    public String name = "Jhones";
    public int idade = 27;
    public double altura = 1.71;
    public BigDecimal saldo = new BigDecimal(2500);
    boolean rico = true;
    Date hoje = new Date();
    Calendar calendario = new GregorianCalendar();
    
    private ObjetoMultiInterno interno = new ObjetoMultiInterno();
    
    List<Object> objetos = new ArrayList<>();
    Set<Object> set = new HashSet<>();
    Map<String, Object> map = new HashMap<>();
    Properties props = new Properties();
    
    EnteA ente = new EnteA();
    
    public List<Entity> entities = new ArrayList<>();

    public ObjetoMulti() {
        this.objetos.add(new ObjetoMultiInterno());
        this.objetos.add(new ObjetoMultiInterno());
        this.objetos.add(new ObjetoMultiInterno());
        this.objetos.add(new ObjetoMultiInterno());
        
        this.set.add(new ObjetoMultiInterno());
        this.set.add(new ObjetoMultiInterno());
        
        this.map.put("a", new ObjetoMultiInterno());
        this.map.put("b", new ObjetoMultiInterno());
        this.map.put("c", new ObjetoMultiInterno());
        
        this.props.put("nome", name);
        this.props.put("idade", idade);
        
        this.ente.name = "Entidade";
        
        this.entities.add(new EnteA("Carlos"));
        this.entities.add(new EnteA("Bel"));
        this.entities.add(new EnteA("Janaina"));
        this.entities.add(new EnteA("Fernando"));
        this.entities.add(new EnteA("Maria"));
    }
    
}
