/*
 * The MIT License
 *
 * Copyright 2024 Jhones Sales.
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

import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.db.errors.DuplicatedUniqueFieldException;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.tools.Reflection;
import br.com.jhondbs.core.tools.ClassDictionary;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import br.com.jhondbs.core.db.filter.FilterCondition;

/**
 * Responsável por transformar um objeto em texto para armazenar no banco de dados e vice-versa.
 * @author Jhones Conrado
 */
public class Capsule {
    
    /**
     * Diretório raiz do banco de dados.
     */
    private static final String rootDirectory = "./db/";
    
    /**
     * Armazena os objetos já serializados para evitar redundâncias.
     */
    private Set<Entity> serializados = new HashSet<>();
    
    /**
     * Armazena os dados que serão salvos no final.
     */
    private Map<Entity, String> capsules = new HashMap<>();
    
    /**
     * Armazena as entidades recuperadas.
     */
    private final Map<String, Entity> recovereds = new HashMap<>();
    
    /**
     * Entidade que será serializada.
     */
    private Entity entity;
    
    /**
     * Entidade textualizada.
     */
    private String str;
    
    /**
     * Utilizado para serializar uma entidade.
     * @param entity Entidade a ser serializada.
     */
    public Capsule(Entity entity) {
        this.entity = entity;
    }
    
    /**
     * Recupera uma entidade do banco de dados a partir de do ID.
     * @param entityClass Tipo de entidade a ser buscado.
     * @param id ID a ser buscado.
     * @throws Exception 
     */
    public Capsule(Class entityClass, String id) throws Exception {
        this.entity = load(entityClass, id);
    }
    
    /**
     * Utilizado para serializar sub-entidades, evitando a repetição.
     * @param entity
     * @param serializados 
     */
    private Capsule(Entity entity, Set<Entity> serializados, Map<Entity, String> capsules) {
        this.entity = entity;
        this.serializados = serializados;
        this.capsules = capsules;
    }
    
    /**
     * Utilizado para desserializar uma entidade.
     * @param str Texto a ser desserializado.
     */
    public Capsule(String str) {
        this.str = str;
    }
    
    /**
     * Inicia o processo de serialização.
     * @return 
     * @throws java.io.IOException
     */
    public String start() throws IOException, Exception {
        if(this.entity == null) {
            throw new NullPointerException("Entidade nula para serialização.");
        }
        this.entity.getId();
        serializados.add(entity);
        Encapsule(entity);
        capsules.put(entity, str);
        return entity.getId();
    }
    
