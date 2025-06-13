/*
 * The MIT License
 *
 * Copyright 2025 jhones.
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
package br.com.jhondbs.core.db.obj;

import br.com.jhondbs.core.db.Mapper;
import br.com.jhondbs.core.db.interfaces.Cascate;
import br.com.jhondbs.core.db.interfaces.Cold;
import br.com.jhondbs.core.db.interfaces.Entity;
import java.awt.Image;

/**
 *
 * @author jhones
 */
public class ColdImage {
    
    @Cold
    @Cascate
    private EnteImage ente;

    public ColdImage() {
    }
    
    public ColdImage(Image image) throws Exception {
        EnteImage e = new EnteImage(image);
        this.ente = e;
    }
    
    public Image image() throws Exception {
        ente = (EnteImage) Mapper.loadCold(this, "ente");
        return ente.image;
    }
    
    private class EnteImage implements Entity {
    
        private String id;
        public Image image;

        public EnteImage() {
        }

        public EnteImage(Image image) {
            this.image = image;
        }

    }
    
}
