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
