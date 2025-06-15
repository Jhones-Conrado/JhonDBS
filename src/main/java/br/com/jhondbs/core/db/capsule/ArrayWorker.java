/*
 * The MIT License
 *
 * Copyright 2025 jhones.
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
package br.com.jhondbs.core.db.capsule;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Fundamental para converter um array do tipo desconhecido "?[]" onde não se sabe 
 * qual o tipo de array. O método toArrayList identifica qual o tipo do array e 
 * converte-o em uma lista do tipo específico.
 * @author jhones
 */
public class ArrayWorker {
    
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> toArrayList(Object array) {
        // Validação de entrada
        if (array == null) {
            throw new IllegalArgumentException("O array não pode ser null");
        }
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException("O argumento deve ser um array");
        }

        // Obtém o tipo do componente do array
        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);

        // Cria o ArrayList com o tipo apropriado
        ArrayList<T> result = new ArrayList<>(length);

        // Trata arrays de tipos primitivos e objetos
        if (componentType.isPrimitive()) {
            // Conversão manual para tipos primitivos
            if (componentType == int.class) {
                int[] intArray = (int[]) array;
                for (int i : intArray) {
                    result.add((T) Integer.valueOf(i)); // Boxing manual
                }
            } else if (componentType == double.class) {
                double[] doubleArray = (double[]) array;
                for (double d : doubleArray) {
                    result.add((T) Double.valueOf(d));
                }
            } else if (componentType == boolean.class) {
                boolean[] boolArray = (boolean[]) array;
                for (boolean b : boolArray) {
                    result.add((T) Boolean.valueOf(b));
                }
            } else if (componentType == byte.class) {
                byte[] byteArray = (byte[]) array;
                for (byte b : byteArray) {
                    result.add((T) Byte.valueOf(b));
                }
            } else if (componentType == char.class) {
                char[] charArray = (char[]) array;
                for (char c : charArray) {
                    result.add((T) Character.valueOf(c));
                }
            } else if (componentType == float.class) {
                float[] floatArray = (float[]) array;
                for (float f : floatArray) {
                    result.add((T) Float.valueOf(f));
                }
            } else if (componentType == long.class) {
                long[] longArray = (long[]) array;
                for (long l : longArray) {
                    result.add((T) Long.valueOf(l));
                }
            } else if (componentType == short.class) {
                short[] shortArray = (short[]) array;
                for (short s : shortArray) {
                    result.add((T) Short.valueOf(s));
                }
            }
        } else {
            // Arrays de objetos (String[], Object[], etc.)
            T[] objArray = (T[]) array;
            result.addAll(Arrays.asList(objArray));
        }

        return result;
    }
    
}
