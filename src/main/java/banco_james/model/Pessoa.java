package banco_james.model;

public class Pessoa {
    private int id;
    private String nome;
    private String email;
    private String cpf;
    private String dataNascimento;
    private String amizade;

public Pessoa(int id, String nome, String email, String cpf, String dataNascimento, String amizade) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.amizade = amizade;
    }
        public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getAmizade() {
        return amizade;
    }
}