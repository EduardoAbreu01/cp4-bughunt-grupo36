# Checkpoint 4 — Bug Hunt StreamFIAP

> Copie este arquivo para a raiz do seu repositório com o nome **README.md**
> e preencha todas as seções.

## Identificação

**Grupo:** ___

| Integrante | RM | Turma |
|---|---|---|
| | | |
| | | |
| | | |
| | | |

| Campo | |
|---|---|
| **Total de bugs corrigidos** | _12_ / 12 |
| **Total de ajustes de Clean Code** | ___ / 6 |

---

## Parte 1 — Bugs encontrados

> Uma linha por bug, na ordem em que você os encontrou. Use a numeração dos seus
> commits (`fix: bug01 ...`). Preencha TODAS as colunas — metade da nota está aqui.

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | A API aceitava cadastrar conteúdos com duracaoMinutos <= 0.|Ausência de validação para duração no modelo Conteudo e encapsulamento violado (acesso direto ao atributo duracaoMinutos sem utilizar getters/setters) |Adicionada a validação duracaoMinutos <= 0 lançando DuracaoInvalidaException no setter do Conteudo, alterada a visibilidade do atributo para private e mapeado o retorno HTTP 400 no GlobalExceptionHandler.|Encapsulamento, Validação de Domínio e Tratamento de Exceções no Spring Boot|
| bug02 |Buscar conteúdo inexistente (GET /api/conteudos/99) retorna resposta vazia com HTTP 200 |Bloco try-catch capturando a exceção e retornando null |Removido o bloco try-catch para permitir a propagação da ConteudoNaoEncontradoException até o GlobalExceptionHandler. |Tratamento de Exceções|
| bug03 |A busca por categoria (GET /api/conteudos/categoria/{categoria}) retornava erro ou lista vazia. |Comparação de Strings realizada de forma incorreta ou insensível a maiúsculas/minúsculas.|Atualizada a comparação de categoria para utilizar .equalsIgnoreCase(). |Comparação de Objetos em Java|
| bug04 |Erro ao cadastrar usuário (POST /api/usuarios) devido a erro de valor nulo na coluna id do banco de dados |A estratégia de geração de chave primária no @GeneratedValue não estava compatível com a sequência do banco de dados. |Alterada a estratégia para GenerationType.SEQUENCE na entidade Usuario |Mapeamento Objeto-Relacional (JPA) e Geração de Chaves Primárias |
| bug05 |Ao cadastrar usuário (POST /api/usuarios), o nome vinha como null na resposta ou no banco. |Falta da palavra-chave this no construtor (nome = nome), resultando em atribuição de variável local em vez de preencher o atributo do objeto. |Alterado para this.nome = nome no construtor da classe Usuario. |Escopo de Variáveis, Encapsulamento e Uso do Operador this em Java |
| bug06 |Ao tentar alugar com um usuarioId inexistente, a API disparava IllegalArgumentException gerando status HTTP incorreto. |Uso de exceção genérica do Java (IllegalArgumentException) em vez de exceção personalizada. |Substituído por UsuarioNaoEncontradoException para retornar HTTP 404 via GlobalExceptionHandler. |Tratamento de Exceções e ResponseEntity |
| bug07 |A exceção de classificação indicativa (ClassificacaoIndicativaException) gerava erro genérico (HTTP 500) em vez de uma resposta clara de erro na API.|Faltava o mapeamento do manipulador da ClassificacaoIndicativaException na classe GlobalExceptionHandler. |Adicionado o tratamento @ExceptionHandler(ClassificacaoIndicativaException.class) no GlobalExceptionHandler para retornar HTTP 400 Bad Request com a mensagem de erro. |Tratamento Global de Exceções no Spring Boot e Mapeamento de Status HTTP |
| bug08 |Usuários com saldo suficiente eram impedidos de alugar, enquanto usuários sem saldo conseguiam alugar sem ter créditos. |Operador relacional invertido no método temCreditosSuficientes (preco >= creditos). |Invertida a comparação para creditos >= preco (ou preco <= creditos). |Operadores Relacionais e Lógica de Negócio |
| bug09 |A API permitia o aluguel de um conteúdo mesmo quando ele não estava disponível. |Ausência de verificação da flag disponivel do conteúdo antes de realizar o aluguel. |Adicionada a validação lançando ConteudoIndisponivelException("Conteúdo indisponível para aluguel") ao tentar alugar. |Regra de Negócio e Validação de Domínio |
| bug10 |Após o primeiro aluguel, o conteúdo ficava permanentemente indisponível para outros usuários. |Chamada indevida de c.setDisponivel(false) dentro do método alugar. |Removida a alteração do status de disponibilidade do conteúdo no processo de aluguel. |Regra de Negócio|
| bug11 |Ao cadastrar uma Serie, os atributos herdados de Conteudo (titulo, categoria, disponivel, classificacaoEtaria, etc.) eram salvos como null ou 0 no banco de dados. |O construtor da classe Serie não repassava os parâmetros herdados para o construtor da superclasse via super |Atualizado o construtor de Serie para receber os campos de Conteudo e realizar a chamada super |Herança em POO |
| bug12 |O aluguel de um Documentario estava debitando valor incorreto dos créditos do usuário no endpoint de aluguel. |A subclasse Documentario não sobrescrevia calcularPrecoAluguel() com o valor/desconto correto de sua regra de negócio (retornando o valor base da superclasse). |Sobrescrito o método calcularPrecoAluguel() em Documentario para aplicar a regra de preço/gratuidade prevista no modelo. |Sobrescrita de Métodos (@Override), Polimorfismo e Regras de Negócio |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boas práticas era violado | O que eu mudei |
|---|---|---|---|
| clean01 | | | |
| clean02 | | | |
| clean03 | | | |
| clean04 | | | |
| clean05 | | | |
| clean06 | | | |

