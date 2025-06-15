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

import java.util.HashMap;
import java.util.Map;

/**
 * Responsável por iniciar todo o processo de exclusão de uma entidade, sendo
 * chamado de forma recursiva em caso de entidades marcadas como Cascate que
 * se tornem órfãs após a exclusão da entidade chamada.
 * A -> B -> C.
 * Se B e C forem marcadas como Cascate, ao excluir A o método a seguir será
 * chamado de forma recursiva para B e depois para C, desde que nenhuma outra
 * entidade também faça referência a estas.
 * Isso significa que uma exclusão em cascata só acontece se a entidade se tornar
 * órfã.
 * @author jhones
 */
public class Ereaser {
    
    public static void flush(Bottle start, Bottle oldStage) throws Exception {
        Ref toRemove = new Ref(start.entity);
        Map<String, Bottle> map = new HashMap();
        map.putAll(oldStage.bottles);
        for(String s : start.bottles.keySet()) {
            map.putIfAbsent(s, start.bottles.get(s));
        }
        Assist.markExclude(start.props);
        Writer.write(start);
        for(String id : map.keySet()) {
            if(!id.equals(start.entity.getId())) {
                Assist.removeExistence(toRemove, new Ref(map.get(id).entity), start.TEMP_DB);
            }
        }
    }
    
}
