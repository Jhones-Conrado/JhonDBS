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

import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.errors.ObjectNotDesserializebleException;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.tools.Reflection;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Responsável por ordenar e garantir o processo de carregamento de entidades de
 * forma segura.
 * @author jhones
 */
public class Loader {
    
    private final BlockingIdList blockeds = new BlockingIdList();
    
    private List<String> blocked_ids = new ArrayList<>();
    private Bottle rootBottle;

    public Loader(Bottle bottle) {
        this.rootBottle = bottle;
    }
    
    public Entity load(Class clazz, String id) throws Exception {
        try {
            this.blocked_ids.add(id);
            if(!blocked_ids.contains(id)) blockeds.add(id);
            Entity entity = (Entity) Reflection.getNewInstance(clazz, Thread.currentThread().getContextClassLoader());
            setId(entity, id);
            rootBottle.entity = entity;
            
            Map<String, Field> mapFields = new HashMap<>();
            FieldsManager.getAllFields(clazz).forEach(f -> mapFields.putIfAbsent(f.getName(), f));

            Properties props = new Properties();
            String path = Assist.getPath(new Ref(clazz, id), rootBottle);
            File file = new File(path);
            if (!file.exists()) {
                throw new FileNotFoundException("Entidade não encontrada: " + path + " | No carregamento da entidade: "+entity+" -> "+id);
            }
            props.load(new FileInputStream(file));

            
            if(props.containsKey("cascate")) {
                rootBottle.cascate = true;
            }

            fillRefs(props);
            fillFields(entity, props.getProperty("fields"));

            return entity;
        } finally {
            blockeds.removeAll(blocked_ids.toArray(new String[0]));
        }
    }
    
