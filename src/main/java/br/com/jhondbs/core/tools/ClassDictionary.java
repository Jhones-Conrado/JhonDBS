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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhonessales
 */
public class ClassDictionary {
    
    private static volatile Properties dictionary;
    private static ConcurrentHashMap<Integer, Class> REVERSE = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();
    
    public static int getIndex(Class clazz){
        return Integer.valueOf(get().getOrDefault(clazz.getName(), "-1").toString());
    }
    
    public static Class fromIndex(int index){
        get();
        return REVERSE.getOrDefault(index, null);
    }
    
    public static Properties get() {
        if (dictionary == null) {
            synchronized (LOCK) {
                if (dictionary == null) {
                    try {
                        startDictionary();
                    } catch (IOException | URISyntaxException ex) {
                        Logger.getLogger(ClassDictionary.class.getName()).log(Level.SEVERE, "Erro ao inicializar o dicionário", ex);
                    }
                }
            }
        }
        return dictionary;
    }
    
    /**
     * Initializes the database class dictionary if it does not already exist.
     * @throws IOException 
     */
    private static void startDictionary() throws IOException, URISyntaxException{
        synchronized (ClassDictionary.class) {
            if (dictionary == null) {
                dictionary = new Properties();
                boolean save = false;

                File dic = new File("db/dictionary.dic");
                if(dic.exists()){
                    try (FileInputStream fis = new FileInputStream(dic)) {
                        dictionary.load(fis);
                    }
                } else {
                    standardClasses();
                    save = true;
                }

                int i = dictionary.size();
                List<Class<?>> all = Reflection.allImplementsNotAbstract(Object.class);
                all.forEach(cl -> dictionary.putIfAbsent(cl.getName(), String.valueOf(dictionary.size())));
                dictionary.forEach((k, v) -> {
                    try {
                        int index = Integer.parseInt(v.toString());
                        Class mc = Reflection.makeClass(k.toString());
                        if(mc != null) REVERSE.putIfAbsent(index, mc);
                    } catch (NumberFormatException e) {
                        Logger.getLogger(ClassDictionary.class.getName()).log(Level.WARNING, 
                            "Valor inválido no dicionário para a classe " + k, e);
                    }
                });
                if(i < dictionary.size()) {
                    save = true;
                }

                if(save) {
                    new File("db").mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(dic)) {
                        dictionary.store(fos, "JhonDBS Class Dictionary");
                    }
                }
            }
        }
    }
    
    private static void standardClasses() {
        /*
        Primitivos.
        */
        dictionary.put(Boolean.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Byte.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Short.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Integer.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Long.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Float.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Double.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(String.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Character.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(BigInteger.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(BigDecimal.class.getName(), String.valueOf(dictionary.size()));

        /*
        Datas.
        */
        dictionary.put(Date.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Calendar.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(GregorianCalendar.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Temporal.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(LocalDate.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(LocalTime.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(LocalDateTime.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(ZonedDateTime.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Instant.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(ChronoPeriod.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Period.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(TemporalAmount.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Duration.class.getName(), String.valueOf(dictionary.size()));

        /*
        Listas, arrays e mapas.
        */
        dictionary.put(List.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Map.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Set.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(Properties.class.getName(), String.valueOf(dictionary.size()));

        /*
        Imagens e arquivos.
        */
        dictionary.put(Image.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(BufferedImage.class.getName(), String.valueOf(dictionary.size()));
        dictionary.put(File.class.getName(), String.valueOf(dictionary.size()));
    }
    
}
