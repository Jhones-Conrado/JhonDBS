/*
 * Copyright (C) 2024 jhones
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jhones
 */
public class ObjetoComplexo {
    
    private String nome = "";
    private char character = 'a';
    private short s = 1;
    private int i = 32;
    private long l = 15000l;
    private float f = 13.233f;
    private double d = 67236.2345d;
    private List<String> strList = new ArrayList<>();
    private String[] arrayStr = {"Casa", "Abelha", "Mariposa"};
    private Map<String, Integer> mapa = new HashMap();
    
    public ObjetoComplexo() {
    }

    public ObjetoComplexo(String nome) {
        this.nome = nome;
    }
    
}
