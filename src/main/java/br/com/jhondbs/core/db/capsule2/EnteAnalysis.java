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

import java.awt.Image;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * O que ainda precisa ser persistido depois de {@code EnteManager.save}.
 *
 * <p>Entidade cujo properties está igual ao arquivo do banco não entra em
 * {@link #properties}. Arquivo cujo nome já está em {@code ./db/files/{id}}
 * não entra em {@link #files}. Imagem cujo MD5 já está em
 * {@code ./db/imgs/{id}} não entra em {@link #imgs}.
 *
 * <p>Nada aqui foi gravado. A etapa seguinte consome estas coleções.
 *
 * @author jhones
 */
public final class EnteAnalysis {

    private final List<Properties> properties;
    private final Map<String, Map<String, File>> files;
    private final Map<String, Map<String, Image>> imgs;
    private final List<String> unchangedIds;

    public EnteAnalysis(List<Properties> properties,
                        Map<String, Map<String, File>> files,
                        Map<String, Map<String, Image>> imgs,
                        List<String> unchangedIds) {
        this.properties = properties == null ? List.of() : List.copyOf(properties);
        this.files = copy(files);
        this.imgs = copy(imgs);
        this.unchangedIds = unchangedIds == null ? List.of() : List.copyOf(unchangedIds);
    }

    /**
     * Entidades novas ou com {@code id}/{@code fields}/{@code refs}/{@code cascate}
     * diferentes do arquivo já gravado. {@code stamp} não conta como mudança.
     */
    public List<Properties> getProperties() {
        return this.properties;
    }

    /**
     * {@code id da entidade → (nome → File)} só dos arquivos que ainda não
     * estão na pasta da entidade.
     */
    public Map<String, Map<String, File>> getFiles() {
        return this.files;
    }

    /**
     * {@code id da entidade → (md5 → Image)} só das imagens cujo MD5 ainda
     * não está na pasta da entidade.
     */
    public Map<String, Map<String, Image>> getImgs() {
        return this.imgs;
    }

    /**
     * Ids cujo properties foi considerado igual ao disco e por isso não segue
     * para a gravação da entidade.
     */
    public List<String> getUnchangedIds() {
        return this.unchangedIds;
    }

    public Map<String, File> filesOf(String entityId) {
        if (entityId == null) return Map.of();
        Map<String, File> found = this.files.get(entityId);
        return found == null ? Map.of() : found;
    }

    public Map<String, Image> imgsOf(String entityId) {
        if (entityId == null) return Map.of();
        Map<String, Image> found = this.imgs.get(entityId);
        return found == null ? Map.of() : found;
    }

    public boolean hasPending() {
        return !this.properties.isEmpty() || !this.files.isEmpty() || !this.imgs.isEmpty();
    }

    private static <T> Map<String, Map<String, T>> copy(Map<String, Map<String, T>> source) {
        if (source == null || source.isEmpty()) return Map.of();
        Map<String, Map<String, T>> outer = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, T>> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) continue;
            Map<String, T> inner = new LinkedHashMap<>();
            for (Map.Entry<String, T> item : entry.getValue().entrySet()) {
                if (item.getKey() == null || item.getValue() == null) continue;
                inner.put(item.getKey(), item.getValue());
            }
            if (!inner.isEmpty()) {
                outer.put(entry.getKey(), Map.copyOf(inner));
            }
        }
        return outer.isEmpty() ? Map.of() : Map.copyOf(outer);
    }
}
