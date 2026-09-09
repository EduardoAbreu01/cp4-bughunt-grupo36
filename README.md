# Checkpoint 4 — Bug Hunt StreamFIAP

## Identificação

**Grupo:** 36

| Integrante | RM | Turma |
|---|---|---|
|Gabriel dos Anjos |565532 |2CCPO |
|Eduardo Abreu |566460 |2CCPO|
|João Pedro de Souza Ferreira|563869 |2CCPO |
|João Pedro da Silva Costa |565031  |2CCPO |
|Gabriel De Biasi Couto |563247 |2CCPO |
|Rodrigo Campos|566386 |2CCPO |

| Campo | |
|---|---|
| **Total de bugs corrigidos** | 15 / 12 |
| **Total de ajustes de Clean Code** | 6 / 6 |

> Encontramos 15 divergências de comportamento em relação ao contrato da seção 3.
> Mantivemos todas corrigidas e documentadas, cada uma em seu próprio commit.

---

## Parte 1 — Bugs encontrados

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | A API aceitava cadastrar conteúdos com `duracaoMinutos <= 0`. | Ausência de validação de duração no `Conteudo`, com o atributo exposto e atribuído direto no construtor, sem passar por setter. | Validação `duracaoMinutos <= 0` lançando `DuracaoInvalidaException` no setter do `Conteudo`, atributo tornado `private` e o construtor passando a usar `setDuracaoMinutos`; HTTP 400 mapeado no `GlobalExceptionHandler`. | Encapsulamento, validação de domínio e tratamento de exceções no Spring Boot |
| bug02 | `GET /api/conteudos/999` respondia HTTP 200 com corpo vazio, como se tivesse dado certo. | `try/catch` vazio no `ConteudoController.buscarPorId` engolindo a `ConteudoNaoEncontradoException` e devolvendo `null`. | Removido o `try/catch`, deixando a exceção propagar até o `GlobalExceptionHandler`, que responde 404 com a mensagem. | Tratamento de exceções |
| bug03 | `GET /api/conteudos/categoria/FICCAO` sempre devolvia lista vazia, mesmo com conteúdos daquela categoria salvos. | Comparação de `String` com `==` (`c.getCategoria() == categoria`) no `ConteudoController.listarPorCategoria`, comparando referências em vez de conteúdo. | Comparação trocada por `.equals()` (depois movida para o `findByCategoria` do repository — ver `clean06`). | Comparação de objetos em Java (`==` vs `equals`) |
| bug04 | `POST /api/usuarios` falhava com erro de valor nulo na coluna `id`. | Estratégia de geração de chave primária do `@GeneratedValue` incompatível com o Oracle da FIAP na entidade `Usuario`. | Estratégia alterada para `GenerationType.SEQUENCE` em `Usuario`. | Mapeamento objeto-relacional (JPA) e geração de chaves primárias |
| bug05 | `POST /api/usuarios` salvava e devolvia o usuário com `nome` nulo, mesmo enviando o nome no JSON. | Construtor de `Usuario` com `nome = nome`: sem o `this`, a atribuição acontecia no próprio parâmetro e o atributo continuava nulo. | Corrigido para `this.nome = nome`. | Escopo de variáveis, encapsulamento e uso do `this` |
| bug06 | Alugar com um `usuarioId` inexistente estourava `IllegalArgumentException` e virava HTTP 500 genérico. | `AluguelController` usava exceção genérica do Java em vez da exceção customizada do projeto. | Substituída por `UsuarioNaoEncontradoException`, que o `GlobalExceptionHandler` traduz em 404 com mensagem. | Tratamento de exceções e `ResponseEntity` |
| bug07 | Aluguel bloqueado por classificação indicativa devolvia HTTP 500 sem explicar o motivo ao cliente. | Faltava `@ExceptionHandler(ClassificacaoIndicativaException.class)` no `GlobalExceptionHandler`. | Adicionado o handler devolvendo 400 Bad Request com a mensagem da regra. | Tratamento global de exceções no Spring Boot e mapeamento de status HTTP |
| bug08 | Usuário com saldo era recusado e usuário sem saldo conseguia alugar — exatamente o inverso da regra. | Comparação invertida em `Usuario.temCreditosSuficientes` (`preco >= creditos`). | Invertida para `preco <= this.creditos`. | Operadores relacionais e lógica de negócio |
| bug09 | Conteúdo com `disponivel = false` era alugado normalmente. | Nenhum ponto do fluxo (nem controller, nem `Usuario.alugar`) verificava a flag `disponivel`. | Validação adicionada no `AluguelController`, lançando `ConteudoIndisponivelException` antes de chamar `alugar`. | Regra de negócio e validação de domínio |
| bug10 | Depois do primeiro aluguel, o conteúdo ficava indisponível para todo mundo. | `Usuario.alugar` chamava `c.setDisponivel(false)`, tratando aluguel de streaming como se fosse locadora física. | Removida a alteração de disponibilidade dentro do aluguel. | Regra de negócio e efeitos colaterais em métodos de domínio |
| bug11 | Série cadastrada por `POST /api/conteudos/serie` salvava `titulo`, `categoria`, `classificacaoEtaria` e `disponivel` nulos/zerados. | Construtor de `Serie` recebia só `numeroTemporadas` e não repassava os atributos herdados ao construtor da superclasse via `super`. | Construtor de `Serie` passou a receber os campos de `Conteudo` e chamar `super(...)`. | Herança e construtores de superclasse |
| bug12 | Alugar um documentário debitava R$ 9,90 dos créditos, e o contrato diz que documentário é gratuito. | `Documentario` não sobrescrevia `calcularPrecoAluguel()`, herdando o preço padrão de `Conteudo`. | Sobrescrito `calcularPrecoAluguel()` em `Documentario` retornando 0,00. | Sobrescrita de métodos (`@Override`) e polimorfismo |
| bug13 | Série com 5 temporadas cobrava R$ 9,90 em vez de R$ 24,50 (4,90 × 5), e o preço não mudava com o número de temporadas. | `Serie.calcularPrecoAluguel(double desconto)` recebia um parâmetro que não existe na superclasse: era **sobrecarga**, não sobrescrita. Como `Usuario.alugar` chama `calcularPrecoAluguel()` sem argumento, quem rodava era o método de `Conteudo`. | Removido o parâmetro `desconto` e adicionado `@Override`, tornando o método uma sobrescrita real. | Sobrescrita vs sobrecarga e o papel do `@Override` |
| bug14 | `GET /api/conteudos/{id}/preco-promocional` de um filme de R$ 14,90 devolvia R$ 17,88 — mais caro que o preço normal. | `Filme.aplicarPromocao` retornava `preco * 1.2`, aumentando 20% em vez de descontar 20%, contrariando o contrato da interface `Promocionavel`. | Trocado para `preco * 0.8`, igual ao que `Serie` já fazia. | Interfaces como contrato e regras de negócio |
| bug15 | `GET /api/usuarios/999` devolvia HTTP 500 sem mensagem útil, enquanto `/api/conteudos/999` já respondia 404 corretamente. | `UsuarioController.buscarPorId` lançava `IllegalArgumentException`, que não tem handler no `GlobalExceptionHandler`. | Substituída por `UsuarioNaoEncontradoException` (mesma correção do bug06, aplicada ao endpoint que ficou de fora). | Exceções customizadas e consistência do tratamento de erros |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boas práticas era violado | O que eu mudei |
|---|---|---|---|
| clean01 | `Usuario.alugar` — parâmetro `Conteudo c` e variável `double p` | Nomes significativos: uma letra não diz o que a variável guarda, e quem lê `p` precisa subir o método inteiro para descobrir que é o preço do aluguel | Renomeados para `conteudo` e `precoAluguel`, deixando as mensagens de exceção e a chamada de `debitarCreditos` autoexplicativas |
| clean02 | Comentário `// adiciona o valor aos créditos do usuário` em `Usuario.debitarCreditos` (o método subtrai) e comentários redundantes em `Serie` | Comentário que mente é pior que a falta de comentário; e comentar o óbvio (`// cria a série com os dados recebidos` acima do construtor) só cria ruído | Removido o comentário enganoso — o nome `debitarCreditos` já diz o que o método faz — e apagados os comentários redundantes de `Serie` |
| clean03 | `ConteudoController`: método privado `calcularDescontoAntigo`, bloco de código de cupom comentado com `TODO`, e o import não usado de `UsuarioNaoEncontradoException` em `Usuario` | Código morto e código comentado: quem lê não sabe se ainda vale, e o histórico do Git já guarda o que foi removido | Removidos o método nunca chamado, o bloco comentado, o import ocioso e o `throws ConteudoIndisponivelException` que `alugar` declarava sem nunca lançar |
| clean04 | `9.90`, `5.00` e `4.90` espalhados dentro de `calcularPrecoAluguel` em `Conteudo`, `Filme` e `Serie` | Números mágicos: o valor aparece sem nome e teria que ser caçado em várias classes se a tabela de preços mudasse | Extraídos para constantes `private static final`: `PRECO_PADRAO`, `PRECO_BASE`, `ADICIONAL_ESTREIA`, `PRECO_POR_TEMPORADA` e `PRECO_GRATUITO` |
| clean05 | Bloco de oito `System.out.println` imprimindo o "RECIBO STREAMFIAP" dentro de `Usuario.alugar` | Responsabilidade única: o model cuida da regra de negócio, não de apresentação. Numa API REST a resposta é o JSON, e imprimir no console mistura camadas e polui o log do servidor | Removida a impressão do recibo; `alugar` ficou só com validar, debitar e devolver o usuário atualizado |
| clean06 | `ConteudoController.listarPorCategoria` fazia `findAll()` e filtrava a lista com um `for` na mão, enquanto `ConteudoRepository.findByCategoria` existia e nunca era chamado | Não reinventar o que o framework já resolve, e não colocar lógica de consulta no controller: o filtro em memória trazia a tabela toda do banco para descartar quase tudo | Método passou a uma linha: `return conteudoRepository.findByCategoria(categoria)`, deixando o filtro virar `WHERE` no SQL |

