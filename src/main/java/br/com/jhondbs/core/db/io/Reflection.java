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
     * Cria uma nova instância de Reflection. Essa classe serve para refletir de
     * forma rápida e fácil todos os arquivos dentro do jar ou projeto.
     * @throws IOException 
     */
    public Reflection() throws IOException {
    }
    
    /**
     * Traz uma lista com todos os arquivos dentro do jar ou projeto.
     * @return Lista com todos os arquivos do jar ou projeto.
     * @throws URISyntaxException
     * @throws IOException 
     */
    public List<String> refletir() throws URISyntaxException, IOException{
        if(array == null){
            String name = Reflection.class.getName();
            String init = name.substring(0, name.indexOf("."));
            List<String> lc = new ArrayList<>();
            URI uri = Reflection.class.getResource("/").toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                myPath = fileSystem.getPath("/");
                Stream<Path> walk = Files.walk(myPath, 100);
                walk.forEach( t -> {
                    lc.add(t.toString());
                });
            } else {
                myPath = Paths.get(uri);
                getFiles(lc, new File(myPath.toString()));
            }
            List<String> pronta = new ArrayList<>();
            lc.forEach(p -> {
                pronta.add(p.substring(p.indexOf("/"+init)+1));
            });
            array = pronta;
        }
        return array;
    }
    
    /**
     * Cria uma chamada recursiva que vai varrer todos os arquivos encontrados
     * dentro do jar ou projeto e adicionar o Path em forma de String na lista
     * de container.
     * @param container List que receberá as Strings de path dos arquivos.
     * @param folder Pasta que será varrida.
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
            List<String> reflexo = refletir();
            for(String s : reflexo){
                Class cl = StringToClass(s);
                if(cl != null){
                    Class c = cl;
                    while(c != null && c != Object.class){
                        if(c.isInstance(classe) || c.getName().equals(classe.getName())
                                || Arrays.asList(c.getInterfaces()).contains(classe)){
                            retorno.add(s);
                            break;
                        }
                        Arrays.asList(c.getAnnotations()).forEach(a -> {
                            if(a.annotationType() == classe){
                                retorno.add(s);
                            }
                        });
                        c = c.getSuperclass();
                    }
                }
            }
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }
    
    /**
     * Retorna uma lista de todas as classes que implementam, extendem ou anotam
     * de 'classe' em que não sejam abstratas e possuam um construtor vazio. </br>
     * ou seja, classes que precisam de parâmetros obrigatórios no construtor não
     * aparecerão na lista.
     * @param classe
     * @return 
     */
    public List<String> allImplementsNotAbstract(Class classe){
        List<String> retorno = new ArrayList<>();
        allImplements(classe).forEach(c -> {
            try {
                if(getNewInstance(c) != null){
                    retorno.add(c);
                }
            } catch (URISyntaxException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
//                Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return retorno;
    }
    
    /**
     * Procura a primeira ocorrência do nome de classe no projeto e tenta criar
     * uma nova instância da mesma.
     * @param <T>
     * @param className Nome da classe - Somente o nome, sem pacotes ou extensões.
     * @return Nova instância da classe solicitada.
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public <T extends Object> T getNewInstance(String className) throws URISyntaxException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        if(className.contains("/")){
            className = className.substring(className.lastIndexOf("/")+1);
        }
        String name = className.replaceAll(".class", "");
        String root = this.getClass().getName().substring(0, this.getClass().getName().indexOf("."));
        String path = refletir()
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
    
    /**
     * Retorna um objeto Class a partir de uma String de referência de caminho para
     * a classe.
     * @param path Caminho para a classe.
     * @return Objeto de classe.
     */
    private Class StringToClass(String path){
        if(path.endsWith(".class")){
            try {
                String n = Reflection.class.getName();
                n = n.substring(0, n.indexOf("."));
                String a = path;

                try {
                    a = a.substring(a.indexOf("/" + n));
                } catch (Exception e) {
                }

                String name = a.substring(path.lastIndexOf("/")+1);
                return Class.forName(refletir()
                        .stream()
                        .filter(f -> f.contains(name))
                        .collect(Collectors.toList())
                        .get(0)
                        .replaceAll("/", ".").replaceAll(".class", "")
                );
            } catch (URISyntaxException | IOException | ClassNotFoundException ex) {
//                Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
}