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
package br.com.jhondbs.core.tools;

import br.com.jhondbs.core.db.capsule2.EnteMemory;
import br.com.jhondbs.core.db.capsule2.EnteSession;
import br.com.jhondbs.core.db.capsule2.Pointer;
import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;

/**
 * Percorre a entidade, monta o grafo e encapsula os campos no mesmo passe.
 * Não grava em disco e não decide o que está redundante: isso fica com
 * {@code EnteManager}.
 *
 * <p>Arquivo e imagem, mesmo dentro de objeto complexo, ficam agrupados pelo
 * id da entidade dona:
 * {@code files[id][nome]} e {@code imgs[id][md5]}.
 *
 * @author jhones
 */
public final class EnteAnalyzer {

    private String rootId;
    private EnteSession session;

    private final Map<Object, Boolean> visitados = new IdentityHashMap<>();
    private final IdentityHashMap<Object, String> encapsulatedCache = new IdentityHashMap<>();
    private final Map<String, String> fieldBodies = new LinkedHashMap<>();
    private final List<Properties> propertiesList = new ArrayList<>();
    private final Map<String, Map<String, File>> files = new LinkedHashMap<>();
    private final Map<String, Map<String, Image>> imgs = new LinkedHashMap<>();

    public EnteAnalyzer() {
    }

    /**
     * Um {@link Properties} por entidade visitada, raiz primeiro.
     * Ainda sem filtro de arquivo prévio.
     */
    public List<Properties> getProperties() {
        return Collections.unmodifiableList(this.propertiesList);
    }

    public String getFieldsCapsule() {
        if (this.rootId == null) return "";
        return this.fieldBodies.getOrDefault(this.rootId, "");
    }

    /**
     * {@code id da entidade → (nome do arquivo → File)}.
     */
    public Map<String, Map<String, File>> getFiles() {
        return this.files;
    }

    /**
     * {@code id da entidade → (md5 → Image)}.
     */
    public Map<String, Map<String, Image>> getImgs() {
        return this.imgs;
    }

    public EnteSession getSession() {
        return this.session;
    }

    public EnteMemory getMemory(String id) {
        if (this.session == null || id == null) return null;
        return this.session.getById(id);
    }

    /**
     * Monta o grafo a partir da raiz e encapsula cada entidade visitada.
     *
     * @return todos os {@link Properties} do passe, sem descartar os que já
     *         estão iguais no disco
     */
    public List<Properties> analyze(Entity entity) throws Exception {
        resetWalk();
        if (entity == null) {
            return this.propertiesList;
        }
        this.rootId = entity.getId();
        analyze((Object) entity, null, null, null);
        sealCapsules();
        return this.propertiesList;
    }

    public String analyze(Object object, EnteMemory parent, Field parentField) throws Exception {
        Object owner = parent != null ? parent.getInstance() : null;
        return analyze(object, parent, owner, parentField);
    }

    /**
     * Caminha um valor, cria ponteiros e devolve a cápsula {@code {chave:valor}}.
     */
    public String analyze(Object object, EnteMemory parent, Object fieldOwner, Field parentField) throws Exception {
        if (object == null) return "{}";

        Class<?> clazz = object.getClass();

        if (clazz.isEnum()) {
            return encapsuleEnum((Enum<?>) object);
        }
        if (Reflection.isPrimitive(clazz) || Reflection.isNumerical(clazz) || Reflection.isDate(clazz)) {
            return encapsulePrimitive(object);
        }
        if (object instanceof File file) {
            return encapsuleFile(file, parent);
        }
        if (object instanceof Image image) {
            return encapsuleImage(image, parent);
        }

        if (visitados.containsKey(object)) {
            if (object instanceof Entity entity) {
                return encapsuleId(entity);
            }
            String cached = encapsulatedCache.get(object);
            return cached != null ? cached : "{}";
        }

        if (Entity.class.isAssignableFrom(clazz)) {
            Entity e = (Entity) object;
            EnteMemory memory = entity(e);
            if (parent == null) parent = memory;

            visitados.put(object, true);
            String fieldsCapsule = complex(object, memory);
            memory.setFieldsCapsule(fieldsCapsule);
            this.fieldBodies.put(e.getId(), fieldsCapsule);
            return encapsuleId(e);
        }

        visitados.put(object, true);

        String encapsulated;
        if (Reflection.isArrayMap(clazz)) {
            encapsulated = analyzeCollection(object, clazz, parent, fieldOwner, parentField);
        } else {
            encapsulated = complex(object, parent);
        }
        encapsulatedCache.put(object, encapsulated);
        return encapsulated;
    }

