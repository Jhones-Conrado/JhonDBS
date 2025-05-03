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
import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.errors.ObjectNotDesserializebleException;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.db.obj.ColdEntity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.tools.Reflection;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Versão 4
 * @author jhones
 */
public final class Bottle {
    
    public String ROOT_DB = "./db/";
    public String TEMP_DB = "./temp/";
    
    public static final int ROOT_STAGE = 0;
    public static final int TEMP_STAGE = 1;
    
    public int modoOperacional = TEMP_STAGE;
    
    public Writer writer;
    public Reader reader;
    
    public Map<String, Bottle> bottles = new HashMap<>();
    public List<String> bottledFields = new ArrayList<>();
    public Set<Ref> referencias = new HashSet<>();
    public Map<String, Image> imgs = new HashMap<>();
    public Map<String, File> files = new HashMap<>();
    public ClassLoader loader;
    
    public Entity entity;

    public boolean isSub = false;
    
    public Properties props = new Properties();
    
    public boolean cascate = false;
    
    private Bottle() {
        
    }
    
    /**
     * Adicionar uma entidade como referenciador desta.
     * @param entity
     * @throws Exception
     */
    public void putRef(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        Ref ref = new Ref(entity);
        this.referencias.add(ref);
    }
    
    /**
     * Remove uma entidade da lista de referenciadores.
     * @param entity
     * @throws Exception 
     */
    public void removeRef(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        Ref ref = new Ref(entity);
        this.referencias.remove(ref);
    }
    
    /**
     * Lê as referências do estado antigo da entidade.
     * @throws Exception 
     */
    public void loadRefs() throws FileNotFoundException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, IOException {
        String path = getPath(this.entity);
        File file = new File(path);
        if(file.exists()) {
            Properties currentProps = new Properties();
            currentProps.load(new FileInputStream(new File(path)));
            this.props = currentProps;
            String refs = currentProps.getProperty("refs").toString();
            if(!refs.isBlank()) {
                this.referencias.addAll(Arrays.asList(refs.split("::"))
                        .stream()
                        .filter(str -> !str.isBlank())
                        .map(str -> new Ref(str))
                        .toList());
            }
        }
    }
    
    /**
     * Utilizado para definir a pasta temporária utilizada para serialização.
     */
    private void defineTemp() {
        String identity = this.toString().replace(this.getClass().getName(), "");
        this.TEMP_DB = this.TEMP_DB + identity +"/";
        this.reader = new Reader(ROOT_DB, TEMP_DB);
        this.writer = new Writer(ROOT_DB, TEMP_DB);
    }
    
    /**
     * Deleta a pasta temporária após a serialização.
     */
    public void cleanFolders() {
        File directory = new File(TEMP_DB);
        if (directory.exists()) {
            deleteContents(directory);
            directory.delete();
        }
    }
    
    /**
     * Deleta o contéudo de uma pasta, utilizado para limpar as pastas temporárias.
     * @param file 
     */
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
    
    /**
     * Transforma todos os campos serializados em Properties que representa a
     * entidade encapsulada.
     * @return 
     */
    public Properties build() {
        StringBuilder fields = new StringBuilder();
        for(String s : bottledFields) {
            fields.append(s).append("\n");
        }
        
        StringBuilder refs = new StringBuilder();
        for(Ref s : referencias) {
            refs.append(s.toString()).append("::");
        }
        
        props.put("fields", fields.toString());
        props.put("refs", refs.toString());
        props.put("stamp", String.valueOf(System.nanoTime()));
        if(this.cascate) {
            props.put("cascate", "true");
        }
        return props;
    }
    
    /**
     * Realiza as verificações de unicidade e consistência de valores e depois grava
     * todas as entidades no banco de dados.
     * @throws Exception 
     */
    public void flush() throws URISyntaxException, IOException, IllegalAccessException, IllegalArgumentException, EntityIdBadImplementationException, FileNotFoundException, NoSuchAlgorithmException, ObjectNotDesserializebleException, ParseException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, DuplicatedUniqueFieldException, Exception {
        Transaction tx = Transaction.begin(ROOT_DB);

        if (bottledFields.isEmpty()) engarafar();
        if (!todosCamposSaoUnicos()) {
            throw new DuplicatedUniqueFieldException("Unique field violation detected");
        }
        try {
            tx.add(this);
            tx.commit();
        } finally {
            cleanFolders();
        }
    }
    
    /**
     * Carrega o estado atual da entidade atualmente no banco de dados.
     * Usado para comparação entre o estado de memória atual e o estado permanente.
     * @return
     * @throws Exception 
     */
    public Bottle loadOldState() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, URISyntaxException, IOException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        String path = new Writer(ROOT_STAGE).getPath(this.entity);
        
