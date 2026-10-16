# Sistema Escolar — Java e Spring Boot

**Autor:** Emanuel Ribeiro Ferreira Nunes e Souza

API REST para cadastro de professores e alunos e registro de matrículas, construída com Java 17, Spring Boot, Spring Web, Spring Data JPA e banco H2 em memória.

## Os 8 erros corrigidos

> Os erros abaixo foram identificados comparando o código-base com o comportamento especificado na atividade. As correções estão aplicadas no código deste repositório.

### 1. H2 declarado somente para testes
- **Arquivo:** `pom.xml`
- **Problema:** a dependência `com.h2database:h2` tinha escopo `test`, deixando o driver indisponível ao iniciar a aplicação normalmente.
- **Como identificar:** a inicialização do datasource fora do contexto de testes poderia falhar por não encontrar o driver H2.
- **Correção:** removido o escopo `test`; o driver fica disponível durante a execução da aplicação.

### 2. Criação automática das tabelas desativada
- **Arquivo:** `src/main/resources/application-test.properties`
- **Problema:** `spring.jpa.hibernate.ddl-auto=none` não permite ao Hibernate criar as tabelas a partir das entidades.
- **Como identificar:** operações de persistência falhariam se as tabelas ainda não existissem no banco H2 recém-criado.
- **Correção:** configurado `spring.jpa.hibernate.ddl-auto=create-drop`, conforme o requisito de banco em memória recriado a cada inicialização.

### 3. ID de professor sem geração automática
- **Arquivo:** `src/main/java/com/github/app/model/professor/Professor.java`
- **Problema:** o campo `id` tinha `@Id`, mas não tinha uma estratégia de geração automática.
- **Como identificar:** ao salvar um professor sem informar ID, a persistência poderia falhar por identificador não atribuído.
- **Correção:** adicionada `@GeneratedValue(strategy = GenerationType.IDENTITY)`.

### 4. Rota-base de professores divergente da especificação
- **Arquivo:** `src/main/java/com/github/app/controller/ProfessorController.java`
- **Problema:** a rota estava definida como `/professor`, no singular, enquanto a atividade exige `/professores`.
- **Como identificar:** chamadas aos caminhos `/professores` retornavam 404.
- **Correção:** alterado o `@RequestMapping` para `/professores`.

### 5. RA do aluno recebia o valor do curso
- **Arquivo:** `src/main/java/com/github/app/model/aluno/Aluno.java`
- **Problema:** o construtor de cadastro atribuía `dados.curso()` ao campo `ra`, descartando o RA enviado.
- **Como identificar:** após cadastrar e listar um aluno, o campo RA continha o texto do curso.
- **Correção:** atribuído `dados.ra()` a `this.ra`.

### 6. Relacionamento de matrícula com aluno não permitia cardinalidade correta
- **Arquivo:** `src/main/java/com/github/app/model/matricula/Matricula.java`
- **Problema:** o relacionamento com `Aluno` estava mapeado como `@OneToOne`, embora um aluno possa ter diversas matrículas.
- **Como identificar:** o modelo não representava a cardinalidade exigida e podia impor unicidade indevida na chave estrangeira.
- **Correção:** alterado o relacionamento para `@ManyToOne`, permitindo várias matrículas por aluno.

### 7. Nome do campo do aluno incompatível com o JSON de matrícula
- **Arquivos:** `src/main/java/com/github/app/model/matricula/DadosCadastroMatricula.java` e `src/main/java/com/github/app/controller/MatriculaController.java`
- **Problema:** o DTO esperava `idAluno`, mas o JSON definido na atividade envia `alunoId`.
- **Como identificar:** o valor enviado como `alunoId` não era lido pelo componente esperado do DTO.
- **Correção:** renomeado o componente para `alunoId` e atualizado o controller para usar `dados.alunoId()`.

### 8. PUT de aluno sem transação
- **Arquivo:** `src/main/java/com/github/app/controller/AlunoController.java`
- **Problema:** a atualização alterava uma entidade gerenciada sem uma transação explícita no método.
- **Como identificar:** era necessário consultar novamente o recurso depois do PUT para verificar se a alteração realmente havia sido persistida.
- **Correção:** adicionada `@Transactional` ao método `atualizar`, garantindo que o dirty checking do JPA ocorra dentro da transação.

## Requisitos

- JDK 17 ou compatível com a versão do Spring Boot definida no `pom.xml`.
- Conexão à internet na primeira execução para que o Maven Wrapper baixe o Maven e as dependências.

## Como executar

Na raiz do projeto, no Windows:

```bat
mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080`. O banco H2 é em memória e perde os dados ao encerrar a aplicação.

## Como executar os testes automatizados

```bash
mvnw.cmd test
```

Os testes de integração em `src/test/java/com/github/app/AppApplicationTests.java` verificam inicialização e endpoints principais, cadastro/listagem com campos corretos, atualização persistida de aluno, múltiplas matrículas para o mesmo aluno e exclusão de alunos/professores.

**Nota de validação:** os testes foram incluídos no projeto, mas não puderam ser executados neste ambiente de preparação porque o Maven Wrapper não conseguiu baixar a distribuição Maven do Maven Central. Execute o comando acima em uma máquina com acesso ao repositório Maven antes da entrega final.

## Endpoints exigidos

| Recurso | Método e rota |
|---|---|
| Professores | `POST /professores` |
| Professores | `GET /professores` (paginado) |
| Professores | `GET /professores/listar` |
| Professores | `GET /professores/todos` |
| Professores | `PUT /professores` |
| Professores | `DELETE /professores/{id}` |
| Alunos | `POST /alunos` |
| Alunos | `GET /alunos` (paginado) |
| Alunos | `GET /alunos/listar` |
| Alunos | `GET /alunos/todos` |
| Alunos | `PUT /alunos` |
| Alunos | `DELETE /alunos/{id}` |
| Matrículas | `POST /matriculas` |
| Matrículas | `GET /matriculas` |
| Matrículas | `DELETE /matriculas/{id}` |

### Exemplos de payload

Cadastro de professor:

```json
{
  "nome": "Ana Souza",
  "email": "ana.souza@escola.com",
  "telefone": "21999990000",
  "registro": "PRF-1001",
  "disciplina": "MATEMATICA",
  "endereco": {
    "logradouro": "Rua das Flores",
    "bairro": "Centro",
    "cep": "20000000",
    "cidade": "Rio de Janeiro",
    "uf": "RJ"
  }
}
```

Cadastro de aluno:

```json
{
  "nome": "Bruno Lima",
  "email": "bruno.lima@escola.com",
  "telefone": "21988880000",
  "ra": "2026001",
  "curso": "ADS",
  "endereco": {
    "logradouro": "Av. Brasil",
    "bairro": "Penha",
    "cep": "21000000",
    "cidade": "Rio de Janeiro",
    "uf": "RJ"
  }
}
```

Cadastro de matrícula:

```json
{
  "alunoId": 1,
  "professorId": 1,
  "turno": "MANHA",
  "semestre": "2026.2",
  "observacao": "Turma A"
}
```

## Publicação no GitHub

1. Crie um repositório público no GitHub.
2. Extraia este projeto e envie todos os arquivos e pastas, incluindo `src`, `pom.xml`, `mvnw`, `.mvn` e este `README.md`.
3. Não envie `target/` nem arquivos de configuração pessoal da IDE.
4. Confirme que o README aparece na página inicial do repositório e envie a URL na plataforma da atividade.
