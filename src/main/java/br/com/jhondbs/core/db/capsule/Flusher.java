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

import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por limpar as referências não mais utilizadas em entidades que
 * não estejam mais sendo referênciadas e em seguida grava os novos arquivos.
 * @author jhones
 */
public class Flusher {
    
    /*
    Passos:
    - Limpar as referências dos oldState removidos.
    - 
    */
    public static void flush(Bottle newState, Bottle oldState) throws Exception {
        cleanOldReferences(newState, oldState);
        // Escreve todos os novos arquivos de entidade.
        for(Bottle bottle : newState.bottles.values()) {
            Writer.write(bottle);
        }
    }
    
    /**
     * Limpa as referencias não mais utilizadas das entidades do estado antigo.
     * Busca quais entidade não estão mais sendo referenciadas no estado novo
     * e então limpa destas entidades antigas os campos de referência.
     * Também verifica se a entidade é cascate e orfã; se for, chama a exclusão
     * em cascata.
     * @param newState
     * @param oldState
     * @throws Exception 
     */
    private static void cleanOldReferences(Bottle newState, Bottle oldState) throws Exception {
        List<Ref> removeds = Assist.removedBetweenStates(newState, oldState);
        List<Ref> toRemove = new ArrayList<>();
        for(Bottle bottle : newState.bottles.values()) {
            toRemove.add(new Ref(bottle.entity));
        }
        for(Ref toBeCleaned : removeds) {
            Assist.removeFromReference(toRemove, toBeCleaned, newState.TEMP_DB);
        }
    }
    
}
