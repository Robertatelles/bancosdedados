package banco_james.repository;

import banco_james.model.Pessoa;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.Result;

import java.util.List;
import java.util.stream.Collectors;

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
                    "p.dataNascimento = $dataNascimento, p.amizade = $amizade",
                    parameters(
                        "id", pessoa.getId(),
                        "nome", pessoa.getNome(),
                        "email", pessoa.getEmail(),
                        "cpf", pessoa.getCpf(),
                        "dataNascimento", pessoa.getDataNascimento().toString(),
                        "amizade", pessoa.getAmizade()
                    )
                );
                return null;
            });
            System.out.println("[NEO4J] Pessoa adicionada ou atualizada no grafo.");

            
            session.executeWrite((TransactionContext tx) -> {
                tx.run(
                    "MATCH (p:Pessoa {id: $id})-[r:CONEXAO_PROFISSIONAL]-() DELETE r",
                    parameters("id", pessoa.getId())
                );
                return null;
            });
            System.out.println("[NEO4J] Conexões antigas removidas.");

            
            if (pessoa.getAmizade() != null && !pessoa.getAmizade().isBlank()) {
                session.executeWrite((TransactionContext tx) -> {
                    tx.run(
                        "MATCH (p1:Pessoa {id: $id}), (p2:Pessoa) " +
                        "WHERE p1.amizade = p2.amizade AND p1.id <> p2.id " +
                        "MERGE (p1)-[:CONEXAO_PROFISSIONAL {amizade: $amizade}]->(p2)",
                        parameters("id", pessoa.getId(), "amizade", pessoa.getAmizade())
                    );
                    return null;
                });
                System.out.println("[NEO4J] Conexões atualizadas com grupo '" + pessoa.getAmizade() + "'.");
            }
        }
    }

    public void listarConexoesProfissionais(int id) {
        try (Session session = driver.session()) {
            List<String> conexoes = session.executeRead((TransactionContext tx) -> {
                Result result = tx.run(
                    "MATCH (p:Pessoa {id: $id})-[:CONEXAO_PROFISSIONAL]-(outro:Pessoa) " +
                    "RETURN outro.id AS id, outro.nome AS nome, outro.amizade AS amizade",
                    parameters("id", id)
                );

                return result.list(record -> String.format(
                    "-> %s (ID: %d, Amizade: %s)",
                    record.get("nome").asString(),
                    record.get("id").asInt(),
                    record.get("amizade").asString()
                ));
            });

            System.out.println("\n👥 Conexões da pessoa ID " + id + ":");
            conexoes.forEach(System.out::println);
        }
    }

    public void listarTodasConexoes() {
        try (Session session = driver.session()) {
            List<String> conexoes = session.executeRead((TransactionContext tx) -> {
                Result result = tx.run(
                    "MATCH (p1:Pessoa)-[:CONEXAO_PROFISSIONAL]-(p2:Pessoa) " +
                    "WHERE id(p1) < id(p2) " +
                    "RETURN DISTINCT p1.nome AS origem, p2.nome AS destino ORDER BY origem"
                );

                return result.list(record -> String.format("👥 %s → %s",
                    record.get("origem").asString(),
                    record.get("destino").asString()
                ));
            });

            System.out.println("\n🔗 TODAS AS CONEXÕES NO GRAFO:");
            conexoes.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar conexões: " + e.getMessage());
        }
    }

    public void listarConexoesNosDoisSentidos() {
        try (Session session = driver.session()) {
            List<String> conexoes = session.executeRead((TransactionContext tx) -> {
                Result result = tx.run(
                    "MATCH (p1:Pessoa)-[:CONEXAO_PROFISSIONAL]-(p2:Pessoa) " +
                    "WHERE p1.nome <> p2.nome " +
                    "RETURN DISTINCT p1.nome AS nome1, p2.nome AS nome2"
                );

                return result.list(record -> {
                    String nome1 = record.get("nome1").asString();
                    String nome2 = record.get("nome2").asString();
                    return List.of(
                        String.format("👥 %s → %s", nome1, nome2),
                        String.format("👥 %s → %s", nome2, nome1)
                    );
                }).stream()
                  .flatMap(List::stream)
                  .distinct()
                  .sorted()
                  .collect(Collectors.toList());
            });

            System.out.println("\n🔁 CONEXÕES EM AMBOS OS SENTIDOS:");
            conexoes.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar conexões duplicadas: " + e.getMessage());
        }
    }
    public void conectarTodosDoGrupo(String amizade) {
    try (Session session = driver.session()) {
        session.executeWrite(tx -> {
            tx.run(
                "MATCH (p:Pessoa {amizade: $amizade}) " +
                "WITH collect(p) AS pessoas " +
                "UNWIND pessoas AS p1 " +
                "UNWIND pessoas AS p2 " +
                "WITH p1, p2 WHERE id(p1) < id(p2) " +
                "MERGE (p1)-[:AMIZADE]->(p2) " +
                "MERGE (p2)-[:AMIZADE]->(p1)",  
                parameters("amizade", amizade)
            );
            return null;
        });
        System.out.println("🤝 Todos do grupo '" + amizade + "' conectados!");
    }
}
}