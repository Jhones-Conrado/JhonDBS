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
package br.com.jhondbs.core.webServer.handlers;

import br.com.jhondbs.core.tools.Reflection;
import br.com.jhondbs.core.webServer.functions.DoMethod;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhonessales
 */
public class HandlerBottle {
    
    private static Map<String, Map<String, DoMethod>> handlers = new HashMap<>();
    private static List<String> roots = new ArrayList<>();
    
    public static void add(String path, String method, DoMethod doMethod){
        if(handlers.containsKey(path.toUpperCase())){
            handlers.get(path.toUpperCase()).put(method.toUpperCase(), doMethod);
        } else {
            Map<String, DoMethod> param = new HashMap<>();
            param.put(method.toUpperCase(), doMethod);
            handlers.put(path.toUpperCase(), param);
        }
    }
    
    public static void remove(String path, String method){
        if(handlers.containsKey(path.toUpperCase())){
            if(handlers.get(path.toUpperCase()).containsKey(method.toUpperCase())){
                handlers.get(path.toUpperCase()).remove(method.toUpperCase());
            }
        }
    }
    
    public static boolean contains(String path, String method){
        if(handlers.containsKey(path.toUpperCase())){
            return handlers.get(path.toUpperCase()).containsKey(method.toUpperCase());
        }
        return false;
    }
    
    public static DoMethod getMethod(String path, String method) throws NoSuchMethodException{
        if(contains(path, method)){
            return handlers.get(path).get(method);
        }
        throw new NoSuchMethodException(method+" "+path);
    }
    
    public static void make(HttpExchange exchange) throws ClassNotFoundException, IOException, NoSuchMethodException{
        String path = exchange.getRequestURI().toString();
        String requestMethod = exchange.getRequestMethod();
        
        if(contains(path, requestMethod)){
            DoMethod domethod = getMethod(path, requestMethod);
            
            if(domethod.numberParameters() > 0){
                withParameters(exchange);
            } else {
                noParameters(exchange);
            }
        } else {
            System.out.println("\nNão encontrado: "+exchange.getRequestURI());
            String notfound = "<h1>Not Found</h1>";
            exchange.sendResponseHeaders(404, notfound.getBytes().length);
            try (OutputStream rb = exchange.getResponseBody()) {
                rb.write(notfound.getBytes());
                rb.flush();
            }
        }
    }
    
    private static void withParameters(HttpExchange exchange) throws ClassNotFoundException, NoSuchMethodException, IOException{
        String path = exchange.getRequestURI().toString();
        String requestMethod = exchange.getRequestMethod();
        Headers headers = exchange.getRequestHeaders();
        
        DoMethod domethod = getMethod(path, requestMethod);
        List<Object> args = new ArrayList<>();

        // CARREGA OS PARÂMETROS NECESSÁRIOS.
        Parameter[] params = domethod.getParameters();
        for(Parameter p : params){
            if(Reflection.isInstance(p.getType(), String.class)){
                if(headers.containsKey(p.getName())){
                    args.add(headers.get(p.getName()));
                } else if(domethod.hasVariable(p.getName())) {
                    args.add(domethod.getVariable(p.getName(), path));
                } else {
                    args.add(null);
                }
            }
        }

        //Caso seja um método que retorne String, deverá ir para o templete engine.
        if(Reflection.isInstance(domethod.getReturnType(), String.class)){
            try {
                String template = domethod.invoke(args.toArray());
                try (OutputStream rb = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(200, template.getBytes().length);
                    rb.write(template.getBytes());
                    rb.flush();
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex) {
                Logger.getLogger(HandlerBottle.class.getName()).log(Level.SEVERE, null, ex);
                sendError(exchange, ex);
            }
        } else {
            // Caso contrário receberá uma resposta padrão.
            try {
                String resp = "Resposta nula";
                try (OutputStream rb = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(200, resp.getBytes().length);
                    rb.write(resp.getBytes());
                    rb.flush();
                }
            } catch (IllegalArgumentException | IOException ex) {
                Logger.getLogger(HandlerBottle.class.getName()).log(Level.SEVERE, null, ex);
                sendError(exchange, ex);
            }
        }
    }
    
    private static void noParameters(HttpExchange exchange) throws IOException{
        try {
            String resp = "Resposta nula";
            try (OutputStream rb = exchange.getResponseBody()) {
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                rb.write(resp.getBytes());
                rb.flush();
            }
        } catch (IllegalArgumentException | IOException ex) {
            Logger.getLogger(HandlerBottle.class.getName()).log(Level.SEVERE, null, ex);
            sendError(exchange, ex);
        }
    }
    
    private static void sendError(HttpExchange exchange, Exception ex) throws IOException{
        String template = "<h1>Internal Server Error</h1>"
                + "<br><br>"
                + "<p>"
                +ex
                +"</p>";
        try (OutputStream rb = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(500, template.getBytes().length);
            rb.write(template.getBytes());
            rb.flush();
        }
    }
    
}
