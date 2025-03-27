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

import br.com.jhondbs.core.db.errors.EntityIdBadImplementationException;
import br.com.jhondbs.core.db.errors.ObjectNotDesserializebleException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface funcional para ações transacionais que podem lançar exceções.
 */
@FunctionalInterface
interface TransactionalAction {
    void execute() throws Exception;
}

/**
 * Classe responsável por gerenciar transações atômicas no banco de dados JhonDBS.
 * Suporta ações que lançam exceções, garantindo bloqueio, backup e rollback.
 *
 * @author jhones
 */
public class Transaction {
    private static final Logger LOGGER = Logger.getLogger(Transaction.class.getName());
    private static final String BACKUP_SUFFIX = ".bak";

    private final String transactionId;
    private final Set<String> lockedIds;
    private final Set<Bottle> bottles;
    private final String rootDb;
    private boolean committed = false;
    private boolean rolledBack = false;
    private Bottle rootBottle;
    private Bottle oldState;

    private static final ConcurrentHashMap<String, Transaction> ACTIVE_TRANSACTIONS = new ConcurrentHashMap<>();

    private Transaction(String rootDb) {
        this.transactionId = UUID.randomUUID().toString();
        this.lockedIds = new HashSet<>();
        this.bottles = new HashSet<>();
        this.rootDb = rootDb.endsWith("/") ? rootDb : rootDb + "/";
        ACTIVE_TRANSACTIONS.put(transactionId, this);
    }

    public static Transaction begin(String rootDb) {
        Transaction tx = new Transaction(rootDb);
        LOGGER.log(Level.INFO, "Transaction started: {0}", tx.transactionId);
        return tx;
    }

    public Transaction add(Bottle bottle) throws IllegalStateException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, URISyntaxException, IOException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if (committed || rolledBack) {
            throw new IllegalStateException("Transaction already completed: " + transactionId);
        }
        if (bottle.entity.getId() == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }
        if(this.rootBottle == null) {
            this.rootBottle = bottle;
        }
        
        for(Bottle b : bottle.bottles.values()) {
            if(!bottles.contains(b)) {
                bottles.add(b);
            }
            if(!lockedIds.contains(b.entity.getId())) {
                lockedIds.add(b.entity.getId());
            }
        }
        
