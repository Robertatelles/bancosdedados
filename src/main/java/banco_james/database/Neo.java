package banco_james.database;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo {
    private static final String URI = "bolt://localhost:7687"; 
    private static final String USER = "neo4j";
    private static final String PASSWORD = "12345678";

    private static Driver driver;

    public static Driver getDriver() {
        if (driver == null) {
            driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
        }
        return driver;
    }

    public static void close() {
        if (driver != null) {
            driver.close();
        }
    }
}