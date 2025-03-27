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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhones
 */
public class IO {
    private static IO io;
    private ConcurrentHashMap<String, LockEntry> lockMap = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(IO.class.getName());

    private static class LockEntry {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        AtomicInteger refCount = new AtomicInteger(0);
    }

    private IO() {}

    public void lockRead(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        LockEntry entry = lockMap.computeIfAbsent(id, k -> new LockEntry());
        entry.refCount.incrementAndGet();
        LOGGER.log(Level.FINE, "Acquiring read lock for ID: {0}", id);
        entry.lock.readLock().lock();
    }

    public void unlockRead(String id) {
        LockEntry entry = lockMap.get(id);
        if (entry == null) {
            LOGGER.log(Level.WARNING, "Tentativa de desbloquear leitura sem bloqueio existente para ID: {0}", id);
            throw new IllegalStateException("Nenhum bloqueio de leitura encontrado para o ID: " + id);
        }
        entry.lock.readLock().unlock();
        LOGGER.log(Level.FINE, "Released read lock for ID: {0}", id);
        cleanUpLock(id, entry);
    }

    public void lockWrite(String id) {
        LockEntry entry = lockMap.computeIfAbsent(id, k -> new LockEntry());
        entry.lock.writeLock().lock();
        entry.refCount.incrementAndGet();
    }

    public void unlockWrite(String id) {
        LockEntry entry = lockMap.get(id);
        if (entry == null) {
            LOGGER.log(Level.WARNING, "Tentativa de desbloquear escrita sem bloqueio existente para ID: {0}", id);
            throw new IllegalStateException("Nenhum bloqueio de escrita encontrado para o ID: " + id);
        }
        entry.lock.writeLock().unlock();
        cleanUpLock(id, entry);
    }

    private void cleanUpLock(String id, LockEntry entry) {
        if (entry.refCount.decrementAndGet() == 0) {
            lockMap.remove(id);
        }
    }
    
    public boolean tryLockRead(String id, long timeout, TimeUnit unit) throws InterruptedException {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        LockEntry entry = lockMap.computeIfAbsent(id, k -> new LockEntry());
        entry.refCount.incrementAndGet();
        return entry.lock.readLock().tryLock(timeout, unit);
    }
    
    public boolean tryLockWrite(String id, long timeout, TimeUnit unit) throws InterruptedException {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        LockEntry entry = lockMap.computeIfAbsent(id, k -> new LockEntry());
        if (entry.lock.writeLock().tryLock(timeout, unit)) {
            entry.refCount.incrementAndGet();
            return true;
        }
        return false;
    }

    public static IO io() {
        if (io == null) {
            synchronized (IO.class) {
                if (io == null) {
                    io = new IO();
                }
            }
        }
        return io;
    }
}
