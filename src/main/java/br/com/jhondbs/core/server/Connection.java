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
import br.com.jhondbs.core.server.errors.InvalidHash;
import br.com.jhondbs.core.server.interpreter.ParticularInterpreter;
import br.com.jhondbs.core.server.interpreter.InterpreterBottle;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Uma conexão básica para troca de informações por String.
 * A conexão pode ser iniciada como cliente ou servidor.
 * @author jhonesconrado
 */
public class Connection {
    
    private Socket socket;
    private Scanner reader;
    private PrintWriter writer;
    
    private final String hash;
    
    private final long start_time;
    private boolean started;
    
    private final List<ParticularInterpreter> interpretadores;
    
    /**
     * Armazena temporariamente as respostas de perguntas feitas na conexão.
     */
    private String resposta;
    
    /**
     * Nova instância de conexão que deverá ser iniciada.
     * @param hash Senha de verificação de autenticidade entre as conexões.
     */
    public Connection(String hash) {
        this.interpretadores = new ArrayList<>();
        this.start_time = System.nanoTime();
        this.started = false;
        this.resposta = null;
        this.hash = hash;
    }
    
    /**
     * Inicia como cliente usando a porta 15200 e localhost.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void startAsClient() throws IOException, ClassNotFoundException, InvalidHash, URISyntaxException{
        startAsClient("localhost");
    }
    
    /**
     * Inicia como cliente usando a porta 15200 e um IP fornecido.
     * @param ip IP do servidor.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void startAsClient(String ip) throws IOException, ClassNotFoundException, InvalidHash, URISyntaxException{
        startAsClient(ip, 15200);
    }
    
    /**
     * Inicia como cliente usando uma Porta e IP fornecidos.
     * @param ip IP do servidor.
     * @param port Porta do servidor.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void startAsClient(String ip, int port) throws IOException, ClassNotFoundException, InvalidHash, URISyntaxException{
        Starter.startInterpretadores();
        this.socket = new Socket(ip, port);
        initIO();
        falar(hash);
        if(reader.hasNextLine()){
            if(reader.nextLine().equals("connected")){
                new Thread(new Listener()).start();
                started = true;
                System.out.println("Started as client at port: "+port);
            } else {
                throw new InvalidHash();
            }
        } else {
            close();
        }
    }
    
    /**
     * Inicia como servidor, recebendo um socket já pronto.
     * @param socket Socket com o cliente.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void startAsServer(Socket socket) throws IOException, ClassNotFoundException, URISyntaxException{
        Starter.startInterpretadores();
        this.socket = socket;
        initIO();
        if(reader.hasNextLine()){
            String h = reader.nextLine();
            if(hash.equals(h)){
                falar("connected");
                new Thread(new Listener()).start();
                started = true;
                System.out.println("Started as server at "+socket.getInetAddress().toString());
            } else {
                falar("rejected");
                close();
            }
        } else {
            close();
        }
    }
    
    /**
     * Inicia a comunicação de entrada e saída com o socket.</br>
     * Deve ser chamado antes de criar uma nova instância de Lestener.
     * @throws IOException 
     */
    private void initIO() throws IOException{
        this.reader = new Scanner(socket.getInputStream());
        this.writer = new PrintWriter(socket.getOutputStream());
    }
    
    /**
     * Adiciona um interpretador particular na conexão. Diferente do interpretador
     * global que recebe mensagens de todas as conexões, esse interpretador receberá
     * somente as mensagens desta conexão em particular. Tornando-se muito útil
     * para programas que precisam de um sistema de login, carrinho de compras e
     * sessão específica.
     * @param interpretador 
     */
    public void addInterpretador(ParticularInterpreter interpretador){
        synchronized (interpretadores) {
            if(!interpretadores.contains(interpretador)){
                interpretadores.add(interpretador);
            }
        }
    }
    
    /**
     * Remove um interpretador particular da conexão. Diferente do interpretador
     * global que recebe mensagens de todas as conexões, esse interpretador receberá
     * somente as mensagens desta conexão em particular. Tornando-se muito útil
     * para programas que precisam de um sistema de login, carrinho de compras e
     * sessão específica.
     * @param interpretador 
     */
    public void removeInterpretrador(ParticularInterpreter interpretador){
        synchronized (interpretadores) {
            if(interpretadores.contains(interpretador)){
                interpretadores.remove(interpretador);
            }
        }
    }
    
    /**
     * Encaminha uma mensagem através da conexão.</br>
     * Se a conexão precisar ser interpretada por quem irá receber e o recebedor
     * também for um JhonDBS, então precisará iniciar com a chave do interpretador.
     * @param msg Mensagem a ser enviada.
     */
    public void falar(String msg){
        if(msg != null && !msg.isBlank()){
            msg = msg.replaceAll("\n", ";;;");
            writer.println(msg);
            writer.flush();
        }
    }
    
    /**
     * Encaminha uma mensagem através da conexão, porém adiciona a palavra chave
     * "resposta" antes da msg.</br>
     * Serve para indicar ao recebedor que se trata da resposta da última pergunta
     * feita.
     * @param msg Resposta a ser enviada.
     */
    public void responder(String msg){
        falar("resposta"+msg);
    }
    
    /**
     * Encaminha uma mensagem através da conexão e fica aguardando até que a
     * próxima resposta chegue.</br>
     * Caso uma resposta jamais chegue, a conexão ficará travada nesse loop.</br>
     * É um método que precisa ser usado com garantias de que a implementação será
     * correta e pode dar problemas em casos de queda de conexão.</br>
     * Precisa ser implementado métodos de segurança para quebra do loop em caso
     * de queda na conexão.
     * @param msg Pergunta a ser feita.
     * @return Resposta da pergunta.
     */
    public String perguntar(String msg){
        falar(msg);
        while(resposta == null){
            Thread.yield();
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
//                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String temp = resposta;
        resposta = null;
        return temp;
    }
    
    /**
     * Responsável por encaminhar uma mensagem aos interpretadores, tanto globais
     * como particulares.
     * @param msg A ser interpretada.
     */
    public void ouvir(String msg){
        InterpreterBottle.interpretar(this, msg);
        synchronized (interpretadores) {
            for(ParticularInterpreter i : interpretadores){
                try {
                    i.interpretar(this, msg);
                } catch (Exception e) {
                    System.out.println("Erro com interpretador: "+i.getClass().getName());
                }
            }
        }
    }
    
    /**
     * Encerra a conexão.
     * @throws IOException 
     */
    public void close() throws IOException{
        reader.close();
        writer.close();
        socket.close();
    }
    
    /**
     * Retorna o tempo que a conexão está aberta em segundos.
     * @return 
     */
    public int getOpenedTimeInSeconds(){
        return (int) ((System.nanoTime()-start_time)/1e9);
    }
    
    /**
     * Retorna o IP da conexão.
     * @return Ip da conexão.
     */
    public String getIp(){
        return socket.getInetAddress().toString();
    }
    
    /**
     * Responsável por ficar ouvindo todas as mensagens recebidas e encaminhando
     * para os interpretadores.
     */
    private class Listener implements Runnable {
        
        @Override
        public void run() {
            try {
                while (reader.hasNextLine()) {
                    String msg = reader.nextLine();
                    
                    try {
                        msg = msg.replaceAll(";;;", "\n");
                    } catch (Exception e) {
                    }
                    
                    if (msg.startsWith("resposta")) {
                        resposta = msg.substring("resposta".length());
                    } else {
                        ouvir(msg);
                    }
                }
            } catch (Exception e) {
            }
            ouvir("ConClosed");
            System.out.println("Conection was closed.");
        }
        
    }
    
}
