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
package br.com.jhondbs.core.db;

import br.com.jhondbs.core.db.filter.BooleanFilter;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.db.filter.NumberFilter;
import br.com.jhondbs.core.db.filter.StringFilter;
import br.com.jhondbs.core.tools.Reflection;
import java.util.List;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.db.filter.FilterCondition;

/**
 * It has methods to convert String into search filters and vice versa.<br>
 * Dispõe de métodos para converter String em fitlros de busca e vice-versa.
 * @author jhonesconrado
 */
public class DBAPI {
    
    /**
     * ENGLISH<br>
     * Receives a string command that must follow the "ClassName CMD Field Parameter" format.</br>
      * example: Customer tci name John. It can be understood as "fetch all entities of the class
      * Customer in which the 'name' variable is of type String and starts with John,
      * ignoring case.
      * For more than one filter, the commands must be separated by a space,
      * semicolon and space, example: " ; " without quotes.
      * Using the previous example.
      * Customer tci name John; n> age 17 <br>
      * Could be translated to: search for all customers named John over the age of 17.
     * <br><br>
     * PORTUGUÊS<br>
     * Recebe um comando em forma de string que deve seguir o formato ClassName CMD Field Parameter.</br>
     * exemplo: Cliente tci nome João. Pode-se entender como "busque todas as entidades da classe
     * Cliente em que a variável 'nome' seja do tipo String e comece com João, ignorando maiúsculas e
     * minúsculas. Para mais de um filtro, deve-se separar os comandos por espaço, ponto e vírgula e
     * espaço, exemplo: " ; " sem as aspas. Utilizando do exemplo mais anterior.
     * Cliente tci nome João ; n> idade 17 <br>
     * Pode ser traduzido para: busque todos os clientes com nome João com idade
     * maior que 17 anos.
     * @param cmd String de comando de busca, seguindo o formato ClassName CMD Field Parameter.
     * @return Lista de entidades que passaram no teste.
     * @throws Exception
     */
    public static List<Entity> getByFilter(String cmd) throws Exception{
        if(cmd.contains(" ")){
            try {
                String classSimpleName = cmd.substring(0, cmd.indexOf(" "));
                String comando = cmd.substring(cmd.indexOf(" ") + 1);
                Entity obj = new Reflection().getNewInstance(classSimpleName);
                System.out.println("CLASSE: "+obj.getClass().getName());
                Filter f = new Filter();
                if(comando.contains(" ; ")){
                    String[] cms = comando.split(" ; ");
                    for(String s : cms){
                        f.addCondition(toFilter(s));
                    }
                } else {
                    f.addCondition(toFilter(comando));
                }
                return obj.loadAll(f);
            } catch (Exception ex) {
                throw ex;
            }
        }
        throw new Exception("Query mau formatada.");
    }
    
    /**
     * It receives a search command and an entity and returns a list of all
     * entities saved in the database that passed the filter test obtained by
     * the filtering command.
     * <br><br>
     * Recebe um comando de busca e uma entidade e trás uma lista de todas as entidades
     * salvas no banco de dados que passaram no teste do filtro obtido pelo comando de
     * filtragem.
     * @param ente Objeto que será buscado no banco de dados por sua classe aplicando-se o filtro.
     * @param filter Filtro de validação para as entidades salvas no banco de dados.
     * @return Lista de objetos que passaram no teste.
     * @throws Exception 
     */
    public List<Entity> getByFilter(Entity ente, String filter) throws Exception{
        Filter fil = new Filter();
        fil.addCondition(toFilter(filter));
        return ente.loadAll(fil);
    }
    
    /**
     * Converts a command line into a filtering object that can be used later to
     * filter system entities.
     * <br><br>
     * Converte uma linha em comando em um objeto de filtragem que poderá ser usado
     * posteriormente para filtrar entidades do sistema.
     * @param cmd Comando a ser convertido em um objeto de filtragem.
     * @return Objeto de filtragem.
     * @throws java.lang.Exception
     */
    public static FilterCondition toFilter(String cmd) throws Exception{
        return shortCmdText(cmd);
    }
    
