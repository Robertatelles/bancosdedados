package banco_james.menu;

import banco_james.model.Pessoa;
import banco_james.repository.*;


import java.util.Scanner;

public class MenuPessoa {

    public static void exibirMenu() {
        System.out.println("\n==== GERENCIAMENTO DE PESSOAS ====");
        System.out.println("1. Cadastrar nova pessoa");
        System.out.println("2. Listar pessoas");
        System.out.println("3. Atualizar pessoa");
        System.out.println("4. Remover pessoa");
        System.out.println("5. Sair");
        System.out.println("6. Ver conexões profissionais");
        System.out.println("7. Ver registros de log no MongoDB");
        System.out.print("Escolha: ");
    }

    public static void executarOpcao(
            int opcao,
            Scanner scanner,
            RepositoryPostgres repoPostgres,
            RepositoryMongo repoMongo,
            RepositoryNeo repoNeo
    ) {
        switch (opcao) {
            case 1 -> cadastrar(scanner, repoPostgres, repoMongo, repoNeo);
            case 2 -> {
                repoPostgres.listar();
                repoMongo.registrarLog("Consulta", "Listagem de pessoas realizada");
            }
            case 3 -> atualizar(scanner, repoPostgres, repoMongo);
            case 4 -> remover(scanner, repoPostgres, repoMongo);
            case 5 -> repoMongo.registrarLog("Encerramento", "Sistema encerrado");
            case 6 -> verConexoes(scanner, repoNeo, repoMongo);
            case 7 -> repoMongo.listarLogs();

            default -> {
                System.out.println("Opção inválida!");
                repoMongo.registrarErro("Erro de entrada", "Opção inválida selecionada: " + opcao);
            }
        }
    }

    private static void cadastrar(Scanner sc, RepositoryPostgres repo, RepositoryMongo log, RepositoryNeo neo) {
        try {
            System.out.print("ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Nome: ");
            String nome = sc.nextLine();

            System.out.print("Email: ");
            String email = sc.nextLine();

            System.out.print("CPF: ");
            String cpf = sc.nextLine();

            System.out.print("Data de nascimento (AAAA-MM-DD): ");
            String nascimento = sc.nextLine();

            System.out.print("Trabalho: ");
            String trabalho = sc.nextLine();

            Pessoa pessoa = new Pessoa(id, nome, email, cpf, nascimento, trabalho);

            repo.adicionar(pessoa);
            log.registrarLog("Cadastro", "Pessoa adicionada: " + nome);
            neo.adicionarPessoa(pessoa);

            System.out.println("✅ Pessoa cadastrada com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao cadastrar pessoa: " + e.getMessage());
            log.registrarErro("Cadastro", e.getMessage());
        }
    }

    private static void atualizar(Scanner sc, RepositoryPostgres repo, RepositoryMongo log) {
        try {
            System.out.print("ID da pessoa a atualizar: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Novo nome: ");
            String novoNome = sc.nextLine();

            repo.atualizar(id, novoNome);
            log.registrarLog("Atualização", "Pessoa ID " + id + " atualizada para nome: " + novoNome);

            System.out.println("✅ Pessoa atualizada!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao atualizar: " + e.getMessage());
            log.registrarErro("Atualização", e.getMessage());
        }
    }

    private static void remover(Scanner sc, RepositoryPostgres repo, RepositoryMongo log) {
        try {
            System.out.print("ID da pessoa a remover: ");
            int id = Integer.parseInt(sc.nextLine());

            repo.remover(id);
            log.registrarLog("Remoção", "Pessoa removida ID: " + id);

            System.out.println("✅ Pessoa removida!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao remover: " + e.getMessage());
            log.registrarErro("Remoção", e.getMessage());
        }
    }

    private static void verConexoes(Scanner sc, RepositoryNeo neo, RepositoryMongo log) {
        try {
            System.out.print("ID da pessoa para visualizar conexões: ");
            int idConsulta = Integer.parseInt(sc.nextLine());

            neo.listarConexoesProfissionais(idConsulta);
            log.registrarLog("Consulta", "Visualização de conexões da pessoa ID: " + idConsulta);

            System.out.println("✅ Conexões listadas com sucesso para o ID " + idConsulta + ".");

        } catch (NumberFormatException e) {
            System.out.println("❌ ID inválido. Por favor, digite um número inteiro.");
        } catch (Exception e) {
            System.out.println("⚠️ Ocorreu um erro ao buscar conexões: " + e.getMessage());
            log.registrarErro("Consulta", e.getMessage());
        }
    }
}