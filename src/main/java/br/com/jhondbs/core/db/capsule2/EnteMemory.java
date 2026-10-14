/*
 * The MIT License
 *
 * Copyright 2026 jhones.
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
package br.com.jhondbs.core.db.capsule2;

import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.IOAssist;
import br.com.jhondbs.core.tools.SessionsCache;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Instância da entidade em memória, grafo de referências, ponteiros e o
 * {@link Properties} pronto para gravar no arquivo da entidade.
 *
 * <p>Chaves do arquivo:
 * <pre>
 * id=uuid
 * fields={nome:{8:Ana}}{cliente:{14:uuid}}
 * refs=10:uuid-pai::11:uuid-outro
 * cascate=true
 * stamp=2026-09-26T15:22:00.123Z
 * </pre>
 * {@code refs} são os <strong>referenciadores</strong> ({@code indice:id} separados
 * por {@code ::}). {@code cascate} só entra se a entidade estiver marcada.
 *
 * @author jhones
 */
public class EnteMemory {

    private Entity instance;
    private String id;
    private Class clazz;

    private boolean cascate = false;

    private String fieldsCapsule = "";
    private Properties fileProperties;

    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, List<Pointer>> ponteiros = new HashMap<>();
    private final Map<String, EnteMemory> referenciados = new HashMap<>();
    private final Map<String, EnteMemory> referenciadores = new HashMap<>();
    private final Map<String, Class> referenciadoresClasses = new HashMap<>();

    public EnteMemory(Entity entity) throws Exception {
        if (entity == null) throw new NullPointerException("A entidade não pode ser nula.");
        this.id = entity.getId();
        this.clazz = entity.getClass();
        this.instance = entity;

        oldAnalyse();
    }

    /**
     * Busca no banco se existe versão antiga desta entidade e preenche
     * referenciadores persistidos.
     */
    public void oldAnalyse() throws Exception {
        Properties properties = IOAssist.entityToProperties(instance);
        if (properties == null) return;

        if ("true".equals(properties.getProperty("cascate"))) {
            this.cascate = true;
        }

        String refString = properties.getProperty("refs");
        if (refString == null || refString.isBlank()) return;

        List<String> listRefIds = Arrays.asList(refString.split("::")).stream()
                .filter(s -> s != null && !s.isBlank())
                .toList();

        for (String s : listRefIds) {
            String[] splited = s.split(":", 2);
            if (splited.length < 2 || splited[0].isBlank() || splited[1].isBlank()) continue;

            Class refClass = ClassDictionary.fromIndex(Integer.parseInt(splited[0].trim()));
            String refID = splited[1].trim();

            if (refClass != null) {
                this.referenciadoresClasses.put(refID, refClass);
            }

            EnteMemory findById = SessionsCache.find(refID);
            if (findById != null) {
                this.referenciadores.put(refID, findById);
            }
        }

        orphanRemove();
    }

    public void addReferenciador(EnteMemory entity) {
        if (entity == null) return;
        if (!this.referenciadores.containsKey(entity.getId())) {
            this.referenciadores.put(entity.getId(), entity);
        }
        this.referenciadoresClasses.put(entity.getId(), entity.getClasse());
    }

    public void removeReferenciador(EnteMemory entity) {
        if (entity == null) return;
        this.referenciadores.remove(entity.getId());
        this.referenciadoresClasses.remove(entity.getId());
    }

    public void addReferenciado(EnteMemory entity) {
        if (entity == null) return;
        this.referenciados.putIfAbsent(entity.getId(), entity);
    }

    public void removeReferenciado(EnteMemory entity) {
        if (entity == null) return;
        this.referenciados.remove(entity.getId());
    }

    /**
     * Registra um ponteiro já montado pelo analyzer.
     */
    public void addPonteiro(Pointer pointer) {
        if (pointer == null) return;
        String pointerId = pointer.getId();
        if (pointerId == null || pointerId.isBlank()) return;
        this.ponteiros.computeIfAbsent(pointerId, k -> new ArrayList<>()).add(pointer);
    }

