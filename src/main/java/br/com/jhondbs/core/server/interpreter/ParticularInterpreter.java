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

/**
 * Semelhante ao interpretador global, porém deve ser usado de modo particular, utilizando
 * uma nova instância para cada conexão.</br>
 * Pensado nas necessidades de uma classe que gerencie uma sessão de login ou o
 * personagem de um jogo online, por exemplo.
 * @author jhonesconrado
 */
public abstract class ParticularInterpreter extends Interpreter {
    
    /**
     * Para instanciar um novo interpretador particular, uma chave deverá ser passada
     * como parâmetro, caso contrário o sistema dará erro.
     * @param comando Chave de comando do interpretador.
     */
    public ParticularInterpreter(String comando) {
        super(comando);
    }
    
}
