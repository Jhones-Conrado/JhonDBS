/*
 * Copyright (C) 2023 jhonessales
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.jhondbs.core.webServer.functions;

import br.com.jhondbs.core.db.io.Reflection;
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
