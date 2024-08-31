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
package br.com.jhondbs.core.db.filter;

import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.jhondbs.core.db.interfaces.Entity;

/**
 * ENGLISH<br>
 * A numerical filter that will check the field of an entity.<br>
 * Usage examples: "ni age 20" without the quotes can be understood as "verify
 * if in the 'age' variable of our filtered entity, the value is equal to 20".<br>
 * <br>
 * Um filtro númerico que vai verificar o campo de uma entidade.<br>
 * Exemplos de uso: "ni idade 20" sem as aspas pode ser entendido como "verifique
 * se na variável 'idade' da nossa entidade filtrada, o valor é igual a 20".<br>
 * <br>
 * Other possible commands:<br>
 * ni 'variable name' 'value' (equal number);<br>
 * n< 'variable name' 'value'. (smaller number);<br>
 * n> 'variable' 'value'. (larger number);<br>
 * n~ 'variable' 'value' 'value'. (number between two values.<br><br>
 *
 * ni age 20 -> is the 'age' field a numerical variable equal to twenty?<br>
 * n< age 18 -> is the 'age' field a numerical variable less than eighteen?<br>
 * n> age 18 -> is the 'age' field a numerical variable greater than eighteen?<br>
 * n~ age 18 30 -> the 'age' field is a numeric variable with a value greater than or
 * equal to eighteen and less than or equal to thirty?
 * <br><br>
 * Outros possíveis comandos:<br>
 * ni 'nome variável' 'valor' (número igual);<br>
 * n< 'nome variável' 'valor'. (número menor);<br>
 * n> 'variável' 'valor'. (número maior);<br>
 * n~ 'variável' 'valor' 'valor'. (número entre dois valores.<br><br>
 * 
 * ni idade 20 -> o campo 'idade' é uma varíavel numérica igual a vinte?<br>
 * n< idade 18 -> o campo 'idade' é uma varíavel numérica menor que dezoito?<br>
 * n> idade 18 -> o campo 'idade' é uma variável numérica maior que dezoito?<br>
 * n~ idade 18 30 -> o campo 'idade' é uma variável numérica com valor maior ou
 * igual a dezoito e menor ou igual a trinta?
 * @author jhonesconrado
 */
public class NumberFilter implements FilterCondition{
    
    /**
     * The filter will only return true if the entity's field is equal to the
     * value declared as a parameter when creating the filter.
     */
    public static final int EQUAL = 0;
    
    /**
     * The filter will only return true if the entity's field is less than the
     * value declared as a parameter when creating the filter.
     */
    public static final int SMALLER = 1;
    
    /**
     * The filter will only return true if the entity's field is greater than
     * the value declared as a parameter when creating the filter.
     */
    public static final int GREATER = 2;
    
    /**
     * It is not received when creating the filter but is used to identify that
     * the method used in the filter should be a range comparison between two values.
     */
    public static final int BETWEEN = 3;
    
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
     * Não é recebido na criação do filtro mas é usado para identificar que o
     * método usado no filtro deverá ser a comparação de intervalo entre dois
     * valores.
     */
    public static final int ENTRE = 3;
    
    private final String field;
    private final int method;
    private final double init;
    private final double end;
    
    /**
     * Creates a numeric filter that will check a given field of an entity.<br>
     * Cria um filtro numérico que verificará um determinado campo de uma entidade.
     * @param field Name of the field that will be searched and filtered.<br>
     * Nome do campo que será buscado e filtrado.
     * @param method Chosen method of filtering, whether "greater than",
     * "less than" or "equal".<br>
     * Método escolhido de filtragem, se "maior que", "menor que" ou "igual".
     * @param parameter Filter criteria number.<br>
     * Numero de critério de filtragem.
     */
    public NumberFilter(String field, int method, double parameter) {
        if(method > 3 || method < 0){
            this.field = field;
            this.method = NumberFilter.ENTRE;
            this.init = method;
            this.end = parameter;
        } else {
            this.field = field;
            this.method = method;
            this.init = parameter;
            this.end = 0;
        }
    }
    
    /**
     * Creates a numeric filter that will check a given field of an entity.
     * Ensuring that the value of this field falls within a defined numeric range.
     * <br>
     * Cria um filtro numérico que verificará um determinado campo de uma entidade.
     * Garantindo que o valor deste campo esteja entre um intervalo numérico definido.
     * @param field Name of the field that will be searched and filtered.<br>
     * Nome do campo que será buscado e filtrado.
     * @param init Start of the filter's numeric range.<br>
     * Início do intervalo numérico do filtro.
     * @param end End of the filter's numeric range.<br>
     * Final do intervalo numérico do filtro.
     */
    public NumberFilter(String field, double init, double end) {
        this.field = field;
        this.method = NumberFilter.ENTRE;
        this.init = init;
        this.end = end;
    }
    
    /**
     * It will check if the entity has a field with the name defined when creating
     * the filter and also if this value passes the test, according to the
     * filtering method defined when creating the filter.
     * <br><br>
     * Vai verificar se a entidade possui um campo com o nome definido na criação
     * do filtro e também se esse valor passa no teste, de acordo com o método de
     * filtragem definido na criação do filtro.
     * @param entity Entity a ser filtrada.
     * @return Verdadeiro para caso tenha passado no teste.
     */
    @Override
    public boolean filter(Entity entity) {
        try {
            
            double value = 0d;
            Object from = entity.getValueFrom(field);
            
            try {
                value = (Short) from;
                return test(value);
            } catch (Exception ex) {
                try {
                    value = (Integer) from;
                    return test(value);
                } catch (Exception ex2) {
                    try {
                        value = (Long) from;
                        return test(value);
                    } catch (Exception ex3) {
                        try {
                            value = (Float) from;
                            return test(value);
                        } catch (Exception ex4) {
                            try {
                                value = (Double) from;
                                return test(value);
                            } catch (Exception ex5) {
                                try {
                                    String val = (String) from;
                                    value = Double.parseDouble((String) val);
                                    return test(value);
                                } catch (NumberFormatException ex6) {
                                    System.out.println("Erro parsing number field.");
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(NumberFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Do numerical verification.<br>
     * Faz a verificação numérica.
     * @param value
     * @return Test result.
     */
    private boolean test(double value){
        switch (this.method) {
                // Verifica se está entre um intervalo.
                case ENTRE:
                    return value >= init && value <= end;
                // Verifica se é menor que o valor definido.
                case MENOR:
                    return value < init;
                // Verifica se é maior que o valor definido.
                case MAIOR:
                    return value > init;
                // Verifica se é igual ao valor definido.
                case IGUAL:
                    return value == init;
                default:
                    return false;
            }
    }
    
    /**
     * Returns the number of the method that the filter was created.<br>
     * Retorna o número do método que o filtro foi criado.
     * @return Method number.
     */
    public int getMethod(){
        return method;
    }
    
    /**
     * Returns the number of the start parameter.
     * This number is used for all filtering methods.
     * <br><br>
     * Retorna o número do parâmetro inicio. Este número é usado para todos os
     * metodos de filtragem.
     * @return Init parameter.
     */
    public double getInitParameter(){
        return init;
    }
    
    /**
     * Returns the end parameter number. This number is only used for filtering
     * the BETWEEN method.
     * <br><br>
     * Retorna o número do parâmetro fim. Este número é usado somente para a
     * filtragem do méotod ENTRE.
     * @return 
     */
    public double getParametroFim(){
        return end;
    }
    
    /**
     * Name of the field to be checked.<br>
     * Nome do campo que será verificado.
     * @return 
     */
    public String getField(){
        return this.field;
    }
    
}
