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
package br.com.jhondbs.core.server;

import br.com.jhondbs.core.Starter;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Cria um servidor básico que ficará ouvindo requisições de novas conexões.
 * @author jhonesconrado
 */
public class Server {
    
    private final String hash;
    private static boolean ativo = false;
    
    private ServerSocket server;
    
    /**
     * @param hash Esse hash é uma string que precisará ser recebida em todas as conexões
     * com os clientes. Caso o cliente envie um hash diferente, a conexão será
     * fechada imediatamente. 
     */
    public Server(String hash) {
        this.hash = hash;
        Starter.printName();
    }
    
    /**
     * Inicia o servidor na porta padrão 15200, requisitando somente o hash de
     * segurança.</br>
     * Esse hash é uma string que precisará ser recebida em todas as conexões
     * com os clientes. Caso o cliente envie um hash diferente, a conexão será
     * fechada imediatamente.
     * @throws IOException 
     */
    public void start() throws IOException{
        start(15200);
    }
    
    /**
     * Inicia o servidor em uma porta específica, rquisitando o número da porta
     * e o hash de segurança.
     * @param port Numero da porta que o servidor ficará ouvindo.
     * @throws IOException 
     */
    public void start(int port) throws IOException{
        this.server = new ServerSocket(port);
        this.server.setSoTimeout(100);
        ativo = true;
        new Thread(new Observador()).start();
    }
    
    /**
     * Thread que ficará aguardando novas requisições de conexão.
     */
    private class Observador implements Runnable {

        @Override
        public void run() {
            System.out.println("Server started.");
            while(ativo){
                try {
                    new Connection(hash).startAsServer(server.accept());
                } catch (IOException | ClassNotFoundException e) {
                }
            }
        }
        
    }
    
}
