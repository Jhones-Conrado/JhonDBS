/*
 * Copyright (C) 2023 jhonessales
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.jhondbs.core.db.io.capsule;

import br.com.jhondbs.core.db.io.capsule.capsulators.CapsuleArrays;
import br.com.jhondbs.core.db.io.capsule.capsulators.CapsulateObject;
import br.com.jhondbs.core.db.Keys;
import br.com.jhondbs.core.db.base.Entity;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.io.IO;
import br.com.jhondbs.core.db.io.OldCapsule;
import br.com.jhondbs.core.db.io.Reflection;
import br.com.jhondbs.core.db.io.capsule.descapsulators.DescapsulateObject;
import br.com.jhondbs.core.db.io.letters.BooleanLetter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhonessales
 */
public class Capsule {
    
    private Object object;
    
    private StringBuilder capsule = new StringBuilder();
    
    private List<Entity> entities = new ArrayList<>();
    private BooleanLetter letter = new BooleanLetter(true);
    
    private Entity superente;
    private Entity root;
    
    private boolean isroot;
    private boolean isSerializable;
    
    /**
     * Chamada inicial com objeto inicial.
     * @param object 
     */
    public Capsule(Object object) {
        this.object = object;
        if(Reflection.isInstance(object.getClass(), Entity.class)){
            this.root = (Entity) object;
            this.isroot = true;
        }
        this.isSerializable = true;
    }
    
    
    /**
     * Chamada para deserializar objetos.
     * @param capsuleContent 
     */
    public Capsule(String capsuleContent) {
        this.capsule.append(capsuleContent);
        this.isSerializable = false;
    }
    
    /**
     * Chamada terciária, pronta para processo serialização.
     * @param object Objeto a ser serializado.
     * @param entities Lista de entidades a serem salvas ao final.
     * @param letter Carta de sucesso das operações.
     * @param superente Superente da entidade atual.
     * @param root Raiz de todo o processo de serialização.
     */
    public Capsule(Object object, List<Entity> entities, BooleanLetter letter, Entity superente, Entity root){
        this.object = object;
        this.entities = entities;
        this.letter = letter;
        this.superente = superente;
        this.root = root;
        this.isroot = false;
        this.isSerializable = true;
    }
    
    /**
     * Chamada incial de serialização.
     * @return 
     */
    public String make(){
        if(isSerializable){
            String m = make2();
            this.capsule.append(m);

            if(isroot){
                if(!entities.isEmpty()){
                    entities.forEach(ente -> {
                        try {
                            String path = IO.getDBFolderWithID(ente);
                            String oldpath = IO.getDBFolder(ente)+"/old"+String.valueOf(ente.getEnteId());
                            File f = new File(path);
                            if(f.exists()){
                                f.renameTo(new File(oldpath));
                            }
                            String newpath = IO.getDBFolder(ente)+"/new"+String.valueOf(ente.getEnteId());
                            File novo = new File(newpath);
                            if(novo.exists()){
                                novo.renameTo(new File(path));
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                            letter.setBool(false);
                        }
                    });
                }

                if(letter.isBool()){
                    entities.forEach(ente -> {
                        try {
                            String oldpath = IO.getDBFolder(ente)+"/old"+String.valueOf(ente.getEnteId());
                            File old = new File(oldpath);
                            old.delete();
                        } catch (Exception ex) {
                            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                            letter.setBool(false);
                        }
                    });
                } else {
                    entities.forEach(ente -> {
                        try {
                            String newpath = IO.getDBFolder(ente)+"/new"+String.valueOf(ente.getEnteId());
                            File novo = new File(newpath);
                            novo.delete();
                        } catch (Exception ex) {
                            Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                            letter.setBool(false);
                        }
                    });
                }
            }

            return m;
        }
        return null;
    }
    
    /**
     * Chamada secundária de serialização.
     * @param object
     * @param entities
     * @param letter
     * @param superente
     * @param root
     * @return 
     */
    private String make2(){
        StringBuilder sb = new StringBuilder();
        
//        ############# DIFERENTE DE ENTIDADE
        if(!Reflection.isInstance(object.getClass(), Entity.class)){
//            ############# PRIMITIVO
            if(Reflection.isPrimitive(object) || Reflection.isInstance(object.getClass(), Number.class)){
                sb.append(CapsulateObject.encapsulate(object, entities, letter, superente, root));
//            ############# ARRAYS
            } else if(ClassDictionary.isArrayMap(object)){
                sb.append(CapsuleArrays.encapMultiples(object, entities, letter, superente, root));
//            ############# DATAS
            } else if(ClassDictionary.isDate(object)){
                sb.append(CapsulateObject.encapDate(object));
//            ############# OBJETOS
            } else {
                sb.append(CapsulateObject.encapsulate(object, entities, letter, superente, root));
            }
        } else {
//            ############# ENTIDADE
            entities.add((Entity) object);
            Entity e = (Entity) object;
            try {
                Keys.gerarId(e);
                
                //Identifica se é uma raiz aqui.
                String s = CapsulateObject.encapsulate(e, this.entities, this.letter, this.superente, this.root);
                new File(IO.getDBFolder(e)).mkdirs();
                String path = IO.getDBFolder(e)+"/new"+String.valueOf(e.getEnteId());
                try(BufferedWriter w = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)){
                    w.write(s);
                    w.flush();
                    w.close();
                }
                return "{"+ClassDictionary.getIndex(object.getClass())+":"+String.valueOf(e.getEnteId())+"}";
            } catch (Exception ex) {
                Logger.getLogger(Capsule.class.getName()).log(Level.SEVERE, null, ex);
                letter.setBool(false);
            }
        }
        
        return sb.toString();
    }
    
