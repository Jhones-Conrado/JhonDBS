#ENGLISH
# JohnDBS
A lightweight, embeddable database and server for small projects
directly within the project.

## About:
JhonDBS creates a directory called "db" inside the folder where the application was
run or inside the root directory of the user who ran the application.
In this "db" folder, subdirectories will be created with the names of packages and classes
of the objects that will be saved, maintaining an easy directory tree structure
to keep. This means that on Linux systems, both local and
remotes can be mounted as subdirectories of this tree, at any desired point,
giving the application maintainer the ability to give more storage to the objects
that are most requested and mount as many HDs and/or partitions as necessary for your
demand, without having to implement anything in the code, using
operating system tools.
In this way it is possible, for example, to use SSD for the objects that are most requested.
and need faster reading and writing while using HDDs
for less requested objects, all this without having to implement absolutely
nothing in the project that uses JhonDBS.

The Database DOES NOT use SerialVersionUID! In this way, any updates
in a class will NOT cause malfunctions. All classes are serialized and
deserialized from the classpath.

Serialized entities that have variables that store another entity will have
these other entities serialized separately, but preserving their reference.
In other words: I have a ClassA, this ClassA has a ClassB variable
called "payment". When I save my ClassA it will be saved in your folder
database-specific while ClassB, i.e. my "payment" will be saved
separately in its specific folder! ClassA, however, will receive the class reference
ClassB and also the "EnteID" of the aforementioned "payment".

### Examples:

> version 1.0
```
public class MyClass {
    
     private String name;
     private integer;
     private boolean teacher;
    
}
```

> version 1.1

```
public class MyClass {
    
     private String name;
     private String lastName;
     private integer;
    
}
```

Changes like the one shown above won't have any problems. In the case of the example above,
objects that are already saved in the database will be loaded with a null value
in the "lastName" parameter, just that.

### Parameters with superclasses:
In case of parameters with super classes, all objects are serialized using a
map format with submaps that preserve objects' class references, ensuring
free use of superclass variables.

#### Example:

> parent class
```
public abstract class Parent {
    
     String name;
    
}
```

> Child Class 1
```
public abstract class ChildOne extends Parent{

     intage;

}
```

> Child Class 2
```
public abstract class ChildTwo extends Parent{

     intage;
     String function;

}
```

> Class with superclass variable

```
public class MyClass extends DefautEntity {

     public Parent my_abstract_variable;

     public MyClass {
         my_abstract_variable = new ChildOne();
     }

}
```

In the example above, the MyClass object will be serialized ensuring that the reference of
class of my_abstract_variable forward to ChildOne. If it had been instantiated
an object of another class, then that other class would be the reference.
See below how our object above would look if serialized.

```
{"objects.MyClass":{"my_abstract_variable":"{\"objects.ChildOne\":{\"age\":{\"int\":0},\"name\":{}}}" ,"enteId":{"long":-1}}}
```

This way it is totally possible to create variables that can store extensions of a
superclass.

## Instructions:
There are two ways to turn a class into an Entity.
- Implement the Entity interface
- Extend the DefaultEntity class

Once this is done, your class will have direct access to the CRUD methods, for example:
- save
- delete
- load
- loadAll
- deleteAll
etc...

### difference between loadAll and loadAllOnlyIds
- The loadAllOnlyIds method, as its name suggests, returns a list of IDs
of all entities of a given type of service that are saved in the database.
- The loadAll method, on the other hand, should be used with caution as it returns an array
with an instance of each of the entities saved in the database of the requested type.
It can cause an exaggerated use of RAM memory if the number of entities is very large.

### difference between delete and fullDelete
If your entity has a reference to another entity, that is, a variable
that contains another entity, this other entity will be saved in the database in its
specific folder, according to its name. That is, the main class is serialized
storing only the class reference and "entity ID" of the object that was serialized
separately. Understanding this, we can understand the functioning of the two methods mentioned;
"delete" and "fullDelete".
- The "delete" method will delete only the entity itself, but all entities
which are your variables and which were saved in the database, will remain saved!
- The fullDelete method will delete not only the entity that called the method itself,
will also erase all entities that are your variables! This method makes a call
recursive by identifying which variables are entities and deleting them too!

