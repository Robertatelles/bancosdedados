package banco_james;

import banco_james.database.Postgres;
import banco_james.menu.MenuPessoa;
import banco_james.repository.*;

import redis.clients.jedis.Jedis;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (
            Scanner scanner = new Scanner(System.in);
            var mongoClient = MongoClients.create("mongodb://localhost:27017");
            Driver neoDriver = GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "12345678") // sua senha aqui
            );
            Jedis redis = new Jedis("localhost", 6379) // Redis ativo aqui!
        ) {
            // 🌐 Conexão com Neo4j
            try (var session = neoDriver.session()) {
                String msg = session.run("RETURN 'Conectado ao Neo4j com sucesso!' AS msg")
                                    .single().get("msg").asString();
                System.out.println("🟢 " + msg);
            }

            // 🐘 Conexão com PostgreSQL
            var database = Postgres.getDatabase();
            System.out.println("🟢 Conectado ao PostgreSQL com sucesso!");

            // 🍃 Conexão com MongoDB
            var mongoDatabase = mongoClient.getDatabase("sistema_logs");
            System.out.println("🟢 Conectado ao MongoDB com sucesso!");

            // 🔴 Conexão com Redis
            if ("PONG".equalsIgnoreCase(redis.ping())) {
                System.out.println("🟢 Conectado ao Redis com sucesso!");
                redis.set("ultima_acao", "Sistema iniciado");
            }

            // 🔗 Repositórios
            var repoPostgres = new RepositoryPostgres(database);
            var repoMongo = new RepositoryMongo(mongoDatabase);
            var repoNeo = new RepositoryNeo(neoDriver);

            // 🎛️ Menu interativo
            int opcao;
            do {
                MenuPessoa.exibirMenu();
                opcao = Integer.parseInt(scanner.nextLine());
                MenuPessoa.executarOpcao(opcao, scanner, repoPostgres, repoMongo, repoNeo);
            } while (opcao != 5);

            repoPostgres.fechar();

            // 🧠 Consulta final no Redis
            String acao = redis.get("ultima_acao");
            System.out.println("🔁 Última ação registrada no Redis: " + acao);

        } catch (Exception e) {
            System.err.println("❌ Erro inesperado: " + e.getMessage());
            try (var mongoClient = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = mongoClient.getDatabase("sistema_logs");
                new RepositoryMongo(db).registrarErro("Exceção", e.getMessage());
            } catch (Exception ex) {
                System.err.println("⚠️ Falha ao registrar erro no MongoDB: " + ex.getMessage());
            }
        }
    }
}