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
import br.com.jhondbs.core.tools.Reflection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
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

/**
 * Versão 4
 * @author jhones
 */
public class Bottle {
    
    public static final String ROOT_DB = "./db/";
    public static final String TEMP_DB = "./temp/";
    
    public static final int ROOT_STAGE = 0;
    public static final int TEMP_STAGE = 1;
    
    public int modoOperacional = TEMP_STAGE;
    
    public Writer writer = new Writer();
    public Reader reader = new Reader();
    
    public Map<String, Bottle> bottles = new HashMap<>();
    public List<String> bottledFields = new ArrayList<>();
    public Set<String> referencias = new HashSet<>();
    public ClassLoader loader;
    
    public Entity entity;
    
    public Bottle(Class clazz, String id, int modoOperacional) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles.put(id, this);
        this.loader = this.getClass().getClassLoader();
        load(clazz, id, bottles, loader);
    }
    
    public Bottle(Class clazz, String id, int modoOperacional, ClassLoader loader) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles.put(id, this);
        this.loader = loader;
        load(clazz, id, bottles, loader);
    }
    
    public Bottle(Class clazz, String id, Map<String, Bottle> bottles, int modoOperacional) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles = bottles;
        this.bottles.put(id, this);
        this.loader = this.getClass().getClassLoader();
        load(clazz, id, bottles, loader);
    }
    
    public Bottle(Class clazz, String id, Map<String, Bottle> bottles, ClassLoader loader, int modoOperacional) throws Exception {
        this.modoOperacional = modoOperacional;
        this.bottles = bottles;
        this.bottles.put(id, this);
        this.loader = loader;
        load(clazz, id, bottles, loader);
    }
    
    public Bottle(Entity entity) throws Exception {
        this.entity = entity;
        this.loader = this.getClass().getClassLoader();
        this.modoOperacional = ROOT_STAGE;
        this.bottles.put(entity.getId(), this);
        loadRefs();
    }
    
    public Bottle(Entity entity, int modoOperacional) throws Exception {
        this.entity = entity;
        this.loader = this.getClass().getClassLoader();
        this.modoOperacional = modoOperacional;
        this.bottles.put(entity.getId(), this);
        loadRefs();
    }
    
    public Bottle(Entity entity, Map<String, Bottle> bottles, int modoOperacional) throws Exception {
        this.entity = entity;
        this.bottles = bottles;
        this.loader = this.getClass().getClassLoader();
        this.modoOperacional = modoOperacional;
        this.bottles.put(entity.getId(), this);
        loadRefs();
    }
    
    public Bottle(Entity entity, Map<String, Bottle> bottles, ClassLoader loader, int modoOperacional) throws Exception {
        this.entity = entity;
        this.bottles = bottles;
        this.loader = loader;
        this.modoOperacional = modoOperacional;
        this.bottles.put(entity.getId(), this);
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
        writer.initDb();
        if (bottledFields.isEmpty()) {
            engarafar();
        }
        
        Set<String> toLock = new HashSet<>();
        
        try {
            if (todosCamposSaoUnicos()) {
                
                for(Bottle b : bottles.values()) {
                    toLock.add(b.entity.getId());
                }
                
                Bottle b2 = null;
                try {
                    b2 = new Bottle(entity.getClass(), entity.getId(), ROOT_STAGE);
                    for(Bottle b : b2.bottles.values()) {
                        toLock.add(b.entity.getId());
                    }
                } catch (Exception e) {
                }
                
                for(String s : toLock) {
                    IO.io().lockWrite(s);
                }
                
                List<Entity> excludeds = reader.listExcludeds(this);
                removeReferences(excludeds);
                
                for (Bottle bottle : bottles.values()) {
                    writer.write(bottle);
                }
                
                handleOrphanEntities(excludeds);
                
                try {
                    deleteFilesEndingWithDelete();
                    moveDirectory();
                } catch (Exception e) {
                    
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for(String s : toLock) {
                IO.io().unlockWrite(s);
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
                Reader r = new Reader(TEMP_STAGE);
                Bottle bot = new Bottle(entity.getClass(), entity.getId(), Bottle.ROOT_STAGE);
                List<Entity> oldcascs = r.listCascateEntities(bot.entity);
                List<Entity> todelete = new ArrayList<>();
                for(Entity e : oldcascs) {
                    if(excludedIds.contains(e.getId())) {
                        todelete.add(e);
                    }
                }
                for(Entity e : todelete) {
                    if(isOrphan(e)) {
                        e.delete();
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
            Reader r = new Reader(TEMP_STAGE);
            Writer ww = new Writer(TEMP_STAGE);
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
    
    public static void moveDirectory() throws IOException {
        Path sourceDir = Paths.get("./temp");
        Path targetDir = Paths.get("./db");
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
    
    public static void deleteFilesEndingWithDelete() throws IOException {
        Path sourceDir = Paths.get("./temp");
        Path targetDir = Paths.get("./db");
        // Walk through the file tree starting from the source directory
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Read all lines of the file
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                
                // Check if the last line ends with "DELETE"
                if (!lines.isEmpty() && lines.get(lines.size() - 1).endsWith("DELETE")) {
                    // Resolve the corresponding file in the target directory (db)
                    Path targetFile = targetDir.resolve(sourceDir.relativize(file));
                    
                    // Delete the file in the target directory if it exists
                    if (Files.exists(targetFile)) {
                        Files.delete(targetFile);
//                        System.out.println("Deleted from db: " + targetFile);
                    }

                    // Delete the file in the source directory (temp)
                    Files.delete(file);
//                    System.out.println("Deleted from temp: " + file);
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    public boolean isOrphan(Entity entity) throws Exception {
        Reader r = new Reader(TEMP_STAGE);
        r.sendToTemp(entity);
        List<String> refs = r.spliteredReferences(entity);
        return refs.isEmpty();
    }
    
    public boolean delete() throws IllegalArgumentException, IllegalAccessException, Exception {
        Reader r = new Reader(TEMP_STAGE);
        Writer w = new Writer(TEMP_STAGE);
        Bottle b = new Bottle(entity);
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
                                Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional);
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
    
    public String encapsularObjeto(Object objeto) throws Exception {
        if(objeto.getClass().isEnum()) {
            return encapsuleEnum((Enum) objeto);
        } else {
            if(Reflection.isPrimitive(objeto.getClass()) || Reflection.isNumerical(objeto.getClass()) || Reflection.isDate(objeto.getClass())) {
                return encapsulePrimitive(objeto);
            } else if(Reflection.isArrayMap(objeto.getClass())) {
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
                    Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional);
                    bottle.engarafar();
                    bottle.putRef(this.entity);
                }
                return encapsuleId(ente);
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
                        Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional);
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
                        Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional);
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
                        Bottle bottle = new Bottle(ente, bottles, loader, modoOperacional);
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
    
    private void load(Class clazz, String id, Map<String, Bottle> bottles, ClassLoader loader) throws Exception {
        IO.io().lockRead(id);
        writer.modoOperacional = this.modoOperacional;
        reader.modoOperacional = this.modoOperacional;
        try {
            this.entity = (Entity) Reflection.getNewInstance(clazz, loader);
            String content = reader.readContent(clazz, id);
            List<String> fields = reader.splitCapsules(reader.getValueFromCapsule(content));
            for(String campo : fields) {
                inserir(this.entity, campo, loader);
                this.bottledFields.add(campo);
            }
            loadRefs();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException ex) {
            Logger.getLogger(Bottle.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Erro ao ler a entidade: "+clazz+" -> "+id);
        } finally {
            IO.io().unlockRead(id);
        }
    }
    
    private void inserir(Object receptor, String capsule, ClassLoader loader) throws Exception {
        String nome_campo = reader.getKeyFromCapsule(capsule);
        String sub_capsula = reader.getValueFromCapsule(capsule);
        
        String indice_classe = reader.getKeyFromCapsule(sub_capsula);
        String conteudo = reader.getValueFromCapsule(sub_capsula);
        
        Object valor = recuperar(indice_classe, conteudo, loader);
        FieldsManager.setValue(nome_campo, receptor, valor);
    }
    
    private Object recuperar(String indice, String conteudo, ClassLoader loader) throws Exception {
        Class classe_do_objeto = null;
        
        classe_do_objeto = switch (indice) {
            case "list" -> List.class;
            case "map" -> Map.class;
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
                Bottle bottle = new Bottle(classe_do_objeto, id, bottles, loader, modoOperacional);
                return bottle.entity;
            }
        } 
        else if (Reflection.isInstance(classe_do_objeto, List.class)) {
            return parseListFromString(conteudo, loader);
        } 
        else if (Reflection.isInstance(classe_do_objeto, Map.class)) {
            return parseMapFromString(conteudo, loader);
        } 
        else {
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
    
    public List parseListFromString(String str, ClassLoader loader) throws Exception {
        List list = new ArrayList();
        List<String> objetos = reader.splitCapsules(str);
        for(String objeto : objetos) {
            if(!objeto.equals("{}")) {
                String indice_da_classe = reader.getKeyFromCapsule(objeto);
                Object obj = recuperar(indice_da_classe, objeto, loader);
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

        Reader reader = new Reader();
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
