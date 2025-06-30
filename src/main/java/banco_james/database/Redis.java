package banco_james.database;

import redis.clients.jedis.Jedis;

public class Redis {
    private Jedis jedis;

    public Redis() {
       
        this.jedis = new Jedis("localhost", 6379);
    }

    public String get(String key) {
        return jedis.get(key);
    }

    public void set(String key, String value) {
        jedis.set(key, value);
    }

    public void del(String key) {
        jedis.del(key);
    }

    public void close() {
        jedis.close();
    }
    public String ping() {
    return jedis.ping();
}
}
