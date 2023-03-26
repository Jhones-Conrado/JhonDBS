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
package br.com.jhondbs.core.servidor.interpretador;

import br.com.jhondbs.core.servidor.Conexão;
import java.util.ArrayList;
import java.util.List;

/**
 * Mentém uma lista com os interpretadores globais do servidor.</br>
 * Essa lista será auto-alimentada na inialização do sistema, instanciando todas
 * as implementações de interpretadores globais que foram criadas.
 * @author jhonesconrado
 */
public class ListaInterpretador {
    
    private static ListaInterpretador instancia;
    private List<Interpretador> interpretadores;
    
    private ListaInterpretador(){
        this.interpretadores = new ArrayList<>();
    }
    
    /**
     * Adiciona um interpretador global ao sistema.
     * @param interpretador 
     */
    public static void addInterpretador(Interpretador interpretador){
        if(!get().interpretadores.contains(interpretador)){
            get().interpretadores.add(interpretador);
        }
    }
    
    /**
     * Remove um interpretador global do sistema.
     * @param interpretador 
     */
    public static void removeInterpretador(Interpretador interpretador){
        if(get().interpretadores.contains(interpretador)){
            get().interpretadores.remove(interpretador);
        }
    }
    
    /**
     * Encaminha uma mensagem à todos os interpretadores.
     * @param conexão Conexão que requisitou a interpretação.
     * @param msg Mensagem a ser interpretada.
     */
    public static void interpretar(Conexão conexão, String msg){
        for(Interpretador i : get().interpretadores){
            i.interpretar(conexão, msg);
        }
    }
    
    /**
     * Faz uma contagem de quantos interpretadores globais existem no sistema.
     * @return Quantidade de interpretadores globais.
     */
    public static int getInterpretadorCount(){
        return get().interpretadores.size();
    }
    
    /**
     * Retorna a instância universal da Lista de Interpretadores Globais.
     * @return Instância da ListaInterpretador.
     */
    private static ListaInterpretador get(){
        if(instancia == null){
            instancia = new ListaInterpretador();
        }
        return instancia;
    }
    
}
