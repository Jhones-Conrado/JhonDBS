/*
 * The MIT License
 *
 * Copyright 2026 jhones.
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

import br.com.jhondbs.core.db.interfaces.Entity;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Assistente para ler e gravar arquivos de entidades no banco de dados.
 * @author jhones
 */
public class IOAssist {

    /**
     * Busca um arquivo properties da entidade no banco de dados.
     * @param entity Entidade a ser buscada
     * @return Properties da entidade ou nulo.
     * @throws Exception 
     */
    public static Properties entityToProperties(Entity entity) throws Exception {
        if(entity == null) return null;
        File file = new File(DirectoryManager.entityToPath(entity));
        if(!file.exists()) return null; // Não existe arquivo anterior, nada a fazer.
        
        Properties p = new Properties();
        p.load(new FileInputStream(file));
        return p;
    }
    
}
