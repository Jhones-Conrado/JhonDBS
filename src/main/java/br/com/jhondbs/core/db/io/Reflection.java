/*
 * Copyright (C) 2022 jhonesconrado
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
package br.com.jhondbs.core.db.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ENGLISH<br>
 * It will go through the folder where the program was run, looking for .jar or 
 * .class files, checking all it finds and putting them in a List.</br>
 * Items in this List can be further filtered to create instances of specific
 * objects, as in the case of interpreters that do not need to be instantiated
 * to function.
 * <br><br>
 * PORTUGUÊS<br>
 * Percorrerá a pasta em que o programa foi executado, procurando por arquivos
 * .jar ou .class, verificando todos que encontrar e os colocando em uma List.</br>
 * Os itens dessa List podem ser filtrados posteriormente para se criar instâncias
 * de objetos específicos, como no caso dos interpretadores que não precisam ser
 * instanciados para funcionar.
 * @author jhonesconrado
 */
public final class Reflection {
    
    private static List<String> array;
    
    /**
     * Creates a new instance of Reflection.
     * This class serves to quickly and easily reflect all the files
     * inside the jar or project.
     * <br><br>
 Cria uma nova instância de Reflection. Essa classe serve para reflect de
 forma rápida e fácil todos os arquivos dentro do jar ou projeto.
     * @throws IOException 
     */
    public Reflection() throws IOException {
    }
    
