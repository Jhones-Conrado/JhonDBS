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

import br.com.jhondbs.core.db.capsule2.EnteMemory;
import br.com.jhondbs.core.db.capsule2.EnteSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por gerir as sessões de entidades ativas no banco de dados.
 * É recomendável que toda entidade seja buscada por aqui.
 * @author jhones
 */
public class SessionsCache {
    
    private static SessionsCache instance = null;
    
    private final List<EnteSession> sessions;

    private SessionsCache() {
        sessions = new ArrayList<>();
    }
    
    private synchronized EnteSession addSession(EnteSession session) {
        if(this.sessions.contains(session)) return session;
        
        /*
        Verifica se alguma sessão já existente possui qualquer um dos IDs da
        sessão que está sendo adicionada. Caso exista, as sessões serão unificadas.
        O conteúdo da sessão que está sendo adicionada será transferido para a
        sessão mais antiga.
         */
        EnteSession orElse = this.sessions.stream()
                .filter(e -> e.containsAny(session.getIds()))
                .findFirst()
                .orElse(null);
        
        // Nenhuma sessão antiga possui os IDs que serão adicionados.
        if(orElse == null) {
            this.sessions.add(session);
            session.touch();
            return session;
        } else {
            // A sessão nova precisa ser inserida na sessão antiga.
            orElse.receive(session);
            return orElse;
        }
    }
    
    /**
     * Remove uma sessão.
     * @param session Sessão a ser removida.
     */
    private synchronized void removeSession(EnteSession session) {
        this.sessions.remove(session);
    }
    
    /**
     * Busca o ID em todas as sessões ativas.
     * @param id ID a ser procurado.
     * @return EnteMemory encontrado ou nulo.
     */
    private EnteMemory findById(String id) {
        /*
        Estou pensando em caso a entidade não seja encontrada em memória, já
        inicializar automaticamente o processo de busca no banco de dados e se
        encontrar, inicializar o processo de carregamento. Unificante os métodos
        de busca em cache com o de carregamento do banco de dados.
        Transformar os meódos "find" e "load" em apenas "find".
        */
        
        EnteSession session = findSessionById(id);
        if(session == null) return null;
        return session.getById(id);
    }
    
    /**
     * Procura alguma sessão que contenha o ID específico.
     * @param id ID a ser procurado.
     * @return Sessão que contém o ID ou nulo.
     */
    private EnteSession findSessionById(String id) {
        return findSessionByIds(new String[]{id});
    }
    
    /**
     * Procura a existência de alguma sessão que possua qualquer um dos IDs citados.
     * @param ids Array de ids a serem buscados.
     * @return Sessão encontrada ou nulo.
     */
    private synchronized EnteSession findSessionByIds(String[] ids) {
        return this.sessions.stream()
                .filter(s -> s.containsAny(ids))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Retorna a instância única de SessionsCache.
     * @return Instância única de SessionsCache.
     */
    private static SessionsCache get() {
        if(instance == null) {
            instance = new SessionsCache();
        }
        return instance;
    }
    
    /**
     * Adiciona uma nova sessão ao cache em memória.
     * @param session Sessão a ser adicionada.
     * @return 
     */
    public static EnteSession add(EnteSession session) {
        return get().addSession(session);
    }
    
    /**
     * Remove uma sessão.
     * @param session Sessão a ser removida.
     */
    public static void remove(EnteSession session) {
        get().removeSession(session);
    }
    
    /**
     * Busca o ID em todas as sessões ativas.
     * @param id ID a ser procurado.
     * @return EnteMemory encontrado ou nulo.
     */
    public static EnteMemory find(String id) {
        return get().findById(id);
    }
    
    /**
     * Procura alguma sessão que contenha o ID específico.
     * @param id ID a ser procurado.
     * @return Sessão que contém o ID ou nulo.
     */
    public static EnteSession findSession(String id) {
        return get().findSessionById(id);
    }
    
    /**
     * Procura a existência de alguma sessão que possua qualquer um dos IDs citados.
     * @param ids Array de ids a serem buscados.
     * @return Sessão encontrada ou nulo.
     */
    public static EnteSession findSession(String[] ids) {
        return get().findSessionByIds(ids);
    }
    
}
