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

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Responsável por bloquear o acesso a arquivos que estejam sendo lidos ou gravados.
 * @author jhones
 */
public class BlockingIdList {
    private static final CopyOnWriteArrayList<String> ids = new CopyOnWriteArrayList<>();
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition idAvailable = lock.newCondition();
    
    public void add(String id) throws InterruptedException {
        addAll(new String[]{id});
    }
    
    /**
     * Adiciona uma lista de IDs à lista de bloqueados, esperando se algum ID já estiver bloqueado.
     * Os IDs são ordenados para evitar deadlocks.
     * @param idList A lista de IDs a serem bloqueados
     * @throws InterruptedException Se a thread for interrompida enquanto espera
     */
    public void addAll(String[] idList) throws InterruptedException {
        // Ordena os IDs para garantir aquisição consistente
        String[] sortedIds = Arrays.copyOf(idList, idList.length);
        Arrays.sort(sortedIds);

        lock.lock();
        try {
            // Verifica se algum ID está bloqueado; se sim, espera
            while (isAnyIdLocked(sortedIds)) {
                idAvailable.await();
            }
            // Adiciona todos os IDs à lista
            for (String id : sortedIds) {
                ids.add(id);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove uma lista de IDs da lista de bloqueados e sinaliza threads esperando.
     * @param idList A lista de IDs a serem removidos
     * @return true se todos os IDs foram removidos, false se algum não estava na lista
     */
    public boolean removeAll(String[] idList) {
        lock.lock();
        try {
            boolean allRemoved = true;
            for (String id : idList) {
                if (!ids.remove(id)) {
                    allRemoved = false;
                }
            }
            if (allRemoved) {
                // Sinaliza todas as threads esperando
                idAvailable.signalAll();
            }
            return allRemoved;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Verifica se algum ID da lista está bloqueado.
     * @param idList A lista de IDs a verificar
     * @return true se algum ID está na lista de bloqueados, false caso contrário
     */
    private boolean isAnyIdLocked(String[] idList) {
        for (String id : idList) {
            if (ids.contains(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se um ID está na lista.
     * @param id O ID a ser verificado
     * @return true se o ID está na lista, false caso contrário
     */
    public boolean contains(String id) {
        return ids.contains(id);
    }

    /**
     * Retorna o tamanho atual da lista.
     * @return O número de IDs na lista
     */
    public int size() {
        return ids.size();
    }
}