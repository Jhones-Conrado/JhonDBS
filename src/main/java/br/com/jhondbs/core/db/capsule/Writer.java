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
import br.com.jhondbs.core.tools.Reflection;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhones
 */
public class Writer {
    
    public int modoOperacional = Bottle.TEMP_STAGE;

    public Writer() {
    }
    
    public Writer(int modoOperacional) {
        this.modoOperacional = modoOperacional;
    }
    
    public boolean write(Bottle bottle) throws Exception {
        String path = getPath(bottle.entity);
        File file = new File(path);
        try(BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write(bottle.build());
            w.flush();
        }
        return true;
    }
    
    public boolean writeText(Class classe, String id, String content) throws Exception {
        try(BufferedWriter w = Files.newBufferedWriter(Paths.get(getPath(classe, id)), StandardCharsets.UTF_8)) {
            w.write(content);
            w.flush();
        }
        return true;
    }
    
    public void removeExistence(Entity entity) throws Exception {
        Reader reader = new Reader(Bottle.TEMP_STAGE);
        List<String> refs = new ArrayList<>();
        String originalContent = null;
        try {
            refs.addAll(reader.spliteredReferences(entity));
            originalContent = reader.readContent(entity);
        } catch (Exception e) {
            reader.modoOperacional = Bottle.ROOT_STAGE;
            try {
                refs.addAll(reader.spliteredReferences(entity));
                originalContent = reader.readContent(entity);
            } catch (Exception ex) {
                System.out.println("Entidade não possui arquivos para referenciamento.");
            }
        }
        reader.modoOperacional = Bottle.TEMP_STAGE;
        Writer w = new Writer(Bottle.TEMP_STAGE);
        if(!originalContent.endsWith("DELETE")) {
            for(String ref : refs) {
                String[] dados = ref.split(":");
                Class classe = ClassDictionary.fromIndex(Integer.parseInt(dados[0]));
                String id = dados[1];

                try {
                    String enteId = entity.getId();
                    String oritxt = reader.sendToTemp(classe, id);
                    if(!oritxt.endsWith("DELETE")) {
                        // Limpeza das referências.
                        List<String> referencias = reader.spliteredReferences(classe, id);
                        List<String> filteredRefs = referencias.stream().filter(r -> !r.contains(enteId)).toList();

                        //Limpeza dos campos.
                        String content = reader.readContent(classe, id);

                        //{nome-do-campo:{valor}}
                        Map<String, String> fields = reader.splitFieldsAsMap(reader.getValueFromCapsule(content));
                        Map<String, String> filteredFields = new HashMap<>();

                        for(String key : fields.keySet()) {
                            if(fields.get(key).contains(enteId)) {
                                String capsulaLista = fields.get(key);
                                if(fields.get(key).contains("list")) {
                                    String filteredList = removeEntityFromList(reader.getValueFromCapsule(capsulaLista), enteId);
                                    filteredFields.put(key, filteredList);
                                } else if(fields.get(key).contains("map")) {
                                    String capsulasMapa = reader.getValueFromCapsule(capsulaLista);
                                    String filteredMap = removeEntityFromMap(capsulasMapa, enteId);
                                    filteredFields.put(key, filteredMap);
                                }
                            } else {
                                filteredFields.put(key, fields.get(key));
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("{").append(dados[0]).append(":");
                        for(String s : filteredFields.keySet()) {
                            sb.append("{");
                            sb.append(s).append(":");
                            sb.append(filteredFields.get(s));
                            sb.append("}");
                        }
                        sb.append("}");
                        if(!filteredRefs.isEmpty()) {
                            sb.append("ref::");
                            for(String s : filteredRefs) {
                                sb.append(s).append("::");
                            }
                        }
                        w.writeText(classe, id, sb.toString());
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
            originalContent = originalContent + "DELETE";
            w.writeText(entity.getClass(), entity.getId(), originalContent);
            List<Entity> casc = reader.listCascateEntities(entity);
            
            String supid = entity.getId();
            for(Entity e : casc) {
                List<String> refss = reader.spliteredReferences(e);
                refss = refss.stream().filter(ref -> !ref.contains(supid)).toList();
                if(refss.isEmpty()) {
                    w.removeExistence(e);
                } else {
                    StringBuilder sb = new StringBuilder();
                    String cont = reader.readContent(e);
                    sb.append(cont).append("ref::");
                    for(String ref : refss) {
                        sb.append(ref).append("::");
                    }
                    w.writeText(e.getClass(), e.getId(), sb.toString());
                }
            }
        }
    }
    
    private String removeEntityFromList(String capsulesList, String idToRemove) {
        Reader reader = new Reader();
        List<String> capsules = reader.splitCapsules(capsulesList);
        capsules = capsules.stream().filter(cap -> !cap.contains(idToRemove)).toList();
        StringBuilder sb = new StringBuilder();
        sb.append("{list:");
        for(String ss : capsules) {
            sb.append(ss);
        }
        sb.append("}");
        return sb.toString();
    }
    
    private String removeEntityFromMap(String capsulesList, String idToRemove) {
        Reader reader = new Reader();
        List<String> capsules = reader.splitCapsules(capsulesList);
        List<String> pares = new ArrayList<>();
        for(int i = 0 ; i < capsules.size()-1 ; i += 2) {
            pares.add(capsules.get(i)+":::"+capsules.get(i+1));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{map:");
        pares = pares.stream().filter(par -> !par.contains(idToRemove)).toList();
        for(String s : pares) {
            String[] split = s.split(":::");
            sb.append(split[0]).append(split[1]);
        }
        sb.append("}");
        return sb.toString();
    }
    
    
    /*
    ************************************************************
    ********************    AUXILIARES    **********************
    ************************************************************
    */
    
    public void initDb() {
        List<String> all = Reflection.allImplementsNotAbstract(Entity.class);
        for(String path : all) {
            File rootdb = new File(Bottle.ROOT_DB+path.replaceAll(".class", "").replaceAll("[.]", "/"));
            File tempdb = new File(Bottle.TEMP_DB+path.replaceAll(".class", "").replaceAll("[.]", "/"));
            rootdb.mkdirs();
            tempdb.mkdirs();
//            System.out.println("Diretório criado -> "+rootdb.getPath());
        }
    }
    
    public String getPath(Entity entity) throws Exception {
        String path = entity.getClass().getName().replace(".class", "").replace(".", "/")+"/"+entity.getId();
        if(modoOperacional == 0) {
            path = Bottle.ROOT_DB+path;
        } else {
            path = Bottle.TEMP_DB+path;
        }
        return path;
    }
    
    public String getPath(Class classe, String id) throws Exception {
        String path = classe.getName().replace(".class", "").replace(".", "/")+"/"+id;
        if(modoOperacional == 0) {
            path = Bottle.ROOT_DB+path;
        } else {
            path = Bottle.TEMP_DB+path;
        }
        return path;
    }
    
}