    private void sealCapsules() {
        this.propertiesList.clear();
        String stamp = Instant.now().toString();

        for (Map.Entry<String, String> entry : this.fieldBodies.entrySet()) {
            String id = entry.getKey();
            String fields = entry.getValue();
            EnteMemory memory = this.session != null ? this.session.getById(id) : null;
            Properties properties = memory != null
                    ? memory.toProperties(fields, stamp)
                    : EnteMemory.of(id, fields, "", false, stamp);
            this.propertiesList.add(properties);
        }
    }

    private void resetWalk() {
        this.rootId = null;
        this.session = null;
        this.visitados.clear();
        this.encapsulatedCache.clear();
        this.fieldBodies.clear();
        this.propertiesList.clear();
        this.files.clear();
        this.imgs.clear();
    }

    private void connect(EnteMemory parent, EnteMemory son, Pointer pointer, Field field) {
        if (parent == null || son == null || pointer == null) return;

        parent.addReferenciado(son);
        son.addReferenciador(parent);
        parent.addPonteiro(pointer);

        if (field != null && field.isAnnotationPresent(Cascate.class)) {
            son.markCascate();
        }
    }

    private void connectField(EnteMemory parent, Entity child, Object owner, Field field) throws Exception {
        if (!canPoint(parent, owner, field) || child == null) return;
        EnteMemory memory = entity(child);
        connect(parent, memory, Pointer.ofField(child.getId(), owner, field), field);
    }

    private void connectCollection(EnteMemory parent, Entity child, Object owner, Field field, Object collection) throws Exception {
        if (!canPoint(parent, owner, field) || child == null) return;
        EnteMemory memory = entity(child);
        connect(parent, memory, Pointer.ofCollection(child.getId(), owner, field, collection), field);
    }

    private void connectArray(EnteMemory parent, Entity child, Object owner, Field field, Object array) throws Exception {
        if (!canPoint(parent, owner, field) || child == null) return;
        EnteMemory memory = entity(child);
        connect(parent, memory, Pointer.ofArray(child.getId(), owner, field, array), field);
    }

    private void connectMapKey(EnteMemory parent, Entity child, Object owner, Field field, Map<?, ?> map) throws Exception {
        if (!canPoint(parent, owner, field) || child == null || map == null) return;
        EnteMemory memory = entity(child);
        connect(parent, memory, Pointer.ofMapKey(child.getId(), owner, field, map), field);
    }

    private void connectMapValue(EnteMemory parent, Entity child, Object owner, Field field, Map<?, ?> map, Object key) throws Exception {
        if (!canPoint(parent, owner, field) || child == null || map == null) return;
        EnteMemory memory = entity(child);
        connect(parent, memory, Pointer.ofMapValue(child.getId(), owner, field, map, key), field);
    }

    private boolean canPoint(EnteMemory parent, Object owner, Field field) {
        return parent != null && owner != null && field != null;
    }

    private String analyzeCollection(Object object, Class<?> clazz, EnteMemory parent, Object fieldOwner, Field parentField) throws Exception {
        if (object instanceof Map<?, ?> map) {
            return analyzeMap(map, parent, fieldOwner, parentField);
        }
        if (object instanceof Iterable<?> iterable) {
            return analyzeIterable(iterable, parent, fieldOwner, parentField);
        }
        if (clazz.isArray()) {
            return analyzeArray(object, parent, fieldOwner, parentField);
        }
        return "{}";
    }

    private String analyzeIterable(Iterable<?> iterable, EnteMemory parent, Object fieldOwner, Field parentField) throws Exception {
        StringBuilder items = new StringBuilder();
        boolean empty = true;
        for (Object item : iterable) {
            empty = false;
            if (item instanceof Entity child) {
                connectCollection(parent, child, fieldOwner, parentField, iterable);
            }
            items.append(analyze(item, parent, fieldOwner, parentField));
        }
        if (empty) return "{list:{}}";
        return "{list:" + items + "}";
    }

