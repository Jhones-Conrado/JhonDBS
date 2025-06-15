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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Aplica as alterações de arquivos durante os commits.
 * Após todas as alterações serem gravadas na pasta temporária essa classe entra
 * em ação, movendo todos os arquivos de entidade não marcados para exclusão, para
 * a pasta de produção do banco de dados.
 * Após mover todos os arquivos de entidades, ele moverá os arquivos anexados e
 * as imagens.
 * @author jhones
 */
public class Applier {
    
    /**
     * Transfere as entidades alteradas para a pasta de produção.
     * @param newState
     * @param oldState
     * @throws Exception 
     */
    public static void turnEntitiesOn(Bottle newState, Bottle oldState) throws Exception {
        Path tempDB = Paths.get(newState.TEMP_DB);
        Path DB = Paths.get(Bottle.ROOT_DB);
        // Walk through the file tree starting from the source directory
        Files.walkFileTree(tempDB, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = DB.resolve(tempDB.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Properties prop = new Properties();
                prop.load(new FileInputStream(file.toFile()));
                if(!Assist.isMarkedToExclude(prop)) {
                    Path targetPath = DB.resolve(tempDB.relativize(file));
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        turnFilesOn(newState, oldState);
        turnImagesOn(newState, oldState);
    }
    
    /**
     * Habilita todos os arquivos para produção, restaurando do estado de backup
     * e movendo para a pasta de produção os novos arquivos.
     * @param newState
     * @param oldState
     * @throws Exception 
     */
    public static void turnFilesOn(Bottle newState, Bottle oldState) throws Exception {
        Map<String, Bottle> map = mapAffecteds(newState, oldState);
        for(Bottle bottle : map.values()) {
            for(File file : bottle.files.values()) {
                if(file.toString().contains(Bottle.ROOT_DB)) {
                    File fileBackup = new File(file.toString()+Transaction.BACKUP_SUFFIX);
                    if(fileBackup.exists()) {
                        fileBackup.renameTo(file);
                    }
                } else {
                    sendFileToDB(file, bottle);
                }
            }
        }
    }
    
    public static void turnImagesOn(Bottle newState, Bottle oldState) throws Exception {
        Map<String, Bottle> map = mapAffecteds(newState, oldState);
        for(Bottle bottle : map.values()) {
            for(String imgHash : bottle.imgs.keySet()) {
                File imgBackup = new File(Bottle.ROOT_DB+"imgs/"+bottle.entity.getId()+"/"+imgHash+Transaction.BACKUP_SUFFIX);
                File imgFile = new File(Bottle.ROOT_DB+"imgs/"+bottle.entity.getId()+"/"+imgHash);
                if(imgBackup.exists()) {
                    imgBackup.renameTo(imgFile);
                } else {
                    ImageWorker.flushImage(bottle.entity.getId(), bottle.imgs.get(imgHash), imgHash, Bottle.ROOT_DB);
                }
            }
        }
    }
    
    public static Map<String, Bottle> mapAffecteds(Bottle newState, Bottle oldState) {
        Map<String, Bottle> map = new HashMap<>();
        map.putAll(newState.bottles);
        if(oldState != null) {
            for(String id : oldState.bottles.keySet()) {
                if(!map.containsKey(id)) {
                    map.put(id, oldState.bottles.get(id));
                }
            }
        }
        return map;
    }
    
    public static void sendFileToDB(File file, Bottle bottle) throws Exception {
        File fileFolder = new File(bottle.ROOT_DB + "files/" + bottle.entity.getId());
        fileFolder.mkdirs();
        File fileDestiny = new File(fileFolder.getPath()+"/"+file.getName());
        if(file.exists()) {
            Files.copy(file.toPath(), fileDestiny.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
}
