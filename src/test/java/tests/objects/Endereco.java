/*
 * Copyright (C) 2024 jhones
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
package tests.objects;

/**
 *
 * @author jhones
 */
public class Endereco {
    
    private String rua;
    private int numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String pais;

    public Endereco() {
    }

    public Endereco(String rua, int numero, String bairro, String cidade, String estado, String pais) {
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = pais;
    }

    @Override
    public String toString() {
        return rua+" "+numero+" "+bairro+" "+cidade+" "+estado+" "+pais;
    }
    
}
