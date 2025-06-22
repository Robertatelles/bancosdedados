package banco_james.repository;

import banco_james.database.Redis;
import banco_james.database.Postgres;
import banco_james.model.Pessoa;

import com.google.gson.Gson;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepositoryPostgres {
    private final Connection connection;
    private final Redis redis;
    private final Gson gson;
    private final String CACHE_KEY = "pessoas:listar";

    public RepositoryPostgres(Connection connection) {
        this.connection = connection;
        this.redis = new Redis();
        this.gson = new Gson();
    }

    public void adicionar(Pessoa pessoa) {
        long start = System.currentTimeMillis();

        String sql = "INSERT INTO pessoas (id, nome, email, cpf, data_nascimento, trabalho) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pessoa.getId());
            stmt.setString(2, pessoa.getNome());
            stmt.setString(3, pessoa.getEmail());
            stmt.setString(4, pessoa.getCpf());
            stmt.setString(5, pessoa.getDataNascimento());
            stmt.setString(6, pessoa.getTrabalho());
            stmt.executeUpdate();
            redis.del(CACHE_KEY);
            System.out.println("Pessoa cadastrada com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Tempo de execução (ADICIONAR): " + duration + " ms");
    }

    public void listar() {
        long start = System.currentTimeMillis();
        String jsonCache = redis.get(CACHE_KEY);

        if (jsonCache != null) {
            Pessoa[] pessoas = gson.fromJson(jsonCache, Pessoa[].class);
            for (Pessoa p : pessoas) {
                exibir(p);
            }
        } else {
            List<Pessoa> pessoaList = new ArrayList<>();
            String sql = "SELECT id, nome, email, cpf, data_nascimento, trabalho FROM pessoas";

            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Pessoa pessoa = new Pessoa(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            rs.getString("cpf"),
                            rs.getString("data_nascimento"),
                            rs.getString("trabalho"));
                    pessoaList.add(pessoa);
                }

                String json = gson.toJson(pessoaList);
                redis.set(CACHE_KEY, json);

                for (Pessoa p : pessoaList) {
                    exibir(p);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Tempo de execução (LISTAR): " + duration + " ms");
    }

    public void atualizar(int id, String novoNome) {
        long start = System.currentTimeMillis();
        String sql = "UPDATE pessoas SET nome = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, novoNome);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            redis.del(CACHE_KEY);
            System.out.println("Pessoa atualizada com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Tempo de execução (ATUALIZAR): " + duration + " ms");
    }

    public void remover(int id) {
        long start = System.currentTimeMillis();
        String sql = "DELETE FROM pessoas WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            redis.del(CACHE_KEY);
            System.out.println("Pessoa removida com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Tempo de execução (REMOVER): " + duration + " ms");
    }

    public void fechar() {
        redis.close();
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exibir(Pessoa p) {
        System.out.println("ID: " + p.getId());
        System.out.println("Nome: " + p.getNome());
        System.out.println("Email: " + p.getEmail());
        System.out.println("CPF: " + p.getCpf());
        System.out.println("Nascimento: " + p.getDataNascimento());
        System.out.println("Trabalho: " + p.getTrabalho());
        System.out.println("-----------------------");
    }
    public boolean existeId(String cpf) {
    String sql = "SELECT 1 FROM pessoas WHERE cpf = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, cpf);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
}