package banco_james.menu;

import banco_james.model.Pessoa;
import banco_james.repository.*;
import banco_james.database.Redis;

import java.util.Scanner;

import com.google.gson.Gson;

public class MenuPessoa {

    public static void exibirMenu() {
        System.out.println("\n==== GERENCIAMENTO DE PESSOAS ====");
        System.out.println("1. Cadastrar nova pessoa");
        System.out.println("2. Listar pessoas");
        System.out.println("3. Atualizar pessoa");
        System.out.println("4. Remover pessoa");
        System.out.println("5. Buscar pessoa por CPF (com cache Redis)");
        System.out.println("6. Ver registros de log no MongoDB");
        System.out.println("7. Ver todas as conexões no grafo (Neo4j)");
        System.out.println("8. Sair");

        System.out.print("Escolha: ");
    }

    public static void executarOpcao(
            int opcao,
            Scanner scanner,
            RepositoryPostgres repoPostgres,
            RepositoryMongo repoMongo,
            RepositoryNeo repoNeo,
            Redis redis) {
        switch (opcao) {
            case 1 -> cadastrar(scanner, repoPostgres, repoMongo, repoNeo);
            case 2 -> {
                repoPostgres.listar();
                repoMongo.registrarLog("Consulta", "Listagem de pessoas realizada");
            }
            case 3 -> atualizar(scanner, repoPostgres, repoMongo);
            case 4 -> remover(scanner, repoPostgres, repoMongo);
            case 5 -> buscarPessoaPorCpf(scanner, repoPostgres, repoMongo, redis);

            default -> {
                System.out.println("Opção inválida!");
                repoMongo.registrarErro("Erro de entrada", "Opção inválida selecionada: " + opcao);
            }
            case 6 -> repoMongo.listarLogs();
            case 7 -> {
                repoNeo.listarTodasConexoes();
                repoMongo.registrarLog("Consulta", "Listagem geral de conexões");
            }
            case 8 -> repoMongo.registrarLog("Encerramento", "Sistema encerrado");
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

            System.out.print("Faculdade: ");
            String amizade = sc.nextLine();

            Pessoa pessoa = new Pessoa(id, nome, email, cpf, nascimento, amizade);

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

            Pessoa atual = repo.buscarPorId(id);

            if (atual == null) {
                System.out.println("❌ Pessoa com ID " + id + " não encontrada.");
                log.registrarErro("Atualização", "Pessoa ID " + id + " não localizada");
                return;
            }

            System.out.println("\n📄 Dados atuais:");
            System.out.println("1. Nome: " + atual.getNome());
            System.out.println("2. Email: " + atual.getEmail());
            System.out.println("3. CPF: " + atual.getCpf());
            System.out.println("4. Nascimento: " + atual.getDataNascimento());
            System.out.println("5. Amizade: " + atual.getAmizade());

            System.out.print("\nDigite o número do campo que deseja atualizar: ");
            String opcao = sc.nextLine();

            String campo = null;
            switch (opcao) {
                case "1" -> campo = "nome";
                case "2" -> campo = "email";
                case "3" -> campo = "cpf";
                case "4" -> campo = "data_nascimento";
                case "5" -> campo = "amizade";
                default -> {
                    System.out.println("❌ Opção inválida.");
                    return;
                }
            }

            System.out.print("Novo valor para " + campo + ": ");
            String novoValor = sc.nextLine();

            boolean ok = repo.atualizarCampo(id, campo, novoValor);

            if (ok) {
                log.registrarLog("Atualização", "Pessoa ID " + id + " teve o campo " + campo + " alterado");
                System.out.println("✅ Atualização realizada com sucesso!");
            } else {
                System.out.println("❌ Falha ao atualizar o campo.");
                log.registrarErro("Atualização", "Erro ao atualizar campo " + campo + " da pessoa ID " + id);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro inesperado: " + e.getMessage());
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

    

    private static void exibirPessoa(Pessoa p) {
    System.out.println("\n📄 Dados da Pessoa:");
    System.out.println("ID: " + p.getId());
    System.out.println("Nome: " + p.getNome());
    System.out.println("Email: " + p.getEmail());
    System.out.println("CPF: " + p.getCpf());
    System.out.println("Nascimento: " + p.getDataNascimento());
    System.out.println("Amizade: " + p.getAmizade());
    System.out.println("------------------------------");
}
private static void buscarPessoaPorCpf(Scanner sc, RepositoryPostgres repoPostgres, RepositoryMongo repoMongo, Redis redis) {
    System.out.print("🔍 Digite o CPF: ");
    String cpf = sc.nextLine();

    long start = System.currentTimeMillis(); // Início do cronômetro

    String cache = redis.get("cpf:" + cpf);

    if (cache != null) {
        Pessoa pessoa = new Gson().fromJson(cache, Pessoa.class);
        System.out.println("🧠 Pessoa encontrada no Redis:");
        exibirPessoa(pessoa);
        repoMongo.registrarLog("Busca por CPF (Redis)", "CPF " + cpf + " carregado do cache");
    } else {
        Pessoa pessoa = repoPostgres.buscarPorCpf(cpf);
        if (pessoa != null) {
            System.out.println("📦 Pessoa encontrada no PostgreSQL:");
            exibirPessoa(pessoa);
            redis.set("cpf:" + cpf, new Gson().toJson(pessoa));
            repoMongo.registrarLog("Busca por CPF (PostgreSQL)", "CPF " + cpf + " salvo no cache");
        } else {
            System.out.println("❌ Pessoa não encontrada.");
            repoMongo.registrarErro("Consulta CPF", "CPF " + cpf + " não localizado");
        }
    }

    long duration = System.currentTimeMillis() - start;
    System.out.println("⏱️ Tempo de resposta: " + duration + " ms");
}
}