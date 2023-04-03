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

import br.com.jhondbs.core.db.Keys;
import br.com.jhondbs.core.db.UniqueAnalyser;
import br.com.jhondbs.core.db.base.Entidade;
import br.com.jhondbs.core.db.errors.DuplicatedUniqueField;
import br.com.jhondbs.core.db.errors.EntIdBadImplementation;
import br.com.jhondbs.core.db.filter.Filter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.stream.Collectors;
import br.com.jhondbs.core.db.filter.ItemFilter;

/**
 * Responsável por salvar, deletar e carregar entidades do banco de dados.
 * @author jhonesconrado
 */
public class IO {
    
    /**
     * Salva uma entidade no banco de dados passando a mesma por filters que garantem
     * o bom funcionamento do sistema.Como verificar se os valores unicos estão disponíveis.
     * @param e Entidade a ser salva no banco de dados.
     * @return Verdadeiro para caso a entidade tenha sido salva. Falso em caso de erro.
     * @throws DuplicatedUniqueField Caso algum campo unico da entidade tenha um
     * valor já usado por outra entidade previamente salva.
     * @throws br.com.jhondbs.core.db.errors.EntIdBadImplementation
     * @throws java.lang.IllegalAccessException
     */
    public static boolean save(Entidade e) throws DuplicatedUniqueField, EntIdBadImplementation, IllegalArgumentException, IllegalAccessException{
        Keys.gerarId(e);
        if(new UniqueAnalyser().analise(e)){ //Precisa passar no teste de campos únicos.
            File pasta = new File("db/"+e.getClass().getName().replaceAll("[.]", "/"));
            pasta.mkdirs(); //Cria a pasta da classe da entidade.
            if(pasta.exists()){ //Verifica se a pasta da classe pôde ser criada com sucesso.
                File entidadeNova = new File(pasta.getPath()+"/"+String.valueOf(e.getEnteId()));
                
                //Converte o objeto em JSON e salva.
                String json = Serializator.serialize(e);
//                String json = g.toJson(e);
                try (BufferedWriter w = Files.newBufferedWriter(entidadeNova.toPath(), StandardCharsets.UTF_8)) {
                    w.write(json);
                    w.flush();
                    return  true;
                } catch (IOException | IllegalArgumentException  ex) {
                    Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }
    
    /**
     * Carrega uma entidade salva no banco de dados, recebendo para isso o tipo
     * da entidade e seu ID. Um objeto nulo será retornado se a entidade não for
     * encontrada.
     * @param e Entidade que servirá para localizar o tipo a ser buscado.
     * @param id Da entidade a ser buscada no banco de dados.
     * @return Entidade carregada do banco de dados. Nulo caso não encontre.
     */
    public static Object load(Entidade e, long id){
        File file = new File("db/"+e.getClass().getName().replaceAll("[.]", "/")+"/"+String.valueOf(id));

        //Método antigo utilizando JSON
        if(file.exists()){
            try {
                String json;
                try (BufferedReader r = Files.newBufferedReader(Paths.get(file.getPath()))) {
                    json = r.readLine();
                }
                return Serializator.deserialize(json);
//                return g.fromJson(json, e.getClass());
            } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        return null;
    }
    
    /**
     * Retorna uma lista com todas as entidades de determinado tipo. Ter cuidado
     * ao usar esse método pois o alto número de entidades pode consumir muita memória.
     * @param e Entidade a ser listada.
     * @return Lista de entidades do tipo solicitado.
     */
    public static List<Entidade> loadAll(Entidade e){
        return e.loadAllOnlyIds().stream().map((t) -> {
            return (Entidade) e.load(t);
        }).collect(Collectors.toList());
    }
    
    /**
     * Retorna uma lista filtrada de entidades de determinado tipo.
     * @param e Entidade que será listado o tipo.
     * @param filtros Lista de filters que será aplicado na busca.
     * @param todos Precisa passar em all os filters, ou somente um basta?
     * Verdadeiro para a necessidade de passar em all, falso para somente um
     * item ser suficiente.
     * @return Lista das entidades aprovadas no teste.
     */
    public static List<Entidade> loadAll(Entidade e, List<ItemFilter> filtros, boolean todos){
        Filter f = new Filter(todos);
        f.filters = filtros;
        return loadAll(e, f);
    }
    
    /**
     * Retorna uma lista filtrada de entidades de determinado tipo.
     * @param e Entidade que será listado o tipo.
     * @param filtro Objeto de filtragem que será usado para validação.
     * @return Lista de entidades que passaram no teste de validação.
     */
    public static List<Entidade> loadAll(Entidade e, Filter filtro){
        return e.loadAllOnlyIds().stream().map((t) -> {
            return (Entidade) e.load(t);
        }).filter((t) -> filtro.filter(t)).collect(Collectors.toList());
    }
    
    /**
     * Retorna a lista de IDs do tipo de entidade. Economiza espaço em memória em
     * relação ao método loadAll que trás as instâncias de todos os objetos já salvos.
     * @param e
     * @return 
     */
    public static List<Long> loadAllOnlyIds(Entidade e){
        List<Long> lista = new ArrayList<>();
        File file = new File("db/"+e.getClass().getName().replaceAll("[.]", "/"));
        if(file.exists()){
            Arrays.asList(file.list()).forEach(n -> {
                try {
                    lista.add(Long.valueOf(n));
                } catch (NumberFormatException ex) {
                }
            });
        }
        return lista;
    }
    
    /**
     * Deleta uma entidade.
     * @param e Entidade a ser deletada.
     * @return 
     */
    public static boolean delete(Entidade e){
        File ente = new File("db/"+e.getClass().getName().replaceAll("[.]", "/")+"/"+e.getEnteId());
        if(ente.exists()){
            return ente.delete();
        }
        return false;
    }
    
    /**
     * Deleta todas as entidades de uma lista.
     * @param entidades Lista de entidades a serem apagadas.
     */
    public static void delete(List<Entidade> entidades){
        entidades.forEach(e -> IO.delete(e));
    }
    
    /**
     * Recebe uma lista de entidades que deverão PERMANECER no banco de dados,
     * apagando qualquer entidade que não esteja nessa lista!
     * @param entidades Lista de entidades que PERMANECERÃO após a execução do
     * método.
     */
    public static void deleteInverse(List<Entidade> entidades){
        if(!entidades.isEmpty()){ //Verifica se há entidades no filtro.
            List<Long> all = entidades.get(0).loadAllOnlyIds();
            List<Long> collect = entidades.stream().map((t) -> t.getEnteId()).collect(Collectors.toList());
            List<Long> toDelete = all.stream().filter((t) -> !collect.contains(t)).collect(Collectors.toList());
            toDelete.forEach(l -> {
                entidades.get(0).load(l).delete();
            });
        }
    }
    
    /**
     * Limpa e apaga um diretório.</br>
     * Faz uma chamada recursiva que apaga all os sub-arquivos e sub-diretórios
     * até limpar o diretório que será então excluído.
     * @param diretorio Diretório que será esvaziado e deletado.
     */
    public static void deleteDiretorio(File diretorio){
        if(diretorio.isDirectory()){
            File[] subs = diretorio.listFiles();
            for(File sub : subs){
                if(sub.isDirectory()){
                    deleteDiretorio(sub);
                }
                sub.delete();
            }
            diretorio.delete();
            while(diretorio.exists()){}
        }
    }
    
}