---

## Parte 3 — Perguntas de reflexão

### 1. Injeção de dependência (Aula 13)

`ConteudoRepository` é uma interface — não existe classe nossa implementando ela, então
`new ConteudoRepository()` nem compilaria. Quem cria o objeto é o Spring Data, que gera
em tempo de execução um proxy implementando os métodos do `JpaRepository` a partir da
assinatura deles. Esse proxy precisa de coisas que o controller não tem: o `DataSource`
com a URL do Oracle, o `EntityManager` da JPA e o gerenciador de transações. Quando o
Spring injeta o bean no `@Autowired` do `ConteudoController`, ele já montou essa cadeia
inteira e entrega o objeto pronto e único (singleton) para toda a aplicação. Com um `new`
comum, cada controller criaria seu próprio objeto sem conexão nenhuma configurada, e o
`AluguelController` — que usa dois repositories ao mesmo tempo — abriria contextos
separados, quebrando a transação do aluguel.

### 2. JDBC vs Spring Data JPA (Aulas 12 e 13)

No `ProdutoDAO` da Aula 12 escrevíamos o SQL, abríamos `Connection`, preenchíamos o
`PreparedStatement`, percorríamos o `ResultSet` campo por campo e fechávamos tudo no
`finally`. O `ConteudoRepository` tem duas linhas e já entrega `save`, `findById`,
`findAll` e `delete`, porque o Spring Data implementa o CRUD sobre o mapeamento das
anotações (`@Entity`, `@Table(name = "conteudos")`, `@Id`, `@GeneratedValue`) e faz o
de/para entre linha e objeto sozinho. O `findByCategoria` funciona sem implementação
porque o Spring lê o **nome do método**: `findBy` + `Categoria` bate com o atributo
`categoria` de `Conteudo`, e ele gera o `select ... where categoria = ?`. O JDBC ainda
ganha quando a consulta é complicada — relatórios com muitos joins, agregações, SQL
específico do Oracle — e quando importa controlar exatamente o que vai ao banco;
com JPA é fácil disparar consulta demais sem perceber, como acontecia no `listarPorCategoria`
antes do `clean06`.