        oldState = rootBottle.loadOldState();
        if(oldState != null) {
            for(Bottle b : oldState.bottles.values()) {
                if(!lockedIds.contains(b.entity.getId())) {
                    lockedIds.add(b.entity.getId());
                }
            }
        }
        return this;
    }
    
    public void commitDel() throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, InterruptedException, URISyntaxException, ParseException, ObjectNotDesserializebleException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if (committed || rolledBack) {
            throw new IllegalStateException("Transaction already completed: " + transactionId);
        }
        try {
            // Tranca entidades
            lockEntities();
            
            // Cria backup do estado atual
            backupCurrentState();
            rootBottle.delete(true);
            
            committed = true;
            LOGGER.log(Level.INFO, "Transaction committed: {0}", transactionId);
            System.out.println("");
        } catch (EntityIdBadImplementationException | IOException | IllegalAccessException | IllegalArgumentException | InterruptedException e) {
            rollback();
            throw e;
        } finally {
            /*
            Deve chamar os métodos de limpeza que eliminarão os rezíduos após o
            flush ou rollback.
            A destranca das entidades também é realizada aqui.
            */

            unlockEntities();
            cleanUp();
            try {
                cleanBakDel();
                cleanOldBottles();
            } catch (Exception ex) {
                Logger.getLogger(Transaction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void commit() throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, InterruptedException, Exception {
        if (committed || rolledBack) {
            throw new IllegalStateException("Transaction already completed: " + transactionId);
        }
        try {
            // Tranca entidades
            lockEntities();
            
            // Cria backup do estado atual
            backupCurrentState();
            
            /*
            Verifica se existe um estado anterior, caso tenha, verifica se alguma
            entidade deixou de ser referenciada pelo estado atual.
            As entidades que deixaram de ser referenciadas serão limpas em um
            processo que remove qualquer referência que ela possa ter das entidades
            presentes atualmente.
            */
            if (oldState != null) {
                List<Ref> removeds = Assist.removedsBetweenStates(oldState, rootBottle);
                for (Ref ref : removeds) {
                    Assist.removeExistenceFromBottle(rootBottle, ref);
                }
                
                for(Bottle bottle : oldState.bottles.values()) {
                    rootBottle.writer.write(bottle);
                }
            }
            
            // Grava as garrafas, os arquivos e as imagens.
            for(Bottle bottle : bottles) {
                rootBottle.writer.write(bottle);
                bottle.flushFiles();
                bottle.flushImgs();
            }
            
            /*
            Delete todas as garrafas marcadas para exclusão, deixando na pasta
            TEMP apenas os arquivos que deverão ser movidos para a pasta de produção.
            */
            rootBottle.deleteFilesWithDelete();
            
            // Move todo o conteúdo da pasta TEMP para a pasta de produção.
            rootBottle.moveDirectory();
            
            /*
            Verifica quais arquivos e imagens permanecem e quais serão deletados.
            Esta é a última etapa crítica da operação de gravação no banco de dados,
            se esta operação for concluída com sucesso, a atomicidade funcionou.
            */
            for(Bottle bottle : bottles) {
                Set<String> arquivos = bottle.files.keySet();
                File fileFolder = new File(bottle.ROOT_DB+"files/"+bottle.entity.getId());
                if(fileFolder.exists()) {
                    Set<String> prodArquivos = Set.copyOf(Arrays.asList(fileFolder.list()));
                    /*
                    Verifica se os arquivos necessários estão na pasta de produção.
                    Utilizar diretamente o arquivo de backup amezina o uso do disco
                    ao evitar cópias do arquivo na pasta temporária.
                    Ou seja, se o arquivo não está com o nome normal mas existe um
                    com o mesmo nome utilizando o .bak, significa que o arquivo não
                    foi alterado, logo pode ser usado o mesmo arquivo de backup.
                    */
                    for(String arquivo : arquivos) {
                        if(!prodArquivos.contains(arquivo)) {
                            if(prodArquivos.contains(arquivo+".bak")) {
                                File bak = new File(fileFolder.getPath()+arquivo+".bak");
                                File prod = new File(fileFolder.getPath()+arquivo);
                                if(!bak.renameTo(prod)) {
                                    throw new Exception("Erro ao renomear arquivo para produção");
                                }
                            } else {
                                throw new Exception("O arquivo não foi salvo na pasta de produção.");
                            }
                        }
                    }
                } else if (!arquivos.isEmpty()) {
                    throw new Exception("Existem arquivos a serem salvos mas o diretório não foi criado.");
                }
                
                Set<String> imagens = bottle.imgs.keySet();
                File imagesFolder = new File(bottle.ROOT_DB+"imgs/"+bottle.entity.getId());
                if(imagesFolder.exists()) {
                    Set<String> prodImagens = Set.copyOf(Arrays.asList(imagesFolder.list()));
                    /*
                    Processo similar ao de cima, mas para imagens
                    */
                    for(String imagem : imagens) {
                        if(!prodImagens.contains(imagem)) {
                            if(prodImagens.contains(imagem+".bak")) {
                                File bak = new File(imagesFolder.getPath()+imagem+".bak");
                                File prod = new File(imagesFolder.getPath()+imagem);
                                if(!bak.renameTo(prod)) {
                                    throw new Exception("Erro ao renomear a imagem para produção");
                                }
                            } else {
                                throw new Exception("A imagem não foi salva na pasta de produção.");
                            }
                        }
                    }
                } else if (!imagens.isEmpty()) {
                    throw new Exception("Existem imagens a serem salvas mas o diretório não foi criado.");
                }
            }
            
            /*
            Nesse ponto do código todas as operações críticas foram realizadas com sucesso
            restando apenas a limpeza dos diretórios e arquivos antigos.
            A partir daqui qualquer erro não é mais crítico ao funcionamento e integridade
            do banco de dados pois se tratam apenas de arquivos .bak ou .del
            */
            
            committed = true;
            LOGGER.log(Level.INFO, "Transaction committed: {0}", transactionId);
            System.out.println("");
            
        } catch (Exception e) {
            rollback();
            throw e;
        } finally {
            /*
            Deve chamar os métodos de limpeza que eliminarão os rezíduos após o
            flush ou rollback.
            A destranca das entidades também é realizada aqui.
            */

            unlockEntities();
            cleanUp();
            try {
                cleanBakDel();
                cleanOldBottles();
            } catch (Exception ex) {
                Logger.getLogger(Transaction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void cleanBakDel() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, Exception {
        for(Bottle bottle : bottles) {
            File fileFolder = new File(bottle.ROOT_DB+"files/"+bottle.entity.getId());
            File imagesFolder = new File(bottle.ROOT_DB+"imgs/"+bottle.entity.getId());

            if(fileFolder.exists()) {
                for(File file : fileFolder.listFiles()) {
                    if(file.getName().endsWith(".del") || file.getName().endsWith(".bak")) {
                        file.delete();
                    }
                }
                if(fileFolder.listFiles().length == 0) {
                    fileFolder.delete();
                }
            }

            if(imagesFolder.exists()) {
                for(File file : imagesFolder.listFiles()) {
                    if(file.getName().endsWith(".del") || file.getName().endsWith(".bak")) {
                        file.delete();
                    }
                }
                if(imagesFolder.listFiles().length == 0) {
                    imagesFolder.delete();
                }
            }
        }
    }
    
    private void cleanOldBottles() throws IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        for(Bottle bottle : bottles) {
            String path = bottle.getRootPath()+".bak";
            File file = new File(path);
            if(file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Executa a transação de forma atômica, aplicando a ação fornecida.
     *
     * @param action Ação a ser executada dentro da transação.
     * @throws Exception Se a ação ou o commit falhar.
     */
    public void execute(TransactionalAction action) throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException, InterruptedException, Exception {
        if (committed || rolledBack) {
            throw new IllegalStateException("Transaction already completed: " + transactionId);
        }
        lockEntities();
        try {
            backupCurrentState(); // Backup antes de qualquer alteração
            action.execute(); // Executa as operações do Bottle
            committed = true;
            LOGGER.log(Level.INFO, "Transaction committed: {0}", transactionId);
        } catch (Exception e) {
            rollback();
            throw e; // Relança para o chamador tratar
        } finally {
            unlockEntities();
            cleanUp();
        }
    }

    private void lockEntities() throws InterruptedException {
        for (String id : lockedIds) {
            if (!IO.io().tryLockWrite(id, 10, TimeUnit.SECONDS)) {
                unlockEntities(); // Libera bloqueios adquiridos
                throw new IllegalStateException("Failed to acquire write lock for ID: " + id);
            }
        }
    }

    private void unlockEntities() {
        for (String id : lockedIds) {
            IO.io().unlockWrite(id);
        }
    }

    private void backupCurrentState() throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        for (Bottle bottle : bottles) {
            String rootPath = bottle.getRootPath();
            File rootFile = new File(rootPath);
            if (rootFile.exists()) {
                File backupFile = new File(rootPath + BACKUP_SUFFIX);
                Files.move(rootFile.toPath(), backupFile.toPath());
            }
            backupFilesAndImages(bottle);
        }
    }

    private void backupFilesAndImages(Bottle bottle) throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        String entityId = bottle.entity.getId();
        Path imgRoot = Paths.get(rootDb, "imgs", entityId);
        Path fileRoot = Paths.get(rootDb, "files", entityId);

        if (Files.exists(imgRoot)) {
            try (var stream = Files.walk(imgRoot).filter(Files::isRegularFile)) {
                stream.forEach(src -> {
                    try {
                        if(!src.toString().endsWith(BACKUP_SUFFIX)) {
                            Path backup = Paths.get(src.toString() + BACKUP_SUFFIX);
                            Files.move(src, backup);
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to backup image: {0}", src);
                    }
                });
            }
        }
        if (Files.exists(fileRoot)) {
            try (var stream = Files.walk(fileRoot).filter(Files::isRegularFile)) {
                stream.forEach(src -> {
                    try {
                        if(!src.toString().endsWith(BACKUP_SUFFIX)) {
                            Path backup = Paths.get(src.toString() + BACKUP_SUFFIX);
                            Files.move(src, backup);
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to backup file: {0}", src);
                    }
                });
            }
        }
    }

    public void rollback() throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        if (!rolledBack) {
            restoreBackup();
            rolledBack = true;
            LOGGER.log(Level.INFO, "Transaction rolled back: {0}", transactionId);
        }
    }

    private void restoreBackup() throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        for (Bottle bottle : bottles) {
            String rootPath = bottle.getRootPath();
            File backupFile = new File(rootPath + BACKUP_SUFFIX);
            if (backupFile.exists()) {
                Files.move(backupFile.toPath(), Paths.get(rootPath));
            } else {
                new File(rootPath).delete();
            }
            restoreFilesAndImages(bottle);
            bottle.cleanFolders();
        }
    }

    private void restoreFilesAndImages(Bottle bottle) throws IOException, IllegalArgumentException, IllegalAccessException, EntityIdBadImplementationException {
        String entityId = bottle.entity.getId();
        File ente = new File(bottle.getRootPath());
        
        Path imgRoot = Paths.get(rootDb, "imgs", entityId);
        Path fileRoot = Paths.get(rootDb, "files", entityId);

        if (Files.exists(imgRoot)) {
            if(!ente.exists()) {
                Files.walk(imgRoot).forEach(src -> {
                    src.toFile().delete();
                });
                imgRoot.toFile().delete();
            } else {
                Files.walk(imgRoot).filter(p -> p.toString().endsWith(BACKUP_SUFFIX) || p.toString().endsWith(BACKUP_SUFFIX+".del")).forEach(src -> {
                    try {
                        Path original = Paths.get(src.toString().substring(0, src.toString().indexOf(".")));
                        Files.move(src, original);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to restore image: {0}", src);
                    }
                });
            }
            
        }
        
        if (Files.exists(fileRoot)) {
            if(!ente.exists()) {
                Files.walk(fileRoot).forEach(src -> {
                    System.out.println("-> deletando "+src);
                    src.toFile().delete();
                });
                fileRoot.toFile().delete();
            } else {
                Files.walk(fileRoot).filter(p -> p.toString().endsWith(BACKUP_SUFFIX) || p.toString().endsWith(BACKUP_SUFFIX+".del")).forEach(src -> {
                    try {
                        Path original = Paths.get(src.toString().substring(0, src.toString().indexOf(".")));
                        Files.move(src, original);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to restore file: {0}", src);
                    }
                });
            }
        }
    }

    private void cleanUp() {
        ACTIVE_TRANSACTIONS.remove(transactionId);
        for (Bottle bottle : bottles) {
            bottle.cleanFolders();
        }
    }

    public boolean isCommitted() {
        return committed;
    }

    public boolean isRolledBack() {
        return rolledBack;
    }

    public String getTransactionId() {
        return transactionId;
    }
}