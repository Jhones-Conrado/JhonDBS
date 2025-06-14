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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe responsável por realizar a chamada ordenada do processo de salvar entidades,
 * criando backups, gravando e em caso de erro, retornando ao estado anterior.
 * @author jhones
 */
public class Transaction {
    /**
     * Mapa de transações ativas.
     */
    public static final ConcurrentHashMap<String, Transaction> ACTIVE_TRANSACTIONS = new ConcurrentHashMap<>();

    /**
     * Lista de IDS que estão bloqueados para operações;
     */
    private final BlockingIdList blockeds = new BlockingIdList();
    
    private static final Logger LOGGER = Logger.getLogger(Transaction.class.getName());
    public static final String BACKUP_SUFFIX = ".bak";

    private final String transactionId;
    private boolean committed = false;
    private boolean rolledBack = false;
    private Bottle newState;
    private Bottle oldState;
    
    private List<String> ids = new ArrayList<>();
    
    public Transaction(Bottle bottle) throws Exception {
        this.transactionId = UUID.randomUUID().toString();
        this.newState = bottle;
    }
    
    private void begin() {
        ACTIVE_TRANSACTIONS.put(transactionId, this);
        LOGGER.log(Level.INFO, "Transaction started: {0}", this.transactionId);
    }
    
    public boolean commit() throws Exception {
        if(committed) return true;
        begin();
        try {
            loadOldState();
            newState.engarrafar();
            fillLock();
            Backuper.doBackup(newState, oldState);
            Flusher.flush(newState, oldState);
            Applier.turnEntitiesOn(newState, oldState);
            return true;
        } catch (Exception e) {
            Backuper.rollBack(newState, oldState);
            rolledBack = true;
            throw e;
        } finally {
            committed = true;
            freeLock();
            Cleaner.cleanDB(newState, oldState);
            ACTIVE_TRANSACTIONS.remove(transactionId);
            LOGGER.log(Level.INFO, "Transaction committed: {0}", transactionId);
        }
    }
    
    private void loadOldState() throws Exception {
        oldState = new Bottle.BottleBuilder().emptyBuild();
        for(Bottle bottle : newState.bottles.values()) {
            String rootPath = Assist.getRootPath(new Ref(bottle.entity));
            File file = new File(rootPath);
            if(file.exists()) {
                new Bottle.BottleBuilder()
                        .bottles(oldState.bottles)
                        .entity(this.newState.entity)
                        .build();
            }
        }
    }
    
    private void fillLock() throws Exception {
        ids.addAll(listIds());
        blockeds.addAll((String[]) ids.toArray(new String[0]));
    }
    
    private void freeLock() throws Exception {
        blockeds.removeAll((String[]) ids.toArray(new String[0]));
    }
    
    private Set<String> listIds() throws Exception {
        Set<String> ids = new HashSet();
        ids.addAll(newState.bottles.keySet());
        if(oldState != null) {
            for(Bottle bottle : oldState.bottles.values()) {
                if(!ids.contains(bottle.entity.getId())) {
                    ids.add(bottle.entity.getId());
                }
            }
        }
        for(Bottle bottle : newState.bottles.values()) {
            String rPath = Assist.getRootPath(new Ref(bottle.entity));
            File file = new File(rPath);
            if(file.exists()) {
                Bottle sub = new Bottle.BottleBuilder()
                        .entityClass(bottle.entity.getClass())
                        .id(bottle.entity.getId())
                        .build();
                for(Bottle sub2 : sub.bottles.values()) {
                    String sub2_id = sub2.entity.getId();
                    if(!ids.contains(sub2_id)) {
                        ids.add(sub2_id);
                    }
                }
            }
        }
        return ids;
    }

}
