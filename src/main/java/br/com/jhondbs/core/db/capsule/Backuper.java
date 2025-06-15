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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classe responsável por criar os arquivos .bak de backup das entidades e também
 * por passar os arquivos .bak de volta para produção em caso de algum erro.
 * @author jhones
 */
public class Backuper {
    
    public static void doBackup(Bottle newState, Bottle oldState) throws Exception {
        Set<String> oldKeys = new HashSet<>();
        if(oldState != null) {
            for(Bottle bottle : oldState.bottles.values()) {
                Ref ref = new Ref(bottle.entity);
                Assist.sendToTemp(ref, newState.TEMP_DB);
                renameBackup(ref);
                oldKeys.add(bottle.entity.getId());
            }
        }
        for(Bottle bottle : newState.bottles.values()) {
            if(!oldKeys.contains(bottle.entity.getId())) {
                Ref ref = new Ref(bottle.entity);
                Assist.sendToTemp(ref, newState.TEMP_DB);
                renameBackup(ref);
            }
        }
        doFileBackup(listAffectedFilesAsList(newState, oldState));
        doImageBackup(oldKeys.stream().toList(), newState.ROOT_DB);
    }
    
    public static void rollBack(Bottle newState, Bottle oldState) throws Exception {
        Set<String> oldKeys = new HashSet<>();
        if(oldState != null) {
            for(Bottle bottle : oldState.bottles.values()) {
                Ref ref = new Ref(bottle.entity);
                renameProduction(ref);
                oldKeys.add(bottle.entity.getId());
            }
        }
        for(Bottle bottle : newState.bottles.values()) {
            if(!oldKeys.contains(bottle.entity.getId())) {
                Ref ref = new Ref(bottle.entity);
                renameProduction(ref);
            }
        }
        doFileRollback(listAffectedFilesAsList(newState, oldState));
        doImageRollback(oldKeys.stream().toList(), newState.ROOT_DB);
    }
    
    private static void doFileBackup(List<File> files) throws IOException {
        for(File file : files) {
            File fileBackup = new File(file.getPath()+Transaction.BACKUP_SUFFIX);
            if(file.exists() && file.getName().contains(Bottle.ROOT_DB)) {
                file.renameTo(fileBackup);
            }
        }
    }
    
    private static void doFileRollback(List<File> files) throws IOException {
        for(File file : files) {
            File fileBackup = new File(file.getPath()+Transaction.BACKUP_SUFFIX);
            if(fileBackup.exists()) {
                Files.move(fileBackup.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
    private static void doImageBackup(List<String> keys, String db) throws Exception {
        File imgFolder = new File(db+"imgs");
        for(String key : keys) {
            File folder = new File(imgFolder.getPath()+"/"+key);
            if(folder.exists()) {
                File[] imgs = folder.listFiles();
                for(File img : imgs) {
                    File bakImg = new File(img.getPath()+Transaction.BACKUP_SUFFIX);
                    img.renameTo(bakImg);
                }
            }
        }
    }
    
    private static void doImageRollback(List<String> keys, String db) throws Exception {
        File imgFolder = new File(db+"imgs");
        for(String key : keys) {
            File folder = new File(imgFolder.getPath()+"/"+key);
            if(folder.exists()) {
                File[] imgs = folder.listFiles();
                for(File img : imgs) {
                    if(img.getName().endsWith(Transaction.BACKUP_SUFFIX)) {
                        File bakImg = new File(img.getPath().substring(0, img.getPath().length()-Transaction.BACKUP_SUFFIX.length()));
                        img.renameTo(bakImg);
                    }
                }
            }
        }
    }
    
    private static void renameBackup(Ref ref) throws FileNotFoundException {
        String path = Assist.getRootPath(ref);
        File file = new File(path);
        if(file.exists()) {
            File newPath = new File(path+Transaction.BACKUP_SUFFIX);
            file.renameTo(newPath);
        }
    }
    
    private static void renameProduction(Ref ref) throws IOException {
        String path = Assist.getRootPath(ref);
        File file = new File(path);
        String pathBack = Assist.getRootPath(ref)+Transaction.BACKUP_SUFFIX;
        File fileBak = new File(pathBack);
        if(fileBak.exists()) {
            Files.move(fileBak.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    /**
     * Obtem a lista de todos os arquivos que precisam ser feito backup ou restaurados.
     * @param newState
     * @param oldState 
     */
    private static Map<String, File> listAffectedFiles(Bottle newState, Bottle oldState) {
        Map<String, File> files = new HashMap();
        oldState.bottles.values().forEach(bottle -> files.putAll(bottle.files));
        newState.bottles.values().forEach(bottle -> {
            bottle.files.entrySet().forEach(entry -> {
                if(!files.containsKey(entry.getKey())) files.put(entry.getKey(), entry.getValue());
            });
        });
        return files;
    }
    
    private static List<File> listAffectedFilesAsList(Bottle newState, Bottle oldState) {
        return listAffectedFiles(newState, oldState).values().stream().toList();
    }
    
}
