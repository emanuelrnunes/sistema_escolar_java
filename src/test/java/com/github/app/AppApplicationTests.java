package com.github.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.github.app.model.aluno.AlunoRepository;
import com.github.app.model.matricula.MatriculaRepository;
import com.github.app.model.professor.ProfessorRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AppApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @BeforeEach
    void limparBanco() {
        matriculaRepository.deleteAll();
        alunoRepository.deleteAll();
        professorRepository.deleteAll();
    }

    @Test
    void contextoSobeEEndpointsPrincipaisRespondem() throws Exception {
        mockMvc.perform(get("/professores")).andExpect(status().isOk());
        mockMvc.perform(get("/alunos")).andExpect(status().isOk());
        mockMvc.perform(get("/matriculas")).andExpect(status().isOk());
    }

    @Test
    void cadastraProfessorEListaOsCamposCorretos() throws Exception {
        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nome":"Ana Souza",
                      "email":"ana.souza@escola.com",
                      "telefone":"21999990000",
                      "registro":"PRF-1001",
                      "disciplina":"MATEMATICA",
                      "endereco":{"logradouro":"Rua das Flores","bairro":"Centro","cep":"20000000","cidade":"Rio de Janeiro","uf":"RJ"}
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/professores/listar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").isNumber())
            .andExpect(jsonPath("$[0].nome").value("Ana Souza"))
            .andExpect(jsonPath("$[0].registro").value("PRF-1001"))
            .andExpect(jsonPath("$[0].disciplina").value("MATEMATICA"));
    }

    @Test
    void cadastraAlunoComRaCorretoEAtualizaCursoPersistido() throws Exception {
        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nome":"Bruno Lima",
                      "email":"bruno.lima@escola.com",
                      "telefone":"21988880000",
                      "ra":"2026001",
                      "curso":"ADS",
                      "endereco":{"logradouro":"Av. Brasil","bairro":"Penha","cep":"21000000","cidade":"Rio de Janeiro","uf":"RJ"}
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/alunos/listar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].ra").value("2026001"))
            .andExpect(jsonPath("$[0].curso").value("ADS"));

        Integer alunoId = alunoRepository.findAll().get(0).getId();
        mockMvc.perform(put("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + alunoId + ",\"curso\":\"Engenharia de Software\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/alunos/listar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].curso").value("Engenharia de Software"));
    }

    @Test
    void permiteVariasMatriculasParaOMesmoAluno() throws Exception {
        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Ana Souza","email":"ana.souza@escola.com","telefone":"21999990000","registro":"PRF-1001","disciplina":"MATEMATICA","endereco":{"logradouro":"Rua das Flores","bairro":"Centro","cep":"20000000","cidade":"Rio de Janeiro","uf":"RJ"}}
                    """))
            .andExpect(status().isOk());
        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Bruno Lima","email":"bruno.lima@escola.com","telefone":"21988880000","ra":"2026001","curso":"ADS","endereco":{"logradouro":"Av. Brasil","bairro":"Penha","cep":"21000000","cidade":"Rio de Janeiro","uf":"RJ"}}
                    """))
            .andExpect(status().isOk());

        Integer alunoId = alunoRepository.findAll().get(0).getId();
        Integer professorId = professorRepository.findAll().get(0).getId();
        mockMvc.perform(post("/matriculas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"alunoId\":" + alunoId + ",\"professorId\":" + professorId + ",\"turno\":\"MANHA\",\"semestre\":\"2026.2\",\"observacao\":\"Turma A\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/matriculas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"alunoId\":" + alunoId + ",\"professorId\":" + professorId + ",\"turno\":\"NOITE\",\"semestre\":\"2026.2\",\"observacao\":\"Turma B\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/matriculas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[1].id").isNumber())
            .andExpect(jsonPath("$[0].aluno.id").value(alunoId))
            .andExpect(jsonPath("$[1].aluno.id").value(alunoId))
            .andExpect(jsonPath("$[0].dataMatricula").exists());
    }

    @Test
    void excluiAlunoEProfessor() throws Exception {
        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Ana Souza","email":"ana.souza@escola.com","telefone":"21999990000","registro":"PRF-1001","disciplina":"MATEMATICA","endereco":{"logradouro":"Rua das Flores","bairro":"Centro","cep":"20000000","cidade":"Rio de Janeiro","uf":"RJ"}}
                    """))
            .andExpect(status().isOk());
        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Bruno Lima","email":"bruno.lima@escola.com","telefone":"21988880000","ra":"2026001","curso":"ADS","endereco":{"logradouro":"Av. Brasil","bairro":"Penha","cep":"21000000","cidade":"Rio de Janeiro","uf":"RJ"}}
                    """))
            .andExpect(status().isOk());

        Integer professorId = professorRepository.findAll().get(0).getId();
        Integer alunoId = alunoRepository.findAll().get(0).getId();
        mockMvc.perform(delete("/professores/" + professorId)).andExpect(status().isOk());
        mockMvc.perform(delete("/alunos/" + alunoId)).andExpect(status().isOk());
        mockMvc.perform(get("/professores/listar")).andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
        mockMvc.perform(get("/alunos/listar")).andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }
}
