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
package br.com.jhondbs.core.tools;

import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.interfaces.Unique;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        if(obj == null) return null;
        return getAllFields(obj.getClass());
    }
    
    /**
     * Ensures that all fields of the class and its superclasses are returned.
     * Garante que todos os campos da classe e suas superclasses sejam retornados.
     * @return list with all fields of the object.<br>
     * lista com todos os campos do objeto.
     */
    public static List<Field> getAllFields(Class clazz){
        if(clazz == null) return null;
        List<Field> fields = new ArrayList<>();
        while(clazz != null){
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
    
    public static List<Field> getAllSerializebleFields(Class clazz){
        Set<Field> fields = new HashSet<>();
        while(clazz != null){
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.stream().filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())).toList();
    }
    
    /**
     * Retorna o field de ID da classe.
     * @param clazz
     * @return 
     * @throws br.com.jhondbs.core.db.errors.EntityIdBadImplementationException 
     */
    public static Field getFieldId(Class clazz) throws EntityIdBadImplementationException {
        List<Field> ids = FieldsManager.getAllFields(clazz).parallelStream()
            .filter(field -> (field.getType().isAssignableFrom(String.class)))
            .filter(field -> (field.getName().toUpperCase().contains("ID")))
            .toList();
        if(!ids.isEmpty()){
            Field selected = null;
            List<Field> toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("enteid")))
                    .toList();
            if(toList.size() == 1){
                selected = toList.get(0);
            } else {
                toList = ids.stream().filter(field -> (field.getName().equalsIgnoreCase("id")))
                    .toList();
                if(toList.size() == 1){
                    selected = toList.get(0);
                } else {
                    selected = ids.get(0);
                }
            }
            if(selected != null){
                return selected;
            } else {
                throw new EntityIdBadImplementationException();
            }
        }
        throw new EntityIdBadImplementationException();
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
        if(obj == null) return null;
        List<Field> fields = getAllFields(obj.getClass());
        Field field = fields.stream()
            .filter(f -> f.getName().equals(fieldName))
            .findFirst().get();
        
        if(field != null) {
            field.setAccessible(true);
            return (T) field.get(obj);
        }
        return null;
    }
    
    public static <T> T getValue(Field field, Object object) throws Exception {
        field.setAccessible(true);
        Class clazz = object.getClass();
        Object casted = clazz.cast(object);
        while(casted.getClass() != Object.class) {
            try {
                return (T) field.get(casted);
            } catch (Exception e) {
                casted = casted.getClass().getSuperclass().cast(casted);
            }
        }
        throw new Exception("Campo não localizado no objeto.");
    }
    
    /**
     * Adds a value to an entity variable.
     * Adiciona um valor à uma varíavel de uma entidade.
     * @param fieldName Field name<br>
     * Nome da variável.
     * @param receptor Object that will receive the value in your variable.<br>
     * Objeto que receberá o valor em sua variável.
     * @param value Value that will be placed in the variable.<br>
     * Valor que será posto na variável.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public static void setValue(String fieldName, Object receptor, Object value) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException{
        List<Field> fields = getAllFields(receptor);
        for(Field f : fields){
            if(f.getName().equals(fieldName)){
                f.setAccessible(true);
                
                if(Reflection.isInstance(f.getType(), Set.class)) {
                    if(Reflection.isInstance(value.getClass(), List.class)) {
                        Set set = new HashSet();
                        set.addAll((List) value);
                        f.set(receptor, set);
                        break;
                    }
                } else if(f.getType().getName().startsWith("[")) {
                    Object array = createArrayFromList(f, (List) value);
                    f.set(receptor, array);
                    break;
                }
                
                f.set(receptor, value);
                break;
           }
        }
    }
    

    /**
     * Cria um novo array do mesmo tipo que field.getType(), populado com os valores de um ArrayList.
     *
     * @param field O Field cujo tipo de array será usado (ex.: String[], byte[], etc.).
     * @param list  O ArrayList contendo os valores a serem inseridos no novo array.
     * @return Um novo array do tipo identificado, preenchido com os valores da lista.
     * @throws IllegalArgumentException Se o tipo do field não for um array ou se os valores da lista forem incompatíveis.
     */
    public static Object createArrayFromList(Field field, List<?> list) {
        // Verifica se o tipo do field é um array
        Class<?> fieldType = field.getType();
        if (!fieldType.isArray()) {
            throw new IllegalArgumentException("O tipo do field deve ser um array: " + fieldType.getName());
        }

        // Obtém o tipo dos elementos do array (ex.: String para String[], byte para byte[])
        Class<?> componentType = fieldType.getComponentType();
        int length = list.size();

        // Cria um novo array com o mesmo tipo e tamanho da lista
        Object newArray = Array.newInstance(componentType, length);

        // Popula o array com os valores da lista
        if (componentType.isPrimitive()) {
            // Tratamento para arrays de tipos primitivos
            if (componentType == byte.class) {
                byte[] array = (byte[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = ((Number) list.get(i)).byteValue(); // Converte para byte
                }
            } else if (componentType == short.class) {
                short[] array = (short[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = ((Number) list.get(i)).shortValue();
                }
            } else if (componentType == int.class) {
                int[] array = (int[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = ((Number) list.get(i)).intValue();
                }
            } else if (componentType == long.class) {
                long[] array = (long[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = ((Number) list.get(i)).longValue();
                }
            } else if (componentType == float.class) {
                float[] array = (float[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = ((Number) list.get(i)).floatValue();
                }
            } else if (componentType == double.class) {
                double[] array = (double[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = ((Number) list.get(i)).doubleValue();
                }
            } else if (componentType == char.class) {
                char[] array = (char[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = (Character) list.get(i); // Unboxing direto
                }
            } else if (componentType == boolean.class) {
                boolean[] array = (boolean[]) newArray;
                for (int i = 0; i < length; i++) {
                    array[i] = (Boolean) list.get(i);
                }
            }
        } else {
            // Tratamento para arrays de objetos (String[], Object[], etc.)
            for (int i = 0; i < length; i++) {
                Object value = list.get(i);
                if (value != null && !componentType.isAssignableFrom(value.getClass())) {
                    throw new IllegalArgumentException(
                        "Elemento na posição " + i + " (" + value.getClass().getName() + 
                        ") não é compatível com o tipo do array: " + componentType.getName()
                    );
                }
                Array.set(newArray, i, value);
            }
        }

        return newArray;
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
    
    public static boolean isFieldPresent(Class clazz, String fieldname){
        List<Field> fields = getAllFields(clazz);
        
        boolean b = false;
        
        for(Field field : fields){
            if(field.getName().equalsIgnoreCase(fieldname)){
                b = true;
            }
        }
        
        return b;
    }
    
}
