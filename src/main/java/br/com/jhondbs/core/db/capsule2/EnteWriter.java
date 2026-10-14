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
import br.com.jhondbs.core.tools.SessionsCache;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Grava um {@link EnteAnalysis} no disco, de forma atômica.
 *
 * <p>A entidade precisa continuar em {@link SessionsCache} — é assim que o
 * caminho de cada id é resolvido, no mesmo padrão do {@link DirectoryManager}.
 * {@link EnteManager#save} deixa a sessão ativa.
 *
 * <ol>
 *   <li>Confere o que realmente precisa ser criado ou substituído. Documento
 *       igual, arquivo já copiado e imagem cujo MD5 já está na pasta são
 *       pulados.</li>
 *   <li>O que será substituído ou deixará de ser usado é renomeado para
 *       terminar em {@code .bak}.</li>
 *   <li>Grava propriedades, copia arquivos e escreve imagens. Arquivo cuja
 *       origem está fora de {@code ./db/files/{id}} e fora do diretório da
 *       entidade é copiado para {@link DirectoryManager#entityFileFolder}.</li>
 *   <li>Confere a gravação. Se passar, apaga os {@code .bak} (o que inclui
 *       versão antiga e anexo que não é mais referenciado). Se falhar, apaga
 *       o que foi criado e devolve cada {@code .bak} ao nome original.</li>
 * </ol>
 *
 * @author jhones
 */
public final class EnteWriter {

    private static final Object LOCK = new Object();
    private static final Pattern FILE_REF = Pattern.compile("\\{file:([^{}]*)\\}");
    private static final Pattern IMG_REF = Pattern.compile("\\{img:([^{}]*)\\}");

    private final List<Backup> backups = new ArrayList<>();
    private final List<File> created = new ArrayList<>();
    private final List<File> temps = new ArrayList<>();
    private final List<File> createdDirs = new ArrayList<>();
    private final Map<String, Location> locations = new LinkedHashMap<>();
    private final List<Write> writes = new ArrayList<>();
    private final List<File> removals = new ArrayList<>();

    private EnteWriter() {
    }

    /**
     * Aplica a análise. Não faz nada quando não há pendência.
     *
     * @throws Exception falha de planejamento, de gravação ou de verificação;
     *                   neste caso o disco volta ao estado anterior
     */
    public static void write(EnteAnalysis analysis) throws Exception {
        if (analysis == null) {
            throw new NullPointerException("A análise não pode ser nula.");
        }
        synchronized (LOCK) {
            new EnteWriter().execute(analysis);
        }
    }

    private void execute(EnteAnalysis analysis) throws Exception {
        if (!analysis.hasPending()) {
            return;
        }

        plan(analysis);
        if (writes.isEmpty() && removals.isEmpty()) {
            return;
        }
        ensureNoLeftoverBak();

        boolean committed = false;
        try {
            for (Write write : writes) {
                if (write.target.exists()) {
                    backup(write.target);
                }
                write.apply(this);
            }
            for (File unused : removals) {
                backup(unused);
            }
            verify();
            committed = true;
        } catch (Exception failure) {
            try {
                rollback();
            } catch (Exception rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }

        if (committed) {
            deleteBackups();
        }
    }

    private void plan(EnteAnalysis analysis) throws Exception {
        Map<String, Properties> documents = new LinkedHashMap<>();
        for (Properties generated : analysis.getProperties()) {
            String id = generated == null ? null : generated.getProperty("id");
            if (!safeId(id)) {
                throw new IllegalArgumentException("ID de entidade inválido na análise: " + id);
            }
            if (documents.containsKey(id)) {
                throw new IllegalArgumentException("ID repetido na análise: " + id);
            }
            documents.put(id, generated);
            location(id);
        }
        for (String id : analysis.getFiles().keySet()) {
            if (!safeId(id)) {
                throw new IllegalArgumentException("ID de entidade inválido nos arquivos: " + id);
            }
            location(id);
        }
        for (String id : analysis.getImgs().keySet()) {
            if (!safeId(id)) {
                throw new IllegalArgumentException("ID de entidade inválido nas imagens: " + id);
            }
            location(id);
        }

        Map<String, Set<String>> keepFiles = new LinkedHashMap<>();
        Map<String, Set<String>> keepImages = new LinkedHashMap<>();
        Map<String, String> fieldsById = new LinkedHashMap<>();

        for (Location location : locations.values()) {
            String fields = fieldsOf(location, documents.get(location.id));
            fieldsById.put(location.id, fields);
            keepFiles.put(location.id, fileRefs(fields));
            keepImages.put(location.id, imageRefs(fields));
        }

        for (Map.Entry<String, Properties> entry : documents.entrySet()) {
            Location location = locations.get(entry.getKey());
            Properties generated = entry.getValue();
            File storedAt = existingProperties(location);
            Properties stored = load(storedAt);
            if (stored != null && sameDocument(stored, generated)) {
                continue;
            }
            writes.add(Write.properties(location.propertiesFile, copyOf(generated)));
            if (storedAt != null && storedAt.isFile() && !sameFile(storedAt, location.propertiesFile)) {
                removals.add(storedAt);
            }
        }

        for (Map.Entry<String, Map<String, File>> byEntity : analysis.getFiles().entrySet()) {
            Location location = locations.get(byEntity.getKey());
            Set<String> keep = keepFiles.get(location.id);
            for (Map.Entry<String, File> item : byEntity.getValue().entrySet()) {
                String name = safeName(item.getKey(), "arquivo");
                File source = item.getValue();
                if (source == null) {
                    throw new IllegalArgumentException("Arquivo nulo para " + location.id + "/" + name);
                }
                if (!source.isFile()) {
                    throw new IOException("Arquivo de origem inexistente: " + source);
                }
                File dest = new File(location.filesDir, name);
                keep.add(name);
                if (alreadyStored(location, source, dest)) {
                    continue;
                }
                if (dest.isFile() && sameContent(source, dest)) {
                    continue;
                }
                writes.add(Write.file(dest, source));
            }
        }

        for (Map.Entry<String, Map<String, Image>> byEntity : analysis.getImgs().entrySet()) {
            Location location = locations.get(byEntity.getKey());
            Set<String> keep = keepImages.get(location.id);
            for (Map.Entry<String, Image> item : byEntity.getValue().entrySet()) {
                String hash = safeHash(item.getKey());
                Image image = item.getValue();
                if (image == null) {
                    throw new IllegalArgumentException("Imagem nula para " + location.id + "/" + hash);
                }
                keep.add(hash);
                if (imageAlreadyStored(location, hash)) {
                    continue;
                }
                byte[] png = imageBytes(image);
                File dest = new File(location.imagesDir, hash + ".png");
                writes.add(Write.image(dest, png, hash));
            }
        }

        Set<String> writeTargets = new LinkedHashSet<>();
        for (Write write : writes) {
            String path = canonical(write.target);
            if (!writeTargets.add(path)) {
                throw new IllegalArgumentException("Dois itens gravariam o mesmo arquivo: " + write.target);
            }
        }

        for (Location location : locations.values()) {
            String fields = fieldsById.get(location.id);
            if (fields == null) {
                continue;
            }
            Set<String> files = keepFiles.get(location.id);
            Set<String> images = keepImages.get(location.id);
            for (File dir : location.fileDirs) {
                for (File child : children(dir)) {
                    if (isBak(child) || files.contains(child.getName())) {
                        continue;
                    }
                    if (writeTargets.contains(canonical(child))) {
                        continue;
                    }
                    removals.add(child);
                }
            }
            for (File dir : location.imageDirs) {
                for (File child : children(dir)) {
                    if (isBak(child) || writeTargets.contains(canonical(child))) {
                        continue;
                    }
                    if (imageKept(child, images)) {
                        continue;
                    }
                    removals.add(child);
                }
            }
        }
    }

    /**
     * Já guardado quando a origem é o destino, está em {@code ./db/files/{id}}
     * ou já está no diretório de arquivos da entidade. Qualquer outro diretório
     * é copiado para {@link DirectoryManager#entityFileFolder}.
     */
    private static boolean alreadyStored(Location location, File source, File dest) throws IOException {
        if (sameFile(source, dest)) {
            return true;
        }
        if (isInside(location.legacyFilesDir, source)) {
            return true;
        }
        return isInside(location.filesDir, source);
    }

    private static boolean imageAlreadyStored(Location location, String hash) throws IOException {
        for (File dir : location.imageDirs) {
            for (File child : children(dir)) {
                if (imageKept(child, Set.of(hash))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean imageKept(File child, Set<String> hashes) throws IOException {
        if (child == null || !child.isFile() || hashes == null || hashes.isEmpty()) {
            return false;
        }
        String lower = child.getName().toLowerCase(Locale.ROOT);
        if (hashes.contains(lower)) {
            return true;
        }
        int dot = lower.lastIndexOf('.');
        if (dot > 0 && hashes.contains(lower.substring(0, dot))) {
            return true;
        }
        try {
            return hashes.contains(md5(child));
        } catch (IOException e) {
            return true;
        }
    }

    private void ensureNoLeftoverBak() throws IOException {
        Set<String> seen = new LinkedHashSet<>();
        for (File file : filesThatWillMove()) {
            File bak = bakOf(file);
            String key = canonical(bak);
            if (!seen.add(key)) {
                throw new IOException("Dois backups cairiam no mesmo .bak: " + bak);
            }
            if (bak.exists()) {
                throw new IOException("Backup já existe, possível gravação interrompida: " + bak);
            }
        }
    }

    private List<File> filesThatWillMove() {
        List<File> files = new ArrayList<>();
        for (File removal : removals) {
            if (removal.exists()) {
                files.add(removal);
            }
        }
        for (Write write : writes) {
            if (write.target.exists()) {
                files.add(write.target);
            }
        }
        return files;
    }

    private void backup(File original) throws IOException {
        if (!original.exists()) {
            return;
        }
        if (!original.isFile()) {
            throw new IOException("Não é um arquivo substituível: " + original);
        }
        File bak = bakOf(original);
        ensureDir(bak.getParentFile());
        moveAtomic(original.toPath(), bak.toPath());
        backups.add(new Backup(original, bak));
    }

    private void verify() throws Exception {
        for (Write write : writes) {
            write.verify();
        }
        for (File removed : removals) {
            if (removed.exists()) {
                throw new IOException("Arquivo que não é mais usado continuou no lugar: " + removed);
            }
            File bak = bakOf(removed);
            if (!bak.isFile()) {
                throw new IOException("Backup do arquivo removido sumiu antes da confirmação: " + bak);
            }
        }
        for (Backup backup : backups) {
            if (!backup.bak.isFile()) {
                throw new IOException("Backup ausente antes da confirmação: " + backup.bak);
            }
        }
    }

    private void deleteBackups() throws IOException {
        IOException failure = null;
        for (int i = backups.size() - 1; i >= 0; i--) {
            Backup backup = backups.get(i);
            try {
                Files.deleteIfExists(backup.bak.toPath());
            } catch (IOException e) {
                if (failure == null) {
                    failure = new IOException("Gravação confirmada, mas não foi possível apagar " + backup.bak, e);
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        for (File temp : temps) {
            try {
                Files.deleteIfExists(temp.toPath());
            } catch (IOException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private void rollback() throws IOException {
        IOException failure = null;
        for (File temp : temps) {
            failure = deleteQuiet(temp, failure);
        }
        for (int i = created.size() - 1; i >= 0; i--) {
            failure = deleteQuiet(created.get(i), failure);
        }
        for (int i = backups.size() - 1; i >= 0; i--) {
            Backup backup = backups.get(i);
            try {
                if (backup.original.exists()) {
                    Files.delete(backup.original.toPath());
                }
                if (backup.bak.exists()) {
                    moveAtomic(backup.bak.toPath(), backup.original.toPath());
                }
            } catch (IOException e) {
                if (failure == null) {
                    failure = new IOException("Falha ao restaurar " + backup.original, e);
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        for (int i = createdDirs.size() - 1; i >= 0; i--) {
            File dir = createdDirs.get(i);
            File[] kids = dir.listFiles();
            if (kids != null && kids.length == 0) {
                dir.delete();
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private static IOException deleteQuiet(File file, IOException failure) {
        try {
            Files.deleteIfExists(file.toPath());
            return failure;
        } catch (IOException e) {
            if (failure == null) {
                return e;
            }
            failure.addSuppressed(e);
            return failure;
        }
    }

    private Location location(String id) throws Exception {
        Location found = locations.get(id);
        if (found != null) {
            return found;
        }
        EnteMemory memory = SessionsCache.find(id);
        if (memory == null || memory.getInstance() == null) {
            throw new IllegalStateException(
                    "Entidade " + id + " não está em sessão. Não há como resolver o diretório.");
        }
        Entity entity = memory.getInstance();
        if (!id.equals(entity.getId())) {
            throw new IllegalStateException("Sessão de " + id + " aponta para a entidade " + entity.getId());
        }
        Location location = new Location(
                id,
                new File(DirectoryManager.entityToPath(entity)),
                alternateProperties(memory, id),
                new File(DirectoryManager.entityFileFolder(entity)),
                new File(DirectoryManager.entityImagesFolder(entity)),
                new File("./db/files/" + id),
                new File("./db/imgs/" + id),
                extraDir("./db/files/", memory, id),
                extraDir("./db/imgs/", memory, id));
        locations.put(id, location);
        return location;
    }

    private static File extraDir(String root, EnteMemory memory, String id) {
        Class<?> clazz = memory.getClasse();
        if (clazz == null) {
            return null;
        }
        return new File(root + clazz.getName().replace('.', '/') + "/" + id);
    }

    private static File alternateProperties(EnteMemory memory, String id) {
        Class<?> clazz = memory.getClasse();
        if (clazz == null) {
            return null;
        }
        return new File("./db/" + clazz.getName().replace('.', '/') + "/" + id);
    }

    private static File existingProperties(Location location) throws IOException {
        if (location.propertiesFile.isFile()) {
            return location.propertiesFile;
        }
        if (location.alternateProperties != null
                && location.alternateProperties.isFile()
                && !sameFile(location.alternateProperties, location.propertiesFile)) {
            return location.alternateProperties;
        }
        return null;
    }

    private String fieldsOf(Location location, Properties generated) throws IOException {
        if (generated != null) {
            String fields = generated.getProperty("fields");
            return fields == null ? "" : fields;
        }
        Properties stored = load(existingProperties(location));
        if (stored == null) {
            return null;
        }
        String fields = stored.getProperty("fields");
        return fields == null ? "" : fields;
    }

    private void ensureDir(File dir) throws IOException {
        if (dir == null || dir.isDirectory()) {
            return;
        }
        if (dir.exists()) {
            throw new IOException("Caminho existe e não é diretório: " + dir);
        }
        List<File> missing = new ArrayList<>();
        File current = dir;
        while (current != null && !current.exists()) {
            missing.add(current);
            current = current.getParentFile();
        }
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("Não foi possível criar o diretório " + dir);
        }
        for (int i = missing.size() - 1; i >= 0; i--) {
            createdDirs.add(missing.get(i));
        }
    }

    private File stage(File target, byte[] bytes) throws IOException {
        ensureDir(target.getParentFile());
        File temp = tempOf(target);
        temps.add(temp);
        Files.write(temp.toPath(), bytes);
        try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(temp.toPath(),
                java.nio.file.StandardOpenOption.WRITE)) {
            channel.force(true);
        }
        moveAtomic(temp.toPath(), target.toPath());
        temps.remove(temp);
        created.add(target);
        return target;
    }

    private File stageCopy(File target, File source) throws IOException {
        ensureDir(target.getParentFile());
        File temp = tempOf(target);
        temps.add(temp);
        try (java.nio.channels.FileChannel in = java.nio.channels.FileChannel.open(source.toPath());
             java.nio.channels.FileChannel out = java.nio.channels.FileChannel.open(temp.toPath(),
                     java.nio.file.StandardOpenOption.CREATE,
                     java.nio.file.StandardOpenOption.WRITE,
                     java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            long pos = 0;
            long size = in.size();
            while (pos < size) {
                long moved = in.transferTo(pos, size - pos, out);
                if (moved <= 0) {
                    break;
                }
                pos += moved;
            }
            out.force(true);
        }
        moveAtomic(temp.toPath(), target.toPath());
        temps.remove(temp);
        created.add(target);
        return target;
    }

    private static File tempOf(File target) {
        return new File(target.getParentFile(), target.getName() + ".writing");
    }

    private static File bakOf(File target) {
        return new File(target.getParentFile(), target.getName() + ".bak");
    }

    private static void moveAtomic(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean sameDocument(Properties stored, Properties generated) {
        if (stored == null || generated == null) {
            return false;
        }
        if (!text(stored, "id").equals(text(generated, "id"))) {
            return false;
        }
        if (!text(stored, "fields").equals(text(generated, "fields"))) {
            return false;
        }
        if (!refSet(stored.getProperty("refs")).equals(refSet(generated.getProperty("refs")))) {
            return false;
        }
        return cascate(stored) == cascate(generated);
    }

    private static boolean cascate(Properties properties) {
        return properties != null && "true".equalsIgnoreCase(properties.getProperty("cascate"));
    }

    private static String text(Properties properties, String key) {
        if (properties == null) {
            return "";
        }
        String value = properties.getProperty(key);
        return value == null ? "" : value;
    }

    private static Set<String> refSet(String refs) {
        Set<String> tokens = new LinkedHashSet<>();
        if (refs == null || refs.isBlank()) {
            return tokens;
        }
        for (String part : refs.split("::")) {
            if (part != null && !part.isBlank()) {
                tokens.add(part.trim());
            }
        }
        return tokens;
    }

    private static Set<String> fileRefs(String fields) {
        Set<String> names = new LinkedHashSet<>();
        if (fields == null || fields.isEmpty()) {
            return names;
        }
        Matcher matcher = FILE_REF.matcher(fields);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (name != null && !name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    private static Set<String> imageRefs(String fields) {
        Set<String> hashes = new LinkedHashSet<>();
        if (fields == null || fields.isEmpty()) {
            return hashes;
        }
        Matcher matcher = IMG_REF.matcher(fields);
        while (matcher.find()) {
            String hash = matcher.group(1);
            if (hash != null && !hash.isBlank()) {
                hashes.add(hash.trim().toLowerCase(Locale.ROOT));
            }
        }
        return hashes;
    }

    private static Properties load(File file) throws IOException {
        if (file == null || !file.isFile()) {
            return null;
        }
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
        return properties;
    }

    private static Properties copyOf(Properties source) {
        Properties copy = new Properties();
        for (String name : source.stringPropertyNames()) {
            copy.setProperty(name, source.getProperty(name));
        }
        return copy;
    }

    private static void store(Properties properties, File target, EnteWriter writer) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        properties.store(buffer, "JhonDBS Entity");
        writer.stage(target, buffer.toByteArray());
    }

    private static boolean sameProperties(Properties expected, Properties actual) {
        if (expected == null || actual == null) {
            return false;
        }
        if (!expected.stringPropertyNames().equals(actual.stringPropertyNames())) {
            return false;
        }
        for (String name : expected.stringPropertyNames()) {
            if (!text(expected, name).equals(text(actual, name))) {
                return false;
            }
        }
        return true;
    }

    private static byte[] imageBytes(Image image) throws IOException {
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
        if (!ImageIO.write(buffered, "png", output)) {
            throw new IOException("PNG não disponível para gravar a imagem");
        }
        return output.toByteArray();
    }

    private static boolean sameContent(File left, File right) throws IOException {
        if (left == null || right == null || !left.isFile() || !right.isFile()) {
            return false;
        }
        if (left.length() != right.length()) {
            return false;
        }
        return md5(left).equals(md5(right));
    }

    private static boolean sameFile(File left, File right) throws IOException {
        if (left == null || right == null) {
            return false;
        }
        if (!left.exists() || !right.exists()) {
            return canonical(left).equals(canonical(right));
        }
        return Files.isSameFile(left.toPath(), right.toPath());
    }

    private static boolean isInside(File dir, File file) throws IOException {
        if (dir == null || file == null) {
            return false;
        }
        Path folder = dir.getCanonicalFile().toPath();
        Path target = file.getCanonicalFile().toPath();
        return target.startsWith(folder) && !target.equals(folder);
    }

    private static String canonical(File file) throws IOException {
        return file.getCanonicalPath();
    }

    private static List<File> children(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return List.of();
        }
        File[] list = dir.listFiles();
        if (list == null || list.length == 0) {
            return List.of();
        }
        List<File> files = new ArrayList<>();
        for (File child : list) {
            if (child.isFile()) {
                files.add(child);
            }
        }
        return files;
    }

    private static boolean isBak(File file) {
        return file.getName().toLowerCase(Locale.ROOT).endsWith(".bak");
    }

    private static boolean safeId(String id) {
        return id != null
                && !id.isBlank()
                && !id.contains("..")
                && !id.contains("/")
                && !id.contains("\\")
                && !id.contains(File.separator);
    }

    private static String safeName(String name, String kind) {
        if (name == null || name.isBlank()
                || name.contains("/")
                || name.contains("\\")
                || name.contains("..")
                || name.equals(".")
                || name.equals(".writing")
                || isBakName(name)) {
            throw new IllegalArgumentException("Nome de " + kind + " inválido: " + name);
        }
        return name;
    }

    private static String safeHash(String hash) {
        if (hash == null || !hash.matches("[0-9a-fA-F]{8,128}")) {
            throw new IllegalArgumentException("MD5 de imagem inválido: " + hash);
        }
        return hash.toLowerCase(Locale.ROOT);
    }

    private static boolean isBakName(String name) {
        return name.toLowerCase(Locale.ROOT).endsWith(".bak");
    }

    private static String md5(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return md5(input);
        }
    }

    private static String md5(byte[] data) {
        try {
            return hex(MessageDigest.getInstance("MD5").digest(data));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 indisponível", e);
        }
    }

    private static String md5(InputStream input) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return hex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 indisponível", e);
        }
    }

    private static String hex(byte[] hash) {
        StringBuilder text = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            text.append(Character.forDigit((b >> 4) & 0xF, 16));
            text.append(Character.forDigit(b & 0xF, 16));
        }
        return text.toString();
    }

    private static final class Location {
        final String id;
        final File propertiesFile;
        final File alternateProperties;
        final File filesDir;
        final File imagesDir;
        final File legacyFilesDir;
        final List<File> fileDirs;
        final List<File> imageDirs;

        Location(String id, File propertiesFile, File alternateProperties, File filesDir, File imagesDir,
                 File legacyFilesDir, File legacyImagesDir, File extraFiles, File extraImages) throws IOException {
            this.id = id;
            this.propertiesFile = propertiesFile;
            this.alternateProperties = alternateProperties;
            this.filesDir = filesDir;
            this.imagesDir = imagesDir;
            this.legacyFilesDir = legacyFilesDir;
            this.fileDirs = uniqueDirs(filesDir, legacyFilesDir, extraFiles);
            this.imageDirs = uniqueDirs(imagesDir, legacyImagesDir, extraImages);
        }

        private static List<File> uniqueDirs(File... dirs) throws IOException {
            List<File> unique = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (File dir : dirs) {
                if (dir == null) {
                    continue;
                }
                if (seen.add(canonical(dir))) {
                    unique.add(dir);
                }
            }
            return unique;
        }
    }

    private static final class Backup {
        final File original;
        final File bak;

        Backup(File original, File bak) {
            this.original = original;
            this.bak = bak;
        }
    }

    private static final class Write {
        final File target;
        final Properties properties;
        final File source;
        final byte[] image;
        final String imageHash;

        private Write(File target, Properties properties, File source, byte[] image, String imageHash) {
            this.target = target;
            this.properties = properties;
            this.source = source;
            this.image = image;
            this.imageHash = imageHash;
        }

        static Write properties(File target, Properties properties) {
            return new Write(target, properties, null, null, null);
        }

        static Write file(File target, File source) {
            return new Write(target, null, source, null, null);
        }

        static Write image(File target, byte[] png, String hash) {
            return new Write(target, null, null, png, hash);
        }

        void apply(EnteWriter writer) throws IOException {
            if (properties != null) {
                store(properties, target, writer);
                return;
            }
            if (source != null) {
                writer.stageCopy(target, source);
                return;
            }
            writer.stage(target, image);
        }

        void verify() throws IOException {
            if (!target.isFile()) {
                throw new IOException("Arquivo não foi gravado: " + target);
            }
            if (properties != null) {
                Properties onDisk = load(target);
                if (!sameProperties(properties, onDisk)) {
                    throw new IOException("Properties gravado difere do previsto: " + target);
                }
                return;
            }
            if (source != null) {
                if (!sameContent(source, target)) {
                    throw new IOException("Cópia difere da origem: " + target);
                }
                return;
            }
            if (!imageHash.equals(hashName(target)) && !imageHash.equals(target.getName().toLowerCase(Locale.ROOT))) {
                throw new IOException("Nome da imagem não é o MD5 previsto: " + target);
            }
            byte[] onDisk = Files.readAllBytes(target.toPath());
            if (!java.util.Arrays.equals(image, onDisk)) {
                throw new IOException("Imagem gravada difere dos bytes previstos: " + target);
            }
        }

        private static String hashName(File target) {
            String name = target.getName().toLowerCase(Locale.ROOT);
            int dot = name.lastIndexOf('.');
            return dot > 0 ? name.substring(0, dot) : name;
        }
    }
}