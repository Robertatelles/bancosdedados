package banco_james.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RepositoryMongo {

    private final MongoCollection<Document> collection;

    public RepositoryMongo(MongoDatabase database) {
        this.collection = database.getCollection("logs");
    }

    public void registrarLog(String acao, String detalhes) {
        Document log = new Document()
                .append("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("acao", acao)
                .append("detalhes", detalhes);

        collection.insertOne(log);
        System.out.println("[LOG] Registro salvo no MongoDB.");
    }

    public void registrarErro(String erro, String detalhes) {
        Document log = new Document()
                .append("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("erro", erro)
                .append("detalhes", detalhes);

        collection.insertOne(log);
        System.err.println("[LOG ERRO] Registro de erro salvo no MongoDB.");
    }
} 
