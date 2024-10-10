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

import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Entity;
import br.com.jhondbs.core.tools.ClassDictionary;
import br.com.jhondbs.core.tools.FieldsManager;
import br.com.jhondbs.core.tools.Reflection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jhones
 */
public class Reader {
    
    public int modoOperacional = Bottle.TEMP_STAGE;

    public Reader() {
    }
    
    public Reader(int modoOperacional) {
        this.modoOperacional = modoOperacional;
    }
    
    
    public String readFile(Entity entity) throws IOException, Exception {
        return readFile(entity.getClass(), entity.getId());
    }
    
    public String readFile(Class classe, String id) throws IOException {
        String path = classe.getName().replace(".class", "").replace(".", "/")+"/"+id;
        if(modoOperacional == 0) {
            path = Bottle.ROOT_DB+path;
        } else {
            path = Bottle.TEMP_DB+path;
        }
        
        File file = new File(path);
        if(file.exists()) {
            return Files.readString(file.toPath());
        }
        throw new FileNotFoundException("Arquivo inexistente: "+path);
    }
    
    public String readContent(Entity entity) throws IOException, Exception {
        String content = readFile(entity);
        if(content.contains("ref::")) {
            return content.substring(0, content.indexOf("ref::"));
        }
        return content;
    }
    
    public String readContent(Class classe, String id) throws IOException, Exception {
        String content = readFile(classe, id);
        if(content.contains("ref::")) {
            return content.substring(0, content.indexOf("ref::"));
        }
        return content;
    }
    
    public String readReferences(Entity entity) throws IOException, Exception {
        String refs = readFile(entity);
        if(refs.contains("ref::")) {
            return refs.substring(refs.indexOf("ref::")+"ref::".length());
        }
        return "";
    }
    
    public String readReferences(Class classe, String id) throws IOException, Exception {
        String refs = readFile(classe, id);
        if(refs.contains("ref::")) {
            return refs.substring(refs.indexOf("ref::")+"ref::".length());
        }
        return "";
    }
    
    public List<String> spliteredReferences(Entity entity) throws IOException, Exception {
        return spliteredReferences(entity.getClass(), entity.getId());
    }
    
    public List<String> spliteredReferences(Class classe, String id) throws IOException, Exception {
        List<String> list = new ArrayList<>();
        String refs = readReferences(classe, id);
        if(refs.contains("::")) {
            String[] split = refs.split("::");
            for(String s : split) {
                if(s != null && !s.isBlank()) {
                    list.add(s);
                }
            }
        }
        return list;
    }
    
    public Map<String, String> splitCapsuleAsKeyValueMap(String str) {
        Map<String, String> map = new HashMap<>();
        if(str.contains(":")) {
            if(str.startsWith("{")) {
                String chave = str.substring(1, str.indexOf(":"));
                String conteudo = str.substring(str.indexOf(":")+1, str.length()-1);
                map.put(chave, conteudo);
            }
        }
        return map;
    }
    
    public Map<String, String> splitFieldsAsMap(String str) {
        Map<String, String> mapa_campos = new HashMap<>();
        List<String> campos = splitCapsules(str);
        for(String s : campos) {
            mapa_campos.put(getKeyFromCapsule(s), getValueFromCapsule(s));
        }
        return mapa_campos;
    }
    
    public String[] splitCapsuleAsKeyValueArray(String str) {
        Map<String, String> splitered = splitCapsuleAsKeyValueMap(str);
        if(!splitered.isEmpty()) {
            String[] back = {splitered.keySet().stream().findFirst().get(), splitered.values().stream().findFirst().get()};
            return back;
        }
        return null;
    }
    
    public String getKeyFromCapsule(String str) {
        String[] split = splitCapsuleAsKeyValueArray(str);
        if(split != null) {
            return split[0];
        }
        return "";
    }
    
    public String getValueFromCapsule(String str) {
        String[] split = splitCapsuleAsKeyValueArray(str);
        if(split != null) {
            return split[1];
        }
        return "";
    }
    
    public List<String> splitCapsules(String str) {
        List<String> list = new ArrayList<>();
        if(str.equals("{}")) {
            return list;
        }
        while(str.contains("{") && str.contains("}")) {
            int a = str.indexOf("{");
            int b = a+1;
            int count = 1;
            while(count != 0 || b > str.length()) {
                if(str.charAt(b) == '{') {
                    count++;
                } else if(str.charAt(b) == '}') {
                    count--;
                }
                b++;
            }
            if(count == 0) {
                list.add(str.substring(a, b));
                str = str.substring(b);
            } else {
                return null;
            }
        }
        return list;
    }
    
