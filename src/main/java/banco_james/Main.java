package banco_james;

import banco_james.database.Postgres;
import banco_james.model.Pessoa;
import banco_james.repository.RepositoryMongo;
import banco_james.repository.RepositoryPostgres;
import banco_james.repository.RepositoryNeo;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Driver;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (
                Scanner scanner = new Scanner(System.in);
                var mongoClient = MongoClients.create("mongodb://localhost:27017");
                Driver neoDriver = GraphDatabase.driver("bolt://localhost:7687") // ajuste o endereço se necessário
        ) {
            var database = Postgres.getDatabase();
            var pessoaRepository = new RepositoryPostgres(database);

            MongoDatabase mongoDatabase = mongoClient.getDatabase("sistema_logs");
            RepositoryMongo repositoryMongo = new RepositoryMongo(mongoDatabase);
            RepositoryNeo repositoryNeo = new RepositoryNeo(neoDriver);

            int opcao;
            do {
                System.out.println("\n==== GERENCIAMENTO DE PESSOAS ====");
                System.out.println("1. Cadastrar nova pessoa");
                System.out.println("2. Ver lista de pessoas cadastradas");
                System.out.println("3. Atualizar dados de uma pessoa");
                System.out.println("4. Excluir pessoa do sistema");
                System.out.println("5. Encerrar programa");
                System.out.println("6. Ver conexões profissionais");
                System.out.print("Digite sua escolha: ");
                opcao = scanner.nextInt();
                scanner.nextLine();

                switch (opcao) {
                    case 1 -> {
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Nome: ");
                        String nome = scanner.nextLine();

                        System.out.print("Email: ");
                        String email = scanner.nextLine();

                        System.out.print("CPF: ");
                        String cpf = scanner.nextLine();

                        System.out.print("Data de nascimento (AAAA-MM-DD): ");
                        String dataNascimento = scanner.nextLine();

                        System.out.print("Trabalho: ");
                        String trabalho = scanner.nextLine();

                        Pessoa pessoa = new Pessoa(id, nome, email, cpf, dataNascimento, trabalho);
                        pessoaRepository.adicionar(pessoa);
                        repositoryMongo.registrarLog("Cadastro", "Pessoa adicionada: " + nome);
                        repositoryNeo.adicionarPessoa(pessoa); // Conexões profissionais no grafo
                    }
                    case 2 -> {
                        pessoaRepository.listar();
                        repositoryMongo.registrarLog("Consulta", "Listagem de pessoas realizada");
                    }
                    case 3 -> {
                        System.out.print("ID da pessoa a atualizar: ");
                        int idAtualizar = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Novo nome: ");
                        String novoNome = scanner.nextLine();

                        pessoaRepository.atualizar(idAtualizar, novoNome);
                        repositoryMongo.registrarLog("Atualização",
                                "Pessoa ID " + idAtualizar + " atualizada para nome: " + novoNome);
                    }
                    case 4 -> {
                        System.out.print("ID da pessoa a remover: ");
                        int idRemover = scanner.nextInt();
                        scanner.nextLine();

                        pessoaRepository.remover(idRemover);
                        repositoryMongo.registrarLog("Remoção", "Pessoa removida ID: " + idRemover);
                    }
                    case 5 -> {
                        System.out.println("Saindo...");
                        repositoryMongo.registrarLog("Encerramento", "Programa finalizado pelo usuário");
                    }
                    default -> {
                        System.out.println("Opção inválida!");
                        repositoryMongo.registrarErro("Erro de entrada", "Opção inválida selecionada: " + opcao);
                    }
                    case 6 -> {
                        System.out.print("ID da pessoa para visualizar conexões: ");
                        int idConsulta = scanner.nextInt();
                        scanner.nextLine();
                        repositoryNeo.listarConexoesProfissionais(idConsulta);
                        repositoryMongo.registrarLog("Consulta",
                                "Visualização de conexões da pessoa ID: " + idConsulta);
                    }

                }
            } while (opcao != 5);

            pessoaRepository.fechar();
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            try (var mongoClient = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase mongoDatabase = mongoClient.getDatabase("sistema_logs");
                RepositoryMongo repositoryMongo = new RepositoryMongo(mongoDatabase);
                repositoryMongo.registrarErro("Exceção", e.getMessage());
            } catch (Exception ex) {
                System.err.println("Falha ao registrar erro no MongoDB: " + ex.getMessage());
            }
        }
    }
}