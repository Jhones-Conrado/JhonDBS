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

import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.errors.ObjectNotDesserializebleException;
import br.com.jhondbs.core.db.interfaces.Cold;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Mantém funções auxiliares utilizadas no encapsulamento e desencapsulamento de
 * entidades.
 * @author jhones
 */
public class Assist {
    
    private static final Logger LOGGER = Logger.getLogger(Assist.class.getName());
    
    /**
     * Faz uma varredura nas capsulas buscando referências a entidades.
     * @param capsules
     * @param entity
     * @return Uma lista de objetos Ref com as referências a todas as entidades encontradas
     * na lista de capsulas enviada.
     */
    public static List<Ref> getEntities(String capsules, Entity entity) {
        List<Ref> list = new ArrayList<>();
        if (capsules == null || capsules.trim().isEmpty()) {
            return list;
        }
        Reader reader = new Reader();
        List<String> caps = reader.splitCapsules(capsules);
        Pattern pattern = Pattern.compile("\\{(\\d+):([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\\}");
        for (String cap : caps) {
            Matcher matcher = pattern.matcher(cap);
            while (matcher.find()) {
                try {
                    int index = Integer.parseInt(matcher.group(1));
                    String uuid = matcher.group(2);
                    Class<?> clazz = ClassDictionary.fromIndex(index);
                    if (clazz != null && Entity.class.isAssignableFrom(clazz)) {
                        list.add(new Ref(uuid, index));
                    } else {
                        int in = cap.indexOf(uuid);
                        int count = 2;
                        while(cap.charAt(in) != '{' && cap.charAt(in) > 0 && count > 0) {
                            in--;
                            count--;
                        }
                        try {
                            String fieldName = cap.substring(in, cap.indexOf(":", in));
                            List<Field> allFields = FieldsManager.getAllFields(entity);
                            Field field = allFields.stream()
                                    .filter(f -> f.getName().equals(fieldName))
                                    .findFirst()
                                    .get();
                            if(field.isAnnotationPresent(Cold.class)) {
                                list.add(new Ref(uuid, index));
                            }
                        } catch (Exception e) {
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Formato inválido de índice em cápsula: {0}", matcher.group(0));
                }
            }
        }
        return list;
    }
    
    /**
     * Busca um UUID dentro de uma String.
     * @param input
     * @return 
     */
    public static int findUUID(String input) {
        // Expressão regular para UUID
        String uuidRegex = "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b";
        Pattern pattern = Pattern.compile(uuidRegex);
        Matcher matcher = pattern.matcher(input);
        
        // Se encontrar, retorna a posição inicial
        if (matcher.find()) {
            return matcher.start();
        }
        
        // Retorna -1 se nenhum UUID for encontrado
        return -1;
    }

    /**
     * Remove a existência de uma entidade em uma outra entidade. Ou seja, removerá ela de todos
     * os campos, listas, mapas, arrays e referências.
     * @param toRemove Entidade a ser removida.
     * @param toBeCleaned Entitdade a ser limpa.
     * @param temp_db Diretório temporário para executar a ação.
     * @throws Exception 
     */
    public static void removeExistence(Ref toRemove, Ref toBeCleaned, String temp_db) throws Exception {
        File file = new File(getPathFromRef(toBeCleaned, temp_db));
        if (!file.exists()) throw new FileNotFoundException("Arquivo temporário não encontrado durante a execução de limpeza da entidade: " + file);
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        if(!isMarkedToExclude(props)) {
            String refPattern = String.format("\\{%d:%s\\}", toRemove.getValue(), Pattern.quote(toRemove.getKey()));
            String strFields = props.get("fields").toString();
            strFields = strFields.replaceAll(refPattern, "");
            props.put("fields", strFields);
            props.store(new FileOutputStream(file), "JhonDBS Entity");
            removeFromReference(toRemove, toBeCleaned, temp_db);
        }
    }
    
    public static Properties removeFromReference(Ref toRemove, Ref toBeCleaned, String temp_db) throws Exception {
        List<Ref> list = new ArrayList<>();
        list.add(toRemove);
        return removeFromReference(list, toBeCleaned, temp_db);
    }
    
    public static Properties removeFromReference(List<Ref> toRemove, Ref toBeCleaned, String temp_db) throws Exception {
        if (toRemove == null || toBeCleaned == null) throw new IllegalArgumentException("Referências 'toRemove' e 'getRemoved' não podem ser nulas");
        
        String path = getPathFromRef(toBeCleaned, temp_db);
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("Arquivo temporário não encontrado durante a execução de limpeza da entidade: " + path);
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        if(!isMarkedToExclude(props)) {
            String refsStr = props.get("refs").toString();
            String updatedRefs = Arrays.stream(refsStr.split("::"))
                    .filter(ref -> !ref.isBlank())
                    .filter(ref -> {
                        Ref r = new Ref(ref);
                        return !toRemove.contains(r);
                    })
                    .collect(Collectors.joining("::"));
            props.put("refs", updatedRefs);
            props.store(new FileOutputStream(file), "JhonDBS Entity");
            
            if(isCascate(props)) {
                int refsSize = Arrays.asList(updatedRefs.split("::"))
                        .stream()
                        .filter(ref -> !ref.isBlank())
                        .toArray().length;
                if(refsSize == 0) {
                    Bottle bd = new Bottle.BottleBuilder()
                            .entityClass(toBeCleaned.recoverClass())
                            .id(toBeCleaned.getKey())
                            .tempDB(temp_db)
                            .build();
                    bd.delete(true);
                    props.load(new FileInputStream(file));
                } else {
                    /*
                    Chamar CascateAnalyzer aqui para verificar referências cruzadas
                    e se o referênciador mantém referência cascata.
                    */
                    CascateAnalyzer.analyze(toBeCleaned, temp_db);
                }
            }
        }
        return props;
    }
    
    /**
     * Envia uma entidade para a pasta temporária para que se inicialize os trabalhos.
     * @param reference
     * @param temp
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void sendToTemp(Ref reference, String temp) throws FileNotFoundException, IOException {
        Class clazz = ClassDictionary.fromIndex(reference.getValue());
        String path = temp+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
        File tempFile = new File(path);
        
        if(!tempFile.exists()) {
            tempFile.getParentFile().mkdirs();
            String dbPath = "./db/"+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
            File root = new File(dbPath);
            
            if(!root.exists()) {
                root = new File(dbPath+Transaction.BACKUP_SUFFIX);
            }
            
            if(root.exists()) {
                Properties p = new Properties();
                p.load(new FileInputStream(root));
                p.store(new FileOutputStream(tempFile), "JhonDBS Entity");
            }
        }
    }
    
    /**
     * Retorna o diretório temporário de uma entidade a partir de sua referência.
     * @param reference
     * @param temp
     * @return 
     */
    public static String getTempPath(Ref reference, String temp) {
        Class clazz = ClassDictionary.fromIndex(reference.getValue());
        return temp+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
    }
    
    /**
     * Retorna o diretório permanente de uma entidade a partir de sua referencia.
     * @param reference
     * @return 
     */
    public static String getRootPath(Ref reference) {
        Class clazz = ClassDictionary.fromIndex(reference.getValue());
        return "./db/"+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
    }
    
    /**
     * Retorna o diretório permanente de uma entidade a partir de seu ID e índice de classe.
     * @param id
     * @param index
     * @return 
     */
    public static String getRootPath(String id, int index) {
        Class clazz = ClassDictionary.fromIndex(index);
        return "./db/"+clazz.getName().replace(".class", "").replace(".", "/")+"/"+id;
    }
    
    public static String getRootPath(Bottle bottle) throws Exception {
        return "./db/"+bottle.entity.getClass().getName().replace(".class", "").replace(".", "/")+"/"+bottle.entity.getId();
    }
    
    /**
     * Retorna o diretório de uma entidade, Raiz ou Temporário, de acordo com o
     * modo operacional da garrafa.
     * @param entity
     * @param bottle
     * @return 
     * @throws java.lang.IllegalAccessException 
     * @throws br.com.jhondbs.core.db.errors.EntityIdBadImplementationException 
     */
    public static String getPath(Bottle bottle) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException  {
        Ref ref = new Ref(bottle.entity);
        return getPath(ref, bottle);
    }
    
    /**
     * Retorna o diretório de uma entidade, Raiz ou Temporário, de acordo com o
     * modo operacional da garrafa.
     * @param ref
     * @param bottle
     * @return 
     */
    public static String getPath(Ref ref, Bottle bottle) {
        if(bottle.modoOperacional == Bottle.ROOT_STAGE) {
            return getRootPath(ref);
        } else {
            return getTempPath(ref, bottle.TEMP_DB);
        }
    }
    
    public static String getPath(Ref ref, String temp) {
        if(temp != null && !temp.isBlank()) {
            return getTempPath(ref, temp);
        } else {
            return getRootPath(ref);
        }
    }
    
    public static List<Ref> removedBetweenStates(Bottle newState, Bottle oldState) throws Exception {
        List<Ref> list = new ArrayList<>();
        Set<String> newStateKeys = newState.bottles.keySet();
        
        for(Bottle bottle : oldState.bottles.values()) {
            if(newStateKeys.contains(bottle.entity.getId())) {
                list.add(new Ref(bottle.entity));
            }
        }
        
        return list;
    }
    
    /**
     * Retorna o caminho temporário de uma entidade a partir de sua referência.
     * @param ref
     * @param temp_db
     * @return 
     */
    public static String getPathFromRef(Ref ref, String temp_db) {
        Class clazz = ClassDictionary.fromIndex(ref.getValue());
        return temp_db+clazz.getName().replace(".class", "").replace(".", "/")+"/"+ref.getKey();
    }
    
    /**
     * Cria uma nova garrafa para uma entidade recebendo a entidade e o mapa de bottles.
     * É utilizado para uma sub-criação de garrafas, necessária para carregamento de
     * entidades.
     * @param ente Ente a ser engarrafado.
     * @param bottles Mapa atual de bottles.
     * @param modoOperacional Modo operacional utilizado no momento.
     * @param ROOT_DB Diretório raiz da produção.
     * @param TEMP_DB Raiz da pasta temporária.
     * @param referencia Entidade de referência que está chamando a sub-construção.
     * @param cascate Se a entidade a ser engarrafada receberá ou não o marcador de cascata.
     * @return
     * @throws IOException
     * @throws EntityIdBadImplementationException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws URISyntaxException
     * @throws ParseException
     * @throws ObjectNotDesserializebleException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException 
     */
    public static Bottle createBottle(Entity ente, Map<String, Bottle> bottles, int modoOperacional, String TEMP_DB, Entity referencia, boolean cascate) throws Exception {
        if (ente == null) {
            throw new IllegalArgumentException("Entidade não pode ser nula");
        }
        if (bottles == null) {
            bottles = new HashMap<>();
        }
        Bottle bottle = new Bottle.BottleBuilder()
                .entity(ente)
                .bottles(bottles)
                .modoOperacional(modoOperacional)
                .tempDB(TEMP_DB)
                .build();

        bottle.engarrafar();
        if (referencia != null) {
            bottle.putRef(referencia);
        }
        if (cascate) {
            bottle.props.put("cascate", "true");
            bottle.cascate = true;
        }
        return bottle;
    }
    
    public static void markCascate(Properties prop) {
        prop.put("cascate", "true");
    }
    
    public static void markExclude(Properties prop) {
        prop.put("exclude", "true");
    }
    
    public static boolean isCascate(Properties prop) {
        return prop.containsKey("cascate") ? prop.getProperty("cascate").equals("true") : false;
    }
    
    public static boolean isMarkedToExclude(Properties prop) {
        return prop.containsKey("exclude");
    }
    
}
