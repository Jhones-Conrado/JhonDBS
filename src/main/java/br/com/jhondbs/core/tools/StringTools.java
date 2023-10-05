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
package br.com.jhondbs.core.tools;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jhonessales
 */
public class StringTools {
    
    public static void splitMsg(List<String> recip, String msg, String open, String close){
        
        while(msg.contains(open) && msg.contains(close) && msg.indexOf(open) < msg.indexOf(close)){
            int count = 0;
            int start = msg.indexOf(open);
            int end = start;
            
            while(end + close.length() <= msg.length()){
                if(msg.substring(end).startsWith(open)){
                    count++;
                } else if (msg.substring(end).startsWith(close)){
                    count--;
                }
                end++;
                if(count == 0){
                    break;
                }
            }

            recip.add(msg.substring(start, end));
            msg = msg.substring(end);
        }
        
    }
    
    public static boolean isNumericalString(String index) {
        // Define uma expressão regular para encontrar apenas números
        String regex = "^[0-9]+$";
        
        // Compila a expressão regular em um padrão
        Pattern pattern = Pattern.compile(regex);
        
        // Cria um Matcher para a string de entrada
        Matcher matcher = pattern.matcher(index);
        
        // Verifica se a string corresponde ao padrão (contém apenas números)
        return matcher.matches();
    }
    
}
