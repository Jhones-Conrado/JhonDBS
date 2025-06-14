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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    private static final Set<Class<?>> PRIMITIVE_TYPES = Set.of(
        byte.class, Byte.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class,
        boolean.class, Boolean.class,
        char.class, Character.class,
        String.class
    );
    
    private static final Set<Class<?>> TINY_NUMERICAL = Set.of(
            short.class,
            int.class,
            long.class,
            float.class,
            double.class
    );
    
    private static final Set<Class<?>> ARRAY_TYPES = Set.of(
            List.class,
            Set.class,
            Map.class
    );
    
    private static final Set<Class<?>> DATE_TYPES = Set.of(
            Date.class,
            Calendar.class,
            Temporal.class,
            Instant.class,
            Period.class,
            ChronoPeriod.class
    );
    
    private static volatile List<String> array; // volatile para thread-safety
    private static List<String> notAbstracts;
    
    private static ConcurrentHashMap<String, Class<?>> CLASSES;
    
    static {
        if(CLASSES == null) {
            CLASSES = new ConcurrentHashMap<>();
        }
        
        if (CLASSES.isEmpty()) {
            synchronized (Reflection.class) {
                if (CLASSES.isEmpty()) {
                    try {
                        List<String> classList = new ArrayList<>();
                        ClassLoader classLoader = Reflection.class.getClassLoader();
                        
                        // Obtém todos os recursos disponíveis no classpath
                        Enumeration<URL> resources = classLoader.getResources("");
                        while (resources.hasMoreElements()) {
                            URL resource = resources.nextElement();
                            String protocol = resource.getProtocol();
                            
                            if ("file".equals(protocol)) {
                                // Diretório de classes no sistema de arquivos
                                File directory = new File(resource.getPath());
                                getFiles(classList, directory);
                            } else if ("jar".equals(protocol)) {
                                // Classes dentro de um JAR
                                try (FileSystem fileSystem = FileSystems.newFileSystem(resource.toURI(), Collections.emptyMap())) {
                                    Path myPath = fileSystem.getPath("/");
                                    Files.walk(myPath, 100).forEach(path -> classList.add(path.toString()));
                                } catch (Exception e) {
                                    Logger.getLogger(Reflection.class.getName(), protocol).log(Level.SEVERE, null, e);
                                }
                            }
                        }
                        
                        classList.stream()
                                .filter(item -> item.endsWith(".class"))
                                .toList()
                                .forEach(path -> {
                                    Class<?> instance = makeClass(path);
                                    if (instance != null) {
                                        CLASSES.putIfAbsent(path, instance);
                                    }
                                });
                    } catch (IOException ex) {
                        Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    
    /**
     * Creates a new instance of Reflection.
     * This class serves to quickly and easily reflect all the files
     * inside the jar or project.
     * <br><br>
     * Cria uma nova instância de Reflection. Essa classe serve para reflect de
     * forma rápida e fácil todos os arquivos dentro do jar ou projeto.
     * @throws IOException 
     */
    public Reflection() throws IOException {
    }
    
    /**
     * Tenta criar uma instância de classe a partir do caminho de classe.
     * @param className Caminho da classe.
     * @return Instância da classe.
     */
    public static Class makeClass(String className) {
        className = className.trim();
        if(className.charAt(0) == '.' && className.length() > 1) className = className.substring(1);
        
        if(CLASSES.containsKey(className)) {
            return CLASSES.get(className);
        } else if(CLASSES.containsKey(className.replace(".class", "").replace("/", ".")+".class")) {
            return CLASSES.get(className.replace(".class", "").replace("/", ".")+".class");
        }
        
        if(className.endsWith(".class")) className = className.substring(0, className.length() - ".class".length());
        className = className.replace("/", ".");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while(!className.isBlank()){
            try {
                if(className.startsWith(".")) className = className.substring(1);
                Class c = cl.loadClass(className);
                if(c != null) return c;
            } catch (ClassNotFoundException e) {
                if(className.contains(".")){
                    className = className.substring(className.indexOf(".")+1);
                } else {
                    return null;
                }
            } catch (Throwable t) {
                if(className.contains(".")){
                    className = className.substring(className.indexOf(".")+1);
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
    public static List<Class<?>> allImplements(Class classe) throws IOException {
        return classe == null ? new ArrayList<>() : CLASSES.values().stream().filter(cl -> classe.isAssignableFrom(cl)).toList();
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
    public static List<Class<?>> allImplementsNotAbstract(Class classe) throws URISyntaxException, IOException{
        return classe == null ? new ArrayList<>() : allImplements(classe)
                .stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .toList();
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
    public static <T> T getNewInstance(String className) throws URISyntaxException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException{
        if(className == null || className.isBlank()) {
            throw new NullPointerException("className não pode nulo ou branco.");
        }
        
        Class found = makeClass(className);
        return getNewInstance(found);
    }
    
    public static <T> T getNewInstance(Class clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException{
        return (T) getNewInstance(clazz, Thread.currentThread().getContextClassLoader());
    }
    
    public static <T> T getNewInstance(Class<T> clazz, ClassLoader loader) 
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
                   InvocationTargetException, ClassNotFoundException, NoSuchMethodException {
        if(clazz == null) throw new IllegalArgumentException("A classe não pode ser nula.");
        
        // Carrega a classe usando o ClassLoader fornecido
        Class<?> loadedClass = Class.forName(clazz.getName(), true, loader);
        
        Constructor[] constructors = Thread.currentThread().getContextClassLoader().loadClass(clazz.getName()).getDeclaredConstructors();
        Constructor constructor = null;
        for (Constructor c : constructors) {
            if (constructor == null || c.getParameterCount() < constructor.getParameterCount()) {
                constructor = c;
            }
        }
        
        try {
            return (T) loadedClass.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            if (constructor == null) {
                throw new NoSuchMethodException("Nenhum construtor encontrado para a classe: " + clazz.getName());
            }
            constructor.setAccessible(true); // Garante acesso ao construtor, mesmo que ele seja privado
            Object[] paramObjects = prepareConstructorParameters(constructor.getParameters());
            Class<?>[] types = constructor.getParameterTypes();
            Constructor<?> cons = loadedClass.getConstructor(types);
            cons.setAccessible(true);
            Object ins = cons.newInstance(paramObjects);
            return (T) ins;
        }
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
        return clazz != null && PRIMITIVE_TYPES.contains(clazz);
    }
    
    /**
     * Verifica se um objeto pertence ao tipo numérico declarado em minúsculo.
     * Perceba a diferença entre "long" e "Long" por exemplo.
     * @param object
     * @return 
     */
    public static boolean isTinyNumerical(Object object){
        return object == null ? false : isTinyNumerical(object.getClass());
    }
    
    /**
     * Verifica se uma classe pertence ao tipo numérico declarado em minúsculo.
     * Perceba a diferença entre "long" e "Long" por exemplo.
     * @param clazz
     * @return 
     */
    public static boolean isTinyNumerical(Class clazz){
        return clazz != null && TINY_NUMERICAL.contains(clazz);
    }
    
    /**
     * Verifica se uma classe é do tipo numérico de forma genérica.
     * @param clazz
     * @return 
     */
    public static boolean isNumerical(Class clazz){
        return Number.class.isAssignableFrom(clazz);
    }
    
    /**
     * Verifica se um objeto é do tipo lista, mapa ou até mesmo array.
     * @param object
     * @return 
     */
    public static boolean isArrayMap(Object object){
        if(object == null) return false;
        return isArrayMap(object.getClass());
    }
    
    public static boolean isArrayMap(Class clazz) {
        return clazz == null ? false : (clazz.getName().startsWith("[") || ARRAY_TYPES.stream().anyMatch(c -> c.isAssignableFrom(clazz)));
    }
    
    /**
     * Verifica se o objeto é algum tipo de marcação de data.
     * @param object
     * @return 
     */
    public static boolean isDate(Object object) {
        return object == null ? false : isDate(object.getClass());
    }
    
    /**
     * Verifica se uma classe pertence à algum tipo de marcação de data.
     * @param clazz
     * @return 
     */
    public static boolean isDate(Class clazz){
        return clazz == null ? false : DATE_TYPES.stream().anyMatch(c -> c.isAssignableFrom(clazz));
    }
    
    public static boolean isComplexObject(Class clazz) {
        return !isPrimitive(clazz) && !isDate(clazz);
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