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

import static br.com.jhondbs.core.db.capsule.Assist.getPathFromRef;
import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.tools.FieldsManager;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Classe ainda em análise, preciso decidir sobre um caso específico onde uma
 * entidade marcada como Cascate possui um campo para outra entidade que por sua
 * vez referencia ela.
 * Ou seja: A -> B <-> C
 * A marca B e, B e C se marcam. Se eu chamar o delete em A, B jamais vai ser
 * orfão porque está sendo referênciado por C, mesmo que C também esteja marcado
 * como Cascate!
 * Tando B como C são marcados, mas por fazer referência um ao outro isso significa
 * que chamar o delete em A não apagará nem B e nem C, a menos que o delete seja
 * chamado diretamente em B ou C!
 * A decisão sobre o funcionamento desse paradoxo é a questão crucial para a
 * conclusão ou não desta classe.
 * @author jhones
 */
public class CascateAnalyzer {
    
    /**
     * Verifica nas referências restantes da entidade quais destas estão marcadas
     * como Cascate e quais são referências cruzadas.
     * @param beCleaned
     * @param temp_db
     * @throws Exception 
     */
    public static void analyze(Ref beCleaned, String temp_db) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream(new File(getPathFromRef(beCleaned, temp_db))));
        List<Ref> refs = getRefs(props);
        Map<String, String> cascateFields = getCascateFields(beCleaned, props);
        for(Ref ref : refs) {
            // Verifica se a referência está nos campos de cascata.
            if(testCascate(ref, cascateFields)) {
                /*
                Realizar a verificação das referências que também são valores de
                algum campo cascata.
                */
                
            }
        }
    }
    
    public static List<Ref> getRefs(Properties properties) {
        return Arrays.asList(properties.getProperty("refs")
                .split("::"))
                .stream()
                .filter(ref -> !ref.isBlank())
                .map(ref -> new Ref(ref))
                .toList();
    }
    
    public static boolean testCascate(Ref ref, Map<String, String> cascateFields) {
        return cascateFields.values().stream()
            .anyMatch(value -> value.contains(ref.getKey()));
    }
    
    public static Map<String, String> getCascateFields(Ref ente, Properties properties) {
        List<String> cascateFieldsNames = FieldsManager.getAllSerializebleFields(ente.recoverClass())
                .stream()
                .filter(field -> field.isAnnotationPresent(Cascate.class))
                .toList()
                .stream().map(field -> field.getName())
                .toList();
        
        return Reader
                .splitCapsules(properties.getProperty("fields")).stream()
                .map(Reader::splitCapsuleAsKeyValueMap)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (v1, v2) -> v1 // Em caso de chaves duplicadas
                ))
                .entrySet().stream()
                .filter(entry -> cascateFieldsNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
}
