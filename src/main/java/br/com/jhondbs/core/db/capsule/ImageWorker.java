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
package br.com.jhondbs.core.db.capsule;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Mantém funções auxiliares para trabalhar com imagens no banco de dados.
 * @author jhones
 */
public class ImageWorker {
    
    public static byte[] getImageData(Image image) {
        BufferedImage bufferedImage = toBufferedImage(image);
        if (bufferedImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = newImage.createGraphics();
            g.drawImage(bufferedImage, 0, 0, null);
            g.dispose();
            bufferedImage = newImage;
        }
        DataBufferByte buffer = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
        return buffer.getData();
    }
    
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }
    
    public static void flushImage(String id, Image image, String hash, String db) throws Exception {
        File file = new File(db+"imgs/"+id+"/"+hash);
        file.getParentFile().mkdirs();
        ImageIO.write((BufferedImage) image, "png", file);
    }
    
}
