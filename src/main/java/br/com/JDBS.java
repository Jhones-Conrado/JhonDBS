/*
 * Copyright (C) 2022 jhonessales
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
package br.com;

import br.com.jhondbs.core.webServer.WebServer;
import br.com.jhondbs.core.webServer.functions.DoMethod;
import br.com.jhondbs.core.webServer.handlers.HandlerBottle;
import java.io.IOException;

/**
 *
 * @author jhonessales
 */
public class JDBS {
    
    public JDBS() {
        
    }
    
    public String metodoteste(String arg1){
        return "DEU CERTO -> "+arg1;
    }
    
    public static void main(String[] args) throws IOException, NoSuchMethodException {
        HandlerBottle.add("/", "GET", new DoMethod(new JDBS(), "metodoteste"));
        WebServer server = new WebServer();
    }
    
}
