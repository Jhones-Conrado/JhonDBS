/*
 * The MIT License
 *
 * Copyright 2024 jhones.
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

import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.tools.Reflection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mantém funções auxiliares que ajudam na leitura de arquivos de entidades.
 * @author jhones
 */
public class Reader {
    
    public Reader() {
    }
    
    /**
     * Carrega o arquivo properties de uma entidade que pode estar no banco de dados
     * ou em uma pasta temporária.
     * @param clazz Classe da entidade.
     * @param id ID da entidade.
     * @param temp Opcional, se for nulo ou branco carregará do Banco de Dados.
     * Se tiver algum path de pasta temporária, carregará a partir de lá.
     * @return
     * @throws Exception 
     */
    public static Properties read(Class clazz, String id, String temp) throws Exception {
        Properties props = new Properties();
        String path = Assist.getPath(new Ref(clazz, id), temp);
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("Entidade não encontrada: " + path + " | No carregamento da entidade: "+clazz+" -> "+id);
        }
        props.load(new FileInputStream(file));
        return props;
    }
    
    /**
     * Transforma uma String do tipo {chave:valor} em um mapa do tipo <String chave, String valor>.
     * @param str
     * @return 
     */
    public static Map<String, String> splitCapsuleAsKeyValueMap(String str) {
        Map<String, String> map = new HashMap<>();
        if(str.contains(":") && str.startsWith("{")) {
            String chave = str.substring(1, str.indexOf(":"));
            String conteudo = str.substring(str.indexOf(":")+1, str.length()-1);
            map.put(chave, conteudo);
        }
        return map;
    }
    
    /**
     * Recebe uma String de várias capsulas e as quebra em um mapa de chave e valor.
     * Ideal para uso em uma String de capsulas de Fields por exemplo.
     * Retornando um mapa com "chave = nome do field" e valor como uma capsula{indice:valor}.
     * @param str
     * @return 
     */
    public static Map<String, String> splitFieldsAsMap(String str) {
        Map<String, String> mapa_campos = new HashMap<>();
        List<String> campos = splitCapsules(str);
        for(String s : campos) {
            mapa_campos.put(getKeyFromCapsule(s), getValueFromCapsule(s));
        }
        return mapa_campos;
    }
    
    public static String getKeyFromCapsule(String str) {
        Map<String, String> map = splitCapsuleAsKeyValueMap(str);
        if(!map.isEmpty()) {
            return map.keySet().stream().findFirst().get();
        }
        return "";
    }
    
    public static String getValueFromCapsule(String str) {
        Map<String, String> map = splitCapsuleAsKeyValueMap(str);
        if(!map.isEmpty()) {
            return map.values().stream().findFirst().get();
        }
        return "";
    }
    
    /**
     * Recebe uma String de várias capsulas aninhadas e as separa em uma lista.
     * @param str
     * @return 
     */
    public static List<String> splitCapsules(String str) {
        List<String> list = new ArrayList<>();
        if (str == null || str.equals("{}")) {
            return list;
        }
        Stack<Integer> stack = new Stack<>();
        int start = -1;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') {
                if (stack.isEmpty()) {
                    start = i;
                }
                stack.push(i);
            } else if (c == '}') {
                if (stack.isEmpty()) {
                    return null; // Desbalanceado
                }
                stack.pop();
                if (stack.isEmpty() && start != -1) {
                    list.add(str.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        if (!stack.isEmpty()) {
            return null; // Desbalanceado
        }
        return list;
    }
    
    public static Calendar parseCalendarFromString(String calendarString, ClassLoader loader) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Pattern patternYear = Pattern.compile("YEAR=(\\d+)");
        Pattern patternMonth = Pattern.compile("MONTH=(\\d+)");
        Pattern patternDayOfMonth = Pattern.compile("DAY_OF_MONTH=(\\d+)");
        Pattern patternHour = Pattern.compile("HOUR_OF_DAY=(\\d+)");
        Pattern patternMinute = Pattern.compile("MINUTE=(\\d+)");
        Pattern patternSecond = Pattern.compile("SECOND=(\\d+)");
        Pattern patternMillisecond = Pattern.compile("MILLISECOND=(\\d+)");
        Pattern patternZone = Pattern.compile("id=\"([^\"]+)\"");
        int year = extractValue(patternYear, calendarString, loader);
        int month = extractValue(patternMonth, calendarString, loader);
        int dayOfMonth = extractValue(patternDayOfMonth, calendarString, loader);
        int hour = extractValue(patternHour, calendarString, loader);
        int minute = extractValue(patternMinute, calendarString, loader);
        int second = extractValue(patternSecond, calendarString, loader);
        int millisecond = extractValue(patternMillisecond, calendarString, loader);
        String timeZoneID = extractString(patternZone, calendarString, loader);
        Class<?> calendarClass = Class.forName("java.util.GregorianCalendar", true, loader);
        Class<?> timeZoneClass = Class.forName("java.util.TimeZone", true, loader);
        Calendar calendar = (Calendar) calendarClass.getDeclaredConstructor().newInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        Object timeZone = timeZoneClass.getMethod("getTimeZone", String.class).invoke(null, timeZoneID);
        calendar.setTimeZone((java.util.TimeZone) timeZone);
        return calendar;
    }

    public static Object parseDateTimeFromString(String dateTimeString, Class<?> type, ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        switch (type.getName()) {
            case "java.time.LocalDate" -> {
                Class<?> localDateClass = Class.forName("java.time.LocalDate", true, loader);
                return localDateClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
            }
            case "java.time.LocalTime" -> {
                Class<?> localTimeClass = Class.forName("java.time.LocalTime", true, loader);
                return localTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
            }
            case "java.time.LocalDateTime" -> {
                Class<?> localDateTimeClass = Class.forName("java.time.LocalDateTime", true, loader);
                return localDateTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
            }
            case "java.time.ZonedDateTime" -> {
                Class<?> zonedDateTimeClass = Class.forName("java.time.ZonedDateTime", true, loader);
                return zonedDateTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
            }
            case "java.time.Instant" -> {
                Class<?> instantClass = Class.forName("java.time.Instant", true, loader);
                return instantClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Formato de data/tempo desconhecido: " + dateTimeString);
    }

    public static Period parsePeriodFromString(String periodString, ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            Class<?> periodClass = Class.forName("java.time.Period", true, loader);
            Object period = periodClass.getMethod("parse", CharSequence.class).invoke(null, periodString);
            return (Period) period;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de período desconhecido: " + periodString, e);
        }
    }

    public static Object parsePrimitiveFromString(String input, Class<?> type, ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (type == String.class) {
            return input; // A string já é o valor
        }
        Class<?> charClass = Class.forName("java.lang.Character", true, loader);
        Class<?> shortClass = Class.forName("java.lang.Short", true, loader);
        Class<?> intClass = Class.forName("java.lang.Integer", true, loader);
        Class<?> longClass = Class.forName("java.lang.Long", true, loader);
        Class<?> floatClass = Class.forName("java.lang.Float", true, loader);
        Class<?> doubleClass = Class.forName("java.lang.Double", true, loader);
        Class<?> booleanClass = Class.forName("java.lang.Boolean", true, loader);
        Class<?> byteClass = Class.forName("java.lang.Byte", true, loader);
        Class<?> bigDecimalClass = Class.forName("java.math.BigDecimal", true, loader);
        Class<?> bigIntegerClass = Class.forName("java.math.BigInteger", true, loader);
        if (type == char.class || type == charClass) {
            if (input.length() == 1) {
                return input.charAt(0); // Converte a string para char se tiver um único caractere
            } else {
                throw new IllegalArgumentException("Formato inválido para char: " + input);
            }
        } else if (type == short.class || type == shortClass) {
            return shortClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == int.class || type == intClass) {
            return intClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == long.class || type == longClass) {
            return longClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == float.class || type == floatClass) {
            return floatClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == double.class || type == doubleClass) {
            return doubleClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == boolean.class || type == booleanClass) {
            return booleanClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == byte.class || type == byteClass) {
            return byteClass.getMethod("valueOf", String.class).invoke(null, input);
        } else if (type == bigDecimalClass) {
            return bigDecimalClass.getConstructor(String.class).newInstance(input);
        } else if (type == bigIntegerClass) {
            return bigIntegerClass.getConstructor(String.class).newInstance(input);
        } else {
            throw new IllegalArgumentException("Tipo não suportado: " + type.getSimpleName());
        }
    }
    
    public static Map<String, String> readUniqueFieldsAsMap(Class entityClass, String id, String temp) throws Exception {
        Map<String, String> map = new HashMap<>();
        Properties props = read(entityClass, id, temp);
        String capsule = props.getProperty("fields");
        Map<String, String> mapaCampos = splitFieldsAsMap(capsule);
        
        List<Field> fields = FieldsManager.getFieldsUnique(FieldsManager.getAllFields(entityClass));
        for(Field field : fields) {
            if(mapaCampos.containsKey(field.getName())) {
                map.put(field.getName(), mapaCampos.get(field.getName()));
            }
        }
        return map;
    }
    
    public static List<Field> listUniqueFields(Class classe) {
        List<Field> allFields = FieldsManager.getAllFields(classe);
        return FieldsManager.getFieldsUnique(allFields);
    }
    
    public static int extractValue(Pattern pattern, String text, ClassLoader loader) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                Class<?> integerClass = Class.forName("java.lang.Integer", true, loader);
                return (int) integerClass.getMethod("parseInt", String.class).invoke(null, matcher.group(1));
            } catch (Exception e) {
                throw new RuntimeException("Erro ao tentar parsear o valor: " + matcher.group(1), e);
            }
        }
        return 0; // valor padrão se o campo não for encontrado
    }
    
    public static String extractString(Pattern pattern, String text, ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> matcherClass = Class.forName("java.util.regex.Matcher", true, loader);
        Matcher matcher = pattern.matcher(text);
        if ((boolean) matcherClass.getMethod("find").invoke(matcher)) {
            return (String) matcherClass.getMethod("group", int.class).invoke(matcher, 1);
        }
        return "GMT"; // valor padrão se o campo não for encontrado
    }
    
    public static List<String> listAllIds(Class entityClass, String prefix) {
        String path = entityClass.getName().replace(".class", "").replace(".", "/");
        if(!prefix.endsWith("/")) prefix = prefix+"/";
        if(prefix != null) {
            path = prefix+path;
        }
        File file = new File(path);
        if(file.exists()) {
            return Arrays.asList(file.list());
        }
        return new ArrayList<>();
    }
    
    /**
     * Sub-método do método listSubEntitites. Sua função é formar uma busca recursiva por sub entidades em um objeto complexo.
     * Apenas a primeira geração de sub-entidades será retornada, a busca não irá recorrer sobre as entidades localizadas.
     * @param o
     * @param list
     * @param parent
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public void fillSubEntities(Object o, List<Entity> list, Entity parent) throws IllegalArgumentException, IllegalAccessException {
        List<Field> fields = FieldsManager.getAllSerializebleFields(o.getClass());
        for(Field field : fields) {
            field.setAccessible(true);
            Object valor = field.get(o);
            if(valor != null) {
                if(ClassDictionary.getIndex(valor.getClass()) != -1 || Reflection.isArrayMap(field.getType())) {
                    if(valor.getClass().isEnum()) {
                    } else if(Reflection.isArrayMap(field.getType())) {
                            if(Reflection.isInstance(valor.getClass(), List.class)) {
                                List l = (List) valor;
                                if(!l.isEmpty()) {
                                    for(Object oo : l) {
                                        if(Reflection.isInstance(oo.getClass(), Entity.class)) {
                                            list.add((Entity) oo);
                                        }
                                    }
                                }
                            } else if(Reflection.isInstance(valor.getClass(), Map.class)) {
                                Map m = (Map) valor;
                                if(!m.isEmpty()) {
                                    for(Object oo : m.keySet()) {
                                        if(Reflection.isInstance(oo.getClass(), Entity.class)) {
                                            list.add((Entity) oo);
                                        } else if(!Reflection.isPrimitive(field.getType()) && !Reflection.isNumerical(field.getType()) && !Reflection.isDate(field.getType()) && !oo.getClass().isEnum()) {
                                            fillSubEntities(oo, list, parent);
                                        }
                                    }
                                    for(Object oo : m.values()) {
                                        if(Reflection.isInstance(oo.getClass(), Entity.class)) {
                                            list.add((Entity) oo);
                                        } else if(!Reflection.isPrimitive(field.getType()) && !Reflection.isNumerical(field.getType()) && !Reflection.isDate(field.getType()) && !oo.getClass().isEnum()) {
                                            fillSubEntities(oo, list, parent);
                                        }
                                    }
                                }
                            }
                    } else {
                        if(Reflection.isInstance(field.getType(), Entity.class)) {
                            Entity ente = (Entity) valor;
                            list.add(ente);
                        } else if(!Reflection.isPrimitive(field.getType()) && !Reflection.isNumerical(field.getType()) && !Reflection.isDate(field.getType())) {
                            fillSubEntities(valor, list, parent);
                        }
                    }
                }
            }
        }
    }
    
    public List<String> extractUUIDs(String input) {
        List<String> uuids = new ArrayList<>();
        // Expressão regular para UUIDs (versão simplificada)
        String uuidPattern = "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b";
        
        // Compilando o padrão
        Pattern pattern = Pattern.compile(uuidPattern);
        Matcher matcher = pattern.matcher(input);
        
        // Percorrendo e capturando os UUIDs
        while (matcher.find()) {
            uuids.add(matcher.group());
        }
        
        return uuids;
    }
    
}