    public Calendar parseCalendarFromString(String calendarString, ClassLoader loader) throws Exception {
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

    public Object parseDateTimeFromString(String dateTimeString, Class<?> type, ClassLoader loader) throws Exception {
        switch (type.getName()) {
            case "java.time.LocalDate" -> {
                try {
                    Class<?> localDateClass = Class.forName("java.time.LocalDate", true, loader);
                    return localDateClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.LocalTime" -> {
                try {
                    Class<?> localTimeClass = Class.forName("java.time.LocalTime", true, loader);
                    return localTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.LocalDateTime" -> {
                try {
                    Class<?> localDateTimeClass = Class.forName("java.time.LocalDateTime", true, loader);
                    return localDateTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.ZonedDateTime" -> {
                try {
                    Class<?> zonedDateTimeClass = Class.forName("java.time.ZonedDateTime", true, loader);
                    return zonedDateTimeClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            case "java.time.Instant" -> {
                try {
                    Class<?> instantClass = Class.forName("java.time.Instant", true, loader);
                    return instantClass.getMethod("parse", CharSequence.class).invoke(null, dateTimeString);
                } catch (DateTimeParseException e) {
                }
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Formato de data/tempo desconhecido: " + dateTimeString);
    }

    public Period parsePeriodFromString(String periodString, ClassLoader loader) {
        try {
            Class<?> periodClass = Class.forName("java.time.Period", true, loader);
            Object period = periodClass.getMethod("parse", CharSequence.class).invoke(null, periodString);
            return (Period) period;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de período desconhecido: " + periodString, e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao tentar parsear o período: " + periodString, e);
        }
    }

    public Object parsePrimitiveFromString(String input, Class<?> type, ClassLoader loader) throws Exception {
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
    
    public Map<String, String> readUniqueFieldsAsMap(Class entityClass, String id) throws Exception {
        Map<String, String> map = new HashMap<>();
        String capsule = readContent(entityClass, id);
        String campos = getValueFromCapsule(capsule);
        Map<String, String> mapaCampos = splitFieldsAsMap(campos);
        
        List<Field> fields = FieldsManager.getFieldsUnique(FieldsManager.getAllFields(entityClass));
        for(Field field : fields) {
            if(mapaCampos.containsKey(field.getName())) {
                map.put(field.getName(), mapaCampos.get(field.getName()));
            }
        }
        return map;
    }
    
    public List<Field> listUniqueFields(Class classe) {
        List<Field> allFields = FieldsManager.getAllFields(classe);
        return FieldsManager.getFieldsUnique(allFields);
    }
    
    public int extractValue(Pattern pattern, String text, ClassLoader loader) {
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

    public String extractString(Pattern pattern, String text, ClassLoader loader) {
        try {
            Class<?> matcherClass = Class.forName("java.util.regex.Matcher", true, loader);
            Matcher matcher = pattern.matcher(text);
            if ((boolean) matcherClass.getMethod("find").invoke(matcher)) {
                return (String) matcherClass.getMethod("group", int.class).invoke(matcher, 1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao tentar extrair a string: " + text, e);
        }
        return "GMT"; // valor padrão se o campo não for encontrado
    }
    
    public String sendToTemp(Entity entity) throws Exception {
        return sendToTemp(entity.getClass(), entity.getId());
    }
    
    public String sendToTemp(Class classe, String id) throws IOException, Exception {
        Reader reader = new Reader(Bottle.TEMP_STAGE);
        try {
            return reader.readFile(classe, id);
        } catch (Exception e) {
            reader.modoOperacional = Bottle.ROOT_STAGE;
        }
        String line = reader.readFile(classe, id);
        Writer writer = new Writer(Bottle.TEMP_STAGE);
        writer.writeText(classe, id, line);
        return line;
    }
    
    public List<String> listAllIds(Class entityClass) {
        String path = entityClass.getName().replace(".class", "").replace(".", "/");
        if(modoOperacional == 0) {
            path = Bottle.ROOT_DB+path;
        } else {
            path = Bottle.TEMP_DB+path;
        }
        
        File file = new File(path);
        if(file.exists()) {
            return Arrays.asList(file.list());
        }
        return new ArrayList<>();
    }
    
    public List<Entity> listExcludeds(Bottle bottle) throws Exception {
        List<Entity> list = new ArrayList<>();
        
        try {
            Bottle bot = new Bottle(bottle.entity.getClass(), bottle.entity.getId(), Bottle.ROOT_STAGE);
            if(bot.entity != null) {
                for(String id : bot.bottles.keySet()) {
                    if(!bottle.bottles.containsKey(id)) {
                        list.add(bot.bottles.get(id).entity);
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return list;
    }
    
    /**
     * Recebe uma entidade e busca por subentidades anotadas como cascata.
     * @param entity
     * @return 
     */
    public List<Entity> listCascateEntities(Entity entity) throws IllegalArgumentException, IllegalAccessException {
        List<Field> fields = FieldsManager.getAllFields(entity);
        fields = fields.stream().filter(field -> field.isAnnotationPresent(Cascate.class)).toList();
        
        List<Entity> entes = new ArrayList<>();
        
        for(Field field : fields) {
            field.setAccessible(true);
            Object valor = field.get(entity);
            if(valor != null) {
                if(ClassDictionary.getIndex(valor.getClass()) != -1 || Reflection.isArrayMap(field.getType())) {
                    if(valor.getClass().isEnum()) {
                    } else if(Reflection.isArrayMap(field.getType())) {
                            if(Reflection.isInstance(valor.getClass(), List.class)) {
                                List l = (List) valor;
                                if(!l.isEmpty()) {
                                    for(Object o : l) {
                                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                                            entes.add((Entity) o);
                                        }
                                    }
                                }
                            } else if(Reflection.isInstance(valor.getClass(), Map.class)) {
                                Map m = (Map) valor;
                                if(!m.isEmpty()) {
                                    for(Object o : m.keySet()) {
                                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                                            entes.add((Entity) o);
                                        }
                                    }
                                    for(Object o : m.values()) {
                                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                                            entes.add((Entity) o);
                                        }
                                    }
                                }
                            }
                    } else {
                        if(Reflection.isInstance(field.getType(), Entity.class)) {
                            Entity ente = (Entity) valor;
                            entes.add(ente);
                        } else if(!Reflection.isPrimitive(field.getType()) && !Reflection.isNumerical(field.getType()) && !Reflection.isDate(field.getType())) {
                            fillSubEntities(valor, entes, entity);
                        }
                    }
                }
            }
        }
        
        return entes;
    }
    
    public List<Entity> listSubEntities(Entity entity) throws IllegalArgumentException, IllegalAccessException {
        List<Entity> entes = new ArrayList<>();
        List<Field> fields = FieldsManager.getAllSerializebleFields(entity.getClass());
        for(Field field : fields) {
            field.setAccessible(true);
            Object valor = field.get(entity);
            if(valor != null) {
                if(ClassDictionary.getIndex(valor.getClass()) != -1 || Reflection.isArrayMap(field.getType())) {
                    if(valor.getClass().isEnum()) {
                    } else if(Reflection.isArrayMap(field.getType())) {
                            if(Reflection.isInstance(valor.getClass(), List.class)) {
                                List l = (List) valor;
                                if(!l.isEmpty()) {
                                    for(Object o : l) {
                                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                                            entes.add((Entity) o);
                                        }
                                    }
                                }
                            } else if(Reflection.isInstance(valor.getClass(), Map.class)) {
                                Map m = (Map) valor;
                                if(!m.isEmpty()) {
                                    for(Object o : m.keySet()) {
                                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                                            entes.add((Entity) o);
                                        }
                                    }
                                    for(Object o : m.values()) {
                                        if(Reflection.isInstance(o.getClass(), Entity.class)) {
                                            entes.add((Entity) o);
                                        }
                                    }
                                }
                            }
                    } else {
                        if(Reflection.isInstance(field.getType(), Entity.class)) {
                            Entity ente = (Entity) valor;
                            entes.add(ente);
                        } else if(!Reflection.isPrimitive(field.getType()) && !Reflection.isNumerical(field.getType()) && !Reflection.isDate(field.getType())) {
                            fillSubEntities(valor, entes, entity);
                        }
                    }
                }
            }
        }
        return null;
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