    /**
     * Compatibilidade: despacha para o factory correto.
     * Valor de mapa com chave nula deve usar {@link Pointer#ofMapValue}.
     */
    public void addPonteiro(Object complexo, Field field, String id, Map map, Object key) {
        if (complexo == null || field == null) return;
        if (id == null || id.isBlank()) return;

        Pointer pointer;
        if (map != null && key != null) {
            pointer = Pointer.ofMapValue(id, complexo, field, map, key);
        } else if (map != null) {
            pointer = Pointer.ofMapKey(id, complexo, field, map);
        } else {
            pointer = Pointer.ofField(id, complexo, field);
        }
        addPonteiro(pointer);
    }

    public void orphanRemove() {
        if (!isOrphan()) return;
    }

    public String getId() {
        return this.id;
    }

    public Class getClasse() {
        return this.clazz;
    }

    public Entity getInstance() {
        return this.instance;
    }

    public void setInstance(Entity instance) {
        if (instance != null) {
            this.instance = instance;
        }
    }

    public boolean isCascate() {
        return this.cascate;
    }

    public Map<String, EnteMemory> getReferenciados() {
        return this.referenciados;
    }

    public Map<String, EnteMemory> getReferenciadores() {
        return this.referenciadores;
    }

    public Map<String, Class> getReferenciadoresClasses() {
        return this.referenciadoresClasses;
    }

    public Map<String, List<Pointer>> getPonteiros() {
        return this.ponteiros;
    }

    /**
     * Concatenação {@code {campo:{indice:valor}}} — valor da chave {@code fields}.
     */
    public String getFieldsCapsule() {
        return this.fieldsCapsule == null ? "" : this.fieldsCapsule;
    }

    public void setFieldsCapsule(String fieldsCapsule) {
        this.fieldsCapsule = fieldsCapsule == null ? "" : fieldsCapsule;
    }

    public Properties getFileProperties() {
        return this.fileProperties;
    }

    /**
     * Verifica se a entidade está órfã: cascade e nenhum referenciador.
     */
    public boolean isOrphan() {
        return this.cascate && this.referenciadores.isEmpty() && this.referenciadoresClasses.isEmpty();
    }

    public void markCascate() {
        this.cascate = true;
    }

    /**
     * Monta o {@link Properties} do arquivo a partir dos fields encapsulados
     * e do grafo de referenciadores (sessão + disco).
     */
    public Properties toProperties() {
        return toProperties(this.fieldsCapsule, Instant.now().toString());
    }

    public Properties toProperties(String fields, String stamp) {
        this.fieldsCapsule = fields == null ? "" : fields;
        this.fileProperties = of(this.id, this.fieldsCapsule, encodeRefs(), this.cascate, stamp);
        return this.fileProperties;
    }

    /**
     * {@code Properties} no formato gravável pelo {@code Loader}.
     * {@code cascate} só é escrito quando {@code true}.
     */
    public static Properties of(String id, String fields, String refs, boolean cascate, String stamp) {
        Properties properties = new Properties();
        properties.setProperty("id", id == null ? "" : id);
        properties.setProperty("fields", fields == null ? "" : fields);
        properties.setProperty("refs", refs == null ? "" : refs);
        if (cascate) {
            properties.setProperty("cascate", "true");
        }
        properties.setProperty("stamp", stamp == null || stamp.isBlank()
                ? Instant.now().toString()
                : stamp);
        return properties;
    }

    /**
     * {@code indice:id::indice:id} — união dos referenciadores em memória com
     * os que só existem no disco.
     */
    public String encodeRefs() {
        Map<String, Class> all = new LinkedHashMap<>();
        for (Map.Entry<String, Class> entry : this.referenciadoresClasses.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
                all.put(entry.getKey(), entry.getValue());
            }
        }
        for (EnteMemory ref : this.referenciadores.values()) {
            if (ref == null || ref.getId() == null || ref.getId().isBlank()) continue;
            if (ref.getId().equals(this.id)) continue;
            Class tipo = ref.getClasse() != null ? ref.getClasse() : all.get(ref.getId());
            if (tipo != null) {
                all.put(ref.getId(), tipo);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Class> entry : all.entrySet()) {
            if (entry.getKey().equals(this.id)) continue;
            int index = ClassDictionary.getIndex(entry.getValue());
            if (index < 0) continue;
            if (sb.length() > 0) sb.append("::");
            sb.append(index).append(':').append(entry.getKey());
        }
        return sb.toString();
    }
}