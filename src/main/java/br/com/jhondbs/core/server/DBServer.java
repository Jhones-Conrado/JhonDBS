/*
 * Copyright (C) 2022 jhonessales
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
package br.com.jhondbs.core.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Um servidor que recebe conexões para o banco de dados.
 * Ficará ouvindo na porta 3310.
 * @author jhonessales
 */
public class DBServer {
    
    private final String hash;
    private ServerSocket server;
    private static boolean ativo = false;

    public DBServer(String hash) {
        this.hash = hash;
    }
    
    public void start() throws IOException{
        this.ativo = true;
        this.server = new ServerSocket(3310);
        new Thread(new Observador()).start();
    }
    
    private class Observador implements Runnable {

        @Override
        public void run() {
            System.out.println("Server started.");
            while(ativo){
                try {
                    new Analyzer(server.accept());
                } catch (IOException e) {
                }
            }
        }
        
    }
    
    /**
     * Responsável por analisar linhas de comando, queries.
     */
    private class Analyzer {
        
        private final Socket socket;
        private final String keys;
        
        private final Scanner in;
        private final PrintWriter out;
        
        public Analyzer(Socket socket) throws IOException {
            this.socket = socket;
            this.keys = "tcittitpitiinin<n>n~btbf";
            //Inicializando o leitor e escritor dentro do construtor, para evitar perda de informações.
            this.in = new Scanner(socket.getInputStream());
            this.out = new PrintWriter(socket.getOutputStream());
            analyze();
        }
        
        private void analyze() throws IOException{
            if(in.hasNextLine()){
                String line = in.nextLine();
                //Verifica a autenticidade do comando.
                if(line.substring(0, line.indexOf(" ")).equals(hash)){
                    //Remove o hash e mantém somente a query.
                    line = line.substring(line.indexOf(" ")+1);
                    String className = line.substring(0, line.indexOf(" "));
                    line = line.substring(line.indexOf(" ")+1);
                    
                }
            }
            
        }
        
    }
    
}
