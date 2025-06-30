package banco_james.repository;

import banco_james.model.Pessoa;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.Result;

import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class RepositoryNeo {

    private final Driver driver;

    public RepositoryNeo(Driver driver) {
        this.driver = driver;
    }

    public void adicionarPessoa(Pessoa pessoa) {
        try (Session session = driver.session()) {
            // Cria ou atualiza o nó da pessoa
            session.executeWrite((TransactionContext tx) -> {
                tx.run(
                        "MERGE (p:Pessoa {id: $id}) " +
                                "SET p.nome = $nome, p.email = $email, p.cpf = $cpf, " +
                                "p.dataNascimento = $dataNascimento, p.trabalho = $trabalho",
                        parameters(
                                "id", pessoa.getId(),
                                "nome", pessoa.getNome(),
                                "email", pessoa.getEmail(),
                                "cpf", pessoa.getCpf(),
                                "dataNascimento", pessoa.getDataNascimento().toString(),
                                "trabalho", pessoa.getTrabalho()));
                return null;
            });
            System.out.println("[NEO4J] Pessoa adicionada ao grafo.");

            // Cria conexões profissionais com pessoas do mesmo trabalho
            session.executeWrite((TransactionContext tx) -> {
                tx.run(
                        "MATCH (p1:Pessoa {id: $id}), (p2:Pessoa) " +
                                "WHERE p1.trabalho = p2.trabalho AND p1.id <> p2.id " +
                                "MERGE (p1)-[:CONEXAO_PROFISSIONAL {trabalho: $trabalho}]->(p2)",
                        parameters("id", pessoa.getId(), "trabalho", pessoa.getTrabalho()));
                return null;
            });
            System.out.println("[NEO4J] Conexões profissionais criadas.");
        }
    }

    public void listarConexoesProfissionais(int id) {
        try (Session session = driver.session()) {
            List<String> conexoes = session.executeRead((TransactionContext tx) -> {
                Result result = tx.run(
                        "MATCH (p:Pessoa {id: $id})-[:CONEXAO_PROFISSIONAL]->(outro:Pessoa) " +
                                "RETURN outro.id AS id, outro.nome AS nome, outro.trabalho AS trabalho",
                        parameters("id", id));

                return result.list(record -> String.format(
                        "-> %s (ID: %d, Trabalho: %s)",
                        record.get("nome").asString(),
                        record.get("id").asInt(),
                        record.get("trabalho").asString()));
            });

            System.out.println("Conexões profissionais da pessoa ID " + id + ":");
            conexoes.forEach(System.out::println);
        }
    }

    public void listarTodasConexoes() {
        try (Session session = driver.session()) {
            List<String> conexoes = session.executeRead((TransactionContext tx) -> {
                Result result = tx.run(
                        "MATCH (p1:Pessoa)-[:CONEXAO_PROFISSIONAL]->(p2:Pessoa) " +
                                "RETURN p1.nome AS origem, p2.nome AS destino ORDER BY origem");

                return result.list(record -> String.format("👥 %s → %s",
                        record.get("origem").asString(),
                        record.get("destino").asString()));
            });

            System.out.println("\n🔗 TODAS AS CONEXÕES NO GRAFO:");
            conexoes.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar todas as conexões: " + e.getMessage());
        }
    }
}