    /**
     * Brings a list of all files inside the jar or project.<br>
     * Traz uma lista com todos os arquivos dentro do jar ou projeto.
     * @return Lista com todos os arquivos do jar ou projeto.
     * @throws URISyntaxException
     * @throws IOException 
     */
    public List<String> reflect() throws URISyntaxException, IOException{
        if(array == null){
            String name = Reflection.class.getName();
            String init = name.substring(0, name.indexOf("."));
            List<String> classList = new ArrayList<>();
            URI uri = Reflection.class.getResource("/").toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                myPath = fileSystem.getPath("/");
                Stream<Path> walk = Files.walk(myPath, 100);
                walk.forEach( t -> {
                    classList.add(t.toString());
                });
            } else {
                myPath = Paths.get(uri);
                getFiles(classList, new File(myPath.toString()));
            }
            List<String> pronta = new ArrayList<>();
            classList.forEach(p -> {
                pronta.add(p.substring(p.indexOf("/"+init)+1));
            });
            array = pronta;
        }
        return array;
    }
    
    /**
     * Creates a recursive call that will scan all files found inside the jar or
     * project and add the Path in the form of a String in the container list.
     * <br><br>
     * Cria uma chamada recursiva que vai varrer todos os arquivos encontrados
     * dentro do jar ou projeto e adicionar o Path em forma de String na lista
     * de container.
     * @param container List that will receive the file path Strings.<br>
     * List que receberá as Strings de path dos arquivos.
     * @param folder Folder to be scanned.<br>
     * Pasta que será varrida.
     */
    private void getFiles(List<String> container, File folder){
        for(File f : folder.listFiles()){
            if(f.isFile()){
                container.add(f.getPath());
            } else if(f.isDirectory()){
                getFiles(container, f);
            }
        }
    }
    
    /**
     * ENGLISH<br>
     * Returns a list of all classes that extend or implement the given class
     * or interface. Ex: if I inform the Serializible interface as a parameter,
     * a list of all classes that implement serializable or that extend some
     * class that implements it will be returned.
     * <br><br>
     * PORTUGUÊS<br>
     * Retorna uma lista de todas as classes que extendem ou implementam a classe
     * ou interface informada. Ex: se eu informar a interface Serializible como
     * parâmetro, será retornado a lista de todas as classes que implementam
     * serializible ou que extendem alguma classe que implemente a mesma.
     * @param classe A ser pesquisada por implementações ou extensões.
     * @return Lista de extensões e implementações.
     */
    public List<String> allImplements(Class classe){
        List<String> retorno = new ArrayList<>();
        try {
            List<String> reflexo = reflect();
            for(String s : reflexo){
                Class tempClass = StringToClass(s);
                if(tempClass != null){
                    while(tempClass != null && tempClass != Object.class){
                        if(isInstance(tempClass, classe) || 
                                Arrays.asList(tempClass.getInterfaces()).contains(classe)){
                            retorno.add(s);
                            break;
                        }
                        Arrays.asList(tempClass.getAnnotations()).forEach(annotation -> {
                            if(annotation.annotationType() == classe){
                                retorno.add(s);
                            }
                        });
                        tempClass = tempClass.getSuperclass();
                    }
                }
            }
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }
    
    /**
     * ENGLISH<br>
     * Returns a list of all classes that implement, extend, or annotate 'class'
     * that are not abstract and have an empty constructor.
     * That is, classes that need mandatory parameters in the constructor will
     * not appear in the list.
     * <br><br>
     * PORTUGUÊS<br>
     * Retorna uma lista de todas as classes que implementam, extendem ou anotam
     * de 'classe' e que não sejam abstratas e possuam um construtor vazio. </br>
     * ou seja, classes que precisam de parâmetros obrigatórios no construtor não
     * aparecerão na lista.
     * @param classe
     * @return 
     */
    public List<String> allImplementsNotAbstract(Class classe){
        List<String> retorno = new ArrayList<>();
        allImplements(classe).forEach(className -> {
            try {
                if(getNewInstance(className) != null){
                    retorno.add(className);
                }
            } catch (URISyntaxException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            }
        });
        return retorno;
    }
    
    /**
     * Looks for the first occurrence of the class name in the project and tries
     * to create a new instance of it.
     * <br><br>
     * Procura a primeira ocorrência do nome de classe no projeto e tenta criar
     * uma nova instância da mesma.
     * @param <T>
     * @param className Class Name - Name only, no packages or extensions.<br>
     * Nome da classe - Somente o nome, sem pacotes ou extensões.
     * @return Nova instância da classe solicitada.
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public <T extends Object> T getNewInstance(String className) throws URISyntaxException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        while(className.endsWith("/")){
            className = className.substring(0, className.length()-1);
        }
        if(!className.isBlank()){
            if(className.contains("/")){
                className = className.substring(className.lastIndexOf("/")+1);
            }
            String name = className.replaceAll(".class", "");
            String path = reflect()
                    .stream()
                    .filter(f -> f.contains(name))
                    .collect(Collectors.toList())
                    .get(0)
                    .replaceAll("/", ".").replaceAll(".class", "");
            //Chamada recursiva que vai limpar o caminho da classe.
            while(true){
                try {
                    Class.forName(path);
                    break;
                } catch (ClassNotFoundException classNotFoundException) {
                    if(path.contains(".")){
                        path = path.substring(path.indexOf(".")+1);
                    } else {
                        break;
                    }
                }
            }
            return (T) Class.forName(path).newInstance();
        }
        throw new ClassNotFoundException("Blank path");
    }
    
    /**
     * Returns a Class object from a String path reference to the class.
     * <br><br>
     * Retorna um objeto Class a partir de uma String de referência de caminho para
     * a classe.
     * @param path Caminho para a classe.
     * @return Objeto de classe.
     */
    private Class StringToClass(String path) throws Exception{
        if(path.endsWith(".class")){
            try {
                String root = Reflection.class.getName();
                root = root.substring(0, root.indexOf("."));
                String tempPath = path;

                try {
                    tempPath = tempPath.substring(tempPath.indexOf("/" + root));
                } catch (Exception e) {
                }

                String className = tempPath.substring(path.lastIndexOf("/")+1);
                return Class.forName(reflect()
                        .stream()
                        .filter(filtered -> filtered.contains(className))
                        .collect(Collectors.toList())
                        .get(0)
                        .replaceAll("/", ".").replaceAll(".class", "")
                );
            } catch (URISyntaxException | IOException | ClassNotFoundException ex) {
            }
        }
        throw new Exception("The path does not refer to a class.");
    }
    
    /**
     * Checks from within a class to see if it is an instance of some other superclass.
     * <br><br>
     * Faz uma verificação a partir de uma classe para identificar se é uma instância
     * de alguma outra superclass.
     * @param son
     * @param dad
     * @return 
     */
    public static boolean isInstance(Class son, Class dad){
        if(son != null && dad != null){
            while(son != Object.class){
                if(Arrays.asList(son.getInterfaces()).contains(dad)){
                    return true;
                } else if(Arrays.asList(son.getAnnotations())
                            .stream()
                            .filter(an -> (an.annotationType() == dad))
                            .count() > 0){
                    return true;
                }
                if(son == dad){
                    return true;
                }
                son = son.getSuperclass();
                if(son == null){
                    break;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if the object is a primitive instance.
     * @param object
     * @return 
     */
    public static boolean isPrimitive(Object object){
        Class<? extends Object> aClass = object.getClass();
        return  isInstance(aClass, Byte.class) ||
                isInstance(aClass, Short.class) ||
                isInstance(aClass, Integer.class) ||
                isInstance(aClass, Long.class) ||
                isInstance(aClass, Float.class) ||
                isInstance(aClass, Double.class) ||
                isInstance(aClass, Boolean.class) ||
                isInstance(aClass, String.class) ||
                isInstance(aClass, byte.class) ||
                isInstance(aClass, short.class) ||
                isInstance(aClass, int.class) ||
                isInstance(aClass, long.class) ||
                isInstance(aClass, float.class) ||
                isInstance(aClass, double.class) ||
                isInstance(aClass, boolean.class);
    }
    
}