    public String getCapsule(){
        return this.capsule.toString();
    }
    
    public <T extends Object> T extract(){
        return DescapsulateObject.extract(this.capsule.toString());
    }
    
    /**
     * Method that will completely delete the object's branch.
     * Including all sub entities of the branch.
     */
    public void fullDelete(){
        try {
            Entity entity = (Entity) this.object;
            try(BufferedReader r = Files.newBufferedReader(Paths.get(new File(IO.getDBFolderWithID(entity)).getPath()))) {
                String line = r.readLine();
                OldCapsule capsule = new OldCapsule(line);
                fullDelete(capsule.extract());
            } catch (Exception ex) {
                Logger.getLogger(OldCapsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassCastException e){
            fullDeleteObj(object);
        }
    }
    
     /**
     * Method that will completely delete the object's branch.
     * Including all sub entities of the branch.
     */
    private void fullDeleteObj(Object object){
        List<Field> fields = FieldsManager.getAllFields(object);
        fields.forEach(field -> {
            try {
                if(!Reflection.isPrimitive(field.getType())){
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if(Reflection.isInstance(value.getClass(), Entity.class)){
                        fullDelete((Entity) value);
                    } else {
                        fullDeleteObj(value);
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
            } catch (Exception ex) {
                Logger.getLogger(OldCapsule.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    /**
     * Method that will completely delete the object's branch.
     * Including all sub entities of the branch.
     * @param entity Entity that opens the branch to be deleted.
     */
    private void fullDelete(Entity entity) throws Exception{
        List<Field> fields = FieldsManager.getAllFields(entity);
        fields.forEach(field -> {
            if(Reflection.isInstance(field.getType(), Entity.class)){
                try {
                    field.setAccessible(true);
                    Entity e = (Entity) field.get(entity);
                    if(e != null){
                        fullDelete(e);
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                } catch (Exception ex) {
                    Logger.getLogger(OldCapsule.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if(Reflection.isInstance(field.getType(), List.class)){
                try {
                    field.setAccessible(true);
                    List list = (List) field.get(entity);
                    if(list != null){
                        list.forEach(obj -> {
                            if(Reflection.isInstance(obj.getClass(), Entity.class)){
                                try {
                                    fullDelete(((Entity) obj));
                                } catch (Exception ex) {
                                    Logger.getLogger(OldCapsule.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                }
            } else if(Reflection.isInstance(field.getType(), Map.class)){
                try {
                    field.setAccessible(true);
                    Map map = (Map) field.get(entity);
                    if(map != null){
                        map.keySet().forEach(key -> {
                            if(Reflection.isInstance(map.get(key).getClass(), Entity.class)){
                                try {
                                    fullDelete(((Entity) map.get(key)));
                                } catch (Exception ex) {
                                    Logger.getLogger(OldCapsule.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                }
            } else if(field.getType().getClass().getName().startsWith("[")){
                try {
                    field.setAccessible(true);
                    Object[] array = (Object[]) field.get(entity);
                    if(array != null){
                        Arrays.asList(array).forEach(obj -> {
                            if(Reflection.isInstance(obj.getClass(), Entity.class)){
                                try {
                                    fullDelete(((Entity) obj));
                                } catch (Exception ex) {
                                    Logger.getLogger(OldCapsule.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                } catch(IllegalAccessException | IllegalArgumentException e){
                }
            }
        });
        File ente = new File(IO.getDBFolderWithID(entity));
        if(ente.exists()){
            ente.delete();
        }
    }
    
}
