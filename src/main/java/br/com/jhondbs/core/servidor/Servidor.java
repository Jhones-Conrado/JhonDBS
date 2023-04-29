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
package br.com.jhondbs.core.servidor;

import br.com.jhondbs.core.Inicializador;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Cria um servidor básico que ficará ouvindo requisições de novas conexões.
 * @author jhonesconrado
 */
public class Servidor {
    
    private final String hash;
    private static boolean ativo = false;
    
    private ServerSocket server;
    
    /**
     * @param hash Esse hash é uma string que precisará ser recebida em todas as conexões
     * com os clientes. Caso o cliente envie um hash diferente, a conexão será
     * fechada imediatamente. 
     */
    public Servidor(String hash) {
        this.hash = hash;
        Inicializador.imprimeNome();
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
                    new Conexão(hash).startAsServer(server.accept());
                } catch (IOException | ClassNotFoundException e) {
                }
            }
        }
        
    }
    
}