    private void setId(Entity entity, String id) throws Exception {
        List<Field> ids = FieldsManager.getAllFields(entity).parallelStream()
                .filter(field -> (field.getType().getName().contains("String")))
                .filter(field -> (field.getName().toUpperCase().contains("ID")))
                .toList();
        if(!ids.isEmpty()){
            Field selected = null;
            List<Field> toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("enteid")))
                    .toList();
            if(toList.size() == 1){
                selected = toList.get(0);
            } else {
                toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("id")))
                    .toList();
                if(toList.size() == 1){
                    selected = toList.get(0);
                } else {
                    selected = ids.get(0);
                }
            }
            if(selected != null){
                FieldsManager.setValue(selected.getName(), entity, id);
            }
        } else {
            throw new EntityIdBadImplementationException("The entity does not have a String type variable for the ID.");
        }
    }
    
    private void fillFields(Object object, String capsules) throws Exception {
        List<Field> fields = FieldsManager.getAllFields(object);
        List<String> fieldsNames = fields.stream().map(field -> field.getName())
                .toList();
        Map<String, String> map = Reader.splitFieldsAsMap(capsules);
        
        for(String field : map.keySet()) {
            if(fieldsNames.contains(field)) {
                inject(object, field, map.get(field));
            }
        }
        
    }
    
    private void fillRefs(Properties properties) {
        String refsStr = properties.getProperty("refs");
        Set<Ref> refs = new HashSet<>();
        refs.addAll(Arrays.asList(refsStr.split("::"))
                .stream().filter(ref -> !ref.isBlank())
                .map(str -> new Ref(str))
                .toList());
        rootBottle.referencias = refs;
    }
    
    private void inject(Object object, String field, String capsule) throws Exception {
        String indice_classe = Reader.getKeyFromCapsule(capsule);
        String conteudo = Reader.getValueFromCapsule(capsule);
        Object valor = recover(indice_classe, conteudo);
        if(valor != null) {
            FieldsManager.setValue(field, object, valor);
        }
    }
    
    private Object recover(String indice, String conteudo) throws Exception {
        if(conteudo == null || conteudo.isBlank()) return null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class classe_do_objeto = null;
        classe_do_objeto = switch (indice) {
            case "list" -> List.class;
            case "map" -> Map.class;
            case "img" -> Image.class;
            case "file" -> File.class;
            default -> ClassDictionary.fromIndex(Integer.parseInt(indice));
        };
        
        if (Reflection.isPrimitive(classe_do_objeto) || Reflection.isNumerical(classe_do_objeto)) {
            return Reader.parsePrimitiveFromString(conteudo, classe_do_objeto, loader);
        } else if (Reflection.isDate(classe_do_objeto)) {
            if (Reflection.isInstance(classe_do_objeto, Date.class)) {
                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                Class<?> formatterClass = Class.forName(formatter.getClass().getName(), true, loader);
                Constructor<?> constructor = formatterClass.getConstructor(String.class, Locale.class);
                SimpleDateFormat form = (SimpleDateFormat) constructor.newInstance("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                return form.parse(conteudo);
            } else if (Reflection.isInstance(classe_do_objeto, Calendar.class)) {
                return Reader.parseCalendarFromString(conteudo, loader);
            } else if (Reflection.isInstance(classe_do_objeto, Temporal.class)) {
                return Reader.parseDateTimeFromString(conteudo, classe_do_objeto, loader);
            } else if (Reflection.isInstance(classe_do_objeto, Period.class)) {
                return Reader.parsePeriodFromString(conteudo, loader);
            } else {
                throw new ObjectNotDesserializebleException("Objeto de data não reconhecido.");
            }
        } else if (Reflection.isInstance(classe_do_objeto, Entity.class)) {
            String id = conteudo;
            if(rootBottle.bottles.containsKey(id)) {
                return rootBottle.bottles.get(id).entity;
            } else {
                Bottle bottle;
                if(rootBottle.modoOperacional == Bottle.TEMP_STAGE) {
                    bottle = new Bottle.BottleBuilder()
                            .entityClass(classe_do_objeto)
                            .id(id)
                            .bottles(rootBottle.bottles)
                            .tempDB(rootBottle.TEMP_DB)
                            .build();
                } else {
                    bottle = new Bottle.BottleBuilder()
                            .entityClass(classe_do_objeto)
                            .id(id)
                            .bottles(rootBottle.bottles)
                            .tempDB(rootBottle.TEMP_DB)
                            .modoOperacional(Bottle.ROOT_STAGE)
                            .build();
                }
                return bottle.entity;
            }
        } else if(Reflection.isArrayMap(classe_do_objeto)) {
            if (Reflection.isInstance(classe_do_objeto, List.class) || classe_do_objeto.isAssignableFrom(List.class)) {
                return parseListFromString(conteudo, loader);
            } else if (Reflection.isInstance(classe_do_objeto, Map.class) || classe_do_objeto.isAssignableFrom(Map.class)) {
                return parseMapFromString(conteudo, loader);
            } else {
                throw new Exception("Tipo de array não suportado: "+classe_do_objeto);
            }
        } else if(Reflection.isInstance(classe_do_objeto, Image.class) || classe_do_objeto.isAssignableFrom(Image.class)) {
            return getImg(conteudo, loader);
        } else if(Reflection.isInstance(classe_do_objeto, File.class) || classe_do_objeto.isAssignableFrom(File.class)) {
            return getFile(conteudo);
        } else if (classe_do_objeto.isEnum()) {
            Class<?> forName = Class.forName(classe_do_objeto.getName(), true, loader);
            return Enum.valueOf((Class<Enum>) forName, conteudo);
        } else {
            Object ins = Reflection.getNewInstance(classe_do_objeto);
            fillFields(ins, conteudo);
            return ins;
        }
    }
    
    public List parseListFromString(String str, ClassLoader loader) throws Exception {
        List list = new ArrayList();
        List<String> objetos = Reader.splitCapsules(str);
        for(String objeto : objetos) {
            if(!objeto.equals("{}")) {
                list.add(recover(Reader.getKeyFromCapsule(objeto), Reader.getValueFromCapsule(objeto)));
            }
        }
        return list;
    }
    
    public Map parseMapFromString(String str, ClassLoader loader) throws Exception {
        Map<Object, Object> map = new HashMap<>();
        List<String> objetos = Reader.splitCapsules(str);
        if (objetos == null || objetos.isEmpty()) {
            return map;
        }
        if(objetos.size() >= 2) {
            for (int i = 0; i < objetos.size() - 1; i += 2) {
                String CapsulaChave = objetos.get(i);
                String CapsulaValor = objetos.get(i+1);
                Object objeto_chave = recover(Reader.getKeyFromCapsule(CapsulaChave), Reader.getValueFromCapsule(CapsulaChave));
                Object objeto_valor = recover(Reader.getKeyFromCapsule(CapsulaValor), Reader.getValueFromCapsule(CapsulaValor));
                map.put(objeto_chave, objeto_valor);
            }
        }
        return map;
    }
    
    public File getFile(String name) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        if(!rootBottle.files.containsKey(name)) {
            File file = new File(rootBottle.ROOT_DB+"files/"+rootBottle.entity.getId()+"/"+name);
            if(!file.exists()) {
                file = new File(file.getPath()+".bak");
                if(!file.exists()) {
                    throw new NullPointerException("Arquivo inexistente: "+file.getPath());
                }
            }
            rootBottle.files.put(name, file);
        }
        return rootBottle.files.get(name);
    }
    
    public Image getImg(String hash, ClassLoader loader) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        if(!rootBottle.imgs.containsKey(hash)) {
            File imageFile = new File(rootBottle.ROOT_DB+"imgs/"+rootBottle.entity.getId()+"/"+hash);
            if (!imageFile.exists()) {
                imageFile = new File(imageFile.getPath()+".bak");
                if(!imageFile.exists()) {
                    throw new IllegalArgumentException("Imagem não encontrada no caminho: " + imageFile.getPath());
                }
            }
            BufferedImage readed = ImageIO.read(imageFile);
            rootBottle.imgs.put(hash, readed);
        }
        return rootBottle.imgs.get(hash);
    }
    
}
