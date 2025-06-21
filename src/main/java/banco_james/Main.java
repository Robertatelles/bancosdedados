package banco_james;

import banco_james.database.Postgres;
import banco_james.repository.RepositoryPostgres;
import banco_james.model.Pessoa;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            var database = Postgres.getDatabase();
            var pessoaRepository = new RepositoryPostgres(database);

            int opcao;
            do {
                System.out.println("\n==== GERENCIAMENTO DE PESSOAS ====");
                System.out.println("1. Cadastrar nova pessoa");
                System.out.println("2. Ver lista de pessoas cadastradas");
                System.out.println("3. Atualizar dados de uma pessoa");
                System.out.println("4. Excluir pessoa do sistema");
                System.out.println("5. Encerrar programa");
                System.out.print("Digite sua escolha: ");
                opcao = scanner.nextInt();
                scanner.nextLine();

                switch (opcao) {
                    case 1 -> {
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();

                        if (pessoaRepository.existeId(id)) {
                            System.out.println("⚠️ ID já cadastrado!! Tente outro.");
                            break; // volta ao menu
                        }

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

                        pessoaRepository.adicionar(new Pessoa(id, nome, email, cpf, dataNascimento, trabalho));
                    }
                    case 2 -> pessoaRepository.listar();
                    case 3 -> {
                        System.out.print("ID da pessoa a atualizar: ");
                        int idAtualizar = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Novo nome: ");
                        String novoNome = scanner.nextLine();

                        pessoaRepository.atualizar(idAtualizar, novoNome);
                    }
                    case 4 -> {
                        System.out.print("ID da pessoa a remover: ");
                        int idRemover = scanner.nextInt();
                        scanner.nextLine();
                        pessoaRepository.remover(idRemover);
                    }
                    case 5 -> System.out.println("Saindo...");
                    default -> System.out.println("Opção inválida!");
                }
            } while (opcao != 5);

            pessoaRepository.fechar();
        }
    }
}