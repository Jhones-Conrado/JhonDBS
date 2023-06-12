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
package br.com.jhondbs.core.webServer;

import br.com.jhondbs.core.webServer.handlers.CoreHandler;
import br.com.jhondbs.core.Starter;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author jhonessales
 */
public class WebServer {
    
    HttpServer server;

    public WebServer() throws IOException {
        Starter.printName();
        System.out.println("Initializing webserver at port: 8080");
        init(8080);
        System.out.println("WebServer started at port: 8080");
    }
    
    public WebServer(int port) throws IOException{
        Starter.printName();
        System.out.println("Initializing webserver at port: "+port);
        init(port);
        System.out.println("WebServer started at port: "+port);
    }
    
    private void init(int port) throws IOException{
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", new CoreHandler());
        this.server.start();
    }
    
}
