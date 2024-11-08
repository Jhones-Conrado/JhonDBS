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
package br.com.jhondbs.core.db.capsule;

import br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.db.obj.ColdEntity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import static br.com.jhondbs.core.tools.FieldsManager.getFields;
import br.com.jhondbs.core.tools.Reflection;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Versão 4
 * @author jhones
 */
public class Bottle {
    
    private String ROOT_DB = "./db/";
    private String TEMP_DB = "./temp/";
    
    public static final int ROOT_STAGE = 0;
    public static final int TEMP_STAGE = 1;
    
    public int modoOperacional = TEMP_STAGE;
    
    private Writer writer;
    private Reader reader;
    
    public Map<String, Bottle> bottles = new HashMap<>();
    public List<String> bottledFields = new ArrayList<>();
    public Set<String> referencias = new HashSet<>();
    public Map<String, Image> imgs = new HashMap<>();
    public Map<String, File> files = new HashMap<>();
    public ClassLoader loader;
    
    public Entity entity;

    private boolean isSub = false;
    
    public Bottle(Class clazz, String id, int modoOperacional) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles.put(id, this);
        this.loader = this.getClass().getClassLoader();
        defineTemp();
        initFolders();
        load(clazz, id, loader);
        cleanFolders();
    }
    
    public Bottle(Class clazz, String id, int modoOperacional, ClassLoader loader) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles.put(id, this);
        this.loader = loader;
        defineTemp();
        initFolders();
        load(clazz, id, loader);
        cleanFolders();
    }
    
    public Bottle(Entity entity) throws Exception {
        this.entity = entity;
        this.loader = this.getClass().getClassLoader();
        this.modoOperacional = ROOT_STAGE;
        this.bottles.put(entity.getId(), this);
        defineTemp();
        initFolders();
        loadRefs();
        cleanFolders();
    }
    
    public Bottle(Class clazz, String id, int modoOperacional, String root, String temp, boolean sub) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles.put(id, this);
        this.loader = this.getClass().getClassLoader();
        this.ROOT_DB = root;
        this.TEMP_DB = temp;
        this.writer = new Writer(modoOperacional, root, temp);
        this.reader = new Reader(modoOperacional, root, temp);
        this.isSub = true;
        load(clazz, id, loader);
    }
    
    private Bottle(Class clazz, String id, Map<String, Bottle> bottles, ClassLoader loader, int modoOperacional, String root, String temp, boolean sub) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles = bottles;
        this.bottles.put(id, this);
        this.loader = loader;
        this.ROOT_DB = root;
        this.TEMP_DB = temp;
        this.writer = new Writer(modoOperacional, root, temp);
        this.reader = new Reader(modoOperacional, root, temp);
        this.isSub = true;
        load(clazz, id, loader);
    }
    
    private Bottle(Entity entity, String root, String temp, boolean sub) throws Exception {
        this.entity = entity;
        this.loader = this.getClass().getClassLoader();
        this.modoOperacional = ROOT_STAGE;
        this.bottles.put(entity.getId(), this);
        this.ROOT_DB = root;
        this.TEMP_DB = temp;
        this.writer = new Writer(modoOperacional, root, temp);
        this.reader = new Reader(modoOperacional, root, temp);
        this.isSub = true;
        loadRefs();
    }
    
    private Bottle(Entity entity, Map<String, Bottle> bottles, ClassLoader loader, int modoOperacional, String root, String temp, boolean sub) throws Exception {
        this.entity = entity;
        this.bottles = bottles;
        this.loader = loader;
        this.modoOperacional = modoOperacional;
        this.bottles.put(entity.getId(), this);
        this.ROOT_DB = root;
        this.TEMP_DB = temp;
        this.writer = new Writer(modoOperacional, root, temp);
        this.reader = new Reader(modoOperacional, root, temp);
        this.isSub = true;
        loadRefs();
    }
    
    public void putRef(Entity entity) throws Exception {
        this.referencias.add(String.valueOf(ClassDictionary.getIndex(entity.getClass())) + ":" +entity.getId());
    }
    
    public void removeRef(Entity entity) throws Exception {
        this.referencias.remove(String.valueOf(ClassDictionary.getIndex(entity.getClass())) + ":" +entity.getId());
    }
    
    public void loadRefs() {
        try {
            this.referencias.addAll(reader.spliteredReferences(entity));
        } catch (Exception e) {
        }
    }
    
    private void defineTemp() {
        String identity = this.toString().replace(this.getClass().getName(), "");
        this.TEMP_DB = this.TEMP_DB + identity +"/";
        this.reader = new Reader(ROOT_DB, TEMP_DB);
        this.writer = new Writer(ROOT_DB, TEMP_DB);
    }
    
    private void initFolders() throws URISyntaxException, IOException {
        List<String> all = Reflection.allImplementsNotAbstract(Entity.class);
        for(String path : all) {
            File tempdb = new File(TEMP_DB+path.replaceAll(".class", "").replaceAll("[.]", "/"));
            tempdb.mkdirs();
        }
    }
    
    public void cleanFolders() {
        File directory = new File(TEMP_DB);
        if (!directory.exists()) {
            return;
        }
        deleteContents(directory);
        directory.delete();
    }
    
    private void deleteContents(File file) {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteContents(f);
                }
            }
        }
        file.delete();
    }
    
    /*
    ************************************************************
    ***************    GRAVAÇÃO DE ENTIDADE    *****************
    ************************************************************
    */
    
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(String.valueOf(ClassDictionary.getIndex(this.entity.getClass())));
        sb.append(":");
        for(String campo : bottledFields) {
            sb.append(campo);
        }
        sb.append("}");

        if(!referencias.isEmpty()) {
            sb.append("ref::");
            for(String ref : referencias) {
                sb.append(ref).append("::");
            }
        }
        return sb.toString();
    }
    
    public void flush() throws Exception {
        initFolders();
        writer.initDb();
        if (bottledFields.isEmpty()) {
            engarafar();
        }
        Set<String> toLock = new HashSet<>();
        try {
            if (todosCamposSaoUnicos()) {
                for (Bottle b : bottles.values()) {
                    toLock.add(b.entity.getId());
                }
                Bottle b2 = null;
                try {
                    b2 = new Bottle(entity.getClass(), entity.getId(), ROOT_STAGE, ROOT_DB, TEMP_DB, true);
                    toLock.addAll(b2.bottles.keySet());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // Tranca todas as entidades
                    for (String s : toLock) {
                        IO.io().lockWrite(s);
                    }
                    
                    List<Entity> excludeds = reader.listExcludeds(this);
                    removeReferences(excludeds);
                    handleOrphanEntities(excludeds);
                    
                    List<File> imgsToDelete = new ArrayList<>();
                    List<File> filesToDelete = new ArrayList<>();
                    for (Bottle bottle : bottles.values()) {
                        writer.write(bottle);
                        flushImgs(bottle, imgsToDelete);
                        flushFiles(bottle, filesToDelete);
                    }
                    
                    applyChanges(imgsToDelete, filesToDelete);
                } catch (Exception e) {
                    e.printStackTrace(); // Exceção no processo de escrita
                    throw e;
                } finally {
                    for (String s : toLock) {
                        IO.io().unlockWrite(s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Exceção na verificação de campos únicos
            throw e;
        } finally {
            cleanFolders();
        }
    }
    
    private void applyChanges(List<File> imgsToDelete, List<File> filesToDelete) throws IOException {
        deleteFilesEndingWithDelete();
        moveDirectory();
        for(File f : imgsToDelete) {
            if(f.exists()) {
                f.delete();
            }
        }
        for(File f : filesToDelete) {
            if(f.exists()) {
                f.delete();
            }
        }

        for(File f : imgsToDelete) {
            if(f.getParentFile().list().length == 0) {
                f.getParentFile().delete();
            }
        }
        for(File f : filesToDelete) {
            if(f.getParentFile().list().length == 0) {
                f.getParentFile().delete();
            }
        }
        
    }
    
    private void flushImgs(Bottle bottle, List<File> imgsToDelete) throws Exception {
        File folder = new File(TEMP_DB+"imgs/"+bottle.entity.getId());
        File prodFolder = new File(ROOT_DB+"imgs/"+bottle.entity.getId());
        if(!bottle.imgs.isEmpty() || prodFolder.exists()) {
            folder.mkdirs();
            prodFolder.mkdirs();
            List<String> hashs = Arrays.asList(prodFolder.list());
            for(String hash : bottle.imgs.keySet()) {
                if(!hashs.contains(hash)) {
                    File out = new File(folder.getPath()+"/"+hash);
                    ImageIO.write((RenderedImage) bottle.imgs.get(hash), "png", out);
                }
            }
            for(String hash : hashs) {
                if(!bottle.imgs.containsKey(hash)) {
                    File del = new File(prodFolder.getPath()+"/"+hash);
                    imgsToDelete.add(del);
                }
            }
            if(prodFolder.list().length == 0 && folder.list().length == 0) {
                prodFolder.delete();
            }
        }
    }

    private void flushFiles(Bottle bottle, List<File> filesToDelete) throws Exception {
        File fileFolder = new File(TEMP_DB+"files/"+bottle.entity.getId());
        File prodFileFolder = new File(ROOT_DB+"files/"+bottle.entity.getId());
        if(!bottle.files.isEmpty() || prodFileFolder.exists()) {
            fileFolder.mkdirs();
            prodFileFolder.mkdirs();
            List<String> fileHashs = Arrays.asList(prodFileFolder.list());
            for(String hash : bottle.files.keySet()) {
                if(!fileHashs.contains(hash)) {
                    File out = new File(fileFolder.getPath()+"/"+hash);
                    Files.copy(bottle.files.get(hash).toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    File antigo = new File(prodFileFolder.getPath()+"/"+hash);
                    File novo = bottle.files.get(hash);
                    if(!areFilesEquals(antigo, novo)) {
                        Files.copy(novo.toPath(), antigo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            for(String hash : fileHashs) {
                if(!bottle.files.containsKey(hash)) {
                    File antigo = new File(prodFileFolder.getPath()+"/"+hash);
                    filesToDelete.add(antigo);
                }
            }
            if(prodFileFolder.list().length == 0 && fileFolder.list().length == 0) {
                prodFileFolder.delete();
            }
        }
    }
    
    /**
     * Verifica quais entidades são do tipo cascata para futura análise de orfandade e exclusão.
     */
    private void handleOrphanEntities(List<Entity> excludeds) throws Exception {
        if(!excludeds.isEmpty()) {
            List<String> excludedIds = new ArrayList<>();
            for(Entity e : excludeds) {
                excludedIds.add(e.getId());
            }
            try {
                Reader r = new Reader(TEMP_STAGE, ROOT_DB, TEMP_DB);
                Bottle bot = new Bottle(entity.getClass(), entity.getId(), Bottle.ROOT_STAGE, ROOT_DB, TEMP_DB, true);
                List<Entity> oldcascs = r.listCascateEntities(bot.entity);
                List<Entity> todelete = new ArrayList<>();
                for(Entity e : oldcascs) {
                    if(excludedIds.contains(e.getId())) {
                        todelete.add(e);
                    }
                }
                for(Entity e : todelete) {
                    if(isOrphan(e)) {
                        Bottle b = new Bottle(entity, ROOT_DB, TEMP_DB, true);
                        if(b.delete(true)) {
                            b.deleteFilesEndingWithDelete();
                            b.moveDirectory();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Remove as referências dos objetos excluídos ou não mais citados.
     * @throws Exception 
     */
    private void removeReferences(List<Entity> excludeds) throws Exception {
        if(!excludeds.isEmpty()) {
            Reader r = new Reader(TEMP_STAGE, ROOT_DB, TEMP_DB);
            Writer ww = new Writer(TEMP_STAGE, ROOT_DB, TEMP_DB);
            for(Entity e : excludeds) {
                reader.sendToTemp(e);
                String cont = r.readContent(e);
                List<String> refs = r.spliteredReferences(e);
                for(Bottle bb : bottles.values()) {
                    String idd = bb.entity.getId();
                    refs = refs.stream().filter(ref -> !ref.contains(idd)).toList();
                }
                StringBuilder sb = new StringBuilder();
                if(!refs.isEmpty()) {
                    sb.append(cont).append("ref::");
                    for(String s : refs) {
                        sb.append(s).append("::");
                    }
                }
                ww.writeText(e.getClass(), e.getId(), sb.toString());
            }
        }
    }
    
    public void moveDirectory() throws IOException {
        Path sourceDir = Paths.get(this.TEMP_DB);
        Path targetDir = Paths.get(this.ROOT_DB);
        // Walk through the file tree starting from the source directory
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Resolve the target directory path
                Path targetPath = targetDir.resolve(sourceDir.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Move each file to the target directory
                Path targetPath = targetDir.resolve(sourceDir.relativize(file));
                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    public void deleteFilesEndingWithDelete() throws IOException {
        Path sourceDir = Paths.get(this.TEMP_DB);
        Path targetDir = Paths.get(this.ROOT_DB);
        // Walk through the file tree starting from the source directory
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(!file.toString().contains("/imgs/") && !file.toString().contains("/files/")) {
                    // Read all lines of the file
                    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

                    // Check if the last line ends with "DELETE"
                    if (!lines.isEmpty() && lines.get(lines.size() - 1).endsWith("DELETE")) {
                        // Resolve the corresponding file in the target directory (db)
                        Path targetFile = targetDir.resolve(sourceDir.relativize(file));

                        // Delete the file in the target directory if it exists
                        if (Files.exists(targetFile)) {
                            File f = targetFile.toFile();
                            if(f.isFile()) {
                                File fileFolder = new File(ROOT_DB+"files/"+f.getName());
                                File imgFolder = new File(ROOT_DB+"imgs/"+f.getName());
                                if(imgFolder.exists()) {
                                    File[] list = imgFolder.listFiles();
                                    for(File ff : list) {
                                        ff.delete();
                                    }
                                    imgFolder.delete();
                                }
                                if(fileFolder.exists()) {
                                    File[] list = fileFolder.listFiles();
                                    for(File ff : list) {
                                        ff.delete();
                                    }
                                    fileFolder.delete();
                                }
                            }
                            Files.delete(targetFile);
                        }
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }
    
    public boolean isOrphan(Entity entity) throws Exception {
        Reader r = new Reader(TEMP_STAGE, ROOT_DB, TEMP_DB);
        r.sendToTemp(entity);
        List<String> refs = r.spliteredReferences(entity);
        return refs.isEmpty();
    }
    
    public boolean delete() throws IllegalArgumentException, IllegalAccessException, Exception {
        initFolders();
        Reader r = new Reader(TEMP_STAGE, ROOT_DB, TEMP_DB);
        Writer w = new Writer(TEMP_STAGE, ROOT_DB, TEMP_DB);
        Bottle b = new Bottle(entity, ROOT_DB, TEMP_DB, true);
        for(Bottle bb : b.bottles.values()) {
            w.removeExistence(bb.entity);
        }
        return true;
    }
    
    private boolean delete(boolean sub) throws IllegalArgumentException, IllegalAccessException, Exception {
        Reader r = new Reader(TEMP_STAGE, ROOT_DB, TEMP_DB);
        Writer w = new Writer(TEMP_STAGE, ROOT_DB, TEMP_DB);
        Bottle b = new Bottle(entity, ROOT_DB, TEMP_DB, true);
        for(Bottle bb : b.bottles.values()) {
            w.removeExistence(bb.entity);
        }
        return true;
    }
    
    /*
    ************************************************************
    *************   ENCAPSULAMENTO DE ENTIDADE    **************
    ************************************************************
    */
    
    public void engarafar() throws IllegalArgumentException, IllegalAccessException, Exception {
        bottledFields.clear();
        List<Field> fields = FieldsManager.getAllSerializebleFields(this.entity.getClass());
        for(Field field : fields) {
            field.setAccessible(true);
            Object valor = field.get(this.entity);
            if(valor != null) {
                if(ClassDictionary.getIndex(valor.getClass()) != -1 || Reflection.isArrayMap(field.getType())) {
                    boolean condicional = true;
                    if(valor instanceof File) {
                        File f = (File) valor;
                        if(!f.exists()) {
                            condicional = false;
                        }
                    }
                    if(condicional) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("{").append(field.getName()).append(":");

                        if(valor.getClass().isEnum()) {
                            sb.append(encapsuleEnum((Enum) valor));
                        } else {
                            if(Reflection.isPrimitive(field.getType()) || Reflection.isNumerical(field.getType()) || Reflection.isDate(field.getType())) {
                                sb.append(encapsulePrimitive(valor));
                            } else if(Reflection.isArrayMap(field.getType())) {
                                if(Reflection.isInstance(valor.getClass(), List.class)) {
                                    List l = (List) valor;
                                    if(!l.isEmpty()) {
                                        sb.append(encapsuleArray(l));
                                    } else {
                                        sb.append("{list:{}}");
                                    }
                                } else if(Reflection.isInstance(valor.getClass(), Map.class)) {
                                    Map m = (Map) valor;
                                    if(!m.isEmpty()) {
                                        sb.append(encapsuleArray(m));
                                    } else {
                                        sb.append("{map:{}}");
                                    }
                                }
                            } else if(Reflection.isInstance(field.getType(), Entity.class)) {
                                Entity ente = (Entity) valor;
                                if(!bottles.containsKey(ente.getId())) {
                                    Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional, ROOT_DB, TEMP_DB, true);
                                    bottle.engarafar();
                                }
                                bottles.get(ente.getId()).putRef(this.entity);
                                sb.append(encapsuleId(ente));
                            } else {
                                sb.append(encapsularObjeto(valor));
                            }
                        }

                        sb.append("}");
                        this.bottledFields.add(sb.toString());
                    }
                }
            }
        }
    }
    
    public String encapsularObjeto(Object objeto) throws Exception {
        if(objeto.getClass().isEnum()) {
            return encapsuleEnum((Enum) objeto);
        } else {
            if(Reflection.isPrimitive(objeto.getClass()) || Reflection.isNumerical(objeto.getClass()) || Reflection.isDate(objeto.getClass())) {
                return encapsulePrimitive(objeto);
            } else if(Reflection.isArrayMap(objeto)) {
                if(Reflection.isInstance(objeto.getClass(), List.class)) {
                    List l = (List) objeto;
                    if(!l.isEmpty()) {
                        return encapsuleArray(objeto);
                    } else {
                        return "{list:{}}";
                    }
                } else if(Reflection.isInstance(objeto.getClass(), Map.class)) {
                    Map m = (Map) objeto;
                    if(!m.isEmpty()) {
                        return encapsuleArray(objeto);
                    } else {
                        return "{map:{}}";
                    }
                }
            } else if(Reflection.isInstance(objeto.getClass(), Entity.class)) {
                Entity ente = (Entity) objeto;
                if(!bottles.containsKey(ente.getId())) {
                    Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional, ROOT_DB, TEMP_DB, true);
                    bottle.engarafar();
                    bottle.putRef(this.entity);
                }
                return encapsuleId(ente);
            } else if(objeto instanceof File) {
                File file = (File) objeto;
                if(file.exists()) {
                    if(!file.getPath().contains(ROOT_DB+"files/")) {
                        if(!this.files.containsKey(file.getName())) {
                            this.files.put(file.getName(), file);
                        }
                        return "{file:"+file.getName()+"}";
                    }
                }
            } else if(objeto instanceof Image) {
                Image img = (Image) objeto;
                byte[] bytes = getImageData(img);
                String hash = generateMD5Hash(bytes);
                if(!this.imgs.containsKey(hash)) {
                    this.imgs.put(hash, img);
                }
                return "{img:"+hash+"}";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append(String.valueOf(ClassDictionary.getIndex(objeto.getClass())));
                sb.append(":");
                        
                List<Field> fields = FieldsManager.getAllSerializebleFields(objeto.getClass());
                for(Field field : fields) {
                    field.setAccessible(true);
                    Object get = field.get(objeto);
                    if(get != null) {
                        sb.append("{").append(field.getName()).append(":");
                        sb.append(encapsularObjeto(get));
                        sb.append("}");
                    }
                }
                sb.append("}");
                return sb.toString();
            }
        }
        throw new Exception("Objeto não serializável: "+objeto.getClass());
    }
    
    private byte[] getImageData(Image image) {
        BufferedImage bufferedImage = toBufferedImage(image);
        if (bufferedImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = newImage.createGraphics();
            g.drawImage(bufferedImage, 0, 0, null);
            g.dispose();
            bufferedImage = newImage;
        }
        DataBufferByte buffer = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
        return buffer.getData();
    }

    // Método auxiliar para converter Image em BufferedImage
    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }
    
    private String generateMD5Hash(byte[] inputBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(inputBytes);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
    
    private byte[] calculateMD5(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md)) {
            while (dis.read() != -1) ; // Lê o arquivo completamente para calcular o hash
        }
        return md.digest();
    }
    
    private boolean areFilesEquals(File fileA, File fileB) throws NoSuchAlgorithmException, IOException {
        byte[] hashA = calculateMD5(fileA);
        byte[] hashB = calculateMD5(fileB);
        return Arrays.equals(hashA, hashB);
    }
    
    private String encapsuleEnum(Enum e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(e.getClass())))
                .append(":")
                .append(e.toString())
                .append("}");
        return sb.toString();
    }
    
    private String encapsulePrimitive(Object object) {
        if(object == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(object.getClass())))
                .append(":")
                .append(object.toString())
                .append("}");
        return sb.toString();
    }
    
    private String encapsuleArray(Object object) throws Exception {
        if(object == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        if(Reflection.isInstance(object.getClass(), List.class) || Reflection.isInstance(object.getClass(), Set.class) || object.getClass().getName().contains("[")) {
            sb.append("{")
                .append("list")
                .append(":");
        } else if (Reflection.isInstance(object.getClass(), Map.class)) {
            sb.append("{")
                .append("map")
                .append(":");
        }
        if(Reflection.isInstance(object.getClass(), List.class) || Reflection.isInstance(object.getClass(), Set.class) || object.getClass().getName().contains("[")) {
            List list = asList(object);
            for(Object obj : list) {
                if(Reflection.isPrimitive(obj) || Reflection.isNumerical(obj.getClass()) || Reflection.isDate(obj.getClass())) {
                    sb.append(encapsulePrimitive(obj));
                } else if(Reflection.isArrayMap(obj)) {
                    sb.append(encapsuleArray(obj));
                } else if(Reflection.isInstance(obj.getClass(), Entity.class)) {
                    Entity ente = (Entity) obj;
                    if(!bottles.containsKey(ente.getId())) {
                        Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional, ROOT_DB, TEMP_DB, true);
                        bottle.engarafar();
                        bottle.putRef(this.entity);
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsularObjeto(obj));
                }
            }
        } else if(Reflection.isInstance(object.getClass(), Map.class)) {
            Map map = (Map) object;
            Set keys = map.keySet();
            for(Object key : keys) {
                if(Reflection.isPrimitive(key) || Reflection.isNumerical(key.getClass()) || Reflection.isDate(key.getClass())) {
                    sb.append(encapsulePrimitive(key));
                } else if(Reflection.isArrayMap(key)) {
                    sb.append(encapsuleArray(key));
                } else if(Reflection.isInstance(key.getClass(), Entity.class)) {
                    Entity ente = (Entity) key;
                    if(!bottles.containsKey(ente.getId())) {
                        Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional, ROOT_DB, TEMP_DB, true);
                        bottle.engarafar();
                        bottle.putRef(this.entity);
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsularObjeto(key));
                }
                if(Reflection.isPrimitive(map.get(key)) || Reflection.isNumerical(map.get(key).getClass()) || Reflection.isDate(map.get(key).getClass())) {
                    sb.append(encapsulePrimitive(map.get(key)));
                } else if(Reflection.isArrayMap(map.get(key))) {
                    sb.append(encapsuleArray(map.get(key)));
                } else if(Reflection.isInstance(map.get(key).getClass(), Entity.class)) {
                    Entity ente = (Entity) map.get(key);
                    if(!bottles.containsKey(ente.getId())) {
                        Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional, ROOT_DB, TEMP_DB, true);
                        bottle.engarafar();
                        bottle.putRef(this.entity);
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsularObjeto(map.get(key)));
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    private String encapsuleId(Entity entity) throws Exception {
        if(entity == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(entity.getClass())))
                .append(":")
                .append(entity.getId())
                .append("}");
        return sb.toString();
    }
    
    private List asList(Object object) {
        try {
            return (List) object;
        } catch (Exception e) {
            try {
                return Arrays.asList(object);
            } catch (Exception ex) {
            }
        }
        return new ArrayList();
    }
    
    
    
    
    
    /*
    ************************************************************
    ***************  RECUPERAÇÃO DE ENTIDADE   *****************
    ************************************************************
    */
    
    private void load(Class clazz, String id, ClassLoader loader) throws Exception {
        if(!isSub) {
            IO.io().lockRead(id);
        }
        writer.modoOperacional = this.modoOperacional;
        reader.modoOperacional = this.modoOperacional;
        try {
            this.entity = (Entity) Reflection.getNewInstance(clazz, loader);
            String content = reader.readContent(clazz, id);
            List<String> fields = reader.splitCapsules(reader.getValueFromCapsule(content));
            
            for(String campo : fields) {
                if(campo.substring(0, campo.indexOf(":")).contains("id")) {
                    inserir(this.entity, campo, loader);
                }
            }
            
            for(String campo : fields) {
                inserir(this.entity, campo, loader);
                this.bottledFields.add(campo);
            }
            loadRefs();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException ex) {
            Logger.getLogger(Bottle.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Erro ao ler a entidade: "+clazz+" -> "+id);
        } finally {
            if(!isSub) {
                IO.io().unlockRead(id);
            }
        }
    }
    
    private synchronized void inserir(Object receptor, String capsule, ClassLoader loader) throws Exception {
        try {
            String nome_campo = reader.getKeyFromCapsule(capsule);
            String sub_capsula = reader.getValueFromCapsule(capsule);
            
            String indice_classe = reader.getKeyFromCapsule(sub_capsula);
            String conteudo = reader.getValueFromCapsule(sub_capsula);

            Object valor = recuperar(indice_classe, conteudo, loader);

            FieldsManager.setValue(nome_campo, receptor, valor);

            Object x = FieldsManager.getValueFrom(nome_campo, receptor);
            
            if(FieldsManager.getValueFrom(nome_campo, receptor) == null && valor != null) {
                List<Field> fields = FieldsManager.getAllFields(receptor);
                for(Field f : fields){
                    if(f.getName().equals(nome_campo)){
                        f.setAccessible(true);
                        f.set(receptor, valor);
                        break;
                   }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    private synchronized Object recuperar(String indice, String conteudo, ClassLoader loader) throws Exception {
        Class classe_do_objeto = null;
        
        classe_do_objeto = switch (indice) {
            case "list" -> List.class;
            case "map" -> Map.class;
            case "img" -> Image.class;
            case "file" -> File.class;
            default -> ClassDictionary.fromIndex(Integer.parseInt(indice));
        };
        
        if (Reflection.isPrimitive(classe_do_objeto) || Reflection.isNumerical(classe_do_objeto)) {
            return reader.parsePrimitiveFromString(conteudo, classe_do_objeto, loader);
        } 
        else if (Reflection.isDate(classe_do_objeto)) {
            if (Reflection.isInstance(classe_do_objeto, Date.class)) {
                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                Class<?> formatterClass = Class.forName(formatter.getClass().getName(), true, loader);
                Constructor<?> constructor = formatterClass.getConstructor(String.class, Locale.class);
                SimpleDateFormat form = (SimpleDateFormat) constructor.newInstance("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                return form.parse(conteudo);
            } 
            else if (Reflection.isInstance(classe_do_objeto, Calendar.class)) {
                return reader.parseCalendarFromString(conteudo, loader);
            } 
            else if (Reflection.isInstance(classe_do_objeto, Temporal.class)) {
                return reader.parseDateTimeFromString(conteudo, classe_do_objeto, loader);
            } 
            else if (Reflection.isInstance(classe_do_objeto, Period.class)) {
                return reader.parsePeriodFromString(conteudo, loader);
            }
        } 
        else if (Reflection.isInstance(classe_do_objeto, Entity.class)) {
            String id = conteudo;
            if(bottles.containsKey(id)) {
                return bottles.get(id).entity;
            } else {
                Bottle bottle = new Bottle(classe_do_objeto, id, bottles, loader, modoOperacional, ROOT_DB, TEMP_DB, true);
                return bottle.entity;
            }
        } 
        else if (Reflection.isInstance(classe_do_objeto, List.class)) {
            return parseListFromString(conteudo, loader);
        } 
        else if (Reflection.isInstance(classe_do_objeto, Map.class)) {
            return parseMapFromString(conteudo, loader);
        } else if(Reflection.isInstance(classe_do_objeto, Image.class)) {
            return getImg(conteudo, loader);
        } else if(Reflection.isInstance(classe_do_objeto, File.class)) {
            return getFile(conteudo);
        } else {
            if (classe_do_objeto.isEnum()) {
                Class<?> forName = Class.forName(classe_do_objeto.getName(), true, loader);
                return Enum.valueOf((Class<Enum>) forName, conteudo);
            } else if(Reflection.isInstance(classe_do_objeto, ColdEntity.class)) {
                Object o = Reflection.getNewInstance(classe_do_objeto, loader);
                List<String> campos = reader.splitCapsules(conteudo);
                for(String campo : campos) {
                    if(campo.contains("loader")) {
                        FieldsManager.setValue("loader", o, loader);
                    } else if(campo.contains("entity")) {
                        //Ignorar o campo entity durante o carregamento.
                    } else {
                        inserir(o, campo, loader);
                    }
                }
                return o;
            } else {
                Object o = Reflection.getNewInstance(classe_do_objeto, loader);
                List<String> campos = reader.splitCapsules(conteudo);
                for(String campo : campos) {
                    inserir(o, campo, loader);
                }
                return o;
            }
        }
        
        throw new Exception("Não foi possível distinguir o tipo de objeto -> "+conteudo);
    }
    
    public File getFile(String name) throws Exception {
        if(!this.files.containsKey(name)) {
            File file = new File(ROOT_DB+"files/"+this.entity.getId()+"/"+name);
            if(!file.exists()) {
                throw new NullPointerException("Arquivo inexistente: "+file.getPath());
            }
            this.files.put(name, file);
        }
        return this.files.get(name);
    }
    
    public Image getImg(String hash, ClassLoader loader) throws ClassNotFoundException, Exception {
        if(!this.imgs.containsKey(hash)) {
            File imageFile = new File(ROOT_DB+"imgs/"+this.entity.getId()+"/"+hash);
            if (!imageFile.exists()) {
                throw new IllegalArgumentException("Imagem não encontrada no caminho: " + imageFile.getPath());
            }
            BufferedImage readed = ImageIO.read(imageFile);
            this.imgs.put(hash, readed);
        }
        return this.imgs.get(hash);
    }
    
    public List parseListFromString(String str, ClassLoader loader) throws Exception {
        List list = new ArrayList();
        List<String> objetos = reader.splitCapsules(str);
        for(String objeto : objetos) {
            if(!objeto.equals("{}")) {
                String indice_da_classe = reader.getKeyFromCapsule(objeto);
                Object obj = recuperar(indice_da_classe, reader.getValueFromCapsule(objeto), loader);
                list.add(obj);
            }
        }
        return list;
    }
    
    public Map parseMapFromString(String str, ClassLoader loader) throws Exception {
        Map<Object, Object> map = new HashMap<>();
        List<String> objetos = reader.splitCapsules(str);
        if (objetos == null || objetos.isEmpty()) {
            return map;
        }
        if(objetos.size() >= 2) {
            for (int i = 0; i < objetos.size() - 1; i += 2) {
                String CapsulaChave = objetos.get(i);
                String CapsulaValor = objetos.get(i+1);
                
                String indice_classe_chave = reader.getKeyFromCapsule(CapsulaChave);
                String capsula_valor_chave = reader.getValueFromCapsule(CapsulaChave);
                Object objeto_chave = recuperar(indice_classe_chave, capsula_valor_chave, loader);
                
                String indice_classe_valor = reader.getKeyFromCapsule(CapsulaValor);
                String capsula_valor_valor = reader.getValueFromCapsule(CapsulaValor);
                Object objeto_valor = recuperar(indice_classe_valor, capsula_valor_valor, loader);
                
                map.put(objeto_chave, objeto_valor);
            }
        }
        return map;
    }
    
    
    
    /*
    ************************************************************
    ******************  TESTES DE ENTIDADE   *******************
    ************************************************************
    */
    
    private boolean todosCamposSaoUnicos() throws Exception {
        for (Bottle bottle : bottles.values()) {
            try {
                if (!uniqueFieldTest(bottle.entity)) {
                    return false;
                }
            } catch (DuplicatedUniqueFieldException ex) {
                Logger.getLogger(Bottle.class.getName()).log(Level.SEVERE, "Duplicated unique field for entity: " + bottle.entity + " -> " + bottle.entity.getId(), ex);
                throw new DuplicatedUniqueFieldException("Duplicated unique field for entity: " + bottle.entity + " -> " + bottle.entity.getId() + ex);
            } catch (Exception ex) {
                Logger.getLogger(Bottle.class.getName()).log(Level.SEVERE, "Error during unique field validation", ex);
                throw new Exception("Erro durante a verificação de unicidade de campos das entidades a serem gravadas -> "+ex);
            }
        }
        return true;
    }
    
    private boolean uniqueFieldTest(Entity entity) throws Exception {
        return testeDeUnicidade(entity, ROOT_STAGE) && testeDeUnicidade(entity, TEMP_STAGE);
    }
    
    private boolean testeDeUnicidade(Entity entity, int modoOperacional) throws Exception {
        if (entity == null) {
            throw new NullPointerException("Entidade nula para teste de unicidade.");
        }
        List<Field> unicos = FieldsManager.getAllFieldsUniques(entity);
        if (unicos.isEmpty()) {
            return true;
        }

        String raiz = (modoOperacional == ROOT_STAGE) ? ROOT_DB : TEMP_DB;
        String[] listaDeIds = new File(raiz + entity.getClass().getName().replace(".class", "").replace(".", "/")).list();

        Reader reader = new Reader(ROOT_DB, TEMP_DB);
        reader.modoOperacional = modoOperacional;

        if (listaDeIds != null && listaDeIds.length > 0) {
            for (String id : listaDeIds) {
                if (!id.equals(entity.getId())) {
                    Map<String, String> paraComparar = reader.readUniqueFieldsAsMap(entity.getClass(), id);
                    for (Field unico : unicos) {
                        if (paraComparar.containsKey(unico.getName())) {
                            Object get = unico.get(entity);
                            if (get != null) {
                                if (Reflection.isInstance(get.getClass(), Entity.class)) {
                                    Entity ente = (Entity) get;
                                    if (paraComparar.get(unico.getName()).contains(ente.getId())) {
                                        throw new DuplicatedUniqueFieldException("Campo unico duplicado:\n"
                                                + "-> " + entity + " id: " + entity.getId() + "\n"
                                                + "-> " + unico.getName() + "\n"
                                                + "-> " + ente.getId() + " igual " + paraComparar.get(unico.getName()));
                                    }
                                } else {
                                    String valorLimpo = reader.getValueFromCapsule(paraComparar.get(unico.getName()));
                                    if (get.toString().equals(valorLimpo)) {
                                        throw new DuplicatedUniqueFieldException("Campo unico duplicado:\n"
                                                + "-> " + entity + " id: " + entity.getId() + "\n"
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
    
    /*
    ************************************************************
    ******************  MÉTODOS ESTÁTICOS   ********************
    ************************************************************
    */
    
    public static <T extends Entity> List<T> loadAll(Class classe, ClassLoader loader) throws Exception {
        return loadAll(classe, null, loader);
    }
    
    public static <T extends Entity> List<T> loadAll(Class classe, Filter filter, ClassLoader loader) throws Exception {
        Writer w = new Writer();
        w.initDb();
        Reader reader = new Reader(ROOT_STAGE);
        List<String> ids = reader.listAllIds(classe);
        List<T> list = new ArrayList<>();
        for(String id : ids) {
            Bottle bottle = new Bottle(classe, id, ROOT_STAGE, loader);
            if(filter != null) {
                if(filter.filter(bottle.entity)) {
                    list.add((T) bottle.entity);
                }
            } else {
                list.add((T) bottle.entity);
            }
        }
        return list;
    }
    
}
