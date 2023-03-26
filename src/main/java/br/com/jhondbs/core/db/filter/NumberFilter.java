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
package br.com.jhondbs.core.db.filter;

import br.com.jhondbs.core.db.base.Entidade;
import br.com.jhondbs.core.db.base.FieldsManager;
import br.com.jhondbs.core.db.errors.AttributeNotFind;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Um filtro númerico que vai verificar o campo de uma entidade.</br>
 * Exemplos de uso: "ni idade 20" sem as aspas pode ser entendido como "verifique
 * se na variável 'idade' da nossa entidade filtrada, o valor é igual a 20".</br>
 * Outros possíveis comandos: ni variável valor (número igual);
 * n< variável valor. (número menor);
 * n> variável valor. (número maior);
 * n~ variável valor valor. (número entre dois valores.
 * @author jhonesconrado
 */
public class NumberFilter implements ItemFilter{
    
    /**
     * O filtro retornará verdadeiro somente se o campo da entidade for igual ao
     * valor declarado como parâmetro na criação do filtro.
     */
    public static final int IGUAL = 0;
    
    /**
     * O filtro retornará verdadeiro somente se o campo da entidade for menor ao
     * valor declarado como parâmetro na criação do filtro.
     */
    public static final int MENOR = 1;
    
    /**
     * O filtro retornará verdadeiro somente se o campo da entidade for maior ao
     * valor declarado como parâmetro na criação do filtro.
     */
    public static final int MAIOR = 2;
    
    /**
     * Não é recebido na criação do método mas é usado para identificar que o
     * método usado no filtro deverá ser a comparação de intervalo entre dois
     * valores.
     */
    public static final int ENTRE = 3;
    
    private final String field;
    private final int method;
    private final double init;
    private final double end;
    
    /**
     * Cria um filtro numérico que verificará um determinado campo de uma entidade.
     * @param field Nome do campo que será buscado e filtrado.
     * @param method Método escolhido de filtragem, se "maior que", "menor que" ou "igual".
     * @param parameter Numero de critério de filtragem.
     */
    public NumberFilter(String field, int method, double parameter) {
        if(method > 3 || method < 0){
            this.field = field;
            this.method = NumberFilter.ENTRE;
            this.init = 0;
            this.end = parameter;
        } else {
            this.field = field;
            this.method = method;
            this.init = parameter;
            this.end = 0;
        }
    }
    
    /**
     * Cria um filtro numérico que verificará um determinado campo de uma entidade.
     * Garantindo que o valor deste campo esteja entre um intervalo numérico definido.
     * @param field Nome do campo que será buscado e filtrado.
     * @param init Início do intervalo numérico do filtro.
     * @param end Final do intervalo numérico do filtro.
     */
    public NumberFilter(String field, double init, double end) {
        this.field = field;
        this.method = NumberFilter.ENTRE;
        this.init = init;
        this.end = end;
    }
    
    /**
     * Vai verificar se a entidade possui um campo com o nome definido na criação
     * do filtro e também se esse valor passa no teste, de acordo com o método de
     * filtragem definido na criação do filtro.
     * @param e Entidade a ser filtrada.
     * @return Verdadeiro para caso tenha passado no teste.
     */
    @Override
    public boolean filtrar(Entidade e) {
        try {
            double valor = Double.parseDouble(String.valueOf(FieldsManager.getValueFrom(field, e)));
            switch (this.method) {
                case ENTRE:
                    return valor >= init && valor <= end;
                    // Verifica se é menor que o valor definido.
                case MENOR:
                    return valor < init;
                    // Verifica se é maior que o valor definido.
                case MAIOR:
                    return valor > init;
                    // Verifica se é igual ao valor definido.
                case IGUAL:
                    return valor == init;
                default:
                    return false;
            }
            
        } catch (IllegalArgumentException | IllegalAccessException ex) {
//            Logger.getLogger(NumberFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(NumberFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Retorna o número do método que o filtro foi criado.
     * @return Metodo.
     */
    public int getMetodo(){
        return method;
    }
    
    /**
     * Retorna o número do parâmetro inicio. Este número é usado para todos os
     * metodos de filtragem.
     * @return Parâmetro inicio.
     */
    public double getParametroInicio(){
        return init;
    }
    
    /**
     * Retorna o número do parâmetro fim. Este número é usado somente para a
     * filtragem do méotod ENTRE.
     * @return 
     */
    public double getParametroFim(){
        return end;
    }
    
    public String getField(){
        return this.field;
    }
    
}
