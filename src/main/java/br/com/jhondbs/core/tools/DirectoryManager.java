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

import br.com.jhondbs.core.db.interfaces.Entity;

/**
 * Responsável por métodos que retornam paths para entidades e configurações,
 * garantindo que os caminhos seguirão um padrão.
 * @author jhones
 */
public class DirectoryManager {
    
    /**
     * Retorna o caminho para o diretório dessa classe de entidade específica no
     * banco de dados.
     * @param entity Entidade a ter seu diretório de classe buscada.
     * @return Caminho para o diretório de classe da entidade.
     */
    public static String entityDBFolder(Entity entity) {
        if(entity == null) return null;
        String caminho = "./db/"+entity.getClass().toString().replace(".class", "").replace(".", "/");
        return caminho;
    }
    
    /**
     * Retorna o caminho para o arquivo da entidade no banco de dados.
     * @param entity Entidade a ter teu arquivo buscado.
     * @return Caminho para o arquivo da entidade.
     * @throws Exception 
     */
    public static String entityToPath(Entity entity) throws Exception {
        if(entity == null) return null;
        String caminho = entityDBFolder(entity) + "/" + entity.getId();
        return caminho;
    }
    
    /**
     * Retorna o caminho para a pasta de arquivos da entidade. O diretório que
     * armazena todos os arquivos anexados na entidade através de um campo File.
     * @param entity Entidade a ter o campo de arquivos localizado.
     * @return Caminho para o diretório de arquivos da entidade.
     * @throws Exception 
     */
    public static String entityFileFolder(Entity entity) throws Exception {
        if(entity == null) return null;
        String caminho = "./db/files/"+entity.getClass().toString().replace(".class", "").replace(".", "/") + "/" + entity.getId();
        return caminho;
    }
    
    /**
     * Retorna o caminho para a pasta de imagens da entidade. O diretório que
     * armazena todos os arquivos de imagem anexados na entidade através de um 
     * campo Image.
     * @param entity Entidade a ter o campo de arquivos localizado.
     * @return Caminho para o diretório de arquivos da entidade.
     * @throws Exception 
     */
    public static String entityImagesFolder(Entity entity) throws Exception {
        if(entity == null) return null;
        String caminho = "./db/imgs/"+entity.getClass().toString().replace(".class", "").replace(".", "/") + "/" + entity.getId();
        return caminho;
    }
    
    /**
     * Retorna o caminho para a pasta de arquivos da entidade. O diretório que
     * armazena todos os arquivos anexados na entidade através de um campo File.
     * @param entity Entidade a ter o campo de arquivos localizado.
     * @return Caminho para o diretório de arquivos da entidade.
     * @throws Exception 
     */
    @Deprecated
    public static String entityFileFolderOld(Entity entity) throws Exception {
        if(entity == null) return null;
        String caminho = "./db/files/" + entity.getId();
        return caminho;
    }
    
    /**
     * Retorna o caminho para a pasta de imagens da entidade. O diretório que
     * armazena todos os arquivos de imagem anexados na entidade através de um 
     * campo Image.
     * @param entity Entidade a ter o campo de arquivos localizado.
     * @return Caminho para o diretório de arquivos da entidade.
     * @throws Exception 
     */
    @Deprecated
    public static String entityImagesFolderOld(Entity entity) throws Exception {
        if(entity == null) return null;
        String caminho = "./db/imgs/" + entity.getId();
        return caminho;
    }
    
}
