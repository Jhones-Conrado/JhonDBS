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
package br.com.jhondbs.core.db.session;

import br.com.jhondbs.core.db.capsule.Bottle;
import br.com.jhondbs.core.db.interfaces.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jhones
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private List<SessionCache> sessions = new ArrayList();
    
    private SessionManager(){
        new Thread(new ExpiredCleaner()).start();
    }
    
    private static SessionManager ins() {
        if(instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    private static void merge(Bottle bottle) throws Exception {
        SessionCache mainSession;
        List<SessionCache> sessions = new ArrayList<>();
    
        mainSession = contains(bottle.entity.getId());
        if(mainSession == null) {
            mainSession = new SessionCache();
            mainSession.addEntity(bottle);
        }
        
        for(Bottle b : bottle.bottles.values()) {
            if(b != bottle) {
                SessionCache contains = contains(b.entity.getId());
                if(contains == null) {
                    mainSession.addEntity(b);
                } else if(contains != mainSession) {
                    mainSession.absorb(contains);
                }
            }
        }
        
    }
    
    public static SessionCache add(Bottle entity) throws Exception {
        SessionCache contains = contains(entity.entity.getId());
        if(contains != null) return contains;
        
        SessionCache session = new SessionCache();
        session.addEntity(entity);
        ins().sessions.add(session);
        merge(entity);
        return session;
    }
    
    public static SessionCache remove(String id) {
        SessionCache contains = contains(id);
        if(contains != null) {
            contains.removeEntity(id);
        }
        return contains;
    }
    
    public static SessionCache remove(Entity entity) throws Exception {
        return remove(entity.getId());
    }
    
    public static SessionCache contains(String id) {
        for(SessionCache session : ins().sessions) {
            if(session.contains(id)) return session;
        }
        return null;
    }
    
    private class ExpiredCleaner implements Runnable {

        @Override
        public void run() {
            List<SessionCache> expireds = new ArrayList<>();
            while(true) {
                for(SessionCache session : sessions) {
                    if(session.isExpired()) expireds.add(session);
                }
                
                if(!expireds.isEmpty()) {
                    for(SessionCache session : expireds) {
                        session.clear();
                        sessions.remove(session);
                    }
                    expireds.clear();
                }
                
                Thread.yield();
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(5));
                } catch (InterruptedException ex) {
                }
            }
        }
        
    }
    
}
