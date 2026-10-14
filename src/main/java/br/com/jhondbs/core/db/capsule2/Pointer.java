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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Ponteiro de uma sub-entidade dentro de outra entidade (ou de um objeto
 * complexo interno). Usado para remover a referência sem substituir a instância
 * do objeto pai.
 *
 * <p>{@code container} é SEMPRE o dono do {@code field} — a entidade ou o
 * complexo {@code O} em {@code E1 → O → E2}. Nunca a coleção em si.
 *
 * <p>Coleções imutáveis ({@code List.of}, {@code Map.of},
 * {@code Collections.unmodifiable*}, {@code Arrays.asList} para {@code remove})
 * não são mutadas in-place: copia-se o holder e a cópia é instalada no pai.
 * Se o pai também for imutável, a substituição sobe até um pai mutável ou até
 * o campo — assim {@code List.of(Map.of("k", entidade))} vira uma lista mutável
 * com um mapa sem a entidade, gravada em {@code field}.
 *
 * @author jhones
 */
public class Pointer {

    /**
     * Papel da entidade no holder. Distingue chave de mapa de valor com chave
     * nula — os dois teriam {@code mapKey == null}.
     */
    public enum Kind {
        FIELD,
        COLLECTION,
        ARRAY,
        MAP_KEY,
        MAP_VALUE
    }

    private final String id;
    private final Object container;
    private final Field field;
    private final Kind kind;

    /**
     * Coleção ou array que contém a entidade de forma imediata. Pode ser o
     * próprio valor do campo ou uma coleção aninhada.
     */
    private final Object immediate;

    /**
     * Mapa que contém a entidade (chave ou valor). Pode ser o valor do campo
     * ou um mapa aninhado.
     */
    private final Map<?, ?> map;
    private final Object mapKey;