    /**
     * Converts a command line into a filtering object that can be used later to
     * filter system entities.
     * <br><br>
     * Converte uma linha em comando em um objeto de filtragem que poderá ser usado
     * posteriormente para filtrar entidades do sistema.
     * @param cmd
     * @return
     * @throws Exception 
     */
    private static FilterCondition shortCmdText(String cmd) throws Exception{
        String[] split = cmd.split(" ");
        
        //Caso seja um filtro de texto
        if(split[0].startsWith("t")){
            StringBuilder sb = new StringBuilder();
            for(int i = 2 ; i < split.length ; i ++){
                sb.append(split[i]).append(" ");
            }
            sb.delete(sb.toString().length()-1, sb.toString().length());
            switch (split[0]) {
                case "tc":
                    return new StringFilter(StringFilter.COMECA, split[1], sb.toString());
                case "tt":
                    return new StringFilter(StringFilter.TERMINA, split[1], sb.toString());
                case "tp":
                    return new StringFilter(StringFilter.POSSUI, split[1], sb.toString());
                case "ti":
                    return new StringFilter(StringFilter.IGUAL, split[1], sb.toString());
                case "tci":
                    return new StringFilter(StringFilter.COMECA, split[1], sb.toString(), true);
                case "tti":
                    return new StringFilter(StringFilter.TERMINA, split[1], sb.toString(), true);
                case "tpi":
                    return new StringFilter(StringFilter.POSSUI, split[1], sb.toString(), true);
                case "tii":
                    return new StringFilter(StringFilter.IGUAL, split[1], sb.toString(), true);
                default:
                    break;
            }
        } else if(split[0].startsWith("n")){ //Caso de filtro de número
            switch (split[0]) {
                case "ni":
                    return new NumberFilter(split[1], NumberFilter.IGUAL, Double.parseDouble(split[2]));
                case "n<":
                    return new NumberFilter(split[1], NumberFilter.MENOR, Double.parseDouble(split[2]));
                case "n>":
                    return new NumberFilter(split[1], NumberFilter.MAIOR, Double.parseDouble(split[2]));
                case "n~":
                    return new NumberFilter(split[1], Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                default:
                    break;
            }
        } else if(split[0].startsWith("b")){
            if(split[0].equals("bt")){
                return new BooleanFilter(split[1], true);
            } else if(split[0].equals("bf")){
                return new BooleanFilter(split[1], false);
            }
        }
        throw new Exception("Comando não identificado, possívelmente mau formatado.");
    }
    
    /**
     * Converts a filtering object into a command line that can be sent over
     * connections or stored in text format.
     * <br><br>
     * Converte um objeto de filtragem em uma linha de comando que poderá ser
     * enviada através de conexões ou armazenada em formato texto.
     * @param filtro Objeto de filtragem do tipo StringFilter ou NumberFilter.
     * @return String de comando do banco de dados.
     */
    public static String toString(FilterCondition filtro){
        StringBuilder sb = new StringBuilder();
        if(filtro.getClass().getName().contains("String")){
            StringFilter f = (StringFilter) filtro;
            sb.append("t");
            switch (f.getMetodo()) {
                case StringFilter.COMECA:
                    sb.append("c");
                    break;
                case StringFilter.POSSUI:
                    sb.append("p");
                    break;
                case StringFilter.IGUAL:
                    sb.append("i");
                    break;
                case StringFilter.TERMINA:
                    sb.append("t");
                    break;
                default:
                    break;
            }
            if(f.isIgnoreCase()){
                sb.append("i");
            }
            sb.append(" ");
            sb.append(f.getField()).append(" ");
            sb.append(f.getParameter());
        } else if(filtro.getClass().getName().contains("Number")){
            NumberFilter f = (NumberFilter) filtro;
            sb.append("n");
            switch (f.getMethod()) {
                case NumberFilter.IGUAL:
                    sb.append("i ");
                    break;
                case NumberFilter.MAIOR:
                    sb.append("> ");
                    break;
                case NumberFilter.MENOR:
                    sb.append("< ");
                    break;
                case NumberFilter.ENTRE:
                    sb.append("~ ");
                    break;
                default:
                    break;
            }
            sb.append(f.getField()).append(" ");
            sb.append(String.valueOf(f.getInitParameter()));
            if(f.getMethod() == NumberFilter.ENTRE){
                sb.append(" ").append(String.valueOf(f.getParametroFim()));
            }
        } else if(filtro.getClass().getName().contains("Boolean")){
            BooleanFilter f = (BooleanFilter) filtro;
            sb.append("b");
            if(f.getValue()){
                sb.append("t ");
            } else {
                sb.append("f ");
            }
            sb.append(f.getField());
        }
        return sb.toString();
    }
    
}
