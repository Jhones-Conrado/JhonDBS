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

/**
 * Semelhante ao interpretador global, porém deve ser usado de modo particular, utilizando
 * uma nova instância para cada conexão.</br>
 * Pensado nas necessidades de uma classe que gerencie uma sessão de login ou o
 * personagem de um jogo online, por exemplo.
 * @author jhonesconrado
 */
public abstract class InterpretadorParticular extends Interpretador {
    
    /**
     * Para instanciar um novo interpretador particular, uma chave deverá ser passada
     * como parâmetro, caso contrário o sistema dará erro.
     * @param comando Chave de comando do interpretador.
     */
    public InterpretadorParticular(String comando) {
        super(comando);
    }
    
}
