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