### 3. Exceções checked vs unchecked (Aula 11)

`ClassificacaoIndicativaException extends Exception`, ou seja, é *checked*: o compilador
obriga quem chama a tratar ou declarar. Por isso `Usuario.alugar` tem `throws
ClassificacaoIndicativaException` e o `AluguelController` repassa o mesmo `throws` no
método do endpoint. As outras exceções do projeto (`CreditosInsuficientesException`,
`ConteudoNaoEncontradoException`, `DuracaoInvalidaException`) estendem `RuntimeException`
e sobem sozinhas até o Spring. O bug não era a herança: a exceção chegava ao Spring, só
que sem nenhum `@ExceptionHandler` para ela o framework caía no erro genérico 500 e a
mensagem da regra sumia. A correção do bug07 foi registrar
`@ExceptionHandler(ClassificacaoIndicativaException.class)` no `GlobalExceptionHandler`,
devolvendo 400 com `e.getMessage()` — que já traz idade do usuário, título e classificação
do conteúdo. Ser checked ajudou aqui como documentação: a assinatura de `alugar` avisa,
em tempo de compilação, que esse aluguel pode ser recusado por idade.

### 4. Sobrescrita vs sobrecarga (Aula 7)

A `Serie` declarava `public double calcularPrecoAluguel(double desconto)`. Para o
compilador isso é um método **novo**, porque a assinatura (nome + lista de parâmetros)
é diferente da de `Conteudo.calcularPrecoAluguel()` — é sobrecarga, não sobrescrita.
Compilava sem reclamar e até parecia certo lendo rápido, mas `Usuario.alugar` chama
`conteudo.calcularPrecoAluguel()`, sem argumento: a resolução de sobrecarga acontece em
tempo de compilação e escolhia o método da superclasse, então toda série custava R$ 9,90
fixo, ignorando o `4.90 * numeroTemporadas` logo ali. Sobrescrita é sobre polimorfismo
em tempo de execução — mesma assinatura, comportamento decidido pelo tipo real do objeto.
Com `@Override` na versão errada o compilador teria acusado de imediato ("method does not
override a method from its supertype"), porque não existe `calcularPrecoAluguel(double)`
em `Conteudo`. Foi exatamente o que fizemos no bug13: tiramos o parâmetro e colocamos a
anotação, do mesmo jeito que `Filme` e `Documentario` já faziam.

### 5. Onde blindar o objeto? (Aulas 3, 4 e 13)

Dá para separar por tipo de regra. Invariante do objeto — algo que nunca pode ser
verdade, como duração `<= 0` — vai no **setter**, e o construtor chama esse setter em vez
de atribuir direto: foi a correção do bug01, e é o que garante que nem o `POST` de
cadastro nem uma alteração posterior deixem passar. Atribuição correta dos campos é
responsabilidade do **construtor**, e foi onde moraram o bug05 (`nome = nome` sem `this`)
e o bug11 (`Serie` sem `super`). Regra que depende de mais de um objeto fica no **método
de domínio**: `Usuario.alugar` é quem sabe comparar idade com classificação e crédito com
preço, porque só ali existem usuário e conteúdo juntos — e é o que impede crédito negativo,
já que `debitarCreditos` só é chamado depois do `temCreditosSuficientes`. Validar em um
lugar só não bastou porque cada camada erra de um jeito diferente: o controller barra o
que vem malformado do cliente (bug09, conteúdo indisponível), mas não protege o objeto de
ser corrompido por outro caminho no código; e o setter protege o atributo, mas não sabe
nada da regra de aluguel.

### 6. Abstração e interface (Aulas 8 e 9)

`Conteudo` é classe abstrata porque as três subclasses **são** conteúdos: compartilham
estado (`titulo`, `categoria`, `duracaoMinutos`, `classificacaoEtaria`, `disponivel`),
o mapeamento JPA da tabela `conteudos` e um comportamento padrão de preço que cada uma
especializa. `Promocionavel` é interface porque promoção é um comportamento opcional,
que atravessa a hierarquia: `Filme` e `Serie` implementam, `Documentario` não — e é
justamente essa ausência que faz `calcularPrecoPromocional` devolver o preço cheio para
documentário. Se o documentário passasse a ter promoção, bastaria `Documentario implements
Promocionavel` e o método `aplicarPromocao`: duas mudanças, em um arquivo. `Conteudo`,
`Filme`, `Serie`, os controllers e os repositories ficariam intactos, porque
`calcularPrecoPromocional` trabalha contra a interface (`this instanceof Promocionavel`),
não contra os tipos concretos. Isso mostra um design razoavelmente aberto para extensão —
ainda que esse `instanceof` com cast seja o ponto mais fraco: o mais elegante seria a
promoção ser polimórfica, sem a classe base precisar perguntar o tipo de ninguém.

---

## Parte 4 — Espaço livre (opcional)

Alguma dificuldade, dúvida ou comentário sobre o checkpoint?

```
O que mais deu trabalho foram os bugs que não aparecem testando a API, só lendo o
código. A sobrecarga da Serie (bug13) é o melhor exemplo: o preço vinha errado, mas o
método com o cálculo certo estava logo ali, aparentemente correto — a pista foi comparar
Filme e Serie lado a lado, como o enunciado sugeria, e notar que só uma delas tinha
@Override.

Também tivemos bugs em cascata. O preço do documentário (bug12) só ficou visível depois
de arrumar o cadastro de usuário (bug04 e bug05), porque antes disso não conseguíamos
nem chegar no endpoint de aluguel.

Encontramos 15 divergências em vez das 12 previstas. Preferimos corrigir e documentar
todas, já que as três extras (bug13, bug14 e bug15) quebravam linhas diretas do contrato
da seção 3.
```
