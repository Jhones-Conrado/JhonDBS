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

/**
 * Um interpretador de uso global que receberá mensagens de todas as conexões,
 * interpretando-as e caso necessário, retornando respostas para as conexões.</br>
 * pensado para funções como CRUD e similares.</br>
 * CUIDADO! Todas as implementações dessa classe PRECISAM ter um construtor SEM
 * argumentos!
 * @author jhonesconrado
 */
public abstract class GlobalInterpreter extends Interpreter{
    
    /**
     * Para instanciar um novo interpretador global, uma chave deverá ser passada
     * como parâmetro, caso contrário o sistema dará erro.
     * @param comando Chave de comando do interpretador.
     */
    public GlobalInterpreter(String comando) {
        super(comando);
    }
    
}
