package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);
    connector.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) NOT NULL, name VARCHAR(128), status VARCHAR(128), created_at DATETIME)").setHandler(done -> {
      if(done.succeeded()){
        System.out.println("completed db migrations");
      } else {
        done.cause().printStackTrace();
      }
    });
  }
}