        if(new File(path).exists()) {
            Bottle bottle = new Bottle.BottleBuilder().entityClass(this.entity.getClass()).id(this.entity.getId()).modoOperacional(Bottle.ROOT_STAGE).build();
            return bottle;
        }
        return null;
    }
    
    /**
     * Retorna o caminho para a entidade no diretório temporário.
     * @param entity
     * @return
     * @throws Exception 
     */
    public String getTempPath(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        return getTempPath(entity.getClass(), entity.getId());
    }
    
    public String getRootPath() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        return getRootPath(this.entity.getClass(), this.entity.getId());
    }
    
    /**
     * Retorna o caminho para a entidade dentro do Banco de Dados.
     * @param classe
     * @param id
     * @return
     * @throws Exception 
     */
    public String getRootPath(Class classe, String id) {
        String path = classe.getName().replace(".class", "").replace(".", "/")+"/"+id;
        path = ROOT_DB+path;
        return path;
    }
    
    /**
     * Retorna o caminho para a entidade no diretório temporário.
     * @param classe
     * @param id
     * @return
     * @throws Exception 
     */
    public String getTempPath(Class classe, String id) {
        String path = classe.getName().replace(".class", "").replace(".", "/")+"/"+id;
        path = TEMP_DB+path;
        return path;
    }
    
    /**
     * Retorna o caminho para a entidade de acordo com o modo operacional da capsula.
     * @param entity
     * @return
     * @throws Exception 
     */
    public String getPath(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        return getPath(entity.getClass(), entity.getId());
    }
    
    /**
     * Retorna o caminho para a entidade de acordo com o modo operacional da capsula.
     * @param classe
     * @param id
     * @return
     * @throws Exception 
     */
    public String getPath(Class classe, String id) {
        String path = classe.getName().replace(".class", "").replace(".", "/")+"/"+id;
        if(modoOperacional == 0) {
            path = ROOT_DB+path;
        } else {
            path = TEMP_DB+path;
        }
        return path;
    }
    
    /**
     * Preenche a lista de IDs a serem bloqueados para leitura e gravação.
     * @param toLock
     * @throws Exception 
     */
    private void fillLock(Set<String> toLock) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        fillLock(toLock, this);
    }
    
    /**
     * Preenche a lista de IDs a serem bloqueados para leitura e gravação.
     * @param toLock
     * @param rootBottle
     * @throws Exception 
     */
    private void fillLock(Set<String> toLock, Bottle rootBottle) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        for (Bottle b : rootBottle.bottles.values()) {
            toLock.add(b.entity.getId());
        }
    }
    
    /**
     * Chamado após o "fillLock" para trancar 
     * @param toLock 
     */
    private void lock(Set<String> toLock) {
        for (String s : toLock) {
            IO.io().lockWrite(s);
        }
    }
    
    /**
     * Aplica as alterações no Banco de Dados.
     * @param imgsToDelete
     * @param filesToDelete
     * @throws IOException 
     */
    private void applyChanges(List<File> imgsToDelete, List<File> filesToDelete) throws IOException {
        deleteFilesWithDelete();
        moveDirectory();
    }
    
    /**
     * Grava todas as novas imagens no banco de dados.
     * @param bottle
     * @param imgsToDelete
     * @throws Exception 
     */
    public void flushImgs() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, IOException {
        File tempFolder = new File(TEMP_DB+"imgs/"+this.entity.getId());
        File prodFolder = new File(ROOT_DB+"imgs/"+this.entity.getId());
        if(!this.imgs.isEmpty()) {
            if(!props.containsKey("exclude")) {
                tempFolder.mkdirs();
                prodFolder.mkdirs();

                Set<String> hashsProducao = Set.copyOf(Arrays.asList(prodFolder.list()));

                /*
                Percorre as imagens atuais comparando com a lista de imagens em produção.
                Se na pasta de produção não tiver uma imagem com o mesmo hash, então
                a imagem atual será salva.
                */
                for(String hash : this.imgs.keySet()) {
                    String fname = hash;
                    if(!fname.endsWith(".bak")) {
                        fname = fname+".bak";
                    }
                    if(!hashsProducao.contains(fname)) {
                        File out = new File(tempFolder.getPath()+"/"+hash);
                        ImageIO.write((RenderedImage) this.imgs.get(hash), "png", out);
                    }
                }

                /*
                Percorre a lista de imagens na pasta de produção após gravar as novas
                imagens (se houverem).
                Localiza imagens que existam na produção mas que precisam ser deletadas.
                */
                Set<String> keySet = this.imgs.keySet();
                for(String hash : hashsProducao) {
                    if(keySet.contains(hash.replace(".bak", ""))) {
                        File toback = new File(prodFolder.getPath()+"/"+hash);
                        Files.move(toback.toPath(), new File(prodFolder.getPath()+"/"+hash.replace(".bak", "")).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        File del = new File(prodFolder.getPath()+"/"+hash);
                        File marked = new File(del.getPath()+".del");
                        if(!del.renameTo(marked)) {
                            throw new FileSystemException("Erro ao renomear o arquivo para exclusão");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Grava todos os novos arquivos no banco de dados.
     * @param bottle
     * @param filesToDelete
     * @throws Exception 
     */
    public void flushFiles() throws IOException, NoSuchAlgorithmException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        System.out.println("FLUSHANDO ARQUIVOS: "+this.files.size());
        File tempFolder = new File(TEMP_DB+"files/"+this.entity.getId());
        File prodFolder = new File(ROOT_DB+"files/"+this.entity.getId());
        if(!this.files.isEmpty()) {
            if(!props.containsKey("exclude")) {
                tempFolder.mkdirs();
                prodFolder.mkdirs();

                Set<String> prodNames = Set.copyOf(Arrays.asList(prodFolder.list()));
                
                /*
                Percorre os arquivos atuais comparando com a lista de arquivos em produção.
                Se na pasta de produção não tiver um arquivo com o mesmo hash, então
                o arqiuvo atual será salvo.
                */
                for(String name : this.files.keySet()) {
                    if(!prodNames.contains(name+".bak")) {
                        File out = new File(tempFolder.getPath()+"/"+name);
                        System.out.println("ARQUIVO: "+this.files.get(name));
                        if(!this.files.get(name).exists()) throw new FileNotFoundException(name);
                        Files.copy(this.files.get(name).toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        // Caso exista um arquivo em produção com o mesmo nome, compara os hashs.
                        File antigo = new File(prodFolder.getPath()+"/"+name+".bak");
                        File novo = this.files.get(name);
                        if(!areFilesEquals(antigo, novo)) {
                            Files.copy(novo.toPath(), new File(prodFolder.getPath()+"/"+name).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }

                /*
                Percorre a lista de arquivos na pasta de produção após gravar os novos
                arquivos (se houverem).
                Localiza arquivos que existam na produção mas que precisam ser deletados.
                */
                for(String hash : prodNames) {
                    if(this.files.containsKey(hash.replace(".bak", ""))) {
                        File toback = new File(prodFolder.getPath()+"/"+hash);
                        Files.move(toback.toPath(), new File(prodFolder.getPath()+"/"+hash.replace(".bak", "")).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        
                    }
                    if(!this.files.containsKey(hash.replace(".bak", ""))) {
                        File todel = new File(prodFolder.getPath()+"/"+hash);
                        File marked = new File(todel.getPath().replace(".bak", ".del"));
                        if(!todel.renameTo(marked)) {
                            throw new FileSystemException("Erro ao renomear o arquivo para exclusão");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Move os diretórios da pasta temporária para a pasta de produção.
     * @throws IOException 
     */
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
    
    /**
     * Faz a limpeza dos arquivos marcados para exclusão.
     * @throws IOException 
     */
    public void deleteFilesWithDelete() throws IOException {
        Path sourceDir = Paths.get(this.TEMP_DB);
        Path targetDir = Paths.get(this.ROOT_DB);
        // Walk through the file tree starting from the source directory
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(!file.toString().contains("/imgs/") && !file.toString().contains("/files/")) {
                    // Read all lines of the file
                    Properties p = new Properties();
                    p.load(new FileInputStream(file.toFile()));
                    if(p.containsKey("exclude")) {
                        if(p.get("exclude").equals("true")) {
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
                    }
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }
    
    /**
     * Deleta uma entidade e todas as subentidades cascata.
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws Exception 
     */
    public boolean delete() throws IllegalArgumentException, IllegalAccessException, IOException, EntityIdBadImplementationException, URISyntaxException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, Exception {
        Transaction tx = Transaction.begin(ROOT_DB);
        try {
            tx.add(this);
            tx.commitDel();
            return true;
        } finally {
            cleanFolders();
        }
    }
    
    private void collectCascadingDeletes(Set<Bottle> toDelete) {
        toDelete.add(this);
        for (Bottle bottle : bottles.values()) {
            if (bottle.cascate && !toDelete.contains(bottle)) {
                bottle.collectCascadingDeletes(toDelete);
            }
        }
    }
    
    /**
     * Método secundário utilizado para exlcuir subentidades.
     * @param sub
     * @return 
     */
    public boolean delete(boolean sub) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, URISyntaxException, IOException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Assist.sendToTemp(new Ref(entity), TEMP_DB);

        Properties tester = new Properties();
        tester.load(new FileInputStream(new File(Assist.getPathFromRef(new Ref(entity), TEMP_DB))));
        if (!tester.containsKey("exclude")) {
            List<Ref> toClean = new ArrayList<>();

            String id = entity.getId();
            toClean.addAll(Assist.getEntities(build().get("fields").toString())
                    .stream()
                    .filter(ref -> !ref.getKey().equals(id))
                    .toList());

            toClean.addAll(Arrays.asList(build().getProperty("refs").toString().split("::"))
                    .stream()
                    .filter(str -> !str.isBlank())
                    .map(str -> new Ref(str))
                    .toList());
            
            Ref toRemove = new Ref(entity);
            for (Ref toBeCleaned : toClean) {
                Assist.removeExistence(toRemove, toBeCleaned, TEMP_DB);
            }

            Properties props = new Properties();
            props.put("exclude", "true");
            props.store(new FileOutputStream(new File(getTempPath(entity))), "JhonDBS Entity");

            for (Ref ref : toClean) {
                Properties p = new Properties();
                p.load(new FileInputStream(new File(Assist.getPathFromRef(ref, TEMP_DB))));
                if (!p.containsKey("exclude")) {
                    List<String> refs = Arrays.asList(p.getProperty("refs").toString().split("::"))
                            .stream().filter(r -> !r.isBlank())
                            .toList();
                    if (refs.isEmpty() && p.containsKey("cascate")) {
                        Bottle bottle = new Bottle.BottleBuilder()
                                .entityClass(ClassDictionary.fromIndex(ref.getValue()))
                                .id(ref.getKey())
                                .modoOperacional(Bottle.TEMP_STAGE)
                                .tempDB(TEMP_DB)
                                .build();
                        bottle.delete(true); // Exclusão recursiva
                    }
                }
            }
        }
        return true;
    }
    
    /*
    ************************************************************
    *************   ENCAPSULAMENTO DE ENTIDADE    **************
    ************************************************************
    */
    
    /**
     * Serializa a entidade encapsulando seus campos.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws Exception 
     */
    public void engarafar() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, FileNotFoundException, NoSuchAlgorithmException, ObjectNotDesserializebleException, URISyntaxException, IOException, ParseException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        
        bottledFields.clear();
        List<Field> fields = FieldsManager.getAllSerializebleFields(this.entity.getClass());
        for(Field field : fields) {
            field.setAccessible(true);
            Object valor = field.get(this.entity);
            if(valor != null) {
                if(ClassDictionary.getIndex(valor.getClass()) != -1 || Reflection.isArrayMap(field.getType()) || Reflection.isArrayMap(valor)) {
                    boolean condicional = true;
                    if(valor instanceof File f) {
                        if(!f.exists()) {
                            condicional = false;
                            throw new FileNotFoundException(f.getPath());
                        }
                    }
                    if(condicional) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("{").append(field.getName()).append(":");

                        if(valor.getClass().isEnum()) {
                            sb.append(encapsuleEnum((Enum) valor));
                        } else {
                            if(Reflection.isPrimitive(valor) || Reflection.isNumerical(valor.getClass()) || Reflection.isDate(valor)) {
                                sb.append(encapsulePrimitive(valor));
                            } else if(Reflection.isArrayMap(field.getType()) || Reflection.isArrayMap(valor)) {
                                sb.append(encapsularObjeto(valor, field.isAnnotationPresent(Cascate.class)));
                            } else if(Reflection.isInstance(field.getType(), Entity.class) || Reflection.isInstance(valor.getClass(), Entity.class)) {
                                Entity ente = (Entity) valor;
                                if(!bottles.containsKey(ente.getId())) {
                                    Bottle bottle = new BottleBuilder()
                                            .entity(ente)
                                            .bottles(bottles)
                                            .modoOperacional(modoOperacional)
                                            .rootDB(ROOT_DB)
                                            .tempDB(TEMP_DB)
                                            .build();
                                    
                                    bottle.engarafar();
                                    if(field.isAnnotationPresent(Cascate.class)) {
                                        bottle.props.put("cascate", "true");
                                        bottle.cascate = true;
                                    }
                                }
                                bottles.get(ente.getId()).putRef(this.entity);
                                sb.append(encapsuleId(ente));
                            } else {
                                sb.append(encapsularObjeto(valor, field.isAnnotationPresent(Cascate.class)));
                            }
                        }

                        sb.append("}");
                        this.bottledFields.add(sb.toString());
                    }
                }
            }
        }
    }
    
    public void sendToTemp() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, IOException {
        sendToTemp(this.entity.getClass(), this.entity.getId());
    }
    
    /**
     * Envia uma entidade para a pasta temporária para que sejam realizadas tarefas
     * de serialização.
     * @param entity
     * @throws Exception 
     */
    private void sendToTemp(Class clazz, String id) throws FileNotFoundException, IOException {
        File temp = new File(getTempPath(clazz, id));
        if(!temp.exists()) {
            temp.getParentFile().mkdirs();
            File root = new File(getRootPath(clazz, id));
            Properties p = new Properties();
            p.load(new FileInputStream(root));
            p.store(new FileOutputStream(temp), "JhonDBS Entity");
        }
        
    }
    
    /**
     * Encapsula um objeto.
     * @param objeto Objeto a ser encapsulado.
     * @param cascate Marca se é um objeto ou entidade em cascata.
     * @return String da capsula.
     * @throws Exception 
     */
    public String encapsularObjeto(Object objeto, boolean cascate) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, FileNotFoundException, NoSuchAlgorithmException, ObjectNotDesserializebleException, URISyntaxException, IOException, ParseException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if(objeto.getClass().isEnum()) {
            return encapsuleEnum((Enum) objeto);
        } else {
            if(Reflection.isPrimitive(objeto.getClass()) || Reflection.isNumerical(objeto.getClass()) || Reflection.isDate(objeto.getClass())) {
                return encapsulePrimitive(objeto);
            } else if(Reflection.isArrayMap(objeto)) {
                if(Reflection.isInstance(objeto.getClass(), List.class)) {
                    List l = (List) objeto;
                    if(!l.isEmpty()) {
                        return encapsuleArray(objeto, cascate);
                    } else {
                        return "{list:{}}";
                    }
                } else if(Reflection.isInstance(objeto.getClass(), Set.class)) {
                  Set set = (Set) objeto;
                  if(!set.isEmpty()) {
                      return encapsuleArray(objeto, cascate);
                  } else {
                      return "{list:{}}";
                  }
                } else if(Reflection.isInstance(objeto.getClass(), Map.class)) {
                    Map m = (Map) objeto;
                    if(!m.isEmpty()) {
                        return encapsuleArray(objeto, cascate);
                    } else {
                        return "{map:{}}";
                    }
                } else {
                    ArrayList<Object> list = toArrayList(objeto);
                    if(!list.isEmpty()) {
                        return encapsuleArray(list, cascate);
                    } else {
                        return "{list:{}}";
                    }
                }
            } else if(Reflection.isInstance(objeto.getClass(), Entity.class)) {
                Entity ente = (Entity) objeto;
                if(!bottles.containsKey(ente.getId())) {
                    Assist.createBottle(ente, bottles, modoOperacional, ROOT_DB, TEMP_DB, this.entity, cascate);
                }
                return encapsuleId(ente);
            } else if(objeto instanceof File file) {
                if(file.exists()) {
                    if(!file.getPath().contains(ROOT_DB+"files/")) {
                        if(!this.files.containsKey(file.getName())) {
                            this.files.put(file.getName(), file);
                        }
                    }
                    return "{file:"+file.getName()+"}";
                } else {
                    throw new FileNotFoundException("Arquivo não encontrado: "+file);
                }
            } else if(objeto instanceof Image img) {
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
                        sb.append(encapsularObjeto(get, cascate));
                        sb.append("}");
                    }
                }
                sb.append("}");
                return sb.toString();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> ArrayList<T> toArrayList(Object array) {
        // Validação de entrada
        if (array == null) {
            throw new IllegalArgumentException("O array não pode ser null");
        }
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException("O argumento deve ser um array");
        }

        // Obtém o tipo do componente do array
        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);

        // Cria o ArrayList com o tipo apropriado
        ArrayList<T> result = new ArrayList<>(length);

        // Trata arrays de tipos primitivos e objetos
        if (componentType.isPrimitive()) {
            // Conversão manual para tipos primitivos
            if (componentType == int.class) {
                int[] intArray = (int[]) array;
                for (int i : intArray) {
                    result.add((T) Integer.valueOf(i)); // Boxing manual
                }
            } else if (componentType == double.class) {
                double[] doubleArray = (double[]) array;
                for (double d : doubleArray) {
                    result.add((T) Double.valueOf(d));
                }
            } else if (componentType == boolean.class) {
                boolean[] boolArray = (boolean[]) array;
                for (boolean b : boolArray) {
                    result.add((T) Boolean.valueOf(b));
                }
            } else if (componentType == byte.class) {
                byte[] byteArray = (byte[]) array;
                for (byte b : byteArray) {
                    result.add((T) Byte.valueOf(b));
                }
            } else if (componentType == char.class) {
                char[] charArray = (char[]) array;
                for (char c : charArray) {
                    result.add((T) Character.valueOf(c));
                }
            } else if (componentType == float.class) {
                float[] floatArray = (float[]) array;
                for (float f : floatArray) {
                    result.add((T) Float.valueOf(f));
                }
            } else if (componentType == long.class) {
                long[] longArray = (long[]) array;
                for (long l : longArray) {
                    result.add((T) Long.valueOf(l));
                }
            } else if (componentType == short.class) {
                short[] shortArray = (short[]) array;
                for (short s : shortArray) {
                    result.add((T) Short.valueOf(s));
                }
            }
        } else {
            // Arrays de objetos (String[], Object[], etc.)
            T[] objArray = (T[]) array;
            result.addAll(Arrays.asList(objArray));
        }

        return result;
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
    
    @SuppressWarnings("empty-statement")
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
    
    /**
     * Encapsula listas, sets, mapas e arrays.
     * @param object Objeto a ser encapsulado.
     * @param cascate Se as subentidades terão marca de cascata.
     * @return
     * @throws Exception 
     */
    private String encapsuleArray(Object object, boolean cascate) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, FileNotFoundException, NoSuchAlgorithmException, ObjectNotDesserializebleException, URISyntaxException, IOException, ParseException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if(object == null) {
            return "{}";
        }
        
        int type = 0;
        
        if(Reflection.isInstance(object.getClass(), Set.class)) {
            type = 1;
        } else if(Reflection.isInstance(object.getClass(), Map.class)) {
            type = 2;
        }
        
        StringBuilder sb = new StringBuilder();
        if(type != 2 || object.getClass().getName().contains("[")) {
            sb.append("{")
                .append("list")
                .append(":");
        } else {
            sb.append("{")
                .append("map")
                .append(":");
        }
        
        if(type == 0) {
            List list = (List) object;
            if(!list.isEmpty()) {
                for(Object obj : list) {
                    if(Reflection.isPrimitive(obj) || Reflection.isNumerical(obj.getClass()) || Reflection.isDate(obj.getClass())) {
                        sb.append(encapsulePrimitive(obj));
                    } else if(Reflection.isArrayMap(obj) && obj != object) {
                        sb.append(encapsuleArray(obj, cascate));
                    } else if(Reflection.isInstance(obj.getClass(), Entity.class)) {
                        Entity ente = (Entity) obj;
                        if(!bottles.containsKey(ente.getId())) {
                            Assist.createBottle(ente, bottles, modoOperacional, ROOT_DB, TEMP_DB, this.entity, cascate);
                        }
                        sb.append(encapsuleId(ente));
                    } else {
                        sb.append(encapsularObjeto(obj, cascate));
                    }
                }
            }
        } else if(Reflection.isInstance(object.getClass(), Map.class)) {
            Map map = (Map) object;
            Set keys = map.keySet();
            for(Object key : keys) {
                if(Reflection.isPrimitive(key) || Reflection.isNumerical(key.getClass()) || Reflection.isDate(key.getClass())) {
                    sb.append(encapsulePrimitive(key));
                } else if(Reflection.isArrayMap(key)) {
                    sb.append(encapsuleArray(key, cascate));
                } else if(Reflection.isInstance(key.getClass(), Entity.class)) {
                    Entity ente = (Entity) key;
                    if(!bottles.containsKey(ente.getId())) {
                        Assist.createBottle(ente, bottles, modoOperacional, ROOT_DB, TEMP_DB, this.entity, cascate);
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsularObjeto(key, cascate));
                }
                if(Reflection.isPrimitive(map.get(key)) || Reflection.isNumerical(map.get(key).getClass()) || Reflection.isDate(map.get(key).getClass())) {
                    sb.append(encapsulePrimitive(map.get(key)));
                } else if(Reflection.isArrayMap(map.get(key))) {
                    sb.append(encapsuleArray(map.get(key), cascate));
                } else if(Reflection.isInstance(map.get(key).getClass(), Entity.class)) {
                    Entity ente = (Entity) map.get(key);
                    if(!bottles.containsKey(ente.getId())) {
                        Assist.createBottle(ente, bottles, modoOperacional, ROOT_DB, TEMP_DB, this.entity, cascate);
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsularObjeto(map.get(key), cascate));
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Encapsula o ID de uma entidade, para ser usado como referência.
     * @param entity Entidade a ter o ID extraído.
     * @return Capsula de referência à entidade.
     * @throws Exception 
     */
    private String encapsuleId(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
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
        if(Reflection.isInstance(object.getClass(), List.class)) {
            return (List) object;
        } else if(Reflection.isInstance(object.getClass(), Set.class)) {
            List list = new ArrayList();
            list.addAll((Set) object);
            return list;
        } else {
            try {
                return Arrays.asList(object);
            } catch (Exception e) {
            }
        }
        return new ArrayList();
    }
    
    /*
    ************************************************************
    ***************  RECUPERAÇÃO DE ENTIDADE   *****************
    ************************************************************
    */
    
    private void load(Class clazz, String id, ClassLoader loader) throws IOException, EntityIdBadImplementationException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, ObjectNotDesserializebleException, URISyntaxException, ParseException {
        IO io = IO.io();
        if (!isSub) {
            io.lockRead(id);
        }
        try {
            this.entity = (Entity) Reflection.getNewInstance(clazz, loader);
            List<String> actualFieldsList = FieldsManager.getAllFields(this.entity.getClass())
                    .stream()
                    .map(Field::getName)
                    .toList();

            sendToTemp(clazz, id);

            Properties props = new Properties();
            String path = getPath(clazz, id);
            File file = new File(path);
            if (!file.exists()) {
                throw new FileNotFoundException("Entidade não encontrada: " + path);
            }
            props.load(new FileInputStream(file));

            this.props = props;

            if (props.containsKey("cascate") && "true".equals(props.getProperty("cascate"))) {
                this.cascate = true;
            }

            List<String> fields = new ArrayList<>();
            String fieldStream = props.get("fields").toString();
            if (fieldStream.endsWith("::")) {
                fieldStream = fieldStream.substring(0, fieldStream.length() - "::".length());
            }
            fields.addAll(reader.splitCapsules(fieldStream));

            Field entityIdField = FieldsManager.getFieldId(this.entity.getClass());
            String idName = entityIdField.getName();

            // Usar Iterator para evitar ConcurrentModificationException
            for (Iterator<String> it = fields.iterator(); it.hasNext();) {
                String campo = it.next();
                if (campo.substring(1, campo.indexOf(":")).equals(idName)) {
                    inserir(this.entity, campo, loader);
                    this.bottledFields.add(campo);
                    it.remove();
                    break;
                }
            }

            for (String campo : fields) {
                if (actualFieldsList.contains(campo.substring(1, campo.indexOf(":")))) {
                    inserir(this.entity, campo, loader);
                    this.bottledFields.add(campo);
                }
            }

            String refs = props.get("refs").toString();
            if (!refs.isBlank()) {
                for (String ref : refs.split("::")) {
                    if (!ref.isBlank()) {
                        this.referencias.add(new Ref(ref));
                    }
                }
            }
        } finally {
            if (!isSub) {
                io.unlockRead(id);
                cleanFolders();
            }
        }
    }
    
    private void inserir(Object receptor, String capsule, ClassLoader loader) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, InstantiationException, InvocationTargetException, NoSuchMethodException, URISyntaxException, ParseException, ObjectNotDesserializebleException {
        String nome_campo = reader.getKeyFromCapsule(capsule);
        String sub_capsula = reader.getValueFromCapsule(capsule);
        
        if(sub_capsula != null && !sub_capsula.isBlank()) {
            String indice_classe = reader.getKeyFromCapsule(sub_capsula);
            String conteudo = reader.getValueFromCapsule(sub_capsula);

            Object valor = recuperar(indice_classe, conteudo, loader);
            FieldsManager.setValue(nome_campo, receptor, valor);
        }
    }
    
    /**
     * 
     * @param indice O número de índice da classe ou também o tipo de contéudo,
     * como "list", "map", "img", "file" e possíveis outros futuros.
     * @param conteudo A cápsula de conteúdo.
     * @param loader Classloader para permitir compatibilidade com SpringBoot DevTools.
     * @return Objeto recuperado.
     * @throws Exception 
     */
    private Object recuperar(String indice, String conteudo, ClassLoader loader) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, InstantiationException, InvocationTargetException, NoSuchMethodException, URISyntaxException, ParseException, ObjectNotDesserializebleException {
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
            } else {
                throw new ObjectNotDesserializebleException("Objeto de data não reconhecido.");
            }
        } 
        else if (Reflection.isInstance(classe_do_objeto, Entity.class)) {
            String id = conteudo;
            if(bottles.containsKey(id)) {
                return bottles.get(id).entity;
            } else {
                Bottle bottle = new Bottle.BottleBuilder()
                        .loader(loader)
                        .entityClass(classe_do_objeto)
                        .id(id)
                        .bottles(bottles)
                        .modoOperacional(modoOperacional)
                        .rootDB(ROOT_DB)
                        .tempDB(TEMP_DB)
                        .build();
                return bottle.entity;
            }
        } 
        else if (Reflection.isInstance(classe_do_objeto, List.class) || classe_do_objeto.isAssignableFrom(List.class)) {
            return parseListFromString(conteudo, loader);
        } 
        else if (Reflection.isInstance(classe_do_objeto, Map.class) || classe_do_objeto.isAssignableFrom(Map.class)) {
            return parseMapFromString(conteudo, loader);
        } else if(Reflection.isInstance(classe_do_objeto, Image.class) || classe_do_objeto.isAssignableFrom(Image.class)) {
            return getImg(conteudo, loader);
        } else if(Reflection.isInstance(classe_do_objeto, File.class) || classe_do_objeto.isAssignableFrom(File.class)) {
            return getFile(conteudo);
        } else {
            if (classe_do_objeto.isEnum()) {
                Class<?> forName = Class.forName(classe_do_objeto.getName(), true, loader);
                return Enum.valueOf((Class<Enum>) forName, conteudo);
            } else if(Reflection.isInstance(classe_do_objeto, ColdEntity.class) || classe_do_objeto.isAssignableFrom(ColdEntity.class)) {
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
                List<String> actualFieldsList = FieldsManager.getAllFields(o.getClass())
                    .stream()
                    .map(field -> field.getName())
                    .toList();
                
                List<String> campos = reader.splitCapsules(conteudo);
                for(String campo : campos) {
                    if(actualFieldsList.contains(campo.substring(1, campo.indexOf(":")))) {
                        inserir(o, campo, loader);
                    }
                }
                return o;
            }
        }
    }
    
    public File getFile(String name) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        if(!this.files.containsKey(name)) {
            File file = new File(ROOT_DB+"files/"+this.entity.getId()+"/"+name);
            if(!file.exists()) {
                throw new NullPointerException("Arquivo inexistente: "+file.getPath());
            }
            this.files.put(name, file);
        }
        return this.files.get(name);
    }
    
    public Image getImg(String hash, ClassLoader loader) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
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
    
    public List parseListFromString(String str, ClassLoader loader) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, InstantiationException, InvocationTargetException, NoSuchMethodException, URISyntaxException, ParseException, ObjectNotDesserializebleException {
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
    
    public Map parseMapFromString(String str, ClassLoader loader) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, InstantiationException, InvocationTargetException, NoSuchMethodException, URISyntaxException, ParseException, ObjectNotDesserializebleException {
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
    
    private boolean todosCamposSaoUnicos() throws EntityIdBadImplementationException, DuplicatedUniqueFieldException, IllegalAccessException, IllegalArgumentException, IOException {
        for (Bottle bottle : bottles.values()) {
            if (!uniqueFieldTest(bottle.entity)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean uniqueFieldTest(Entity entity) throws IllegalAccessException, EntityIdBadImplementationException, DuplicatedUniqueFieldException, IllegalArgumentException, IOException {
        return testeDeUnicidade(entity, ROOT_STAGE) && testeDeUnicidade(entity, TEMP_STAGE);
    }
    
    private boolean testeDeUnicidade(Entity entity, int modoOperacional) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, DuplicatedUniqueFieldException, IOException {
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
    
    public static <T extends Entity> List<T> loadAll(Class classe) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, URISyntaxException, IOException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        return loadAll(classe, null, Thread.currentThread().getContextClassLoader());
    }
    
    public static <T extends Entity> List<T> loadAll(Class classe, ClassLoader loader) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, URISyntaxException, IOException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        return loadAll(classe, null, loader);
    }
    
    public static <T extends Entity> List<T> loadAll(Class classe, Filter filter, ClassLoader loader) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, URISyntaxException, IOException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Writer w = new Writer();
        w.initDb();
        Reader reader = new Reader(ROOT_STAGE);
        List<String> ids = reader.listAllIds(classe);
        List<T> list = new ArrayList<>();
        for(String id : ids) {
            Bottle bottle = new Bottle.BottleBuilder().entityClass(classe).id(id).modoOperacional(Bottle.ROOT_STAGE).build();
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
    
    
    /**
     * Classe ainda em construção para analisar se dessa forma o uso da classe Bottle
     * se tornará mais simples e eficiente.
     * O objetico é criar um construtor unificado e inteligente que consiga definir
     * as formas de carregar e encapsular entidades com mais eficiência, evitando
     * a confusão entre os vários tipos de construtores e o possível uso errado de
     * algum deles.
     */
    public static class BottleBuilder {
        private String ROOT_DB = "./db/";
        private String TEMP_DB = "./temp/";
        private int modoOperacional = ROOT_STAGE;
        private Map<String, Bottle> bottles = new HashMap<>();
        private List<String> bottledFields = new ArrayList<>();
        private Set<Ref> referencias = new HashSet<>();
        private Map<String, Image> imgs = new HashMap<>();
        private Map<String, File> files = new HashMap<>();
        private ClassLoader loader = Thread.currentThread().getContextClassLoader();
        private Entity entity;
        private boolean isSub = false;
        private Properties props = new Properties();
        private boolean cascate = false;
        
        private int index = -1;
        private String id = "";
        
        
        public BottleBuilder rootDB(String root) {
            this.ROOT_DB = root;
            return this;
        }
        
        public BottleBuilder tempDB(String temp) {
            this.TEMP_DB = temp;
            return this;
        }
        
        public BottleBuilder modoOperacional(int modo) {
            this.modoOperacional = modo;
            return this;
        }
        
        public BottleBuilder bottles(Map<String, Bottle> bottles) {
            this.bottles = bottles;
            return this;
        }
        
        public BottleBuilder bottleFields(List<String> fields) {
            this.bottledFields = fields;
            return this;
        }
        
        public BottleBuilder references(Set<Ref> referencias) {
            this.referencias = referencias;
            return this;
        }
        
        public BottleBuilder imgs(Map<String, Image> imgs) {
            this.imgs = imgs;
            return this;
        }
        
        public BottleBuilder files(Map<String, File> files) {
            this.files = files;
            return this;
        }
        
        public BottleBuilder loader(ClassLoader loader) {
            this.loader = loader;
            return this;
        }
        
        public BottleBuilder entity(Entity entity) {
            this.entity = entity;
            return this;
        }
        
        public BottleBuilder sub(boolean sub) {
            this.isSub = sub;
            return this;
        }
        
        public BottleBuilder properties(Properties properties) {
            this.props = properties;
            return this;
        }
        
        public BottleBuilder cascate(boolean cascate) {
            this.cascate = cascate;
            return this;
        }
        
        public BottleBuilder index(int index) {
            this.index = index;
            return this;
        }
        
        public BottleBuilder entityClass(Class clazz) {
            this.index = ClassDictionary.getIndex(clazz);
            return this;
        }
        
        public BottleBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public Bottle build() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, URISyntaxException, IOException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
            Bottle bottle = new Bottle();
            bottle.loader = this.loader;
            bottle.entity = this.entity;
            bottle.ROOT_DB = this.ROOT_DB;
            bottle.TEMP_DB = this.TEMP_DB;
            bottle.modoOperacional = this.modoOperacional;
            bottle.bottles = this.bottles;
            bottle.bottledFields = this.bottledFields;
            bottle.referencias = this.referencias;
            bottle.imgs = this.imgs;
            bottle.files = this.files;
            bottle.isSub = this.isSub;
            bottle.props = this.props;
            bottle.cascate = this.cascate;
            bottle.writer = new Writer(modoOperacional, ROOT_DB, TEMP_DB);
            bottle.reader = new Reader(modoOperacional, ROOT_DB, TEMP_DB);
            
            boolean sub = false;
            
            if(this.TEMP_DB.equals("./temp/")) {
                bottle.defineTemp();
//                bottle.initFolders();
            } else {
                sub = true;
            }
            
            if(this.index != -1 && !this.id.isBlank()) {
                bottle.bottles.put(id, bottle);
                bottle.load(ClassDictionary.fromIndex(index), id, loader);
            } else if(this.entity != null) {
                bottle.bottles.put(this.entity.getId(), bottle);
                bottle.loadRefs();
            } else {
                throw new NullPointerException("Entidade não definida");
            }

            if(!sub) {
                bottle.cleanFolders();
            }
            
            return bottle;
        }
        
    }
    
}