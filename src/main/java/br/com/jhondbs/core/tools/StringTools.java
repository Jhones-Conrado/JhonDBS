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
