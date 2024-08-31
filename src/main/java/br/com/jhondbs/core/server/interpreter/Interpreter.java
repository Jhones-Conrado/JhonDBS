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

/**
 * Responsável por analisar se uma mensagem começa com uma chave específica.</br>
 * Se sim, então interpreta a mensagem.
 * @author jhonesconrado
 */
public abstract class Interpreter {
    
    /**
     * Chave de comando que o interpretador usará para saber se uma determinada
     * mensagem deve ser ou não interpretada por ele.</br>
     * Exemplo: Se a chave for "criarcliente", então a mensagem deverá começar
     * com específicamente "criarcliente". De outro modo o interpretador irá
     * ignorar completamente a mensagem e não fará nada.
     */
    private String comando;
    
    /**
     * Cria um novo interpretador com uma chave de comando.
     * @param comando Chave de comando.
     */
    public Interpreter(String comando) {
        this.comando = comando;
    }
    
    /**
     * Verifica se a mensagem pertence a este interpretador, se pertencer, realiza
     * a sua função programada.
     * @param conexão Connection que solicitou a interpretaçaõ.
     * @param msg Mensagem a ser interpretada.
     */
    public void interpretar(Connection conexão, String msg){
        if(msg.startsWith(comando)){
            try {
                onInterpretar(conexão, msg.substring(comando.length()));
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    
    /**
     * Implementação do interpretador, responsável por toda a sua lógica de comando.
     * @param conexão Connection que chamou o interpretador.
     * @param msg Mensagem da interpretação subtraída a chave inicial.
     */
    protected abstract void onInterpretar(Connection conexão, String msg);
    
    /**
     * Converte uma String em um array de bytes, bastante útil quando se quer enviar
     * ou receber um arquivo através da conexão, já que o JhonDBS usa textos como
     * método de troca de mensagens.
     * @param msg String a ser convertida em array de bytes.
     * @return Array de bytes extraído da String.
     */
    protected byte[] getBytes(String msg){
        return msg.substring(comando.length()).getBytes();
    }
    
}
