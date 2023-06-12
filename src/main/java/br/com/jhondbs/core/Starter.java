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
package br.com.jhondbs.core;

import br.com.jhondbs.core.db.io.Reflection;
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