## Filters
Database filters are treated as entities that can store a
filtering configuration and be saved for future repetitive uses. see below
how to use the filter to retrieve entities from the database.

### creating a filter
Filters can be created manually or via the Text API.

- **API** An API filter can be created using textual calls following
the "ClassName CMD Field Parameter" format, as in the following example:
```
DBAPI.getByFilter(Client tci name Jhones);
```
The above method will fetch a class called "Client" and then filter all entities
saved that have a field "name" of type String and that starts with "Jhones", ignoring
upper and lowercase.

- **Manually** To create a filter manually, first create an object that is
of one of the following types: BooleanFilter; NumberFilter or StringFilter; An example
of String filter repeating the previous example would be:
```
Filter filter = new Filter();
StringFilter requirement = new StringFilter("name", "Jhones", true);
filter.addItem(requirement);

filter.filter(MyEntity);
```


#PORTUGUÊS
# JhonDBS
Um banco de dados e servidor leve, para pequenos projetos, que pode ser embutido
diretamente dentro do projeto.

## Sobre:
O JhonDBS cria um diretório chamado "db" dentro da pasta onde a aplicação foi
executada ou dentro do diretório raíz do usuário que executou o aplicativo.
Nessa pasta "db" serão criados subdiretórios com os nomes dos pacotes e classes
dos objetos que serão salvos, mantendo uma estrutura de árvore de diretórios fácil
de manter. Isso significa que em sistemas Linux, partições de HDs tanto locais quanto
remotos podem ser montados como subdiretórios dessa árvore, em qualquer ponto desejado,
dando ao mantenedor do aplicativo capacidade de dar mais armazenamento para os objetos
que forem mais requisitados e de montar quantos HDs e/ou partições forem necessárias à sua
demanda, sem que pra isso precise implementar qualquer coisa no código, utilizando
ferramentas do próprio sistema operacional.
Desta forma é possível por exemplo usar SSD para os objetos que são mais requisitados
e precisam de uma leitura e escrita mais rápida ao mesmo tempo em que se use HDs
para os objetos menos requisitados, tudo isso sem que se precise implementar absolutamente
nada no projeto que utiliza o JhonDBS.

O Banco de Dados DISPENSA o uso de SerialVersionUID! Desta forma, quaisquer atualizações
em uma classe NÃO acarretarão em quebras de funcionamento. Todas as classes são serializadas e
desserializadas a partir do caminho da classe.

As entidades serializadas que possuem variáveis que armazenam outra entidade, terão
essas outras entidades serializadas separadamente, porém preservando sua referência.
Em outras palavras: Tenho uma ClasseA, esta ClasseA possui uma variável ClasseB
chamada "pagamento". Quando eu salvar minha ClasseA, esta será salva na sua pasta
específica do bando de dados enquanto ClasseB, ou seja, meu "pagamento" será salvo
separadamente em sua pasta específica! ClasseA, porém, receberá a referência de classe
de ClasseB e também o "EnteID" do citado "pagamento".

### Exemplos:

> versão 1.0
```
public class MyClass {
    
    private String name;
    private int age;
    private boolean teacher;
    
}
```

> versão 1.1

```
public class MyClass {
    
    private String name;
    private String lastName;
    private int age;
    
}
```

Mudanças como essa mostrada acima não terão nenhum problema. No caso do exemplo acima,
os objetos que já estiverem salvos no banco de dados serão carregadas com um valor nulo
no parâmetro "lastName", apenas isso.

### Parâmetros com superclasses:
Em caso de parâmetros com super classes, todos os objetos são serializados utilizando um
formato de mapa com submapas que preservam as referências de classe dos objetos, garantindo
o uso livre de variáveis de superclasse.

