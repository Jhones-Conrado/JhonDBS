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

import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jhones
 * Classe de funções auxiliares para o bottle.
 */
public class Assist {
    
    public static List<Ref> getEnties(String capsules) {
        List<Ref> list = new ArrayList<>();
        Reader reader = new Reader();
        List<String> caps = reader.splitCapsules(capsules);
        for(String cap : caps) {
            while(findUUID(cap) != -1) {
                int found = findUUID(cap);
                int end = found + 36;
                while(cap.charAt(found) != '{') {
                    found--;
                }
                String uuid = cap.substring(found+1, end);
                String[] split = uuid.split(":");
                if(ClassDictionary.fromIndex(Integer.parseInt(split[0])).isAssignableFrom(Entity.class)) {
                    Ref ref = new Ref(split[1], Integer.valueOf(split[0]));
                    list.add(ref);
                }
                cap = cap.replace(uuid, "");
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
    public static void removeExistence(Ref toRemove, Ref getRemoved, String temp_db) throws Exception {
        sendToTemp(getRemoved, temp_db);
        Class clazz = ClassDictionary.fromIndex(getRemoved.getValue());
        String path = temp_db+clazz.getName().replace(".class", "").replace(".", "/")+"/"+getRemoved.getKey();
        
        Properties p = new Properties();
        p.load(new FileInputStream(new File(path)));
        
        String refToRemove = "{"+String.valueOf(toRemove.getValue())+":"+toRemove.getKey()+"}";
        String refToRemove2 = "{"+String.valueOf(toRemove.getValue())+"\\:"+toRemove.getKey()+"}";
        String strFields = p.get("fields").toString();
        strFields = strFields.replace(refToRemove, "").replace(refToRemove2, "");
        
        p.put("fields", strFields);
        
        StringBuilder sb = new StringBuilder();
        String refsStr = p.get("refs").toString();
        Arrays.asList(refsStr.split("::"))
                .stream()
                .filter(ref -> !ref.contains(toRemove.getKey()) && !ref.isBlank())
                .forEach(ref -> {
                    sb.append(ref).append("::");
                });
        p.put("refs", sb.toString());
        p.store(new FileOutputStream(new File(path)), "JhonDBS Entity");
    }
    
    public static void removeExistenceFromBottle(Bottle bottle, Ref getRemoved) throws Exception {
        for(Bottle b : bottle.bottles.values()) {
            removeExistence(new Ref(b.entity), getRemoved, b.TEMP_DB);
        }
    }
    
    public static void sendToTemp(Ref reference, String temp) throws Exception {
        Class clazz = ClassDictionary.fromIndex(reference.getValue());
        String path = temp+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
        File tempFile = new File(path);
        if(!tempFile.exists()) {
            tempFile.getParentFile().mkdirs();
            String dbPath = "./db/"+clazz.getName().replace(".class", "").replace(".", "/")+"/"+reference.getKey();
            File root = new File(dbPath);
            if(root.exists()) {
                Properties p = new Properties();
                p.load(new FileInputStream(root));
                p.store(new FileOutputStream(tempFile), "JhonDBS Entity");
            }
        }
    }
    
    public static List<Ref> removedsBetweenStates(Bottle bottle) throws Exception {
        List<Ref> list = new ArrayList<>();
        
        Class clazz = bottle.entity.getClass();
        String tempPath = bottle.TEMP_DB+clazz.getName().replace(".class", "").replace(".", "/")+"/"+bottle.entity.getId();
        
        File tempFile = new File(tempPath);
        
        if(tempFile.exists()) {
            Bottle db = new Bottle.BottleBuilder().entityClass(clazz).id(bottle.entity.getId()).modoOperacional(Bottle.ROOT_STAGE).build();
            Map<String, Bottle> map = db.bottles;
            
            bottle.bottles.keySet().stream().forEach(id -> {
                map.remove(id);
            });
        }
        
        return list;
    }
    
    public static String getPathFromRef(Ref ref, String temp_db) {
        Class clazz = ClassDictionary.fromIndex(ref.getValue());
        return temp_db+clazz.getName().replace(".class", "").replace(".", "/")+"/"+ref.getKey();
    }
    
    public static Bottle createBottle(Entity ente, Map<String, Bottle> bottles, int modoOperacional, String ROOT_DB, String TEMP_DB, Entity referencia, boolean cascate) throws Exception {
        Bottle bottle = new Bottle.BottleBuilder()
                .entity(ente)
                .bottles(bottles)
                .modoOperacional(modoOperacional)
                .rootDB(ROOT_DB)
                .tempDB(TEMP_DB)
                .build();
        
        bottle.engarafar();
        bottle.putRef(referencia);
        if(cascate) {
            bottle.props.put("cascate", "true");
            bottle.cascate = true;
        }
        return bottle;
    }
    
}
