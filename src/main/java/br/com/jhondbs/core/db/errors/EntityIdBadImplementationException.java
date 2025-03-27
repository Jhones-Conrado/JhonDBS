/*
 * The MIT License
 *
 * Copyright 2024 Jhones Sales.
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
package br.com.jhondbs.core.db.errors;

/**
 * ENGLISH<br>
 * Error thrown if an object that implements Entity does not have an adequate
 * implementation of the getId and setId methods, where the value returned for
 * the ID variable will always be -1l.<br><br>
 * PORTUGUÊS<br>
 * Erro lançado caso um objeto que implemente Entidade não possua uma
 * implementação adequada dos métodos getId e setId, onde o valor retornado para
 * a variável ID será sempre -1l.
 * @author jhonesconrado
 */
public class EntityIdBadImplementationException extends Exception {

    /**
     * Creates a new instance of <code>EntIdBadImplementation</code> without
     * detail message.
     */
    public EntityIdBadImplementationException() {
    }

    /**
     * Constructs an instance of <code>EntIdBadImplementation</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public EntityIdBadImplementationException(String msg) {
        super(msg);
    }
}
