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
import br.com.jhondbs.core.servidor.errors.InvalidHash;
import br.com.jhondbs.core.servidor.interpretador.InterpretadorParticular;
import br.com.jhondbs.core.servidor.interpretador.ListaInterpretador;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Uma conexão básica para troca de informações por String.
 * A conexão pode ser iniciada como cliente ou servidor.
 * @author jhonesconrado
 */
public class Conexão {
    
    private Socket socket;
    private Scanner reader;
    private PrintWriter writer;
    
    private final String hash;
    
    private final long start_time;
    private boolean started;
    
    private final List<InterpretadorParticular> interpretadores;
    
    /**
     * Armazena temporariamente as respostas de perguntas feitas na conexão.
     */
    private String resposta;
    
    /**
     * Nova instância de conexão que deverá ser iniciada.
     * @param hash Senha de verificação de autenticidade entre as conexões.
     */
    public Conexão(String hash) {
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
    public void startAsClient() throws IOException, ClassNotFoundException, InvalidHash{
        startAsClient("localhost");
    }
    
    /**
     * Inicia como cliente usando a porta 15200 e um IP fornecido.
     * @param ip IP do servidor.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void startAsClient(String ip) throws IOException, ClassNotFoundException, InvalidHash{
        startAsClient(ip, 15200);
    }
    
    /**
     * Inicia como cliente usando uma Porta e IP fornecidos.
     * @param ip IP do servidor.
     * @param port Porta do servidor.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void startAsClient(String ip, int port) throws IOException, ClassNotFoundException, InvalidHash{
        Inicializador.startInterpretadores();
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
    public void startAsServer(Socket socket) throws IOException, ClassNotFoundException{
        Inicializador.startInterpretadores();
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
    public void addInterpretador(InterpretadorParticular interpretador){
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
    public void removeInterpretrador(InterpretadorParticular interpretador){
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
//                Logger.getLogger(Conexão.class.getName()).log(Level.SEVERE, null, ex);
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
        ListaInterpretador.interpretar(this, msg);
        synchronized (interpretadores) {
            for(InterpretadorParticular i : interpretadores){
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
