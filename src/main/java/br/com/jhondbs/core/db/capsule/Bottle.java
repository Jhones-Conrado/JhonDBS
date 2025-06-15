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

import br.com.jhondbs.core.db.Mapper;
import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.tools.Reflection;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Versão 5. </br>
 * Classe central do banco de dados, responsável por estruturar todas as outras
 * funções responsáveis pelo encapsulamento e leitura das entidades.
 * @author jhones
 */
public final class Bottle {
    
    public static final int ROOT_STAGE = 0;
    public static final int TEMP_STAGE = 1;
    
    public static final String ROOT_DB = "./db/";
    public String TEMP_DB = "./temp/";
    public int modoOperacional = TEMP_STAGE;
    
    public Map<String, Bottle> bottles = new HashMap<>();
    public List<String> bottledFields = new ArrayList<>();
    public Set<Ref> referencias = new HashSet<>();
    public Map<String, Image> imgs = new HashMap<>();
    public Map<String, File> files = new HashMap<>();
    public Entity entity;
    public Properties props = new Properties();
    public boolean cascate = false;
    
    private Bottle() {
    }
    
    /**
     * Adicionar uma entidade como referenciador desta.
     * @param entity
     */
    public void putRef(Entity entity) throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        Ref ref = new Ref(entity);
        this.referencias.add(ref);
    }
    
    /**
     * Lê as referências do estado antigo da entidade.
     */
    public void loadRefs() throws Exception {
        String path = Assist.getRootPath(this);
        File file = new File(path);
        if(file.exists()) {
            Properties currentProps = new Properties();
            currentProps.load(new FileInputStream(new File(path)));
            this.props = currentProps;
            String refs = currentProps.getProperty("refs").toString();
            loadRefs(refs);
        }
    }
    
    public void loadRefs(String refs) {
        if(!refs.isBlank()) {
            this.referencias.addAll(Arrays.asList(refs.split("::"))
                    .stream()
                    .filter(str -> !str.isBlank())
                    .map(str -> new Ref(str))
                    .toList());
        }
        Mapper.add(entity, referencias);
    }
    
    /**
     * Utilizado para definir a pasta temporária utilizada para serialização.
     */
    private void defineTemp() {
        String identity = this.toString().replace(this.getClass().getName(), "");
        this.TEMP_DB = this.TEMP_DB + identity +"/";
    }
    
    /**
     * Transforma todos os campos serializados em Properties que representa a
     * entidade encapsulada.
     * @return 
     */
    public Properties build() {
        StringBuilder fields = new StringBuilder();
        for(String s : bottledFields) {
            fields.append(s).append("\n");
        }
        
        StringBuilder refs = new StringBuilder();
        for(Ref s : referencias) {
            refs.append(s.toString()).append("::");
        }
        
        props.put("fields", fields.toString());
        props.put("refs", refs.toString());
        props.put("stamp", String.valueOf(System.nanoTime()));
        if(this.cascate) {
            props.put("cascate", "true");
        }
        return props;
    }
    
    /**
     * Realiza as verificações de unicidade e consistência de valores e depois grava
     * todas as entidades no banco de dados.
     * @throws Exception 
     */
    public void flush() throws Exception {
        Transaction tx2 = new Transaction(this);
        tx2.commit();
    }
    
    /**
     * Deleta uma entidade e todas as subentidades cascata.
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws Exception 
     */
    public boolean delete() throws Exception {
        Deleter delete = new Deleter(this);
        delete.commit();
        return false;
    }
    
    /**
     * Método secundário utilizado para exlcuir subentidades.
     * @param sub
     * @return 
     */
    public boolean delete(boolean sub) throws Exception {
        Ereaser.flush(this, this);
        return true;
    }
    
    /**
     * Serializa a entidade encapsulando seus campos.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws Exception 
     */
    public void engarrafar() throws Exception {
        bottledFields.clear();
        List<Field> fields = FieldsManager.getAllSerializebleFields(this.entity.getClass());
        for(Field field : fields) {
            field.setAccessible(true);
            Object valor = field.get(this.entity);
            if(valor != null) {
                if(ClassDictionary.getIndex(valor.getClass()) != -1 || Reflection.isArrayMap(field.getType()) || Reflection.isArrayMap(valor)) {
                    boolean condicional = true;
                    if(valor instanceof File f) {
                        if(!f.exists()) {
                            condicional = false;
                            throw new FileNotFoundException(f.getPath());
                        }
                    }
                    if(condicional) {
                        this.bottledFields.add(Encapsulator.encapsularField(field, valor, this));
                    }
                }
            }
        }
    }
    
    private void load(Class clazz, String id) throws Exception {
        Loader load = new Loader(this);
        this.entity = load.load(clazz, id);
    }
    
    /**
     * Classe ainda em construção para analisar se dessa forma o uso da classe Bottle
     * se tornará mais simples e eficiente.
     * O objetico é criar um construtor unificado e inteligente que consiga definir
     * as formas de carregar e encapsular entidades com mais eficiência, evitando
     * a confusão entre os vários tipos de construtores e o possível uso errado de
     * algum deles.
     */
    public static class BottleBuilder {
        private String TEMP_DB = "./temp/";
        private int modoOperacional = ROOT_STAGE;
        private Map<String, Bottle> bottles = new HashMap<>();
        private List<String> bottledFields = new ArrayList<>();
        private Set<Ref> referencias = new HashSet<>();
        private Map<String, Image> imgs = new HashMap<>();
        private Map<String, File> files = new HashMap<>();
        private Entity entity;
        private Properties props = new Properties();
        private boolean cascate = false;
        private String inicializador = "";
        
        private int index = -1;
        private String id = "";
        
        public BottleBuilder tempDB(String temp) {
            this.TEMP_DB = temp;
            this.modoOperacional = TEMP_STAGE;
            return this;
        }
        
        public BottleBuilder modoOperacional(int modo) {
            this.modoOperacional = modo;
            return this;
        }
        
        public BottleBuilder bottles(Map<String, Bottle> bottles) {
            this.bottles = bottles;
            return this;
        }
        
        public BottleBuilder bottleFields(List<String> fields) {
            this.bottledFields = fields;
            return this;
        }
        
        public BottleBuilder references(Set<Ref> referencias) {
            this.referencias = referencias;
            return this;
        }
        
        public BottleBuilder imgs(Map<String, Image> imgs) {
            this.imgs = imgs;
            return this;
        }
        
        public BottleBuilder files(Map<String, File> files) {
            this.files = files;
            return this;
        }
        
        public BottleBuilder entity(Entity entity) {
            this.entity = entity;
            return this;
        }
        
        public BottleBuilder properties(Properties properties) {
            this.props = properties;
            return this;
        }
        
        public BottleBuilder cascate(boolean cascate) {
            this.cascate = cascate;
            return this;
        }
        
        public BottleBuilder index(int index) {
            this.index = index;
            return this;
        }
        
        public BottleBuilder entityClass(Class clazz) {
            this.index = ClassDictionary.getIndex(clazz);
            return this;
        }
        
        public BottleBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public BottleBuilder inicializador(Entity entity, String id) {
            this.inicializador = inicializador + " -> " + entity.getClass().getName()+" | "+id;
            return this;
        }
        
        public Bottle emptyBuild() {
            return new Bottle();
        }
        
        public Bottle build() throws Exception {
            Bottle bottle = new Bottle();
            bottle.entity = this.entity;
            bottle.TEMP_DB = this.TEMP_DB;
            bottle.modoOperacional = this.modoOperacional;
            bottle.bottles = this.bottles;
            bottle.bottledFields = this.bottledFields;
            bottle.referencias = this.referencias;
            bottle.imgs = this.imgs;
            bottle.files = this.files;
            bottle.props = this.props;
            bottle.cascate = this.cascate;
            
            if(this.TEMP_DB.equals("./temp/")) {
                bottle.defineTemp();
            }
            
            if(this.index != -1 && !this.id.isBlank()) {
                bottle.bottles.put(id, bottle);
                bottle.load(ClassDictionary.fromIndex(index), id);
            } else if(this.entity != null) {
                bottle.bottles.put(this.entity.getId(), bottle);
                bottle.loadRefs();
            } else {
                throw new NullPointerException("Entidade não definida");
            }

            return bottle;
        }
        
    }
    
}