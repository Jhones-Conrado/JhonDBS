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
package br.com.jhondbs.jsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Funções básicas para manipular JSON.
 * @author jhonesconrado
 */
public class JsonUtils {
    
    /**
     * Recupera o valor de um atributo de um JSON.
     * @param key Nome do atributo que será buscado.
     * @param json JSON que será buscado o atributo.
     * @return Valor do atributo caso encontrado.
     */
    public static String getAttribute(String key, String json){
        try {
            return getAttributes(json).get(key);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Converte um Mapa de Strings em um JSON.
     * @param map Map<String, String> que será convertido em JSON.
     * @return JSON String.
     */
    public static String mapToJson(Map<String, String> map){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for(String a : map.keySet()){
            if(sb.length() > 1){
                sb.append(",");
            }
            sb.append("\"");
            sb.append(a);
            sb.append("\":");
            String v = map.get(a);
            boolean number = false;
            try {
                Double.valueOf(v);
                number = true;
            } catch (NumberFormatException e) {
            }
            try {
                Integer.valueOf(v);
                number = true;
            } catch (NumberFormatException e) {
            }
            if(!number){
                sb.append("\"");
            }
            sb.append(v);
            if(!number){
                sb.append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Coloca um atributo no Mapa.
     * @param map Mapa<String, String> que receberá o atributo.
     * @param key Chave do atributo.
     * @param value Valor do atributo.
     */
    public static void putAttribute(Map<String, String> map, String key, String value){
        if(value.startsWith("\"")){
            value = value.substring(1);
        }
        if(value.endsWith("\"")){
            value = value.substring(0, value.length()-1);
        }
        map.put(key, value);
    }
    
    /**
     * Coloca um atributo em um JSON.
     * @param json Que receberá o atributo.
     * @param key Nome do atributo.
     * @param value Valor do atributo.
     * @return JSON com o novo atributo adicionado.
     */
    public static String puAttribute(String json, String key, String value){
        Map<String, String> att = getAttributes(json);
        att.put(key, value);
        return mapToJson(att);
    }
    
    
    /**
     * Converte um JSON em um Mapa de atributos do tipo <String, String>.
     * @param json JSON que terá os atributos extraídos.
     * @return Mapa com os valores extraídos do JSON.
     */
    public static Map<String, String> getAttributes(String json){
        String temp = json;
        Map<String, String> att = new HashMap<>();
        
        while(temp.length() > 0){
            
            int increment = 1;
            
            int primeira = temp.indexOf("\"");
            int segunda = temp.indexOf("\"", primeira+1);
            
            String chave = temp.substring(primeira+1, segunda);
            String valor = null;
            
            if(temp.substring(segunda+2, segunda+3).equals("\"")){
                primeira = segunda+3;
                segunda = temp.indexOf("\"", primeira);
                int count = 1;
                while(temp.substring(segunda-1, segunda).equals("\\")){
                    segunda = temp.indexOf("\"", primeira+count);
                    count++;
                }
                valor = temp.substring(primeira, segunda);
                att.put(chave, valor);
            } else if(temp.substring(segunda+2, segunda+3).equals("{")){
                primeira = segunda+3;
                int count = 1;
                int loop = 3;
                while(count > 0){
                    if(temp.substring(segunda+loop, segunda+loop+1).equals("{")){
                        count++;
                    } else if(temp.substring(segunda+loop, segunda+loop+1).equals("}")){
                        count--;
                    }
                    loop++;
                }
                segunda = segunda+loop;
                valor = temp.substring(primeira-1, segunda);
                att.put(chave, valor);
            } else if(temp.substring(segunda+2, segunda+3).equals("[")){
                primeira = segunda+3;
                int count = 1;
                int loop = 3;
                while(count > 0){
                    if(temp.substring(segunda+loop, segunda+loop+1).equals("[")){
                        count++;
                    } else if(temp.substring(segunda+loop, segunda+loop+1).equals("]")){
                        count--;
                    }
                    loop++;
                }
                segunda = segunda+loop-1;
                valor = temp.substring(primeira-1, segunda+1);
                att.put(chave, valor);
            } else {
                primeira = segunda+2;
                int count = 0;
                while("0123456789.".contains(temp.substring(primeira+count, primeira+count+1))){
                    count++;
                }
                segunda = primeira+count;
                valor = temp.substring(primeira, segunda);
                att.put(chave, valor);
                increment = 0;
            }
            
            temp = temp.substring(segunda+increment);
            
            if(!temp.startsWith(",") && !temp.startsWith(" ,") && !temp.startsWith(", ")){
                break;
            }
        }
        return att;
    }
    
}
