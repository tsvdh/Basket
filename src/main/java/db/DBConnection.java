package db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.lang.Nullable;
import org.bson.Document;

public class DBConnection {

    private static final class InstanceHolder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private @Nullable MongoDatabase database;

    private DBConnection() {
        // disable unimportant logging
        // LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        // Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        // rootLogger.setLevel(Level.WARN);

        this.connect();
    }

    public void connect() {
        database = getDatabase();
    }

    private static @Nullable MongoDatabase getDatabase() {
        String address = "mongodb+srv://user:iqfNJTKxN5SRxkTa@cluster0.mklnk.mongodb.net"
                + "/Cluster0?retryWrites=true&w=majority";
        MongoClientSettings settings;
        try {
            ConnectionString connectionString = new ConnectionString(address);
            settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
        } catch (Exception e) {
            return null;
            // throw new DBConnectException("Error when trying to connect to the database", e);
        }
        return MongoClients.create(settings).getDatabase("apps");
    }

    public Iterable<Document> getAppInfo() throws DBConnectException {
        if (this.database == null) {
            throw new DBConnectException("Failed to establish connection");
        }
        try {
            return this.database.getCollection("info").find();
        } catch (Exception e) {
            throw new DBConnectException("Connection failed after successful creation", e);
        }
    }
}
