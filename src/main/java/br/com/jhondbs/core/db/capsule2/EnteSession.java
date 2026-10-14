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
package br.com.jhondbs.core.db.capsule2;

import br.com.jhondbs.core.tools.SessionsCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Preserva uma sessão de entidades relacionadas, se nenhuma das entidades da
 * sessão for acessada durante o tempo de vida médio da sessão, então as entidades
 * serão removidas do cache de memória e a sessão será encerrada.
 * @author jhones
 */
public class EnteSession {
    
    /**
     * Método estático para buscar o memory de uma entidade caso esta já tenha
     * sido carregada.
     * @param id
     * @return 
     */
    public static EnteMemory find(String id) {
        return SessionsCache.find(id);
    }
    
    
    /**
     * Tempo de vida médio de uma sessão em minutos.
     */
    private long lifeTime = TimeUnit.MINUTES.toMillis(20);
    
    /**
     * Última vez que a sessão foi acessada
     */
    private long lastTouch = System.currentTimeMillis();
    
    private List<String> ids = new ArrayList<>();
    private Map<String, EnteMemory> entes = new HashMap();

    public EnteSession() {
    }
    
    public void addEntity(EnteMemory entity) throws Exception {
        String id = entity.getId();
        if(!ids.contains(id)) {
            ids.add(id);
            entes.put(id, entity);
        }
        touch();
    }
    
    /**
     * Retorna um EnteMemory caso o ID da entidade esteja presente nesta sessão.
     * @param id ID a ser buscado.
     * @return EnteMemory buscado ou nulo.
     */
    public EnteMemory getById(String id) {
        if (id == null || id.isBlank()) return null;
        if(!entes.containsKey(id)) return null;
        return entes.get(id);
    }
    
    /**
     * Recebe os EnteMemory de outra sessão ao mesmo tempo em que limpa a sessão
     * recebida. Evitando que duas sessões existem com os mesmos EnteMemory.
     * @param from Sessão que será mesclada com esta e limpa.
     */
    public synchronized void receive(EnteSession from) {
        for(String id : from.entes.keySet()) {
            if(!this.entes.containsKey(id)) {
                this.entes.put(id, from.entes.get(id));
                this.ids.add(id);
            }
        }
        from.entes.clear();
        touch();
    }
    
    /**
     * Retorna um array String[] com todos os IDs presentes na sessão.
     * @return 
     */
    public String[] getIds() {
        return this.ids.toArray(String[]::new);
    }
    
    /**
     * Verifica se essa sessão contém qualquer um dos IDs passados no array.
     * @param ids Array de IDs a serem buscados.
     * @return Verdadeiro caso qualquer ID seja encontrado.
     */
    public boolean containsAny(String[] ids) {
        return Arrays.asList(ids).stream().anyMatch(id -> this.ids.contains(id));
    }
    
    /**
     * Renova o tempo de vída da sessão.
     */
    public void touch() {
        lastTouch = System.currentTimeMillis() + lifeTime;
    }
    
    /**
     * Verífica se a sessão está sem ser acessada a mais tempo do que a média de
     * vida.
     * @return Verdadeiro se estiver sem acesso a mais tempo que o lifeTime.
     */
    public boolean isUnused() {
        return System.currentTimeMillis() > lifeTime;
    }
    
    /**
     * Limpa a sessão e a remove do cache.
     */
    public void kill() {
        this.entes.clear();
        this.ids.clear();
        this.lastTouch -= this.lifeTime;
        SessionsCache.remove(this);
    }
    
}
