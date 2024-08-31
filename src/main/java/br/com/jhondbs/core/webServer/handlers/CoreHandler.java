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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhonessales
 */
public class CoreHandler implements HttpHandler{
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Method: "+exchange.getRequestMethod());
        System.out.println("Request URI: "+exchange.getRequestURI());
        System.out.println("Path: "+exchange.getHttpContext().getPath());
        Headers h = exchange.getRequestHeaders();
        h.keySet().forEach(key -> {
            System.out.println(key+": "+h.get(key));
        });
        
        try {
            HandlerBottle.make(exchange);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            Logger.getLogger(CoreHandler.class.getName()).log(Level.SEVERE, null, ex);
            exchange.close();
        }
    }
    
}
