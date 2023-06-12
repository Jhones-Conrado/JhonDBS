/*
 * Copyright (C) 2023 jhonessales
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
