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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Responsável por fazer a limpeza das pastas e arquivos desnecessários após
 * commits no banco de dados.
 * @author jhones
 */
public class Cleaner {
    
    /**
     * Exclui todos os arquivos .bak da pasta do banco de dados.
     * @param newState
     * @param oldState
     * @throws Exception 
     */
    public static void cleanDB(Bottle newState, Bottle oldState) throws Exception {
        Map<String, Bottle> map = new HashMap<>();
        map.putAll(newState.bottles);
        if(oldState != null) {
            for(String id : oldState.bottles.keySet()) {
                if(!map.containsKey(id)) {
                    map.put(id, oldState.bottles.get(id));
                }
            }
        }
        for(Bottle bottle : map.values()) {
            String path = Assist.getRootPath(new Ref(bottle.entity));
            File file = new File(path+Transaction.BACKUP_SUFFIX);
            if(file.exists()) {
                file.delete();
            }
        }
        cleanFilesDB(map);
        cleanImagesDB(map);
        cleanTemp(newState.TEMP_DB);
    }
    
    private static void cleanFilesDB(Map<String, Bottle> map) throws Exception {
        for(Bottle bottle : map.values()) {
            Properties props = Reader.read(bottle.entity.getClass(), bottle.entity.getId(), bottle.TEMP_DB);
            File entityFilesFolder = new File(bottle.ROOT_DB+"/files/"+bottle.entity.getId());
            
            if(entityFilesFolder.exists()) {
                File[] entityFiles = entityFilesFolder.listFiles();
                if(Assist.isMarkedToExclude(props)) {
                    for(File exclude : entityFiles) {
                        exclude.delete();
                    }
                    entityFilesFolder.delete();
                } else {
                    for(File file : entityFiles) {
                        String fileName = file.getName();
                        if(fileName.endsWith(Transaction.BACKUP_SUFFIX)) {
                            fileName = fileName.substring(0, fileName.length()-Transaction.BACKUP_SUFFIX.length());
                        }
                        if(!bottle.files.containsKey(fileName)) {
                            file.delete();
                        }
                    }
                    if(entityFilesFolder.listFiles().length == 0) {
                        entityFilesFolder.delete();
                    }
                }
            }
        }
    }
    
    private static void cleanImagesDB(Map<String, Bottle> map) throws Exception {
        for(Bottle bottle : map.values()) {
            Properties props = Reader.read(bottle.entity.getClass(), bottle.entity.getId(), bottle.TEMP_DB);
            File entityImagesFolder = new File(bottle.ROOT_DB+"/imgs/"+bottle.entity.getId());
            
            if(entityImagesFolder.exists()) {
                File[] entityImages = entityImagesFolder.listFiles();
                if(Assist.isMarkedToExclude(props)) {
                    for(File exclude : entityImages) {
                        exclude.delete();
                    }
                    entityImagesFolder.delete();
                } else {
                    for(File image : entityImages) {
                        String imageHash = image.getName();
                        if(imageHash.endsWith(Transaction.BACKUP_SUFFIX)) {
                            imageHash = imageHash.substring(0, imageHash.length()-Transaction.BACKUP_SUFFIX.length());
                        }
                        if(!bottle.imgs.containsKey(imageHash)) {
                            image.delete();
                        }
                    }
                    if(entityImagesFolder.listFiles().length == 0) {
                        entityImagesFolder.delete();
                    }
                }
            }
        }
    }
    
    /**
     * Chamada recursiva para excluir um diretório.
     * @param tempFolder 
     */
    public static void cleanTemp(String tempFolder) {
        File file = new File(tempFolder);
        if(file.exists()) {
            if(file.isFile()) {
                file.delete();
            } else {
                File[] list = file.listFiles();
                for(File subFile : list) {
                    cleanTemp(subFile.toString());
                }
                file.delete();
            }
        }
    }
    
}
