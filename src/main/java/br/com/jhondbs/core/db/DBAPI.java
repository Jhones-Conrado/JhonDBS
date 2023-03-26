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
package br.com.jhondbs.core.db;

import br.com.jhondbs.core.db.base.Entidade;
import br.com.jhondbs.core.db.filter.BooleanFilter;
import br.com.jhondbs.core.db.filter.Filter;
import br.com.jhondbs.core.db.filter.NumberFilter;
import br.com.jhondbs.core.db.filter.StringFilter;
import br.com.jhondbs.core.db.filter.ItemFilter;
import br.com.jhondbs.core.db.io.Reflection;
import java.util.List;

/**
 * Dispõe de métodos para converter String em fitlros de busca e vice-versa.
 * @author jhonesconrado
 */
public class DBAPI {
    
    /**
     * Recebe um comando em forma de string que deve seguir o formato ClassName CMD Field Parameter.</br>
     * exemplo: Cliente tci nome João. Pode-se entender como "busque todas as entidades da classe
     * Cliente em que a variável 'nome' seja do tipo String e comece com João, ignorando maiúsculas e
     * minúsculas. Para mais de um filtro, deve-se separar os comandos por espaço, ponto e vírgula e
     * espaço, exemplo: " ; " sem as aspas. Utilizando do exemplo mais anterior.
     * Cliente tci nome João ; n> idade 17
     * @param cmd String de comando de busca, seguindo o formato ClassName CMD Field Parameter.
     * @return Lista de entidades que passaram no teste.
     * @throws Exception
     */
    public static List<Entidade> getByFilter(String cmd) throws Exception{
        if(cmd.contains(" ")){
            try {
                String cName = cmd.substring(0, cmd.indexOf(" "));
                String cm = cmd.substring(cmd.indexOf(" ") + 1);
                Entidade obj = new Reflection().getNewInstance(cName);
                System.out.println("CLASSE: "+obj.getClass().getName());
                Filter f = new Filter();
                if(cm.contains(" ; ")){
                    String[] cms = cm.split(" ; ");
                    for(String s : cms){
                        f.addItem(toFilter(s));
                    }
                } else {
                    f.addItem(toFilter(cm));
                }
                return obj.loadAll(f);
            } catch (Exception ex) {
                throw ex;
            }
        }
        throw new Exception("Query mau formatada.");
    }
    
    /**
     * Recebe um comando de busca e uma entidade e trás uma lista de todas as entidades
     * salvas no banco de dados que passaram no teste do filtro obtido pelo comando de
     * filtragem.
     * @param ente Objeto que será buscado no banco de dados por sua classe aplicando-se o filtro.
     * @param filter Filtro de validação para as entidades salvas no banco de dados.
     * @return Lista de objetos que passaram no teste.
     * @throws Exception 
     */
    public List<Entidade> getByFilter(Entidade ente, String filter) throws Exception{
        Filter fil = new Filter();
        fil.addItem(toFilter(filter));
        return ente.loadAll(fil);
    }
    
    /**
     * Converte uma linha em comando em um objeto de filtragem que poderá ser usado
     * posteriormente para filtrar entidades do sistema.
     * @param cmd Comando a ser convertido em um objeto de filtragem.
     * @return Objeto de filtragem.
     * @throws java.lang.Exception
     */
    public static ItemFilter toFilter(String cmd) throws Exception{
        return shortCmdText(cmd);
    }
    
    private static ItemFilter shortCmdText(String cmd) throws Exception{
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
        } else if(split[0].startsWith("i")){ //Caso de filtro de número
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
     * Converte um objeto de filtragem em uma linha de comando que poderá ser
     * enviada através de conexões ou armazenada em formato texto.
     * @param filtro Objeto de filtragem do tipo StringFilter ou NumberFilter.
     * @return String de comando do banco de dados.
     */
    public static String toString(ItemFilter filtro){
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
            sb.append(f.getParametro());
        } else if(filtro.getClass().getName().contains("Number")){
            NumberFilter f = (NumberFilter) filtro;
            sb.append("n");
            switch (f.getMetodo()) {
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
            sb.append(String.valueOf(f.getParametroInicio()));
            if(f.getMetodo() == NumberFilter.ENTRE){
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
