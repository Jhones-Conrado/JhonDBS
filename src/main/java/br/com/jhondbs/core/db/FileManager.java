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
package br.com.jhondbs.core.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Gerencia os bloqueios de leitura e escrita dos arquivos para garantir thread
 * safe e consistência do banco de dados.
 * @author jhones
 */
public class FileManager {
    
    // Singleton instance
    private static final FileManager INSTANCE = new FileManager();
    
    // Mapa para armazenar bloqueios de arquivos
    private final ConcurrentHashMap<String, FileLockInfo> fileLocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanerService = Executors.newScheduledThreadPool(1);

    // Classe para armazenar o bloqueio e o contador de uso
    private static class FileLockInfo {
        final ReadWriteLock lock;
        long lastUsed; // Timestamp da última vez que o bloqueio foi usado

        FileLockInfo(ReadWriteLock lock) {
            this.lock = lock;
            this.lastUsed = System.currentTimeMillis();
        }
    }

    // Private constructor to prevent instantiation
    private FileManager() {
        // Agendar a tarefa de limpeza para remover bloqueios ociosos a cada 5 minutos
        cleanerService.scheduleAtFixedRate(this::cleanUpLocks, 5, 5, TimeUnit.MINUTES);
    }

    // Método para obter a instância do Singleton
    public static FileManager getInstance() {
        return INSTANCE;
    }

    // Obtém ou cria um bloqueio para o arquivo específico
    private ReadWriteLock getLockForFile(String fileName) {
        FileLockInfo lockInfo = fileLocks.computeIfAbsent(fileName, k -> new FileLockInfo(new ReentrantReadWriteLock()));
        lockInfo.lastUsed = System.currentTimeMillis(); // Atualiza o tempo de uso
        return lockInfo.lock;
    }

    // Método para ler um arquivo
    public String readFile(String fileName) throws IOException {
        ReadWriteLock lock = getLockForFile(fileName);
        lock.readLock().lock();
        try {
            Path filePath = Paths.get(fileName);
            return Files.readString(filePath);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Método para gravar em um arquivo
    public void writeFile(String fileName, String content) throws IOException {
        ReadWriteLock lock = getLockForFile(fileName);
        lock.writeLock().lock();
        try {
            Path filePath = Paths.get(fileName);
            Files.writeString(filePath, content);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Método para gravar em múltiplos arquivos
    public void writeMultipleFiles(List<String> fileNames, List<String> contents) throws IOException {
        fileNames.sort(String::compareTo);
        for (String fileName : fileNames) {
            getLockForFile(fileName).writeLock().lock();
        }

        try {
            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = fileNames.get(i);
                String content = contents.get(i);
                Path filePath = Paths.get(fileName);
                Files.writeString(filePath, content);
            }
        } finally {
            for (String fileName : fileNames) {
                getLockForFile(fileName).writeLock().unlock();
            }
        }
    }
    
    /**
     * Recebe uma lista com os ids das entidades que precisam ser trancadas pois
     * irão ser lidas.
     * @param ids 
     */
    public void lockRead(List<String> ids) {
//        ids.sort(String::compareTo);
        for (String id : ids) {
            getLockForFile(id).readLock().lock();
        }
    }
    
    /**
     * Recebe uma lista com os ids das entidades que precisam ser liberadas da leitura.
     * @param ids 
     */
    public void unlockRead(List<String> ids) {
//        ids.sort(String::compareTo);
        for (String id : ids) {
            getLockForFile(id).readLock().unlock();
        }
    }
    
    /**
     * Recebe uma lista com os ids das entidades que precisam ser trancadas para
     * gravação.
     * @param ids 
     */
    public void lockWrite(List<String> ids) {
//        ids.sort(String::compareTo);
        for (String id : ids) {
            getLockForFile(id).writeLock().lock();
        }
    }
    
    /**
     * Recebe uma lista com os ids das entidades que precisam ser liberadas da gravação.
     * @param ids 
     */
    public void unlockWrite(List<String> ids) {
//        ids.sort(String::compareTo);
        for (String id : ids) {
            getLockForFile(id).writeLock().unlock();
        }
    }

    // Método para limpar bloqueios ociosos
    private void cleanUpLocks() {
        long currentTime = System.currentTimeMillis();
        fileLocks.forEach((fileName, lockInfo) -> {
            // Verifica se o bloqueio não foi usado nos últimos 10 minutos
            if (currentTime - lockInfo.lastUsed > TimeUnit.MINUTES.toMillis(10)) {
                // Tenta remover o bloqueio se ninguém estiver usando
                fileLocks.remove(fileName, lockInfo);
            }
        });
    }
    
}