    private String analyzeMap(Map<?, ?> map, EnteMemory parent, Object fieldOwner, Field parentField) throws Exception {
        if (map.isEmpty()) return "{map:{}}";

        StringBuilder items = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (key instanceof Entity childKey) {
                connectMapKey(parent, childKey, fieldOwner, parentField, map);
            }
            if (value instanceof Entity childValue) {
                connectMapValue(parent, childValue, fieldOwner, parentField, map, key);
            }

            items.append(analyze(key, parent, fieldOwner, parentField));
            items.append(analyze(value, parent, fieldOwner, parentField));
        }
        return "{map:" + items + "}";
    }

    private String analyzeArray(Object array, EnteMemory parent, Object fieldOwner, Field parentField) throws Exception {
        int length = Array.getLength(array);
        if (length == 0) return "{list:{}}";

        StringBuilder items = new StringBuilder();
        for (int i = 0; i < length; i++) {
            Object item = Array.get(array, i);
            if (item instanceof Entity child) {
                connectArray(parent, child, fieldOwner, parentField, array);
            }
            items.append(analyze(item, parent, fieldOwner, parentField));
        }
        return "{list:" + items + "}";
    }

    private String complex(Object object, EnteMemory parent) throws Exception {
        if (object == null) return "{}";

        StringBuilder fieldsCapsule = new StringBuilder();
        List<Field> fieldsOfObject = FieldsManager.getAllSerializableFields(object.getClass());
        for (Field field : fieldsOfObject) {
            Object value = field.get(object);
            if (value == null) continue;

            if (value instanceof Entity child) {
                connectField(parent, child, object, field);
            }

            String encapsulated = analyze(value, parent, object, field);
            fieldsCapsule.append("{")
                    .append(field.getName())
                    .append(":")
                    .append(encapsulated)
                    .append("}");
        }

        if (object instanceof Entity) {
            return fieldsCapsule.toString();
        }
        return "{" + ClassDictionary.getIndex(object.getClass()) + ":" + fieldsCapsule + "}";
    }

    private EnteMemory entity(Entity entity) throws Exception {
        if (entity == null) {
            throw new NullPointerException("A entidade não pode ser nula.");
        }
        String id = entity.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O ID da entidade não pode ser nulo ou vazio.");
        }

        EnteSession found = SessionsCache.findSession(id);

        if (found != null) {
            if (this.session == null) {
                this.session = found;
                found.touch();
            } else if (this.session != found) {
                found.receive(this.session);
                this.session.kill();
                this.session = found;
            }
        } else if (this.session == null) {
            this.session = new EnteSession();
            this.session.addEntity(new EnteMemory(entity));
        } else if (this.session.getById(id) == null) {
            this.session.addEntity(new EnteMemory(entity));
        }

        this.session = SessionsCache.add(this.session);

        EnteMemory memory = this.session.getById(id);
        if (memory == null) {
            memory = new EnteMemory(entity);
            this.session.addEntity(memory);
            this.session = SessionsCache.add(this.session);
        }
        return memory;
    }

    private String encapsuleEnum(Enum<?> value) {
        return "{" + ClassDictionary.getIndex(value.getClass()) + ":" + value.toString() + "}";
    }

    private String encapsulePrimitive(Object object) {
        if (object == null) return "{}";
        return "{" + ClassDictionary.getIndex(object.getClass()) + ":" + object.toString() + "}";
    }

    private String encapsuleId(Entity entity) throws Exception {
        if (entity == null) return "{}";
        return "{" + ClassDictionary.getIndex(entity.getClass()) + ":" + entity.getId() + "}";
    }

    private String encapsuleFile(File file, EnteMemory parent) throws FileNotFoundException {
        if (file == null) return "{}";
        if (!file.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + file);
        }
        String owner = ownerId(parent);
        if (owner != null) {
            this.files.computeIfAbsent(owner, key -> new LinkedHashMap<>())
                    .putIfAbsent(file.getName(), file);
        }
        return "{file:" + file.getName() + "}";
    }

    private String encapsuleImage(Image image, EnteMemory parent) throws Exception {
        if (image == null) return "{}";
        String hash = md5Hex(imageBytes(image));
        String owner = ownerId(parent);
        if (owner != null) {
            this.imgs.computeIfAbsent(owner, key -> new LinkedHashMap<>())
                    .putIfAbsent(hash, image);
        }
        return "{img:" + hash + "}";
    }

    /**
     * Entidade mais próxima. Objeto complexo interno não é dono do arquivo.
     */
    private String ownerId(EnteMemory parent) {
        if (parent != null && parent.getId() != null && !parent.getId().isBlank()) {
            return parent.getId();
        }
        if (this.rootId != null && !this.rootId.isBlank()) {
            return this.rootId;
        }
        return null;
    }

    private byte[] imageBytes(Image image) throws Exception {
        BufferedImage buffered;
        if (image instanceof BufferedImage bufferedImage) {
            buffered = bufferedImage;
        } else {
            int width = Math.max(image.getWidth(null), 1);
            int height = Math.max(image.getHeight(null), 1);
            buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = buffered.createGraphics();
            try {
                graphics.drawImage(image, 0, 0, null);
            } finally {
                graphics.dispose();
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(buffered, "png", output);
        return output.toByteArray();
    }

    private static String md5Hex(byte[] data) throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5").digest(data);
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}