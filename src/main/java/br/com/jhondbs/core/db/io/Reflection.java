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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
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
            List<String> classList = new ArrayList<>();
            String r1 = Reflection.class.getResource("/").getPath();
            File f1 = new File(r1).getParentFile();
            URI uri = f1.toURI();
            Path myPath;
            if (Reflection.class.getResource("/").toURI().getScheme().equals("jar")) {
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
                Class s = makeClass(p);
                if(s != null){
                    pronta.add(s.getName());
                }
            });
            array = pronta;
        }
        return array;
    }
    
    private Class makeClass(String str){
        str = str.replaceAll(".class", "").replaceAll("/", ".");
        while(!str.isBlank()){
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                return cl.loadClass(str);
//                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                if(str.contains(".")){
                    str = str.substring(str.indexOf(".")+1);
                } else {
                    return null;
                }
            }
        }
        return null;
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
            return reflexo.stream().filter(path -> {
                try {
                    return isInstance(makeClass(path), classe);
                } catch (Exception ex) {
//                    Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
            })
                    .toList();
        } catch (URISyntaxException | IOException ex) {
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
        return allImplements(classe).stream().filter(clName -> {
            try {
                return !Modifier.isAbstract(makeClass(clName).getModifiers());
            } catch (Exception ex) {
                Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }).toList();
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
    public <T extends Object> T getNewInstance(String className) throws URISyntaxException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception{
        if(!className.isBlank()){
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            String path = reflect().stream().filter(classPath -> (classPath.replaceAll(".class", "").endsWith(className))).iterator().next();
            if(path != null){
                return getNewInstance(makeClass(path));
            }
        }
        throw new ClassNotFoundException("Blank path");
    }
    
    public <T extends Object> T getNewInstance(Class clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Constructor[] constructors = clazz.getDeclaredConstructors();

        Constructor a = null;
        for(Constructor c : constructors){
            if(a == null){
                a = c;
            }
            if(c.getParameterCount() < a.getParameterCount()){
                a = c;
            }
        }

        List<Object> paramObjects = new ArrayList<>();
        Parameter[] parameters = a.getParameters();

        if(parameters.length == 0){
            return (T) clazz.newInstance();
        }

        for(Parameter p : parameters){
            if(Reflection.isInstance(p.getType(), Number.class)){
                if(Reflection.isInstance(p.getType(), BigInteger.class)){
                    paramObjects.add(new BigInteger("0"));
                } else if(Reflection.isInstance(p.getType(), BigDecimal.class)){
                    paramObjects.add(new BigDecimal(0));
                } else {
                    paramObjects.add(Integer.valueOf("0"));
                }
            } else if(Reflection.isInstance(p.getType(), String.class)){
                paramObjects.add("");
            } else if(Reflection.isTinyNumerical(p.getType())){
                paramObjects.add(Short.valueOf("0"));
            } else {
                paramObjects.add(null);
            }
        }
        return (T) a.newInstance(paramObjects.toArray());
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
            if(!son.getClass().getName().startsWith("[")){
                while(son != null){
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
        }
        return false;
    }
    
    /**
     * Checks if the object is a primitive instance.
     * @param object
     * @return 
     */
    public static boolean isPrimitive(Object object){
        return  isPrimitive(object.getClass());
    }
    
    public static boolean isPrimitive(Class clazz){
        return  isInstance(clazz, Byte.class) ||
                isInstance(clazz, Short.class) ||
                isInstance(clazz, Integer.class) ||
                isInstance(clazz, Long.class) ||
                isInstance(clazz, Float.class) ||
                isInstance(clazz, Double.class) ||
                isInstance(clazz, Boolean.class) ||
                isInstance(clazz, String.class) ||
                isInstance(clazz, byte.class) ||
                isInstance(clazz, short.class) ||
                isInstance(clazz, int.class) ||
                isInstance(clazz, long.class) ||
                isInstance(clazz, float.class) ||
                isInstance(clazz, double.class) ||
                isInstance(clazz, boolean.class);
    }
    
    public static boolean isTinyNumerical(Object object){
        Class<? extends Object> aClass = object.getClass();
        return isInstance(aClass, short.class) ||
                isInstance(aClass, int.class) ||
                isInstance(aClass, long.class) ||
                isInstance(aClass, float.class) ||
                isInstance(aClass, double.class);
    }
    
    public static boolean isTinyNumerical(Class clazz){
        return isInstance(clazz, short.class) ||
                isInstance(clazz, int.class) ||
                isInstance(clazz, long.class) ||
                isInstance(clazz, float.class) ||
                isInstance(clazz, double.class);
    }
    
    public static Method getMethod(String name, Class clazz) throws NoSuchMethodException{
        for(Method method : clazz.getMethods()){
            if(method.getName().toUpperCase().equals(name.toUpperCase())){
                return method;
            }
        }
        throw new NoSuchMethodException(name);
    }
    
}