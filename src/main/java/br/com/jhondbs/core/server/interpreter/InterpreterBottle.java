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
package br.com.jhondbs.core.server.interpreter;

import br.com.jhondbs.core.server.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Mentém uma lista com os interpretadores globais do servidor.</br>
 * Essa lista será auto-alimentada na inialização do sistema, instanciando todas
 * as implementações de interpretadores globais que foram criadas.
 * @author jhonesconrado
 */
public class InterpreterBottle {
    
    private static InterpreterBottle instancia;
    private List<Interpreter> interpretadores;
    
    private InterpreterBottle(){
        this.interpretadores = new ArrayList<>();
    }
    
    /**
     * Adiciona um interpretador global ao sistema.
     * @param interpretador 
     */
    public static void addInterpretador(Interpreter interpretador){
        if(!get().interpretadores.contains(interpretador)){
            get().interpretadores.add(interpretador);
        }
    }
    
    /**
     * Remove um interpretador global do sistema.
     * @param interpretador 
     */
    public static void removeInterpretador(Interpreter interpretador){
        if(get().interpretadores.contains(interpretador)){
            get().interpretadores.remove(interpretador);
        }
    }
    
    /**
     * Encaminha uma mensagem à todos os interpretadores.
     * @param conexão Connection que requisitou a interpretação.
     * @param msg Mensagem a ser interpretada.
     */
    public static void interpretar(Connection conexão, String msg){
        for(Interpreter i : get().interpretadores){
            i.interpretar(conexão, msg);
        }
    }
    
    /**
     * Faz uma contagem de quantos interpretadores globais existem no sistema.
     * @return Quantidade de interpretadores globais.
     */
    public static int getInterpretersCount(){
        return get().interpretadores.size();
    }
    
    /**
     * Retorna a instância universal da Lista de Interpretadores Globais.
     * @return Instância da InterpreterBottle.
     */
    private static InterpreterBottle get(){
        if(instancia == null){
            instancia = new InterpreterBottle();
        }
        return instancia;
    }
    
}
