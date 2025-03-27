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
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.Reflection;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author jhones
 */
public class Writer {
    
    public int modoOperacional = Bottle.TEMP_STAGE;
    
    private String ROOT_DB = "./db/";
    private String TEMP_DB = "./temp/";
    
    public Writer() {
    }
    
    public Writer(String root, String temp) {
        this.ROOT_DB = root;
        this.TEMP_DB = temp;
    }
    
    public Writer(int modoOperacional) {
        this.modoOperacional = modoOperacional;
    }
    
    public Writer(int modoOperacional, String root, String temp) {
        this.modoOperacional = modoOperacional;
        this.ROOT_DB = root;
        this.TEMP_DB = temp;
    }
    
    public boolean write(Bottle bottle) throws IOException, IllegalAccessException, EntityIdBadImplementationException {
        String path = getPath(bottle.entity);
        File file = new File(path);
        file.getParentFile().mkdirs(); // Garantir diretórios
        Properties build = bottle.build();
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            build.store(bos, "JhonDBS Entity");
        }
        return true;
    }
    
    public boolean writeText(Class classe, String id, String content) throws IOException {
        try(BufferedWriter w = Files.newBufferedWriter(Paths.get(getPath(classe, id)), StandardCharsets.UTF_8)) {
            w.write(content);
            w.flush();
        }
        return true;
    }
    
    /*
    ************************************************************
    ********************    AUXILIARES    **********************
    ************************************************************
    */
    
    public void initDb() throws URISyntaxException, IOException {
        File file  = null;
        if(ROOT_DB.endsWith("/")) {
            file  = new File(ROOT_DB.substring(0, ROOT_DB.length()-1));
        } else {
            file  = new File(ROOT_DB);
        }
        
        if(!file.exists()) {
            List<Class<?>> all = Reflection.allImplementsNotAbstract(Entity.class);
            for(Class path : all) {
                File rootdb = new File(ROOT_DB+path.getName().replace(".class", "").replace(".", "/"));
                rootdb.mkdirs();
            }
        }
    }
    
    public String getPath(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        return getPath(entity.getClass(), entity.getId());
    }
    
    public String getPath(Class classe, String id) {
        String path = classe.getName().replace(".class", "").replace(".", "/")+"/"+id;
        if(modoOperacional == 0) {
            path = ROOT_DB+path;
        } else {
            path = TEMP_DB+path;
        }
        return path;
    }
    
}
