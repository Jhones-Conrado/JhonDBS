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

import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Usado específicamente para gravar o arquivo properties de uma entidade no
 * banco de dados.
 * Sempre irá gravar na pasta temporária. Um arquivo NUNCA deve ser gravado
 * diretamente na pasta de produção.
 * @author jhones
 */
public class Writer {
    
    public Writer() {
    }
    
    public static boolean write(Bottle bottle) throws IOException, IllegalAccessException, EntityIdBadImplementationException {
        String path = Assist.getTempPath(new Ref(bottle.entity), bottle.TEMP_DB);
        File file = new File(path);
        file.getParentFile().mkdirs(); // Garantir diretórios
        Properties build = bottle.build();
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            build.store(bos, "JhonDBS Entity");
        }
        return true;
    }
    
}
