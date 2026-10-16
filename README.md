# Sistema Escolar — Java e Spring Boot

**Autor:** Emanuel Ribeiro Ferreira Nunes e Souza

## Descrição do projeto

API REST desenvolvida com Java e Spring Boot para cadastro de professores e alunos e gerenciamento de matrículas, utilizando Spring Data JPA e banco de dados H2 em memória.

## Erros identificados e correções

### 1. Dependência do H2 com escopo incorreto
- **Arquivo:** `pom.xml`
- **Erro:** a dependência do H2 estava disponível apenas durante os testes.
- **Identificação:** a configuração poderia impedir a inicialização normal do banco de dados.
- **Correção:** removido o escopo `test` da dependência.

### 2. Configuração de criação das tabelas
- **Arquivo:** `src/main/resources/application-test.properties`
- **Erro:** a propriedade `spring.jpa.hibernate.ddl-auto` estava definida como `none`.
- **Identificação:** as tabelas não seriam criadas automaticamente a partir das entidades.
- **Correção:** alterada a propriedade para `create-drop`.

### 3. Geração do identificador de professor
- **Arquivo:** `src/main/java/com/github/app/model/professor/Professor.java`
- **Erro:** o campo `id` não possuía geração automática.
- **Identificação:** o cadastro poderia falhar quando o identificador não fosse informado.
- **Correção:** adicionada a anotação `@GeneratedValue(strategy = GenerationType.IDENTITY)`.

### 4. Rota de professores
- **Arquivo:** `src/main/java/com/github/app/controller/ProfessorController.java`
- **Erro:** a rota estava definida como `/professor`.
- **Identificação:** o caminho não correspondia ao endpoint `/professores` especificado na atividade.
- **Correção:** alterado o mapeamento para `/professores`.

### 5. Atribuição do RA do aluno
- **Arquivo:** `src/main/java/com/github/app/model/aluno/Aluno.java`
- **Erro:** o campo `ra` recebia o valor de `curso`.
- **Identificação:** os dados retornados no cadastro apresentavam informações incorretas.
- **Correção:** alterada a atribuição para utilizar `dados.ra()`.

### 6. Relacionamento entre aluno e matrícula
- **Arquivo:** `src/main/java/com/github/app/model/matricula/Matricula.java`
- **Erro:** o relacionamento estava definido como `@OneToOne`.
- **Identificação:** o mapeamento não correspondia à possibilidade de um aluno possuir várias matrículas.
- **Correção:** alterado para `@ManyToOne`.

### 7. Campo de identificação do aluno na matrícula
- **Arquivos:** `src/main/java/com/github/app/model/matricula/DadosCadastroMatricula.java` e `src/main/java/com/github/app/controller/MatriculaController.java`
- **Erro:** o DTO esperava o campo `idAluno`, diferente de `alunoId`, utilizado no JSON da atividade.
- **Identificação:** incompatibilidade entre o campo enviado na requisição e o esperado pelo DTO.
- **Correção:** alterado o componente para `alunoId` e atualizado seu uso no controller.

### 8. Atualização de aluno
- **Arquivo:** `src/main/java/com/github/app/controller/AlunoController.java`
- **Erro:** o método de atualização não possuía anotação transacional explícita.
- **Identificação:** era necessário verificar se as alterações realizadas no objeto persistido eram gravadas corretamente.
- **Correção:** adicionada a anotação `@Transactional` ao método `atualizar`.

## Como executar o projeto

É necessário ter o JDK 17 instalado.

No Windows, execute na raiz do projeto:

```bat
mvnw.cmd spring-boot:run
```

Para executar os testes automatizados:

```bat
mvnw.cmd test
```

A API utiliza o banco H2 em memória, cujos dados são reiniciados conforme a configuração da aplicação.