    /**
     * Grava as entidades no banco de dados de forma atômica. Ou seja, qualquer erro
     * durante a gravação dos novos dados irá anular todas as novas alterações e será
     * preservado o estado atual das entidades.
     * @return
     * @throws DuplicatedUniqueFieldException
     * @throws Exception 
     */
    public boolean flush() throws DuplicatedUniqueFieldException, Exception {
        initDb();
        
        System.out.println("Inicializando a gravação no banco de dados: "+capsules.size());
        
        Set<Entity> keys = capsules.keySet();
        
        /*
        Grava o estado novo das entidades.
        */
        for(Entity entity : keys) {
            printLine();
            System.out.println("Iniciando gravação: "+getPath(entity));
            if(capsules.get(entity).isBlank()) {
                for(Entity e : keys) {
                    clearInDB(e);
                }
                throw new NullPointerException("Entidade com capsula vazia -> "+entity.getClass().getName()+" -> "+entity.getId());
            }
            try {
                try {
                    if (uniqueFieldTest(entity)) {
                        String path = getNewPath(entity);
                        try {
                            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
                                writer.write(capsules.get(entity));
                                writer.flush();
                            }
                            System.out.println("Gravado: " + path);
                        } catch (IOException ex) {
                            for (Entity e : keys) {
                                clearInDB(e);
                            }
                            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                            throw new IOException(ex);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            } catch (Exception ex) {
                for(Entity e : keys) {
                    clearInDB(e);
                }
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                throw new DuplicatedUniqueFieldException(ex.getMessage());
            }
        }
        printLine();
        System.out.println("Gravadas todas as novas entidades");
        
        /*
        Renomeia as entidades de produção para estado velho.
        */
        System.out.println("Criando backup do estado atual de entidades.");
        boolean match = keys.stream().allMatch(e -> {
            try {
                File file = new File(getPath(e));
                if(file.exists()) {
                    return renameToOld(e);
                } else {
                    return true;
                }
            } catch (Exception ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        });
        
        if(match) {
            System.out.println("Backup de entidades criado");
            
            /*
            Reonmeia todas as novas entidades para o estado de produção.
            */
            System.out.println("Aplicando novas entidades para produção.");
            match = keys.stream().allMatch(e -> {
                try {
                    System.out.println("Aplicando: -> "+e.getId());
                    return renameFromNew(e);
                } catch (Exception ex) {
                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            });
            
            if(!match) {
                System.out.println("Erro ao por novas entidades para produção, retornando ao estado antigo.");
                for(Entity e : keys) {
                    try {
                        renameToNew(e);
                        renameFromOld(e);
                        clearInDB(e);
                    } catch (Exception ex) {
                        return false;
                    }
                }
            } else {
                System.out.println("Todas as entidades aplicadas em produção.");
                return true;
            }
        } else {
            System.out.println("Erro ao criar backup do estado atual, retornando ao estado antigo.");
            for(Entity e : keys) {
                try {
                    renameFromOld(e);
                    clearInDB(e);
                } catch (Exception ex) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Faz o encapsulamento da entidade e marca as próximas subentidades para a
     * próxima geração de serialização.
     * @param entity Entidade a ser serializada.
     */
    private String Encapsule(Entity entity) throws IOException, Exception {
        if(!uniqueFieldTest(entity)) {
            throw new DuplicatedUniqueFieldException("Entidade com campo único duplicado: "+entity);
        } else {
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(entity.getClass())))
                .append(":");
        
        List<Field> fields = FieldsManager.getAllFields(entity.getClass());
        for(Field field : fields) {
            if(!Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())){
                field.setAccessible(true);
                Object value = FieldsManager.getValue(field, entity);
                if(value != null) {
                    sb.append("{")
                            .append(field.getName())
                            .append(":");
                    
                    if(Reflection.isPrimitive(field.getType()) || Reflection.isNumerical(field.getType()) || Reflection.isDate(field.getType())) {
                        sb.append(encapsulePrimitive(value));
                    } else if(Reflection.isArrayMap(field.getType())) {
                        sb.append(encapsuleArray(value));
                    } else if(Reflection.isInstance(field.getType(), Entity.class)) {
                        Entity ente = (Entity) value;
                        if(!serializados.contains(ente)) {
                            Capsule capsule = new Capsule(ente, serializados, capsules);
                            try {
                                capsule.start();
                            } catch (Exception ex) {
                                throw ex;
                            }
                        }
                        sb.append(encapsuleId(ente));
                    } else {
                        sb.append(encapsuleObject(value));
                    }

                    sb.append("}");
                }
            }
        }
        
        sb.append("}");
        str = sb.toString();
        return entity.getId();
    }
    
    /**
     * Encapsula um objeto primitivo, que pode ser texto, número ou boleano.
     * @param object
     * @return 
     */
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
     * Faz o encapsulamento de um objeto geral.
     * @param object Objeto geral que não seja uma entidade.
     * @return String do objeto encapsulado.
     */
    private String encapsuleObject(Object object) throws Exception {
        if(object == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(String.valueOf(ClassDictionary.getIndex(object.getClass())))
                .append(":");
        
        List<Field> fields = FieldsManager.getAllFields(object.getClass());
        for(Field field : fields) {
            if(!Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())){
                field.setAccessible(true);
                Object value = FieldsManager.getValue(field, object);
                
                if(value != null) {
                    sb.append("{")
                            .append(field.getName())
                            .append(":");

                    if(Reflection.isPrimitive(field.getType()) || Reflection.isNumerical(field.getType()) || Reflection.isDate(field.getType())) {
                        sb.append(encapsulePrimitive(value));
                    } else if(Reflection.isArrayMap(field.getType())) {
                        sb.append(encapsuleArray(value));
                    } else if(Reflection.isInstance(field.getType(), Entity.class)) {
                        Entity ente = (Entity) value;
                        if(!serializados.contains(ente)) {
                            Capsule capsule = new Capsule(ente, serializados, capsules);
                            capsule.start();
                        }
                        sb.append(encapsuleId(ente));
                    } else {
                        sb.append(encapsuleObject(value));
                    }

                    sb.append("}");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Encapsula um array que pode ser do tipo lista ou mapa.
     * @param object Lista ou mapa para encapsular.
     * @return String da lista ou mapa encapsulado.
     */
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
                    if(!serializados.contains(ente)) {
                        Capsule capsule = new Capsule(ente, serializados, capsules);
                        capsule.start();
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsuleObject(obj));
                }
            }
        } else if(Reflection.isInstance(object.getClass(), Map.class)) {
            Map map = (Map) object;
            Set keys = map.keySet();

            for(Object key : keys) {
                
                /*
                Adiciona o valor da chave, podendo ser um primitivo ou um objeto completo.
                */
                
                if(Reflection.isPrimitive(key) || Reflection.isNumerical(key.getClass()) || Reflection.isDate(key.getClass())) {
                    sb.append(encapsulePrimitive(key));
                } else if(Reflection.isArrayMap(key)) {
                    sb.append(encapsuleArray(key));
                } else if(Reflection.isInstance(key.getClass(), Entity.class)) {
                    Entity ente = (Entity) key;
                    if(!serializados.contains(ente)) {
                        Capsule capsule = new Capsule(ente, serializados, capsules);
                        capsule.start();
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsuleObject(key));
                }
                
                /*
                Adiciona o valor serializado do objeto na posição atual do mapa.
                */
                
                if(Reflection.isPrimitive(map.get(key)) || Reflection.isNumerical(map.get(key).getClass()) || Reflection.isDate(map.get(key).getClass())) {
                    sb.append(encapsulePrimitive(map.get(key)));
                } else if(Reflection.isArrayMap(map.get(key))) {
                    sb.append(encapsuleArray(map.get(key)));
                } else if(Reflection.isInstance(map.get(key).getClass(), Entity.class)) {
                    Entity ente = (Entity) map.get(key);
                    if(!serializados.contains(ente)) {
                        Capsule capsule = new Capsule(ente, serializados, capsules);
                        capsule.start();
                    }
                    sb.append(encapsuleId(ente));
                } else {
                    sb.append(encapsuleObject(map.get(key)));
                }
            }
            
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Encapsula uma entidade no formato de ID para ser utilizado em um campo.
     * @param entity A ter seu ID encapsulado.
     * @return ID encapsulado.
     * @throws Exception 
     */
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
    
    public String getCapsule() {
        return this.str;
    }
    
    /**
     * Converte um objeto em uma instância de lista;
     * @param object
     * @return 
     */
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
    DESSERIALIZAÇÃO
    */
    
    /**
     * Recupera a entidade encapsulada.
     * @param <T>
     * @return 
     * @throws java.lang.InstantiationException 
     * @throws java.lang.IllegalAccessException 
     * @throws java.lang.reflect.InvocationTargetException 
     * @throws java.lang.ClassNotFoundException 
     * @throws java.lang.NoSuchMethodException 
     * @throws java.text.ParseException 
     */
    public <T extends Entity> T recover() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ParseException, Exception {
        if(this.entity != null) {
            return (T) this.entity;
        }
        return (T) recover(str, recovereds);
    }
    
    private <T extends Object> T recover(String str, Map<String, Entity> recovereds) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, Exception {
        Entity root = Reflection.getNewInstance(getType(str));
        List<String> fields = getFields(str);
        
        Field fieldId = FieldsManager.getFieldId(root.getClass());
        for(String field : fields) {
            if(recoverFieldName(field).contains(fieldId.getName())) {
                injectField(root, field, recovereds);
                fields.remove(field);
                recovereds.put(root.getId(), root);
                break;
            }
        }
        
        for(String field : fields) {
            injectField(root, field, recovereds);
        }
        return (T) root;
    }
    
    /**
     * Recupera apenas um mapa com os nomes e valores dos campos em formato de String.
     * @param entityClass
     * @param id
     * @return
     * @throws Exception 
     */
    private Map<String, Object> recoverOnlyFields(Class entityClass, String id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String capsule = Files.readString(Paths.get(getPath(entityClass, id)));
        List<String> capsules = getFields(capsule);
        for(String field : capsules) {
            map.put(recoverFieldName(field), recoverValueCapsule(recoverValueCapsule(field)));
        }
        return map;
    }
    
    /**
     * Utilizado para recuperar um mapa contendo apenas os campos únicos de uma entidade.
     * @param entityClass Classe da entidade.
     * @param id Id da entidade.
     * @return  Mapa contendo o nome do campo como chave e o valor do campo no formato String.
     * @throws Exception 
     */
    private Map<String, String> recoverOnlyUniqueFields(Class entityClass, String id) throws Exception {
        Map<String, String> map = new HashMap<>();
        String capsule = Files.readString(Paths.get(getPath(entityClass, id)));
        List<String> fieldsCapsules = getFields(capsule);
        List<Field> fields = FieldsManager.getFieldsUnique(FieldsManager.getAllFields(entityClass));
        
        List<String> achados = new ArrayList<>();
        for(Field field : fields) {
            for(String fieldCapsule : fieldsCapsules) {
                if(field.getName().equals(recoverFieldName(fieldCapsule))) {
                    achados.add(fieldCapsule);
                }
            }
        }
        
        for(String fieldCapsule : achados) {
            map.put(recoverFieldName(fieldCapsule), recoverValueCapsule(recoverValueCapsule(fieldCapsule)));
        }
        return map;
    }
    
    private void injectField(Object object, String fieldCapsule, Map<String, Entity> recovereds) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ParseException, Exception {
        /*
        Nome do campo de variável.
        */
        String name = recoverFieldName(fieldCapsule);
        
        /*
        String capsula do valor da variável.
        */
        String valueCapsule = recoverValueCapsule(fieldCapsule);
        
        /*
        Valor da capsula, pronto para ser inserido no construtor.
        */
        String value = recoverValueCapsule(valueCapsule);
        
        /*
        Classe do tipo de objeto que pertence a variável.
        */
        Class type = getType(valueCapsule);
        
        FieldsManager.setValue(name, object, recoverFromCapsule(valueCapsule, recovereds));
    }
    
    
    private <T extends Object> T recoverFromCapsule(String capsule, Map<String, Entity> recovereds) throws Exception {
        Class type = getType(capsule);
        /*
        String capsula do valor da variável.
        */
        String value = recoverValueCapsule(capsule);
        
        if(Reflection.isPrimitive(type) || Reflection.isNumerical(type)){
            return (T) parsePrimitiveFromString(value, type);
        } else if(Reflection.isDate(type)) {
            if(Reflection.isInstance(type, Date.class)) {
                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                return (T) formatter.parse(value);
            } else if(Reflection.isInstance(type, Calendar.class)) {
                return (T) parseCalendarFromString(value);
            } else if(Reflection.isInstance(type, Temporal.class)) {
                return (T) parseDateTimeFromString(value, type);
            } else if(Reflection.isInstance(type, Period.class)) {
                return (T) parsePeriodFromString(value);
            }
        } else if(Reflection.isInstance(type, Entity.class)) {
            if(recovereds.containsKey(value)) {
                return (T) recovereds.get(value);
            } else {
                String path = getPath(type, value);
                return (T) recover(Files.readString(Paths.get(path)), recovereds);
            }
        } else if(Reflection.isInstance(type, List.class)) {
            return (T) parseListFromString(capsule, recovereds);
        } else if(Reflection.isInstance(type, Map.class)) {
            return (T) parseMapFromString(value, recovereds);
        } else {
            Object o = Reflection.getNewInstance(type);
            List<String> fields = getFields(capsule);
            for(String field : fields) {
                injectField(o, field, recovereds);
            }
            return (T) o;
        }
    throw new Exception("Capsula não identificada.");
    }
    
    /**
     * Recebe a capsula de um Campo e retorna qual o nome da variável encapsulada.
     * @param fieldCapsule
     * @return 
     */
    private String recoverFieldName(String fieldCapsule) {
        if(fieldCapsule != null && !fieldCapsule.isBlank() && fieldCapsule.contains(":")) {
            int end = fieldCapsule.indexOf(":");
            return fieldCapsule.substring(1, end);
        }
        throw new NullPointerException("String de capsula com erro ou nula.");
    }
    
    /**
     * Recebe a capsula de uma variável e retorna a capsula do objeto que pertence ao campo.
     * @param fieldCapsule
     * @return 
     */
    private String recoverValueCapsule(String fieldCapsule) {
        if(fieldCapsule != null && !fieldCapsule.isBlank() && fieldCapsule.contains(":")) {
            int init = fieldCapsule.indexOf(":") + 1;
            return fieldCapsule.substring(init, fieldCapsule.length()-1);
        }
        throw new NullPointerException("String de capsula com erro ou nula.");
    }
    
    /**
     * Recebe uma String de capsula e retorna uma instância de classe de acordo com
     * o tipo do objeto encapsulado.
     * @param str Capsula do objeto.
     * @return 
     */
    private Class getType(String str) {
        int pointer = str.indexOf(":");
        String type = str.substring(1, pointer);
        return switch (type) {
            case "list" -> List.class;
            case "map" -> Map.class;
            default -> ClassDictionary.fromIndex(Integer.parseInt(type));
        };
    }
    
    /**
     * Recebe uma String de capsula de objeto ou entidade e retorna uma lista com
     * as capsulas dos campos.
     * @param str
     * @return 
     */
    private List<String> getFields(String str) {
        if(str == null || str.isBlank()) {
            return null;
        }
        
        List<String> fields = new ArrayList<>();
        int init = str.indexOf(":") + 1;
        String split = str.substring(init);
        
        while(split.contains("{") && split.contains("}")) {
            int a = split.indexOf("{");
            int b = a+1;
            int count = 1;
            while(count != 0 || b > split.length()) {
                if(split.charAt(b) == '{') {
                    count++;
                } else if(split.charAt(b) == '}') {
                    count--;
                }
                b++;
            }
            
            if(count == 0) {
                fields.add(split.substring(a, b));
                split = split.substring(b);
            } else {
                return null;
            }
        }
        return fields;
    }
    
    /**
     * Recebe uma lista de capsulas e retorna uma nova instância da lista preenchida.
     * @param str Capsula com os valores da lista.
     * @param recovereds Mapa de entidades já recuperadas.
     * @return Lista preenchida a partir das capsulas.
     * @throws Exception 
     */
    private List parseListFromString(String str, Map<String, Entity> recovereds) throws Exception {
        List list = new ArrayList();
        List<String> fields = getFields(str);
        for(String field : fields) {
            Class type = getType(field);
            list.add(recoverFromCapsule(field, recovereds));
        }
        return list;
    }
    
    /**
     * Recebe uma lista de capsulas e transforma em um mapa.
     * A lista de capsulas deverá sempre seguir a ordem "chave -> valor", ou seja
     * indices ímpares são as chaves, índices pares são os valores.
     * @param str Lista de capsulas com chave e valor.
     * @param recovereds Lista de entidades já recuperadas.
     * @return Mapa preenchido.
     * @throws Exception 
     */
    private Map parseMapFromString(String str, Map<String, Entity> recovereds) throws Exception {
        Map<Object, Object> map = new HashMap<>();
        List<String> fields = getFields(str);
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("A string fornecida não contém campos válidos.");
        }
        for (int i = 0; i < fields.size() - 1; i += 2) {
            Object key = recoverFromCapsule(fields.get(i), recovereds);
            Object value = recoverFromCapsule(fields.get(i + 1), recovereds);
            map.put(key, value);
        }
        return map;
    }
    
    /**
     * Recebe a capsula de um Calendar e retorna uma nova instância.
     * @param calendarString Capsula do calendar.
     * @return Instância de calendário com a data fornecida na capsula.
     */
    private Calendar parseCalendarFromString(String calendarString) {
        // Expressões regulares para extrair os campos relevantes
        Pattern patternYear = Pattern.compile("YEAR=(\\d+)");
        Pattern patternMonth = Pattern.compile("MONTH=(\\d+)");
        Pattern patternDayOfMonth = Pattern.compile("DAY_OF_MONTH=(\\d+)");
        Pattern patternHour = Pattern.compile("HOUR_OF_DAY=(\\d+)");
        Pattern patternMinute = Pattern.compile("MINUTE=(\\d+)");
        Pattern patternSecond = Pattern.compile("SECOND=(\\d+)");
        Pattern patternMillisecond = Pattern.compile("MILLISECOND=(\\d+)");
        Pattern patternZone = Pattern.compile("id=\"([^\"]+)\"");

        // Capturando os valores
        int year = extractValue(patternYear, calendarString);
        int month = extractValue(patternMonth, calendarString);
        int dayOfMonth = extractValue(patternDayOfMonth, calendarString);
        int hour = extractValue(patternHour, calendarString);
        int minute = extractValue(patternMinute, calendarString);
        int second = extractValue(patternSecond, calendarString);
        int millisecond = extractValue(patternMillisecond, calendarString);
        String timeZoneID = extractString(patternZone, calendarString);

        // Criando uma instância de Calendar com os valores extraídos
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        calendar.setTimeZone(java.util.TimeZone.getTimeZone(timeZoneID));

        return calendar;
    }
    
    /**
     * Recebe uma capsula de data e retorna uma nova instância de acordo com o tipo
     * de data.
     * @param dateTimeString Capsula de tempo.
     * @param type Tipo de tempo e data.
     * @return Instância de tempo e data.
     */
    private Object parseDateTimeFromString(String dateTimeString, Class type) {
        if(type == LocalDate.class) {
            try {
                // Tentativa de parsing para LocalDate
                return LocalDate.parse(dateTimeString);
            } catch (DateTimeParseException e) {
                // Ignorar e tentar o próximo tipo
            }
        } else if(type == LocalTime.class) {
            try {
                // Tentativa de parsing para LocalTime
                return LocalTime.parse(dateTimeString);
            } catch (DateTimeParseException e) {
                // Ignorar e tentar o próximo tipo
            }
        } else if(type == LocalDateTime.class) {
            try {
                // Tentativa de parsing para LocalDateTime
                return LocalDateTime.parse(dateTimeString);
            } catch (DateTimeParseException e) {
                // Ignorar e tentar o próximo tipo
            }
        } else if(type == ZonedDateTime.class) {
            try {
                // Tentativa de parsing para ZonedDateTime
                return ZonedDateTime.parse(dateTimeString);
            } catch (DateTimeParseException e) {
                // Ignorar e tentar o próximo tipo
            }
        } else if(type == Instant.class) {
            try {
                // Tentativa de parsing para Instant
                return Instant.parse(dateTimeString);
            } catch (DateTimeParseException e) {
                // Ignorar e continuar
            }
        }
        // Se nenhum tipo foi identificado
        throw new IllegalArgumentException("Formato de data/tempo desconhecido: " + dateTimeString);
    }
    
    /**
     * Retorna uma nova instância de Period a partir de uma capsula.
     * @param periodString Capsula do Period.
     * @return Instância do Period fornecido na capsula.
     */
    private Period parsePeriodFromString(String periodString) {
        try {
            // Tentativa de parsing da string para o objeto Period
            return Period.parse(periodString);
        } catch (DateTimeParseException e) {
            // Lança uma exceção se a string não estiver no formato ISO-8601 correto
            throw new IllegalArgumentException("Formato de período desconhecido: " + periodString, e);
        }
    }
    
    /**
     * Retorna uma nova instância primitiva a partir de uma capsula.
     * @param input Capsula do primitivo.
     * @param type Tipo do primitivo.
     * @return Instância do primitivo.
     */
    private Object parsePrimitiveFromString(String input, Class<?> type) {
        if (type == String.class) {
            return input; // A string já é o valor
        } else if (type == char.class || type == Character.class) {
            if (input.length() == 1) {
                return input.charAt(0); // Converte a string para char se tiver um único caractere
            } else {
                throw new IllegalArgumentException("Formato inválido para char: " + input);
            }
        } else if (type == short.class || type == Short.class) {
            return Short.valueOf(input); // Converte para short
        } else if (type == int.class || type == Integer.class) {
            return Integer.valueOf(input); // Converte para int
        } else if (type == long.class || type == Long.class) {
            return Long.valueOf(input); // Converte para long
        } else if (type == float.class || type == Float.class) {
            return Float.valueOf(input); // Converte para float
        } else if (type == double.class || type == Double.class) {
            return Double.valueOf(input); // Converte para double
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(input); // Converte para boolean
        } else if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(input); // Converte para byte
        } else if (type == BigDecimal.class) {
            return new BigDecimal(input);
        } else if (type == BigInteger.class) {
            return new BigInteger(input);
        } else {
            throw new IllegalArgumentException("Tipo não suportado: " + type.getSimpleName());
        }
    }

    // Método auxiliar para extrair inteiros da string
    private int extractValue(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0; // valor padrão se o campo não for encontrado
    }

    // Método auxiliar para extrair strings (como o fuso horário)
    private String extractString(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "GMT"; // valor padrão se o campo não for encontrado
    }
    
    
    /*
    DELETAR
    */
    
    /**
     * Deleta uma entidade preservando todas as outras subentidades.
     * @return
     * @throws Exception 
     */
    public boolean delete() throws Exception {
        String path = getPath(this.entity);
        File file = new File(path);
        if(file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * Deleta a entidade atual e todas as outras anotadas com Cascate.
     * @return
     * @throws Exception 
     */
    public boolean deleteCascate() throws Exception {
        Map<String, Entity> entities = recursiveCascadeEntities();
        Set<String> keys = entities.keySet();
        
        if(keys.stream()
                .allMatch(key -> {
                    try {
                        renameToOld(entities.get(key));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })) {
            
            keys.forEach(key -> {
                try {
                    clearInDB(entities.get(key));
                } catch (Exception ex) {
                    Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            return true;
        }
        return false;
    }
    
    
    /*
    MÉTODOS COMPLEMENTARES
    */
    
    /**
     * Inicializa o banco de dados criando todos os subdiretórios de entidades.
     */
    private void initDb() {
        List<String> all = Reflection.allImplementsNotAbstract(Entity.class);
        for(String path : all) {
            File file = new File(rootDirectory+path.replaceAll(".class", "").replaceAll("[.]", "/"));
            file.mkdirs();
            System.out.println("Diretório criado -> "+file.getPath());
        }
    }
    
    /**
     * Passa uma entidade por um teste de valor único de campo.
     * Garantindo que nenhuma outra entidade do mesmo tipo já tenha sido salva
     * com aquele valor que deve ser único.
     * @param entity Entidade a ser verificada.
     * @return Resultado da análise.
     * @throws Exception 
     */
    private boolean uniqueFieldTest(Entity entity) throws Exception {
        if(entity == null) {
            throw new NullPointerException("Entidade nula para teste de unicidade.");
        }
        
        List<Field> unicos = FieldsManager.getAllFieldsUniques(entity);
        if(unicos.isEmpty()) {
            return true;
        }
        
        String[] listaDeIds = new File(rootDirectory+entity.getClass().getName().replaceAll(".class", "").replaceAll("[.]", "/")).list();
        if(listaDeIds != null && listaDeIds.length > 0) {
            for(String id : listaDeIds) {
                if(!id.equals(entity.getId())) {
                    Map<String, String> camposParaAnalisar = recoverOnlyUniqueFields(entity.getClass(), id);
                    if(!unicos.stream().noneMatch(unico -> {
                        if(!camposParaAnalisar.containsKey(unico.getName())) {
                            return false;
                        } else {
                            try {
                                return FieldsManager.getValue(unico, entity).toString().equals(camposParaAnalisar.get(unico.getName()));
                            } catch (Exception ex) {
                                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                                return true;
                            }
                        }
                    })) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public <T extends Entity> T load(Class entityClass, String id) throws Exception {
        String path = getPath(entityClass, id);
        return (T) recover(Files.readString(Paths.get(path)), recovereds);
    }
    
    /**
     * Faz uma busca recursiva por todas as entidades referenciadas a partir da
     * entidade atual da capsula e que sejam anotadas com Cascate.
     * @return
     * @throws Exception 
     */
    private Map<String, Entity> recursiveCascadeEntities() throws Exception {
        Map<String, Entity> map = new HashMap<>();
        return recursiveCascadeEntities(map);
    }
    
    /**
     * Faz uma busca recursiva por todas as entidades referenciadas a partir da
     * entidade atual da capsula e que sejam anotadas com Cascate.
     * @param map
     * @return
     * @throws Exception 
     */
    private Map<String, Entity> recursiveCascadeEntities(Map<String, Entity> map) throws Exception {
        if(this.entity == null) {
            throw new NullPointerException("Capsula com entidade nula para buscar.");
        }
        
        if(!map.containsKey(this.entity.getId())) {
            map.put(this.entity.getId(), entity);
            List<Field> fields = FieldsManager.getAllFields(this.entity.getClass()).stream()
                    .filter(field -> {
                        Class type = field.getType();
                        if(Reflection.isInstance(type, Cascate.class)) {
                            return
                                    Reflection.isInstance(type, Entity.class) ||
                                    Reflection.isArrayMap(type);
                        } else {
                            return false;
                        }
                    })
                    .toList();

            for(Field field : fields) {
                if(Reflection.isInstance(field.getType(), Entity.class)) {
                    Entity ente = FieldsManager.getValue(field, this.entity);
                    Capsule cap = new Capsule(ente);
                    cap.recursiveEntities(map);
                } else if(Reflection.isInstance(field.getType(), List.class)) {
                    List list = FieldsManager.getValue(field, this.entity);
                    for(Object o : list) {
                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                            Capsule cap = new Capsule((Entity) o);
                            cap.recursiveEntities(map);
                        }
                    }
                } else if(Reflection.isInstance(field.getType(), Map.class)) {
                    Map mapa = FieldsManager.getValue(field, this.entity);
                    Set keys = mapa.keySet();

                    for(Object key : keys) {
                        Object get = mapa.get(key);
                        if(Reflection.isInstance(get.getClass(), Entity.class)) {
                            Capsule cap = new Capsule((Entity) get);
                            cap.recursiveEntities(map);
                        }
                    }
                }
            }
        }
        return map;
    }
    
    /**
     * Faz uma busca recursiva por todas as entidades referenciadas a partir da
     * entidade atual da capsula.
     * @return Entidades referenciadas a partir da raiz da capsula.
     */
    private Map<String, Entity> recursiveEntities() throws Exception {
        Map<String, Entity> map = new HashMap<>();
        return recursiveEntities(map);
    }
    
    /**
     * Faz uma busca recursiva por todas as entidades referenciadas a partir da
     * entidade atual da capsula.
     * @return Entidades referenciadas a partir da raiz da capsula.
     */
    private Map<String, Entity> recursiveEntities(Map<String, Entity> map) throws Exception {
        if(this.entity == null) {
            throw new NullPointerException("Capsula com entidade nula para buscar.");
        }
        
        if(!map.containsKey(this.entity.getId())) {
            map.put(this.entity.getId(), entity);
            List<Field> fields = FieldsManager.getAllFields(this.entity.getClass()).stream()
                    .filter(field -> {
                        Class type = field.getType();
                        return
                                Reflection.isInstance(type, Entity.class) ||
                                Reflection.isArrayMap(type);
                    })
                    .toList();

            for(Field field : fields) {
                if(Reflection.isInstance(field.getType(), Entity.class)) {
                    Entity ente = FieldsManager.getValue(field, this.entity);
                    Capsule cap = new Capsule(ente);
                    cap.recursiveEntities(map);
                } else if(Reflection.isInstance(field.getType(), List.class)) {
                    List list = FieldsManager.getValue(field, this.entity);
                    for(Object o : list) {
                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                            Capsule cap = new Capsule((Entity) o);
                            cap.recursiveEntities(map);
                        }
                    }
                } else if(Reflection.isInstance(field.getType(), Map.class)) {
                    Map mapa = FieldsManager.getValue(field, this.entity);
                    Set keys = mapa.keySet();

                    for(Object key : keys) {
                        Object get = mapa.get(key);
                        if(Reflection.isInstance(get.getClass(), Entity.class)) {
                            Capsule cap = new Capsule((Entity) get);
                            cap.recursiveEntities(map);
                        }
                    }
                }
            }
        }
        return map;
    }
    
    /**
     * Retorna o diretório onde a entidade é armazenada.
     * @param entityClass
     * @return 
     */
    private String getFolder(Class entityClass) {
        return rootDirectory+entityClass.getName().replaceAll(".class", "").replaceAll("[.]", "/");
    }
    
    /**
     * Retorna o caminho completo para a entidade.
     * @param entity
     * @return
     * @throws Exception 
     */
    private String getPath(Entity entity) throws Exception {
        return rootDirectory+entity.getClass().getName().replaceAll(".class", "").replaceAll("[.]", "/")+"/"+entity.getId();
    }
    
    /**
     * Retorna o caminho completo para a entidade.
     * @param entityClass
     * @param id
     * @return
     * @throws Exception 
     */
    private String getPath(Class entityClass, String id) throws Exception {
        return rootDirectory+entityClass.getName().replaceAll(".class", "").replaceAll("[.]", "/")+"/"+id;
    }
    
    /**
     * Retorna o caminho completo para a versão antiga da entidade.
     * @param entity
     * @return
     * @throws Exception 
     */
    private String getOldPath(Entity entity) throws Exception {
        return rootDirectory+entity.getClass().getName().replaceAll(".class", "").replaceAll("[.]", "/")+"/"+entity.getId()+"old";
    }
    
    /**
     * Retorna o caminho completo para a versão recente da entidade, que ainda não
     * está no ambiente de produção.
     * @param entity
     * @return
     * @throws Exception 
     */
    private String getNewPath(Entity entity) throws Exception {
        return rootDirectory+entity.getClass().getName().replaceAll(".class", "").replaceAll("[.]", "/")+"/"+entity.getId()+"new";
    }
    
    /**
     * Converte o arquivo atual da entidade no banco de dados para o tipo "old"
     * utilizado como backup na hora de gravar os dados no banco de dados.
     * @param entity
     * @return
     * @throws Exception 
     */
    private boolean renameToOld(Entity entity) throws Exception {
        File file = new File(getPath(entity));
        if(file.exists()) {
            return file.renameTo(new File(getOldPath(entity)));
        }
        return true;
    }
    
    /**
     * Retorna uma entidade do estado de backup para o estado de uso no banco de dados.
     * @param entity
     * @return
     * @throws Exception 
     */
    private boolean renameFromOld(Entity entity) throws Exception {
        File file = new File(getOldPath(entity));
        if(file.exists()) {
            file.renameTo(new File(getPath(entity)));
            return true;
        }
        return false;
    }
    
    /**
     * Retorna uma entidade do estado de produção para o estado de gravação.
     * @param entity
     * @return
     * @throws Exception 
     */
    private boolean renameToNew(Entity entity) throws Exception {
        File file = new File(getPath(entity));
        if(file.exists()) {
            file.renameTo(new File(getNewPath(entity)));
            return true;
        }
        return false;
    }
    
    /**
     * Passa os dados recém gravados de uma entidade para o estado de produção
     * do banco de dados.
     * @param entity
     * @return
     * @throws Exception 
     */
    private boolean renameFromNew(Entity entity) throws Exception {
        File file = new File(getNewPath(entity));
        if(file.exists()) {
            return file.renameTo(new File(getPath(entity)));
        }
        return false;
    }
    
    /**
     * Elimina arquivos de backup e recém gravados, preservando apenas o valor
     * em produção.
     * @param entity
     * @throws Exception 
     */
    private void clearInDB(Entity entity) throws Exception {
        File old = new File(getOldPath(entity));
        File newer = new File(getNewPath(entity));
        if(old.exists()) {
            old.delete();
        }
        if (newer.exists()) {
            newer.delete();
        }
    }

    private void printLine() {
        System.out.println("---------------------------------------------------------------------------");
    }
    
    /**
     * Retorna uma lista com todas as entidades do tipo de classe no banco de dados.
     * @param <T> Entidade.
     * @param clazz Classe a ser buscada.
     * @return Lista com todas as entidades de classe.
     * @throws Exception 
     */
    public static <T extends Entity> List<T> loadAll(Class clazz) throws Exception{
        return loadAll(clazz, null);
    }
    
    /**
     * Retorna todas as entidades que tenham passado pela filtragem de valores de
     * acordo com a opção de "todos" ou "um".
     * @param <T> Entidade.
     * @param clazz Classe da entidade a ser buscada.
     * @return Lista de encontrados.
     * @throws Exception 
     */
    public static <T extends Entity> List<T> loadAll(Class clazz, Filter filter) throws Exception{
        List<T> list = new ArrayList<>();
        Capsule cap = new Capsule("");
        File folder = new File(cap.getFolder(clazz));
        if(folder.exists()) {
            List<String> ids = Arrays.asList(folder.list());
            if(ids != null) {
                for(String id : ids) {
                    Capsule capsule = new Capsule(clazz, id);
                    T ente = capsule.recover();
                    if(filter != null) {
                        if(filter.filter(ente)) {
                            list.add(ente);
                        }
                    } else {
                        list.add(ente);
                    }
                }
            }
        }
        
        return list;
    }
    
    /**
     * Retorna a lista de IDs de todas as entidades do tipo salvas no banco de dados.
     * @param clazz
     * @return 
     */
    public static List<String> getAllIds(Class clazz) {
        Capsule cap = new Capsule("");
        File folder = new File(cap.getFolder(clazz));
        if(folder.exists()) {
            String[] list = folder.list();
            if(list != null) {
                return Arrays.asList(list);
            }
        }
        return new ArrayList<>();
    }
    
}
