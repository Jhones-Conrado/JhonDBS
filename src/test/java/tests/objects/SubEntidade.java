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

import br.com.jhondbs.core.db.interfaces.Entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jhones
 */
public class SubEntidade implements Entity {
    
    private String id;
    
    private char character = 'a';
    private short s = 1;
    private int i = 32;
    private long l = 15000l;
    private float f = 13.233f;
    private double d = 67236.2345d;
    private List<String> strList = new ArrayList<>();
    private String[] arrayStr = {"Casa", "Abelha", "Mariposa"};
    private Map<String, Integer> mapa = new HashMap();
    private List<ObjetoComplexo> subEntes = new ArrayList<>();
    
    private String type;

    public EntidadePrincipal dono;
    
    private EnumBasico meu;
    
    public SubEntidade() {
        this.meu = EnumBasico.USUARIO;
        populate();
    }

    public SubEntidade(String type) {
        this.meu = EnumBasico.USUARIO;
        this.type = type;
        populate();
    }

    public SubEntidade(String id, String type) {
        this.id = id;
        this.type = type;
        populate();
    }
    
    private void populate() {
        subEntes.add(new ObjetoComplexo("cachorro"));
        subEntes.add(new ObjetoComplexo("esposa"));
        subEntes.add(new ObjetoComplexo("barco"));
    }
    
}
