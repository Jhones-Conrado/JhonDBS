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
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author jhones
 * Classe de funções auxiliares para o bottle.
 */
public class Assist {
    
    private static final Logger LOGGER = Logger.getLogger(Assist.class.getName());
    
    public static List<Ref> getEntities(String capsules) {
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
                        LOGGER.log(Level.WARNING, "Classe inválida para índice {0} em cápsula: {1}", new Object[]{index, cap});
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Formato inválido de índice em cápsula: {0}", matcher.group(0));
                }
            }
        }
        return list;
    }
    
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
    
    public static Matcher matcherFindUUID(String input) {
        // Expressão regular para UUID
        String uuidRegex = "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b";
        Pattern pattern = Pattern.compile(uuidRegex);
        return pattern.matcher(input);
    }

    /**
     * Remove a existência de uma entidade em uma outra entidade. Ou seja, removerá ela de todos
     * os campos, listas, mapas, arrays e referências.
     * @param toRemove Entidade a ser removida.
     * @param getRemoved Entitdade a ser limpa.
     * @param temp_db Diretório temporário para executar a ação.
     * @throws Exception 
     */
    public static void removeExistence(Ref toRemove, Ref getRemoved, String temp_db) throws IOException {
        if (toRemove == null || getRemoved == null) {
            throw new IllegalArgumentException("Referências 'toRemove' e 'getRemoved' não podem ser nulas");
        }
        
        sendToTemp(getRemoved, temp_db);
        String path = getPathFromRef(getRemoved, temp_db);
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("Arquivo temporário não encontrado: " + path);

        Properties p = new Properties();
        p.load(new FileInputStream(file));

        String refPattern = String.format("\\{%d:%s\\}", toRemove.getValue(), Pattern.quote(toRemove.getKey()));
        String strFields = p.get("fields").toString();
        strFields = strFields.replaceAll(refPattern, "");
        p.put("fields", strFields);

        String refsStr = p.get("refs").toString();
        String updatedRefs = Arrays.stream(refsStr.split("::"))
                .filter(ref -> !ref.contains(toRemove.getKey()) && !ref.isBlank())
                .collect(Collectors.joining("::"));
        p.put("refs", updatedRefs);

        p.store(new FileOutputStream(file), "JhonDBS Entity");
    }
    
    public static void removeExistenceFromBottle(Bottle bottle, Ref getRemoved) throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        if (bottle == null || bottle.bottles.isEmpty()) {
            LOGGER.log(Level.FINE, "Nenhuma sub-bottle para remover existência: {0}", getRemoved);
            return;
        }
        for (Bottle b : bottle.bottles.values()) {
            removeExistence(new Ref(b.entity), getRemoved, b.TEMP_DB);
        }
    }
    
    public static void sendToTemp(Ref reference, String temp) throws FileNotFoundException, IOException {
        Class clazz = ClassDictionary.fromIndex(reference.getValue());
        String path = temp+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
        File tempFile = new File(path);
        
        if(!tempFile.exists()) {
            tempFile.getParentFile().mkdirs();
            String dbPath = "./db/"+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
            File root = new File(dbPath);
            
            if(!root.exists()) {
                root = new File(dbPath+".bak");
            }
            
            if(root.exists()) {
                Properties p = new Properties();
                p.load(new FileInputStream(root));
                p.store(new FileOutputStream(tempFile), "JhonDBS Entity");
            }
        }
    }
    
    public static List<Ref> removedsBetweenStates(Bottle bottle) throws IOException, EntityIdBadImplementationException, IllegalArgumentException, IllegalAccessException, URISyntaxException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        
        List<Ref> list = new ArrayList<>();
        Class clazz = bottle.entity.getClass();
        String tempPath = bottle.TEMP_DB + clazz.getName().replace(".class", "").replace(".", "/") + "/" + bottle.entity.getId();
        File tempFile = new File(tempPath);

        if (tempFile.exists()) {
            Bottle db = new Bottle.BottleBuilder()
                    .entityClass(clazz)
                    .id(bottle.entity.getId())
                    .modoOperacional(Bottle.ROOT_STAGE)
                    .build();
            Map<String, Bottle> map = db.bottles;
            for (String id : map.keySet()) {
                if (!bottle.bottles.containsKey(id)) {
                    list.add(new Ref(id, ClassDictionary.getIndex(clazz)));
                }
            }
        }
        return list;
    }
    
    public static List<Ref> removedsBetweenStates(Bottle old, Bottle actual) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        List<Ref> list = new ArrayList<>();
        for(String id : old.bottles.keySet().stream()
                .filter(id -> !actual.bottles.containsKey(id)).toList()) {
            list.add(new Ref(old.bottles.get(id).entity));
        }
        return list;
    }
    
    public static String getPathFromRef(Ref ref, String temp_db) {
        Class clazz = ClassDictionary.fromIndex(ref.getValue());
        return temp_db+clazz.getName().replace(".class", "").replace(".", "/")+"/"+ref.getKey();
    }
    
    public static Bottle createBottle(Entity ente, Map<String, Bottle> bottles, int modoOperacional, String ROOT_DB, String TEMP_DB, Entity referencia, boolean cascate) throws IOException, EntityIdBadImplementationException, IllegalArgumentException, IllegalAccessException, URISyntaxException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, FileNotFoundException, NoSuchAlgorithmException {
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
                .rootDB(ROOT_DB)
                .tempDB(TEMP_DB)
                .build();

        bottle.engarafar();
        if (referencia != null) {
            bottle.putRef(referencia);
        }
        if (cascate) {
            bottle.props.put("cascate", "true");
            bottle.cascate = true;
        }
        return bottle;
    }
    
}
