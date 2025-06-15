/*
 * The MIT License
 *
 * Copyright 2025 jhones.
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
package br.com.jhondbs.core.db.capsule;

import br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException;
import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.tools.Reflection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Responsável por verificar a unicidade de campos anotados como @Unique.
 * Uma entidade só poderá ser gravada no banco de dados se passar nos testes de
 * unidade desta classe.
 * @author jhones
 */
public class FieldSecurity {
    
    /**
     * Verifica se uma entidade passa no teste de unicidade de campos no contexto
     * da bottle que está contida (Se modo root ou temp).
     * @param bottle
     * @return
     * @throws EntityIdBadImplementationException
     * @throws DuplicatedUniqueFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IOException 
     */
    public static boolean testUnicity(Bottle bottle) throws Exception {
        for (Bottle bot : bottle.bottles.values()) {
            if (!testeDeUnicidade(bottle)) return false;
        }
        return true;
    }
    
    private static boolean testeDeUnicidade(Bottle bottle) throws Exception {
        if(bottle == null) throw new NullPointerException("Bottle nula para teste de unicidade de campos.");
        if(bottle.entity == null) throw new NullPointerException("Entidade nula para o teste de unidade de campos.");
        List<Field> unicos = FieldsManager.getAllFieldsUniques(bottle.entity);
        if (unicos.isEmpty()) {
            return true;
        }
        
        String path = Assist.getPath(bottle);
        String[] ids = new File(path).getParentFile().list();
        
        if (ids != null && ids.length > 0) {
            for (String id : ids) {
                if (!id.equals(bottle.entity.getId())) {
                    // Mapa de valores únicos do objeto no banco de dados.
                    Map<String, String> paraComparar = Reader.readUniqueFieldsAsMap(bottle.entity.getClass(), id, bottle.TEMP_DB);
                    
                    //Itera sobre os campos únicos do objeto principal
                    for (Field unico : unicos) {
                        
                        // Verifica se o objeto lido tem algum campo único com o mesmo nome de variável.
                        if (paraComparar.containsKey(unico.getName())) {
                            
                            // Valor do objeto principal
                            Object get = unico.get(bottle.entity);
                            if (get != null) {
                                
                                // Verifica se o objeto é uma entidade
                                if (Reflection.isInstance(get.getClass(), Entity.class)) {
                                    Entity ente = (Entity) get;
                                    if (paraComparar.get(unico.getName()).contains(ente.getId())) {
                                        throw new DuplicatedUniqueFieldException("Campo unico duplicado:\n"
                                                + "-> " + bottle.entity + " id: " + bottle.entity.getId() + "\n"
                                                + "-> " + unico.getName() + "\n"
                                                + "-> " + ente.getId() + " igual " + paraComparar.get(unico.getName()));
                                    }
                                } 
                                // Caso não seja uma entidade
                                else {
                                    String valorLimpo = Reader.getValueFromCapsule(paraComparar.get(unico.getName()));
                                    if (get.toString().equals(valorLimpo)) {
                                        throw new DuplicatedUniqueFieldException("Campo unico duplicado:\n"
                                                + "-> " + bottle.entity + " id: " + bottle.entity.getId() + "\n"
                                                + "-> " + unico.getName() + "\n"
                                                + "-> " + get.toString() + " igual " + valorLimpo);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
}