    private Pointer(String id, Object container, Field field, Kind kind,
                    Object immediate, Map<?, ?> map, Object mapKey) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O ID da entidade referenciada não pode ser nulo ou vazio.");
        }
        if (container == null) {
            throw new NullPointerException("Objeto container não pode ser nulo");
        }
        if (field == null) {
            throw new NullPointerException("O campo de referencia para a entidade não pode ser nulo");
        }
        if (kind == null) {
            throw new NullPointerException("O tipo do ponteiro não pode ser nulo");
        }
        if ((kind == Kind.MAP_KEY || kind == Kind.MAP_VALUE) && map == null) {
            throw new NullPointerException("O mapa para buscar a entidade referenciada não pode ser nulo");
        }

        field.setAccessible(true);

        this.id = id;
        this.container = container;
        this.field = field;
        this.kind = kind;
        this.immediate = immediate;
        this.map = map;
        this.mapKey = mapKey;
    }

    /**
     * Entidade como valor direto de um campo.
     */
    public Pointer(String id, Object container, Field field) {
        this(id, container, field, Kind.FIELD, null, null, null);
    }

    /**
     * Entidade como chave de um mapa.
     */
    public Pointer(String id, Object container, Field field, Map map) {
        this(id, container, field, Kind.MAP_KEY, map, requireMap(map), null);
    }

    /**
     * Entidade como valor de um mapa. {@code mapKey} pode ser nulo (chave nula
     * válida em {@link java.util.HashMap}).
     */
    public Pointer(String id, Object container, Field field, Map map, Object mapKey) {
        this(id, container, field, Kind.MAP_VALUE, map, requireMap(map), mapKey);
    }

    public static Pointer ofField(String id, Object container, Field field) {
        return new Pointer(id, container, field);
    }

    public static Pointer ofCollection(String id, Object container, Field field, Object collection) {
        if (collection != null && collection.getClass().isArray()) {
            return ofArray(id, container, field, collection);
        }
        return new Pointer(id, container, field, Kind.COLLECTION, collection, null, null);
    }

    public static Pointer ofArray(String id, Object container, Field field, Object array) {
        return new Pointer(id, container, field, Kind.ARRAY, array, null, null);
    }

    public static Pointer ofMapKey(String id, Object container, Field field, Map<?, ?> map) {
        return new Pointer(id, container, field, map);
    }

    /**
     * @param mapKey chave atual da entrada; pode ser {@code null}.
     */
    public static Pointer ofMapValue(String id, Object container, Field field, Map<?, ?> map, Object mapKey) {
        return new Pointer(id, container, field, map, mapKey);
    }

    public String getId() {
        return this.id;
    }

    public Object getContainer() {
        return this.container;
    }

    public Field getField() {
        return this.field;
    }

    public Kind getKind() {
        return this.kind;
    }

    /**
     * Remove a entidade apontada do holder, preservando a instância do
     * container. Se o holder for imutável, copia e instala a cópia no pai —
     * subindo pela cadeia de coleções até um pai mutável ou até o campo.
     */
    public void remove() throws Exception {
        switch (kind) {
            case MAP_VALUE -> removeMapValue();
            case MAP_KEY -> removeMapKey();
            case ARRAY -> removeFromArray();
            case COLLECTION -> removeFromCollection();
            case FIELD -> removeFromField();
        }
    }

    private void removeFromField() throws Exception {
        Class<?> tipo = field.getType();

        if (Entity.class.isAssignableFrom(tipo)) {
            Object current = field.get(container);
            if (isTarget(current)) {
                field.set(container, null);
            }
            return;
        }

        if (tipo.isArray()) {
            removeFromArray();
            return;
        }
        if (Map.class.isAssignableFrom(tipo)) {
            removeMatchingFromMap(resolveMap());
            return;
        }
        if (Iterable.class.isAssignableFrom(tipo)) {
            removeFromCollection();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void removeMapValue() throws Exception {
        Map mapInst = resolveMap();
        if (mapInst == null) return;

        if (containsKey(mapInst, mapKey) && isTarget(mapInst.get(mapKey))) {
            removeMapEntry(mapInst, mapKey);
            return;
        }

        Object foundKey = null;
        boolean found = false;
        for (Object raw : mapInst.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) raw;
            if (isTarget(entry.getValue())) {
                foundKey = entry.getKey();
                found = true;
                break;
            }
        }
        if (found) {
            removeMapEntry(mapInst, foundKey);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void removeMapKey() throws Exception {
        Map mapInst = resolveMap();
        if (mapInst == null) return;

        Object foundKey = null;
        for (Object key : mapInst.keySet()) {
            if (isTarget(key)) {
                foundKey = key;
                break;
            }
        }
        if (foundKey != null) {
            removeMapEntry(mapInst, foundKey);
        }
    }

    /**
     * Fallback para campo {@code Map} sem kind de chave/valor: remove a
     * entidade se ela for chave ou valor.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void removeMatchingFromMap(Map mapInst) throws Exception {
        if (mapInst == null) return;

        Object asKey = null;
        Object asValueKey = null;
        boolean valueFound = false;

        for (Object raw : mapInst.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) raw;
            if (asKey == null && isTarget(entry.getKey())) {
                asKey = entry.getKey();
            }
            if (!valueFound && isTarget(entry.getValue())) {
                asValueKey = entry.getKey();
                valueFound = true;
            }
            if (asKey != null && valueFound) break;
        }

        if (asKey != null) {
            removeMapEntry(mapInst, asKey);
        }
        if (valueFound && asValueKey != asKey) {
            Map current = resolveMap();
            if (current != null) {
                removeMapEntry(current, asValueKey);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void removeMapEntry(Map mapInst, Object key) throws Exception {
        try {
            mapInst.remove(key);
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            Map<Object, Object> copy = copyMap(mapInst);
            copy.remove(key);
            installReplacement(mapInst, copy);
        }
    }

    private void removeFromCollection() throws Exception {
        Object target = resolveCollection();
        if (target == null) return;

        if (target instanceof Collection<?> col) {
            Entity found = findInIterable(col);
            if (found == null) return;
            try {
                col.remove(found);
            } catch (UnsupportedOperationException | IllegalArgumentException e) {
                Collection<Object> copy = copyCollection(col);
                copy.remove(found);
                installReplacement(col, copy);
            }
            return;
        }

        if (target instanceof Iterable<?> iterable) {
            removeFromGenericIterable(iterable);
        }
    }

    private void removeFromGenericIterable(Iterable<?> iterable) throws Exception {
        Entity found = findInIterable(iterable);
        if (found == null) return;

        Iterator<?> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (!isTarget(next)) continue;
            try {
                iterator.remove();
                return;
            } catch (IllegalStateException | UnsupportedOperationException e) {
                List<Object> copy = new ArrayList<>();
                boolean skipped = false;
                for (Object item : iterable) {
                    if (!skipped && isTarget(item)) {
                        skipped = true;
                        continue;
                    }
                    copy.add(item);
                }
                installReplacement(iterable, copy);
                return;
            }
        }
    }

    private void removeFromArray() throws Exception {
        Object target = resolveArray();
        if (target == null || !isObjectArray(target)) return;

        int length = Array.getLength(target);
        for (int i = 0; i < length; i++) {
            if (isTarget(Array.get(target, i))) {
                Array.set(target, i, null);
                return;
            }
        }
    }

    /**
     * Instala {@code newHolder} no lugar de {@code oldHolder} a partir do valor
     * atual do campo. Mutação in-place quando o pai permite; senão copia cada
     * ancestral imutável até conseguir {@code field.set}.
     *
     * <p>{@code List.of(Map.of("a", e))} → copia o mapa sem {@code e} → o
     * {@code List.of} não aceita {@code set} → copia a lista → grava a lista
     * nova no campo. O container (entidade/complexo) permanece a mesma instância.
     */
    private void installReplacement(Object oldHolder, Object newHolder) throws Exception {
        if (oldHolder == null) return;
        if (oldHolder == newHolder) return;

        Object root = field.get(container);
        if (root == null) return;

        if (root == oldHolder) {
            field.set(container, newHolder);
            return;
        }

        Object updated = replaceIn(root, oldHolder, newHolder, new IdentityHashMap<>());
        if (updated != root) {
            field.set(container, updated);
        }
    }

    /**
     * @return a mesma instância se mutou in-place ou não achou {@code oldHolder};
     *         uma cópia se algum ancestral imutável precisou ser recriado
     */
    private Object replaceIn(Object current, Object oldHolder, Object newHolder,
                             IdentityHashMap<Object, Boolean> seen) throws Exception {
        if (current == null) return null;
        if (current == oldHolder) return newHolder;
        if (current instanceof Entity) return current;
        if (seen.containsKey(current)) return current;
        seen.put(current, Boolean.TRUE);

        if (current instanceof List<?> list) {
            return replaceInList(list, oldHolder, newHolder, seen);
        }
        if (current instanceof Set<?> set) {
            return replaceInSet(set, oldHolder, newHolder, seen);
        }
        if (current instanceof Map<?, ?> asMap) {
            return replaceInMap(asMap, oldHolder, newHolder, seen);
        }
        if (current instanceof Collection<?> col) {
            return replaceInCollection(col, oldHolder, newHolder, seen);
        }
        if (isObjectArray(current)) {
            return replaceInArray(current, oldHolder, newHolder, seen);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private Object replaceInList(List<?> list, Object oldHolder, Object newHolder,
                                 IdentityHashMap<Object, Boolean> seen) throws Exception {
        List<Object> raw = (List<Object>) list;
        int size = raw.size();
        int index = -1;
        Object replacement = null;

        for (int i = 0; i < size; i++) {
            Object item = raw.get(i);
            if (item == oldHolder) {
                index = i;
                replacement = newHolder;
                break;
            }
            Object next = replaceIn(item, oldHolder, newHolder, seen);
            if (next != item) {
                index = i;
                replacement = next;
                break;
            }
        }
        if (index < 0) return list;

        try {
            raw.set(index, replacement);
            return list;
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            List<Object> copy = new ArrayList<>(raw);
            copy.set(index, replacement);
            return copy;
        }
    }

    private Object replaceInSet(Set<?> set, Object oldHolder, Object newHolder,
                                IdentityHashMap<Object, Boolean> seen) throws Exception {
        return replaceInCollection(set, oldHolder, newHolder, seen);
    }

    @SuppressWarnings("unchecked")
    private Object replaceInCollection(Collection<?> col, Object oldHolder, Object newHolder,
                                       IdentityHashMap<Object, Boolean> seen) throws Exception {
        Object direct = null;
        Object nestedOld = null;
        Object nestedNew = null;

        for (Object item : col) {
            if (item == oldHolder) {
                direct = item;
                break;
            }
            Object next = replaceIn(item, oldHolder, newHolder, seen);
            if (next != item) {
                nestedOld = item;
                nestedNew = next;
                break;
            }
        }

        if (direct != null) {
            return swapInCollection(col, direct, newHolder);
        }
        if (nestedOld != null) {
            return swapInCollection(col, nestedOld, nestedNew);
        }
        return col;
    }

    @SuppressWarnings("unchecked")
    private Object swapInCollection(Collection<?> col, Object oldItem, Object newItem) throws Exception {
        Collection<Object> raw = (Collection<Object>) col;
        try {
            boolean removed = removeByIdentity(raw, oldItem);
            if (!removed) {
                raw.remove(oldItem);
            }
            if (newItem != null) {
                raw.add(newItem);
            }
            return col;
        } catch (UnsupportedOperationException | IllegalArgumentException | ClassCastException e) {
            Collection<Object> copy = copyCollection(col);
            if (!removeByIdentity(copy, oldItem)) {
                copy.remove(oldItem);
            }
            if (newItem != null) {
                copy.add(newItem);
            }
            return copy;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object replaceInMap(Map map, Object oldHolder, Object newHolder,
                                IdentityHashMap<Object, Boolean> seen) throws Exception {
        boolean foundValue = false;
        Object valueKey = null;
        Object valueReplacement = null;

        for (Object raw : map.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) raw;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value == oldHolder) {
                foundValue = true;
                valueKey = key;
                valueReplacement = newHolder;
                break;
            }
            Object next = replaceIn(value, oldHolder, newHolder, seen);
            if (next != value) {
                foundValue = true;
                valueKey = key;
                valueReplacement = next;
                break;
            }
        }

        if (foundValue) {
            return putOrCopy(map, valueKey, valueReplacement);
        }

        Object oldKey = null;
        Object remappedKey = null;
        Object keptValue = null;
        boolean foundKey = false;

        for (Object raw : map.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) raw;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == oldHolder) {
                foundKey = true;
                oldKey = key;
                remappedKey = newHolder;
                keptValue = value;
                break;
            }
            Object next = replaceIn(key, oldHolder, newHolder, seen);
            if (next != key) {
                foundKey = true;
                oldKey = key;
                remappedKey = next;
                keptValue = value;
                break;
            }
        }

        if (foundKey) {
            return remapKeyOrCopy(map, oldKey, remappedKey, keptValue);
        }
        return map;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object putOrCopy(Map map, Object key, Object value) throws Exception {
        try {
            map.put(key, value);
            return map;
        } catch (UnsupportedOperationException | IllegalArgumentException | ClassCastException e) {
            Map<Object, Object> copy = copyMap(map);
            copy.put(key, value);
            return copy;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object remapKeyOrCopy(Map map, Object oldKey, Object newKey, Object value) throws Exception {
        try {
            map.remove(oldKey);
            map.put(newKey, value);
            return map;
        } catch (UnsupportedOperationException | IllegalArgumentException | ClassCastException e) {
            Map<Object, Object> copy = copyMap(map);
            copy.remove(oldKey);
            copy.put(newKey, value);
            return copy;
        }
    }

    private Object replaceInArray(Object array, Object oldHolder, Object newHolder,
                                  IdentityHashMap<Object, Boolean> seen) throws Exception {
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object item = Array.get(array, i);
            if (item == oldHolder) {
                Array.set(array, i, newHolder);
                return array;
            }
            Object next = replaceIn(item, oldHolder, newHolder, seen);
            if (next != item) {
                Array.set(array, i, next);
                return array;
            }
        }
        return array;
    }

    @SuppressWarnings("rawtypes")
    private Map resolveMap() throws Exception {
        Object root = field.get(container);
        if (root == null) {
            return map;
        }

        if (map != null && isReachable(root, map) && mapDirectlyContainsTarget(map)) {
            return map;
        }
        if (root instanceof Map<?, ?> asField && mapDirectlyContainsTarget(asField)) {
            return asField;
        }

        Map<?, ?> live = findMapContainingTarget(root, new IdentityHashMap<>());
        if (live != null) return live;

        if (map != null && isReachable(root, map)) return map;
        if (root instanceof Map<?, ?> asField) return asField;
        if (immediate instanceof Map<?, ?> asImmediate && isReachable(root, asImmediate)) {
            return asImmediate;
        }
        return map;
    }

    private Object resolveCollection() throws Exception {
        Object root = field.get(container);

        if (immediate instanceof Iterable<?> && root != null
                && isReachable(root, immediate) && directlyContainsTarget((Iterable<?>) immediate)) {
            return immediate;
        }
        if (root instanceof Iterable<?> asField && directlyContainsTarget(asField)) {
            return asField;
        }

        if (root != null) {
            Object live = findCollectionContainingTarget(root, new IdentityHashMap<>());
            if (live != null) return live;
        }

        if (immediate instanceof Iterable<?> && root != null && isReachable(root, immediate)) {
            return immediate;
        }
        if (root instanceof Iterable<?>) return root;
        return immediate;
    }

    private Object resolveArray() throws Exception {
        Object root = field.get(container);

        if (immediate != null && isObjectArray(immediate) && root != null
                && isReachable(root, immediate) && arrayDirectlyContainsTarget(immediate)) {
            return immediate;
        }
        if (root != null && isObjectArray(root) && arrayDirectlyContainsTarget(root)) {
            return root;
        }

        if (root != null) {
            Object live = findArrayContainingTarget(root, new IdentityHashMap<>());
            if (live != null) return live;
        }

        if (immediate != null && isObjectArray(immediate) && root != null && isReachable(root, immediate)) {
            return immediate;
        }
        if (root != null && isObjectArray(root)) return root;
        return immediate;
    }

    private Map<?, ?> findMapContainingTarget(Object current, IdentityHashMap<Object, Boolean> seen) throws Exception {
        if (current == null || current instanceof Entity) return null;
        if (seen.containsKey(current)) return null;
        seen.put(current, Boolean.TRUE);

        if (current instanceof Map<?, ?> asMap) {
            if (mapDirectlyContainsTarget(asMap)) return asMap;
            for (Map.Entry<?, ?> entry : asMap.entrySet()) {
                Map<?, ?> found = findMapContainingTarget(entry.getValue(), seen);
                if (found != null) return found;
                found = findMapContainingTarget(entry.getKey(), seen);
                if (found != null) return found;
            }
            return null;
        }
        if (current instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                Map<?, ?> found = findMapContainingTarget(item, seen);
                if (found != null) return found;
            }
            return null;
        }
        if (isObjectArray(current)) {
            int length = Array.getLength(current);
            for (int i = 0; i < length; i++) {
                Map<?, ?> found = findMapContainingTarget(Array.get(current, i), seen);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Object findCollectionContainingTarget(Object current, IdentityHashMap<Object, Boolean> seen) throws Exception {
        if (current == null || current instanceof Entity) return null;
        if (seen.containsKey(current)) return null;
        seen.put(current, Boolean.TRUE);

        if (current instanceof Iterable<?> iterable && !(current instanceof Map)) {
            if (directlyContainsTarget(iterable)) return current;
            for (Object item : iterable) {
                Object found = findCollectionContainingTarget(item, seen);
                if (found != null) return found;
            }
            return null;
        }
        if (current instanceof Map<?, ?> asMap) {
            for (Map.Entry<?, ?> entry : asMap.entrySet()) {
                Object found = findCollectionContainingTarget(entry.getValue(), seen);
                if (found != null) return found;
                found = findCollectionContainingTarget(entry.getKey(), seen);
                if (found != null) return found;
            }
            return null;
        }
        if (isObjectArray(current)) {
            int length = Array.getLength(current);
            for (int i = 0; i < length; i++) {
                Object found = findCollectionContainingTarget(Array.get(current, i), seen);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Object findArrayContainingTarget(Object current, IdentityHashMap<Object, Boolean> seen) throws Exception {
        if (current == null || current instanceof Entity) return null;
        if (seen.containsKey(current)) return null;
        seen.put(current, Boolean.TRUE);

        if (isObjectArray(current)) {
            if (arrayDirectlyContainsTarget(current)) return current;
            int length = Array.getLength(current);
            for (int i = 0; i < length; i++) {
                Object found = findArrayContainingTarget(Array.get(current, i), seen);
                if (found != null) return found;
            }
            return null;
        }
        if (current instanceof Map<?, ?> asMap) {
            for (Map.Entry<?, ?> entry : asMap.entrySet()) {
                Object found = findArrayContainingTarget(entry.getValue(), seen);
                if (found != null) return found;
                found = findArrayContainingTarget(entry.getKey(), seen);
                if (found != null) return found;
            }
            return null;
        }
        if (current instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                Object found = findArrayContainingTarget(item, seen);
                if (found != null) return found;
            }
        }
        return null;
    }

    private boolean isReachable(Object root, Object target) {
        if (root == null || target == null) return false;
        return isReachable(root, target, new IdentityHashMap<>());
    }

    private boolean isReachable(Object current, Object target, IdentityHashMap<Object, Boolean> seen) {
        if (current == null) return false;
        if (current == target) return true;
        if (current instanceof Entity) return false;
        if (seen.containsKey(current)) return false;
        seen.put(current, Boolean.TRUE);

        if (current instanceof Map<?, ?> asMap) {
            for (Map.Entry<?, ?> entry : asMap.entrySet()) {
                if (isReachable(entry.getKey(), target, seen) || isReachable(entry.getValue(), target, seen)) {
                    return true;
                }
            }
            return false;
        }
        if (current instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (isReachable(item, target, seen)) return true;
            }
            return false;
        }
        if (isObjectArray(current)) {
            int length = Array.getLength(current);
            for (int i = 0; i < length; i++) {
                if (isReachable(Array.get(current, i), target, seen)) return true;
            }
        }
        return false;
    }

    private boolean mapDirectlyContainsTarget(Map<?, ?> asMap) throws Exception {
        if (asMap == null) return false;
        if (kind == Kind.MAP_KEY) {
            for (Object key : asMap.keySet()) {
                if (isTarget(key)) return true;
            }
            return false;
        }
        if (kind == Kind.MAP_VALUE) {
            if (containsKey(asMap, mapKey) && isTarget(asMap.get(mapKey))) return true;
            for (Object value : asMap.values()) {
                if (isTarget(value)) return true;
            }
            return false;
        }
        for (Map.Entry<?, ?> entry : asMap.entrySet()) {
            if (isTarget(entry.getKey()) || isTarget(entry.getValue())) return true;
        }
        return false;
    }

    private boolean directlyContainsTarget(Iterable<?> iterable) throws Exception {
        return findInIterable(iterable) != null;
    }

    private boolean arrayDirectlyContainsTarget(Object array) throws Exception {
        if (!isObjectArray(array)) return false;
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (isTarget(Array.get(array, i))) return true;
        }
        return false;
    }

    private boolean removeByIdentity(Collection<Object> col, Object target) {
        Iterator<Object> iterator = col.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == target) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private Collection<Object> copyCollection(Collection<?> col) {
        if (col instanceof SortedSet<?> sorted) {
            try {
                Comparator<? super Object> comparator = (Comparator<? super Object>) sorted.comparator();
                TreeSet<Object> copy = new TreeSet<>(comparator);
                copy.addAll(sorted);
                return copy;
            } catch (ClassCastException | NullPointerException e) {
                return new LinkedHashSet<>(col);
            }
        }
        if (col instanceof Set<?>) {
            return new LinkedHashSet<>(col);
        }
        return new ArrayList<>(col);
    }

    private Map<Object, Object> copyMap(Map<?, ?> asMap) {
        if (asMap instanceof SortedMap<?, ?> sorted) {
            try {
                Comparator<? super Object> comparator = (Comparator<? super Object>) sorted.comparator();
                TreeMap<Object, Object> copy = new TreeMap<>(comparator);
                copy.putAll(sorted);
                return copy;
            } catch (ClassCastException | NullPointerException e) {
                return new LinkedHashMap<>(asMap);
            }
        }
        return new LinkedHashMap<>(asMap);
    }

    private boolean isObjectArray(Object value) {
        return value != null && value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive();
    }

    private boolean containsKey(Map<?, ?> mapInst, Object key) {
        try {
            return mapInst.containsKey(key);
        } catch (NullPointerException | ClassCastException e) {
            return false;
        }
    }

    private boolean isTarget(Object value) throws Exception {
        return value instanceof Entity entity && id.equals(entity.getId());
    }

    /**
     * Percorre um Iterable e devolve a entidade com o ID deste ponteiro.
     */
    private Entity findInIterable(Iterable<?> iterable) throws Exception {
        if (iterable == null) return null;
        for (Object next : iterable) {
            if (isTarget(next)) {
                return (Entity) next;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static Map requireMap(Map map) {
        if (map == null) {
            throw new NullPointerException("O mapa para buscar a entidade referenciada não pode ser nulo");
        }
        return map;
    }
}