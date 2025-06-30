package banco_james.model;

public class Pessoa {
    private int id;
    private String nome;
    private String email;
    private int cpf;
    private String dataNascimento;
    private String trabalho;

public Pessoa(int id, String nome, String email, int cpf, String dataNascimento, String trabalho) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.trabalho = trabalho;
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

    public int getCpf() {
        return cpf;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getTrabalho() {
        return trabalho;
    }
}