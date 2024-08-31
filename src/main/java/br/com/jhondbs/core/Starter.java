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
package br.com.jhondbs.core;

import br.com.jhondbs.core.tools.Reflection;
import br.com.jhondbs.core.server.interpreter.GlobalInterpreter;
import br.com.jhondbs.core.server.interpreter.InterpreterBottle;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicializadores de funções cruciais como carregar os interpretadores globais do projeto.
 * @author jhonesconrado
 */
public class Starter {
    
    private static boolean namePrinted = false;
    
    /**
     * Indica se os interpretadores já foram carregados.
     */
    private static boolean interpretadoresCarregados = false;
    
    /**
     * Imprime na saída do sistema o nome da biblioteca, versão e autor.
     */
    public static void printName(){
        if(!namePrinted){
            System.out.println(
            "************************************************************************\n" +
            "@@@@@@@@ @@    @@    @@@@    @@@    @@     @@@@@@    @@@@@@@    @@@@@@@\n" +
            "   @@    @@    @@  @@    @@  @@@@   @@     @@   @@@  @@   @@@  @@@\n" +
            "   @@    @@@@@@@@  @@    @@  @@ @@  @@     @@    @@  @@@@@@     @@@@@\n" +
            "@@ @@    @@    @@  @@    @@  @@  @@ @@     @@   @@@  @@   @@@       @@@\n" +
            " @@@     @@    @@    @@@@    @@   @@@@     @@@@@@    @@@@@@    @@@@@@\n" +
            "************************************************************************\n" +
            " Version: 2.1\n" +
            " Author: Jhones Sales Conrado\n\n");
        }
    }
    
    /**
     * Busca todas as classes que extendem GlobalInterpreter, tenta criar uma
     * instância das classes achadas e as coloca em funcionamento para ouvir as 
     * mensagens recebidas a partir de todas as conexões.
     * @throws IOException
     */
    public static void startInterpretadores() throws IOException{
        if(!interpretadoresCarregados){
            printName();
            Reflection r = new Reflection();
            r.allImplementsNotAbstract(GlobalInterpreter.class).forEach(i -> {
                try {
                    InterpreterBottle.addInterpretador(r.getNewInstance(i));
                } catch (URISyntaxException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(Starter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Starter.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            interpretadoresCarregados = true;
            System.out.println("Interpreters: "+InterpreterBottle.getInterpretersCount());
        }
    }
    
    /**
     * Verifica se os interpretadores do projeto já foram inicializados.
     * @return 
     */
    public boolean isStarted(){
        return interpretadoresCarregados;
    }
    
}