#### Exemplo:

> Classe Pai
```
public abstract class Pai {
    
    String name;
    
}
```

> Classe Filho 1
```
public abstract class FilhoUm extends Pai{

    int age;

}
```

> Classe Filho 2
```
public abstract class FilhoDois extends Pai{

    int age;
    String function;

}
```

> Classe com variável de superclasse

```
public class MyClass extends DefautEntity {

    public Pai minha_variável_abstrata;

    public MyClass {
        minha_variável_abstrata = new FilhoUm();
    }

}
```

No exemplo superior o objeto MyClass será serializado garantindo que a referência de
classe de minha_variável_abstrata encaminhe para FilhoUm. Se tivesse sido instanciado
um objeto de outra classe, então essa outra classe seria a referência.
Veja abaixo como ficaria o nosso objeto acima se serializado.

```
{"objects.MyClass":{"minha_variável_abstrata":"{\"objects.FilhoUm\":{\"age\":{\"int\":0},\"name\":{}}}","enteId":{"long":-1}}}
```

Desta forma é totalmente possível criar varáveis que possam armazenar extensões de uma
superclasse.

## Instruções:
Para tornar uma classe em Entidade há duas maneiras.
- Implementar a interface Entity
- Extender a classe DefaultEntity

Uma vez feito isto, sua classe terá acesso direto aos métodos de CRUD como, por exemplo:
- save
- delete
- load
- loadAll
- deleteAll
etc...

### diferença entre loadAll e loadAllOnlyIds
- O método loadAllOnlyIds, como o próprio nome sugere, retorna uma lista com os IDs
de todas as entidades de determinado tipo solicidade que estão salvas no banco de dados.
- O método loadAll, por outro lado, deve ser utilizado com cautela pois retorna um array
com uma instância de cada uma das entidades salvas no banco de dados do tipo solicitado.
Podendo causar um uso exagerado de memória RAM caso o número de entidades seja muito grande.

### diferença entre delete e fullDelete
Caso sua entidade possua uma referência para outra entidade, ou seja, uma variável
que contém uma outra entidade, esta outra entidade será salva no banco de dados em sua
pasta específica, de acordo com seu nome. Ou seja, a classe principal é serializada
armazenando apenas a referência de classe e o "ID de entidade" do objeto que foi serializado
separadamente. Entendendo isto, podemos entender o funcionamento dos dois métodos citado;
"delete" e "fullDelete".
- O método "delete" irá apagar somente a própria entidade, porém todas as entidades
que são suas variáveis e que foram salvas no banco de dados, permanecerão salvas!
- O método fullDelete irá apagar não apenas a própria entidade que chamou o método,
apagará também todas as entidades que são suas variáveis! Este método faz uma chamada
recursiva identificando quais variáveis são entidades e apagando-as também!

## Filtros
Os filtros do banco de dados são tratados como entidades que podem armazenar uma
configuração de filtragem e ser salvos para futuros usos repetitivos. Veja abaixo
como utilizar o filtro para recuperar entidades do banco de dados.

### criando um filtro
Filtros podem ser criados manualmente ou através da API de texto.

- **API** Um filtro por API pode ser criado utilizando chamadas textuais seguindo
o formato "ClassName CMD Field Parameter", como no exemplo a seguir:
```
DBAPI.getByFilter(Client tci name Jhones);
```
O método acima buscará uma classe chamada "Client" e então filtrará todas as entidades
salvas que tenham um campo "name" do tipo String e que comece com "Jhones", ignorando
maiúsculas e minúsculas.

- **Manualmente** Para criar um filtro manualmente, primeiro crie um objeto que seja
de um dos seguintes tipos: BooleanFilter; NumberFilter ou StringFilter; Um exemplo
de String filter repetindo o exemplo anterior seria:
```
Filter filtro = new Filter();
StringFilter exigência = new StringFilter("name", "Jhones", true);
filtro.addItem(exigência);

filtro.filter(MyEntity);
```