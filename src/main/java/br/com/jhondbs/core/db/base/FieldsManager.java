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
package br.com.jhondbs.core.db.base;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsável por manusear os campos das entidades, extraindo os campos, seus nomes,
 * seus valores, etc... Necessário para reflexão.
 * @author jhonesconrado
 */
public class FieldsManager {

    private FieldsManager() {}
    
    /**
     * Retorna uma lista com todos os campos declarados da entidade.
     * @param e Entidade a ser analisada.
     * @return Lista de todos os campos da entidade.
     */
    public static List<Field> getFields(Object e){
        return Arrays.asList(e.getClass().getDeclaredFields());
    }
    
    /**
     * Filtra uma lista de campos retornando somente os que estiverem anotados como @unique.
     * @param list Lista de campos a serém filtrados.
     * @return Lista com os campos anotados com @unique. Vazia, se não existirem campos unicos.
     */
    public static List<Field> getFieldsUnique(List<Field> list){
        return list.stream().filter((t) -> t.isAnnotationPresent(Unique.class)).collect(Collectors.toList());
    }
    
    /**
     * Retorna todos os campos de uma entidade que estiverem anotados como unico.</br>
     * Apenas uma forma mais direta e rápida de chamar o método getFields seguido
     * do getFieldsUnique.
     * @param e Entidade a ser analisada.
     * @return Lista com campos unicos.
     */
    public static List<Field> getFieldsUnique(Object e){
        return getFieldsUnique(getFields(e));
    }
    
    /**
     * Busca nos campos da entidade se encontra algum campo com mesmo nome passado
     * como parâmetro do método, retornando o seu valor na forma de um Object.
     * @param <T>
     * @param campo Nome do parâmetro a ser buscado.
     * @param e Entidade a ter o valor do seu parâmetro extraído.
     * @return Objeto do campo solicitado.
     * @throws java.lang.NoSuchFieldException
     * @throws IllegalArgumentException.
     * @throws IllegalAccessException Caso, por algum motivo, não tenha sido obtido acesso
     * ao valor da variável.
     */
    public static <T> T getValueFrom(String campo, Object e) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException{
        /*
        * Essa parte foi preciso ser inclusa, pois estava dando erro na hora de obter
        * o valor de campos privados de alguns objetos.
        * Ainda assim, é apenas uma medida paleativa.
        */
        if(e instanceof Entidade){
            Entidade ee = Entidade.class.cast(e);
            return ee.getValueFrom(campo);
        } else {
            Field field = null;
            Class clazz = e.getClass();

            while(field == null && clazz != Object.class){

                List<Field> list = Arrays.asList(clazz.getDeclaredFields());

                for(Field f : list){
                    if(f.getName().endsWith(campo)){
                        Object cast = clazz.cast(e);
                        if(cast != null){
                            f.setAccessible(true);
                            return (T) f.get(cast);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * Adiciona um valor à uma varíavel de uma entidade.
     * @param field Nome da variável.
     * @param ente Objeto que receberá o valor em sua variável.
     * @param value Valor que será posto na variável.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public static void setValue(String field, Object ente, Object value) throws IllegalArgumentException, IllegalAccessException{
        List<Field> fields = getFields(ente);
        for(Field f : fields){
            if(f.getName().equals(field)){
                f.setAccessible(true);
                f.set(ente, value);
                break;
           }
        }
    }
    
}
