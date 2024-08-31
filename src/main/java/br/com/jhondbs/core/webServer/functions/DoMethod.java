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
package br.com.jhondbs.core.webServer.functions;

import br.com.jhondbs.core.tools.Reflection;
import br.com.jhondbs.core.webServer.annotation.DeleteRequest;
import br.com.jhondbs.core.webServer.annotation.GetRequest;
import br.com.jhondbs.core.webServer.annotation.PatchRequest;
import br.com.jhondbs.core.webServer.annotation.PostRequest;
import br.com.jhondbs.core.webServer.annotation.PutRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 *
 * @author jhonessales
 */
public class DoMethod {
    
    private Object ins;
    private Method method;

    public DoMethod(Object ins, Method method) throws NullPointerException{
        if(ins != null && method != null){
            this.ins = ins;
            this.method = method;
        } else {
            throw new NullPointerException();
        }
    }
    
    public DoMethod(Object ins, String methodName) throws NoSuchMethodException{
        if(ins != null && methodName != null){
            this.ins = ins;
            this.method = Reflection.getMethod(methodName, ins.getClass());
        } else {
            throw new NullPointerException();
        }
    }
    
    public <T> T invoke(Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        return (T) method.invoke(ins, args);
    }
    
    public int numberParameters(){
        return method.getParameterCount();
    }
    
    public Type[] getParametersTypes(){
        return method.getGenericParameterTypes();
    }
    
    public Class<?> getReturnType(){
        return method.getReturnType();
    }

    public Parameter[] getParameters(){
        return method.getParameters();
    }
    
    public String getPath(){
        Annotation[] annotations = method.getAnnotations();
        for(Annotation a : annotations){
            if(Reflection.isInstance(a.annotationType(), GetRequest.class)){
                GetRequest r = (GetRequest) a;
                return r.path();
            } else if(Reflection.isInstance(a.annotationType(), PostRequest.class)){
                PostRequest r = (PostRequest) a;
                return r.path();
            } else if(Reflection.isInstance(a.annotationType(), PutRequest.class)){
                PutRequest r = (PutRequest) a;
                return r.path();
            } else if(Reflection.isInstance(a.annotationType(), PatchRequest.class)){
                PatchRequest r = (PatchRequest) a;
                return r.path();
            } else if(Reflection.isInstance(a.annotationType(), DeleteRequest.class)){
                DeleteRequest r = (DeleteRequest) a;
                return r.path();
            }
        }
        return "";
    }
    
    public boolean hasVariable(String name){
        return getPath().contains("{"+name+"}");
    }
    
    public String getVariable(String name, String path){
        if(hasVariable(name)){
            int init = getPath().indexOf("{"+name+"}");
            if(path.length() >= init){
                boolean continua = path.substring(init).contains("/");
                if(continua){
                    return path.substring(init, path.indexOf("/", init));
                } else {
                    return path.substring(init);
                }
            }
        }
        return null;
    }
    
}
