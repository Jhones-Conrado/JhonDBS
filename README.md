# JhonDBS
Small DB and WebServer

# Português
Um projeto para Banco de Dados e servidor web leve, prático e eficiente para pequenas aplicações ou necessidades.

## Como usar o Banco de Dados
Após adicionar o banco de dados ao seu projeto, comece criando uma classe que deverá ser salva no banco de dados.
Essa classe precisa implementar Entity e possuir pelo menos uma variável do tipo String que CONTENHA "id" no nome.
A variável não precisa ser pública, isso é opcional. Não precisa ser inicializada, na verdade é recomendável que
não se inicialize a variável do ID.
A variável NÃO precisa receber nenhuma anotação!
A variável pode ter QUALQUER nome, desde que o nome possua a expressão "id", exemplos: "id", "meuId", "idPessoa", "idididididid"...

### Salvando no Banco de Dados: 

```
public class Pessoa implements Entity {
    private String id;
}
```

Apenas isso já é suficiente e sua classe já está pronta para ser salva no banco de dados!

Para salvar, basta chamar o método "save" na própria instância do objeto. Como no exemplo a seguir.

```
Pessoa pessoa = new Pessoa();
pessoa.save();
```

### Recuperando do Banco de Dados
Para recuperar uma entidade do banco de dados use o exemplo a seguir:

```
Pessoa pessoa = new Pessoa().load(id);
```

### Recuperando do Banco de Dados com filtragem de valores.
Para aplicar filtros na consulta do Banco de Dados, basta criar um objeto Filter e adicionar
todas as condições dentro.

```
Filter filter = new Filter(true);
StringFilter stringFilter = new StringFilter("name", "Jhones");
NumberFilter numberFilter = new NumberFilter("age", NumberFilter.GREATER, 17);
BooleanFilter booleanFilter = new BooleanFilter("client", true);

filter.addCondition(stringFilter);
filter.addCondition(numberFilter);
filter.addCondition(booleanFilter);

pessoa.loadAll(filter);
```
O parâmetro boleano passando em "new Filter(boolean)" é utilizado para saber qual o critério de filtragem.
- true -> Todas as condições de filtragem precisam ser verdadeiras.
- false -> Basta que a entidade passe em pelo menos uma das condições.

### Deletando do banco de Dados
Há duas maneiras de deletar uma entidade do Banco de Dados.
- A primeira é deletando apenas a entidade solicitada.
- A segunda é deletando em cascata, o que apagará todas as entidades marcadas como "Cascate".

Para deletar use o exemplo a seguir:
```
pessoa.delete();
pessoa.deleteCascate();
```

### Marcando campos como Único ou Cascata.
Para informar que o valor de um campo precisa ser único e que nenhuma outra entidade poderá ter o mesmo
valor para aquele mesmo campo, basta apenas anotar a variável como @Unique

```
public class Pessoa implements Entity {
    private String id;
    
    @Unique
    private String name;    
}
```

Já para marcar que uma subentidade deverá ser deletada em cascata, basta anotar o campo com @Cascate

```
public class Pessoa implements Entity {
    private String id;

    @Cascate
    private Casa casa;
}
```
