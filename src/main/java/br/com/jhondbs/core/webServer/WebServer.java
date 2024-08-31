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
