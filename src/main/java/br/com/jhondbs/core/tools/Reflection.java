/*
 * The MIT License
 *
 * Copyright 2024 Jhones Sales.
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
package br.com.jhondbs.core.tools;

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
import java.time.Instant;
import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public static List<String> reflect() throws URISyntaxException, IOException{
        if(array == null){
            List<String> classList = new ArrayList<>();
            String r1 = Reflection.class.getResource("/").getPath();
            File f1 = new File(r1).getParentFile();
            URI uri = f1.toURI();
            Path myPath;
            if (Reflection.class.getResource("/").toURI().getScheme().equals("jar")) {
                uri = Reflection.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
                    myPath = fileSystem.getPath("/");
                    Stream<Path> walk = Files.walk(myPath, 100);
                    walk.forEach( t -> {
                        classList.add(t.toString());
                    });
                }
            } else {
                myPath = Paths.get(uri);
                getFiles(classList, new File(myPath.toString().replaceAll("%20", " ")));
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
    
    /**
     * Tenta criar uma instância de classe a partir do caminho de classe.
     * @param str Caminho da classe.
     * @return Instância da classe.
     */
    private static Class makeClass(String str){
        str = str.replaceAll(".class", "").replaceAll("/", ".");
        while(!str.isBlank()){
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class c = cl.loadClass(str);
                if(c != null){
                    return c;
                }
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
    private static void getFiles(List<String> container, File folder){
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
    public static List<String> allImplements(Class classe){
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
    public static List<String> allImplementsNotAbstract(Class classe){
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
    public static <T extends Object> T getNewInstance(String className) throws URISyntaxException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception{
        if(!className.isBlank()){
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                return (T) cl.loadClass(className).newInstance();
            } catch (Exception e) {
                try {
                    return (T) Class.forName(className).newInstance();
                } catch (Exception ex) {
                    List<String> list = reflect().stream().filter(classPath -> (classPath.replaceAll(".class", "").endsWith(className))).toList();
                    if(list.size() > 0){
                        String path = list.get(0);
                        return getNewInstance(makeClass(path));
                    }
                }
            }
        }
        throw new ClassNotFoundException("Blank path or not found.");
    }
    
    public static <T extends Object> T getNewInstance(Class clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException{
        Constructor[] constructors = Thread.currentThread().getContextClassLoader().loadClass(clazz.getName()).getDeclaredConstructors();

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
            return (T) Thread.currentThread().getContextClassLoader().loadClass(clazz.getName()).newInstance();
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
    
    public static <T> T getNewInstance(Class<T> clazz, ClassLoader loader) 
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
                   InvocationTargetException, ClassNotFoundException, NoSuchMethodException {

        // Carrega a classe usando o ClassLoader fornecido
        Class<?> loadedClass = Class.forName(clazz.getName(), true, loader);

        // Obtém todos os construtores declarados da classe
        Constructor<?>[] constructors = loadedClass.getDeclaredConstructors();

        // Seleciona o construtor com o menor número de parâmetros
        Constructor<?> selectedConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (selectedConstructor == null || constructor.getParameterCount() < selectedConstructor.getParameterCount()) {
                selectedConstructor = constructor;
            }
        }

        if (selectedConstructor == null) {
            throw new NoSuchMethodException("Nenhum construtor encontrado para a classe: " + clazz.getName());
        }

        // Prepara os parâmetros para o construtor
        Object[] paramObjects = prepareConstructorParameters(selectedConstructor.getParameters());

        // Cria uma nova instância usando o construtor selecionado
        return (T) selectedConstructor.newInstance(paramObjects);
    }

    private static Object[] prepareConstructorParameters(Parameter[] parameters) {
        List<Object> paramObjects = new ArrayList<>();
        for (Parameter parameter : parameters) {
            Class<?> paramType = parameter.getType();
            if (Reflection.isInstance(paramType, Number.class)) {
                if (paramType.equals(BigInteger.class)) {
                    paramObjects.add(new BigInteger("0"));
                } else if (paramType.equals(BigDecimal.class)) {
                    paramObjects.add(new BigDecimal("0"));
                } else {
                    paramObjects.add(0); // Usar Integer.valueOf(0) seria redundante aqui
                }
            } else if (paramType.equals(String.class)) {
                paramObjects.add("");
            } else if (Reflection.isTinyNumerical(paramType)) {
                paramObjects.add((short) 0); // Usar Short.valueOf("0") é redundante
            } else {
                paramObjects.add(null);
            }
        }
        return paramObjects.toArray();
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
            if(son.getName().contains(dad.getName())) {
                return true;
            }
            if(dad.isAssignableFrom(son)) {
                return true;
            }
            if(!son.getName().startsWith("[")){
                
                while(son != null){
                    
                    if(son == dad || son.getName().equals(dad.getName())){
                        return true;
                    }
                    if(Arrays.asList(son.getInterfaces()).contains(dad)){
                        return true;
                    } else if(Arrays.asList(son.getAnnotations()).contains(dad)){
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
    
    /**
     * Verifica se uma classe é do tipo primitivo, ou seja, se é um número básico,
     * um boleano, um byte ou até mesmo uma String.
     * @param clazz
     * @return 
     */
    public static boolean isPrimitive(Class clazz){
        return  isInstance(clazz, Byte.class) ||
                isInstance(clazz, Short.class) ||
                isInstance(clazz, Integer.class) ||
                isInstance(clazz, Long.class) ||
                isInstance(clazz, Float.class) ||
                isInstance(clazz, Double.class) ||
                isInstance(clazz, Boolean.class) ||
                isInstance(clazz, Character.class) ||
                isInstance(clazz, String.class) ||
                isInstance(clazz, byte.class) ||
                isInstance(clazz, char.class) ||
                isInstance(clazz, short.class) ||
                isInstance(clazz, int.class) ||
                isInstance(clazz, long.class) ||
                isInstance(clazz, float.class) ||
                isInstance(clazz, double.class) ||
                isInstance(clazz, boolean.class);
    }
    
    /**
     * Verifica se um objeto pertence ao tipo numérico declarado em minúsculo.
     * Perceba a diferença entre "long" e "Long" por exemplo.
     * @param object
     * @return 
     */
    public static boolean isTinyNumerical(Object object){
        Class<? extends Object> aClass = object.getClass();
        return isInstance(aClass, short.class) ||
                isInstance(aClass, int.class) ||
                isInstance(aClass, long.class) ||
                isInstance(aClass, float.class) ||
                isInstance(aClass, double.class);
    }
    
    /**
     * Verifica se uma classe pertence ao tipo numérico declarado em minúsculo.
     * Perceba a diferença entre "long" e "Long" por exemplo.
     * @param clazz
     * @return 
     */
    public static boolean isTinyNumerical(Class clazz){
        return isInstance(clazz, short.class) ||
                isInstance(clazz, int.class) ||
                isInstance(clazz, long.class) ||
                isInstance(clazz, float.class) ||
                isInstance(clazz, double.class);
    }
    
    /**
     * Verifica se uma classe é do tipo numérico de forma genérica.
     * @param clazz
     * @return 
     */
    public static boolean isNumerical(Class clazz){
        return isInstance(clazz, Number.class);
    }
    
    /**
     * Verifica se um objeto é do tipo lista, mapa ou até mesmo array.
     * @param object
     * @return 
     */
    public static boolean isArrayMap(Object object){
        boolean um = object.toString().contains(List.class.getName()) || object.toString().contains(Map.class.getName());
        boolean dois = false;
        
        if(object instanceof Class) {
            dois = ((Class) object).getName().contains("[");
        }
        
        return
                isInstance(object.getClass(), List.class) ||
                isInstance(object.getClass(), Set.class) ||
                isInstance(object.getClass(), Map.class) ||
                object.getClass().getName().contains("[") ||
                um || dois;
    }
    
    /**
     * Verifica se o objeto é algum tipo de marcação de data.
     * @param object
     * @return 
     */
    public static boolean isDate(Object object) {
        if(object instanceof Class) {
            return isDate((Class) object);
        }
        return isDate(object.getClass());
    }
    
    /**
     * Verifica se uma classe pertence à algum tipo de marcação de data.
     * @param clazz
     * @return 
     */
    public static boolean isDate(Class clazz){
        return
                isInstance(clazz, Date.class) ||
                isInstance(clazz, Calendar.class) ||
                isInstance(clazz, Temporal.class) ||
                isInstance(clazz, Instant.class) ||
                isInstance(clazz, Period.class) ||
                isInstance(clazz, ChronoPeriod.class);
    }
    
    /**
     * Retorna um método de uma classe a partir do seu nome.
     * @param name
     * @param clazz
     * @return
     * @throws NoSuchMethodException 
     */
    public static Method getMethod(String name, Class clazz) throws NoSuchMethodException{
        for(Method method : clazz.getMethods()){
            if(method.getName().toUpperCase().equals(name.toUpperCase())){
                return method;
            }
        }
        throw new NoSuchMethodException(name);
    }
    
}