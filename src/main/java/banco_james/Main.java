package banco_james;

import banco_james.database.Postgres;
import banco_james.database.Redis;
import banco_james.menu.MenuPessoa;
import banco_james.repository.*;

import com.mongodb.client.MongoClients;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        
        Redis redis = new Redis();

        try (
                Scanner scanner = new Scanner(System.in);
                var mongoClient = MongoClients.create("mongodb://localhost:27017");
                Driver neoDriver = GraphDatabase.driver(
                        "bolt://localhost:7687",
                        AuthTokens.basic("neo4j", "12345678") // sua senha aqui
                )) {
         
            try (var session = neoDriver.session()) {
                String msg = session.run("RETURN 'Conectado ao Neo4j com sucesso!' AS msg")
                        .single().get("msg").asString();
                System.out.println("🟢 " + msg);
            }

            
            var database = Postgres.getDatabase();
            System.out.println("🟢 Conectado ao PostgreSQL com sucesso!");

           
            var mongoDatabase = mongoClient.getDatabase("sistema_logs");
            System.out.println("🟢 Conectado ao MongoDB com sucesso!");

            
            if ("PONG".equalsIgnoreCase(redis.ping())) {
                System.out.println("🟢 Conectado ao Redis com sucesso!");
                redis.set("ultima_acao", "Sistema iniciado");
            }

          
            var repoPostgres = new RepositoryPostgres(database);
            var repoMongo = new RepositoryMongo(mongoDatabase);
            var repoNeo = new RepositoryNeo(neoDriver);

           
            int opcao;
            do {
                MenuPessoa.exibirMenu();
                opcao = Integer.parseInt(scanner.nextLine());
                MenuPessoa.executarOpcao(opcao, scanner, repoPostgres, repoMongo, repoNeo, redis);
            } while (opcao != 8);
        }

    }
}