---

## Parte 3 — Perguntas de reflexão

> Responda com suas palavras, 5 a 10 linhas cada, **usando o código real do projeto
> como exemplo**. Respostas genéricas de tutorial não pontuam.

### 1. Injeção de dependência (Aula 13)
Os controllers recebem os repositories via `@Autowired` (ex.: `ConteudoController`
usa `ConteudoRepository`). Explique por que o Spring precisa gerenciar esses objetos
em vez de criarmos com `new ConteudoRepository()`. O que exatamente o Spring faz ao
injetar um bean, e por que isso não funcionaria com um `new` comum?

### 2. JDBC vs Spring Data JPA (Aulas 12 e 13)
Na Aula 12 escrevemos um `ProdutoDAO` na mão com `Connection`, `PreparedStatement` e
`ResultSet`. Aqui o `ConteudoRepository` tem 2 linhas e faz CRUD completo. Compare as
duas abordagens: o que o Spring Data JPA automatiza, o que o JDBC/DAO ainda resolve
melhor, e como o `findByCategoria` consegue funcionar sem implementação.

### 3. Exceções checked vs unchecked (Aula 11)
A `ClassificacaoIndicativaException` estourava como um erro genérico do servidor,
sem mensagem útil para o cliente. Explique a diferença entre `extends Exception` e
`extends RuntimeException` no contexto desse bug, e como você fez a mensagem da
regra (classificação indicativa) chegar de forma clara ao cliente da API.

### 4. Sobrescrita vs sobrecarga (Aula 7)
Um dos bugs compilava sem nenhum erro: o método da `Serie` parecia sobrescrever
`calcularPrecoAluguel`, mas na verdade sobrecarregava. Explique a diferença entre
override e overload nesse caso e por que a anotação `@Override` teria impedido o bug.

### 5. Onde blindar o objeto? (Aulas 3, 4 e 13)
Vimos bugs de dados inválidos aceitos (duração negativa, créditos negativos, campos
nulos). Em quais lugares (construtor, setter, método do model) cada tipo de validação
deve ficar? Justifique usando os bugs que você encontrou e explique por que validar só
em um lugar não foi suficiente.

### 6. Abstração e interface (Aulas 8 e 9)
`Conteudo` é abstrata e `Promocionavel` é uma interface. Explique a diferença de
propósito entre as duas nesse projeto e o que mudaria no código se o Documentário
passasse a ter promoções — quais classes/linhas seriam tocadas e quais ficariam
intactas? O que isso diz sobre o design do sistema?

---

## Parte 4 — Espaço livre (opcional)

Alguma dificuldade, dúvida ou comentário sobre o checkpoint?

```

```
