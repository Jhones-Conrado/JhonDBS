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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author jhones
 */
public class IO {
    
    private static IO io;
    
    private ConcurrentHashMap<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();
    
    private IO(){}
    
    // Método para adquirir bloqueio de leitura
    public void lockRead(String id) {
        getLock(id).readLock().lock();
    }
    
    // Método para liberar bloqueio de leitura e remover o lock se não for mais necessário
    public void unlockRead(String id) {
        getLock(id).readLock().unlock();
        cleanUpLock(id);
    }

    // Método para adquirir bloqueio de escrita
    public void lockWrite(String id) {
        getLock(id).writeLock().lock();
    }

    // Método para liberar bloqueio de escrita e remover o lock se não for mais necessário
    public void unlockWrite(String id) {
        getLock(id).writeLock().unlock();
        cleanUpLock(id);
    }
    
    // Obtém ou cria o ReadWriteLock associado ao arquivo
    private ReadWriteLock getLock(String id) {
        return lockMap.computeIfAbsent(id, path -> new ReentrantReadWriteLock());
    }
    
    // Verifica se o lock não está mais sendo utilizado e remove do mapa
    private void cleanUpLock(String id) {
        ReadWriteLock lock = getLock(id);
        if (lock.writeLock().tryLock() && lock.readLock().tryLock()) {
            lockMap.remove(id);
        }
    }
    
    public static IO io(){
        if(io == null) {
            io = new IO();
        }
        return io;
    }
    
}
