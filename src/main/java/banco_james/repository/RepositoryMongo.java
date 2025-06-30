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

    public void listarLogs() {
        System.out.println("\n📋 REGISTROS NO MONGODB:\n");

        for (Document doc : collection.find()) {
            String timestamp = doc.getString("timestamp");
            String acao = doc.getString("acao");
            String erro = doc.getString("erro");
            String detalhes = doc.getString("detalhes");

            if (acao != null) {
                System.out.println("🟢 [AÇÃO]");
                System.out.println("   📅 Data: " + timestamp);
                System.out.println("   📝 Descrição: " + acao);
                System.out.println("   ℹ️ Detalhes: " + detalhes);
                System.out.println("--------------------------------------");
            } else if (erro != null) {
                System.out.println("🔴 [ERRO]");
                System.out.println("   📅 Data: " + timestamp);
                System.out.println("   ❌ Tipo: " + erro);
                System.out.println("   ℹ️ Detalhes: " + detalhes);
                System.out.println("--------------------------------------");
            } else {
                System.out.println("🔍 [OUTRO REGISTRO]");
                System.out.println("   " + doc.toJson());
                System.out.println("--------------------------------------");
            }
        }
    }
}
