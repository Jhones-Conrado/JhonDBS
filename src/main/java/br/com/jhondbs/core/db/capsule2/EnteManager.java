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
import br.com.jhondbs.core.tools.DirectoryManager;
import br.com.jhondbs.core.tools.EnteAnalyzer;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Prepara a gravação. Ainda não escreve entidade, arquivo nem imagem.
 *
 * <p>{@link #save(Entity)} monta o grafo, encapsula os campos e descarta o que
 * já está igual no banco:
 * <ul>
 *   <li>properties — compara {@code id}, {@code fields}, {@code refs} e
 *       {@code cascate} com o arquivo existente. {@code stamp} não conta.</li>
 *   <li>arquivos — nome já presente em {@code ./db/files/{id}} é pulado.</li>
 *   <li>imagens — MD5 já presente em {@code ./db/imgs/{id}} é pulado.</li>
 * </ul>
 *
 * @author jhones
 */
public class EnteManager {

    private EnteManager() {
    }

    /**
     * @return só o que a próxima etapa precisa gravar
     */
    public static EnteAnalysis save(Entity entity) throws Exception {
        if (entity == null) {
            throw new NullPointerException("A entidade não pode ser nula.");
        }

        EnteAnalyzer analyzer = new EnteAnalyzer();
        analyzer.analyze(entity);

        List<Properties> pendingProperties = new ArrayList<>();
        List<String> unchangedIds = new ArrayList<>();

        for (Properties generated : analyzer.getProperties()) {
            String id = generated.getProperty("id");
            EnteMemory memory = analyzer.getMemory(id);
            Properties stored = loadStored(memory, id);
            if (sameDocument(stored, generated)) {
                if (id != null && !id.isBlank()) unchangedIds.add(id);
                continue;
            }
            pendingProperties.add(copy(generated));
        }

        Map<String, Map<String, File>> pendingFiles = pendingFiles(analyzer);
        Map<String, Map<String, Image>> pendingImages = pendingImages(analyzer);

        return new EnteAnalysis(pendingProperties, pendingFiles, pendingImages, unchangedIds);
    }

    private static Map<String, Map<String, File>> pendingFiles(EnteAnalyzer analyzer) throws Exception {
        Map<String, Map<String, File>> pending = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, File>> byEntity : analyzer.getFiles().entrySet()) {
            String id = byEntity.getKey();
            EnteMemory memory = analyzer.getMemory(id);
            Set<String> existing = storedFileNames(memory, id);
            Map<String, File> keep = new LinkedHashMap<>();
            for (Map.Entry<String, File> item : byEntity.getValue().entrySet()) {
                String name = item.getKey();
                if (name == null || item.getValue() == null) continue;
                if (existing.contains(name)) continue;
                keep.put(name, item.getValue());
            }
            if (!keep.isEmpty()) pending.put(id, keep);
        }
        return pending;
    }

    private static Map<String, Map<String, Image>> pendingImages(EnteAnalyzer analyzer) throws Exception {
        Map<String, Map<String, Image>> pending = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Image>> byEntity : analyzer.getImgs().entrySet()) {
            String id = byEntity.getKey();
            EnteMemory memory = analyzer.getMemory(id);
            Set<String> existing = storedImageHashes(memory, id);
            Map<String, Image> keep = new LinkedHashMap<>();
            for (Map.Entry<String, Image> item : byEntity.getValue().entrySet()) {
                String hash = item.getKey();
                if (hash == null || item.getValue() == null) continue;
                if (existing.contains(hash.toLowerCase(Locale.ROOT))) continue;
                keep.put(hash, item.getValue());
            }
            if (!keep.isEmpty()) pending.put(id, keep);
        }
        return pending;
    }

    /**
     * {@code id}, {@code fields}, {@code refs} (sem ordem) e {@code cascate}.
     * {@code stamp} fica de fora: ele muda em todo passe mesmo sem alteração.
     */
    private static boolean sameDocument(Properties stored, Properties generated) {
        if (stored == null) return false;
        if (!text(stored, "id").equals(text(generated, "id"))) return false;
        if (!text(stored, "fields").equals(text(generated, "fields"))) return false;
        if (!refSet(stored.getProperty("refs")).equals(refSet(generated.getProperty("refs")))) return false;
        return cascate(stored) == cascate(generated);
    }

    private static boolean cascate(Properties properties) {
        return properties != null && "true".equalsIgnoreCase(properties.getProperty("cascate"));
    }

    private static String text(Properties properties, String key) {
        if (properties == null) return "";
        String value = properties.getProperty(key);
        return value == null ? "" : value;
    }

    private static Set<String> refSet(String refs) {
        Set<String> tokens = new LinkedHashSet<>();
        if (refs == null || refs.isBlank()) return tokens;
        for (String part : refs.split("::")) {
            if (part != null && !part.isBlank()) tokens.add(part.trim());
        }
        return tokens;
    }

    private static Properties loadStored(EnteMemory memory, String id) throws Exception {
        for (File candidate : entityFiles(memory, id)) {
            if (!candidate.isFile()) continue;
            Properties properties = new Properties();
            try (FileInputStream input = new FileInputStream(candidate)) {
                properties.load(input);
            }
            return properties;
        }
        return null;
    }

    private static List<File> entityFiles(EnteMemory memory, String id) throws Exception {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        Entity instance = memory != null ? memory.getInstance() : null;
        if (instance != null) {
            addPath(paths, DirectoryManager.entityToPath(instance));
        }
        if (safeId(id) && memory != null && memory.getClasse() != null) {
            addPath(paths, "./db/" + memory.getClasse().getName().replace('.', '/') + "/" + id);
        }
        return toFiles(paths);
    }

    /**
     * Nomes já gravados. Consulta {@code ./db/files/{id}} e também o caminho
     * atual do {@link DirectoryManager}, para não recolocar arquivo que já
     * está em qualquer um dos dois layouts.
     */
    private static Set<String> storedFileNames(EnteMemory memory, String id) throws Exception {
        Set<String> names = new HashSet<>();
        for (File dir : directories(memory, id, true)) {
            File[] children = dir.listFiles();
            if (children == null) continue;
            for (File child : children) {
                if (child.isFile()) names.add(child.getName());
            }
        }
        return names;
    }

    /**
     * MD5 já gravados: nome do arquivo (com ou sem extensão) e hash do conteúdo.
     */
    private static Set<String> storedImageHashes(EnteMemory memory, String id) throws Exception {
        Set<String> hashes = new HashSet<>();
        for (File dir : directories(memory, id, false)) {
            File[] children = dir.listFiles();
            if (children == null) continue;
            for (File child : children) {
                if (!child.isFile()) continue;
                String name = child.getName().toLowerCase(Locale.ROOT);
                hashes.add(name);
                int dot = name.lastIndexOf('.');
                if (dot > 0) hashes.add(name.substring(0, dot));
                try (InputStream input = new FileInputStream(child)) {
                    hashes.add(md5(input));
                } catch (IOException ignored) {
                    // arquivo ilegível não conta como já persistido
                }
            }
        }
        return hashes;
    }

    private static List<File> directories(EnteMemory memory, String id, boolean files) throws Exception {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        Entity instance = memory != null ? memory.getInstance() : null;
        if (instance != null) {
            if (files) {
                addPath(paths, DirectoryManager.entityFileFolder(instance));
                addPath(paths, DirectoryManager.entityFileFolderOld(instance));
            } else {
                addPath(paths, DirectoryManager.entityImagesFolder(instance));
                addPath(paths, DirectoryManager.entityImagesFolderOld(instance));
            }
        }
        if (safeId(id)) {
            addPath(paths, (files ? "./db/files/" : "./db/imgs/") + id);
            if (memory != null && memory.getClasse() != null) {
                String pkg = memory.getClasse().getName().replace('.', '/');
                addPath(paths, (files ? "./db/files/" : "./db/imgs/") + pkg + "/" + id);
            }
        }
        return toFiles(paths);
    }

    private static void addPath(Set<String> paths, String path) {
        if (path != null && !path.isBlank()) paths.add(path);
    }

    private static List<File> toFiles(Set<String> paths) {
        List<File> files = new ArrayList<>();
        for (String path : paths) files.add(new File(path));
        return files;
    }

    private static boolean safeId(String id) {
        return id != null
                && !id.isBlank()
                && !id.contains("..")
                && !id.contains("/")
                && !id.contains("\\");
    }

    private static Properties copy(Properties source) {
        Properties copy = new Properties();
        for (String name : source.stringPropertyNames()) {
            copy.setProperty(name, source.getProperty(name));
        }
        return copy;
    }

    private static String md5(InputStream input) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 indisponível", e);
        }
    }
}