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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ENGLISH<br>
 * Responsible for handling the fields of the entities, extracting the fields,
 * their names, their values, etc... Necessary for reflection.<br><br>
 * PORTUGUÊS<br>
 * Responsável por manusear os campos das entidades, extraindo os campos, seus nomes,
 * seus valores, etc... Necessário para reflexão.
 * @author jhonesconrado
 */
public class FieldsManager {

    private FieldsManager() {}
    
    /**
     * Returns a list of all declared fields of the entity.<br>
     * Retorna uma lista com todos os campos declarados da entidade.
     * @param obj Entity a ser analisada.
     * @return Lista de todos os campos da entidade.
     */
    public static List<Field> getFields(Object obj){
        return Arrays.asList(obj.getClass().getDeclaredFields());
    }
    
    /**
     * Ensures that all fields of the class and its superclasses are returned.
     * Garante que todos os campos da classe e suas superclasses sejam retornados.
     * @param obj Which will have its fields extracted<br>
     * Que terá seus campos extraídos.
     * @return list with all fields of the object.<br>
     * lista com todos os campos do objeto.
     */
    public static List<Field> getAllFields(Object obj){
        List<Field> fields = new ArrayList<>();
        Class clazz = obj.getClass();
        while(clazz != null){
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
    
    public static List<Field> getSerializableFields(Object obj){
        return getAllFields(obj).stream()
                .filter(field -> (!Modifier.isStatic(field.getModifiers())
                        && !Modifier.isTransient(field.getModifiers())))
                .toList();
    }
    
    /**
     * Filters a list of fields, returning only those annotated with @unique.<br>
     * Filtra uma lista de campos retornando somente os que estiverem anotados como @unique.
     * @param list List of fields to be filtered.<br>
     * Lista de campos a serém filtrados.
     * @return List of fields annotated with @unique. Empty, if there are no
     * unique fields.<br>
     * Lista com os campos anotados com @unique. Vazia, se não existirem campos unicos.
     */
    public static List<Field> getFieldsUnique(List<Field> list){
        return list.stream().filter((t) -> t.isAnnotationPresent(Unique.class)).collect(Collectors.toList());
    }
    
    /**
     * ENGLISH<br>
     * Returns all fields of an entity that are annotated as unique.</br>
     * Just a more direct and faster way to call the getFields method followed by
     * getFieldsUnique.<br><br>
     * PORTUGUÊS<br>
     * Retorna todos os campos de uma entidade que estiverem anotados como unico.</br>
     * Apenas uma forma mais direta e rápida de chamar o método getFields seguido
     * do getFieldsUnique.
     * @param obj Object to be analyzed.<br>
     * Objeto a ser analisada.
     * @return List with unique fields.<br>
     * Lista com campos unicos.
     */
    public static List<Field> getFieldsUnique(Object obj){
        return getFieldsUnique(getFields(obj));
    }
    
    /**
     * ENGLISH<br>
     * Returns all fields of an entity and it's superclasses that are annotated as unique.</br>
     * Just a more direct and faster way to call the getFields method followed by
     * getFieldsUnique.<br><br>
     * PORTUGUÊS<br>
     * Retorna todos os campos de uma entidade que estiverem anotados como unico.</br>
     * Apenas uma forma mais direta e rápida de chamar o método getFields seguido
     * do getFieldsUnique.
     * @param obj Object to be analyzed.<br>
     * Objeto a ser analisada.
     * @return List with unique fields.<br>
     * Lista com campos unicos.
     */
    public static List<Field> getAllFieldsUniques(Object obj){
        return getFieldsUnique(getAllFields(obj));
    }
    
    /**
     * ENGLISH<br>
     * Search in the entity's fields and in it's to find a field with the same name passed as
     * a method parameter, returning its value in the form of an Object.<br><br>
     * PORTUGUÊS<br>
     * Busca nos campos da entidade se encontra algum campo com mesmo nome passado
     * como parâmetro do método, retornando o seu valor na forma de um Object.
     * @param <T>
     * @param fieldName Name of the parameter to be searched.<br>
     * Nome do parâmetro a ser buscado.
     * @param obj
     * @return Requested field object.<br>
     * Objeto do campo solicitado.
     * @throws java.lang.NoSuchFieldException
     * @throws IllegalArgumentException.
     * @throws IllegalAccessException If, for some reason, access to the 
     * variable's value has not been obtained.<br>
     * Caso, por algum motivo, não tenha sido obtido acesso ao valor da variável.
     */
    public static <T> T getValueFrom(String fieldName, Object obj) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException{
        /*
        This part had to be included, as it was giving an error when getting the
        value of private fields of some objects.
        Still, it's just a palliative measure.
        */
        
        /*
        Essa parte foi preciso ser inclusa, pois estava dando erro na hora de obter
        o valor de campos privados de alguns objetos.
        Ainda assim, é apenas uma medida paleativa.
        */
        if(obj instanceof Entity){
            Entity ee = Entity.class.cast(obj);
            return ee.getValueFrom(fieldName);
        } else {
            Field field = null;
            Class clazz = obj.getClass();

            while(field == null && clazz != Object.class){

                List<Field> list = Arrays.asList(clazz.getDeclaredFields());

                for(Field f : list){
                    if(f.getName().endsWith(fieldName)){
                        Object cast = clazz.cast(obj);
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
     * Adds a value to an entity variable.
     * Adiciona um valor à uma varíavel de uma entidade.
     * @param fieldName Field name<br>
     * Nome da variável.
     * @param entity Object that will receive the value in your variable.<br>
     * Objeto que receberá o valor em sua variável.
     * @param value Value that will be placed in the variable.<br>
     * Valor que será posto na variável.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public static void setValue(String fieldName, Object entity, Object value) throws IllegalArgumentException, IllegalAccessException{
        List<Field> fields = getFields(entity);
        for(Field f : fields){
            if(f.getName().equals(fieldName)){
                f.setAccessible(true);
                f.set(entity, value);
                break;
           }
        }
    }
    
    /**
     * Checks whether the field is numeric, boolean, byte, or text.<br>
     * Verifica se o campo é numérico, boleano, byte ou texto.
     * @param field
     * @return 
     */
    public static boolean isPrimitive(Field field){
        return !(field.getType() != Short.TYPE &&
            field.getType() != Integer.TYPE &&
            field.getType() != Long.TYPE &&
            field.getType() != Byte.TYPE &&
            field.getType() != Float.TYPE &&
            field.getType() != Double.TYPE &&
            field.getType() != Boolean.TYPE &&
            field.getType() != String.class);
    }
    
}
