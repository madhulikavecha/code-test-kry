package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.client.WebClient;

import java.util.HashMap;
import java.util.List;

public class BackgroundPoller {

  private WebClient client;
  private Vertx vertx;
  private DBConnector connector;

  public BackgroundPoller(Vertx vertx, DBConnector connector) {
    this.vertx = vertx;
    this.connector = connector;
  }

  public void pollServices(DBConnector connector, HashMap<String, Service> services) {
    System.out.println("polling services...");
    getDataFromDB(connector, services);
    pollServiceStatus(connector, services);
  }

  public HashMap<String, Service> getDataFromDB(DBConnector connector, HashMap<String, Service> services) {
    String getQuery = "SELECT * from service";
    connector.query(getQuery).setHandler(done -> {
      if(done.succeeded()){
        ResultSet result = done.result();
        System.out.println(result.getRows());
        List<JsonArray> urlServices = result.getResults();
        for (JsonArray urlService : urlServices) {
          Service service = new Service(urlService.getString(1),urlService.getString(2));
          service.setDate(urlService.getString(3));
          services.put(urlService.getString(0),service);
        }
      } else {
        done.cause().printStackTrace();
      }
    });
    return services;
  }

  public void pollServiceStatus(DBConnector connector, HashMap<String, Service> services) {
    client = WebClient.create(vertx);
    services.forEach((url,service) -> {
      System.out.println("url is "+url+" service name is "+service.serviceName);
      client.get(80, url, "/").send(ar -> {
          updateService(url, ar.result().statusMessage());
      });
    });
  }

  private void updateService(String url, String statusMessage) {
    String updateQuery = "UPDATE service SET status = ? WHERE url = ?;commit;";
    System.out.println("service url is "+url);
    JsonArray params = new JsonArray();
    if(!statusMessage.equalsIgnoreCase("OK")){
      statusMessage = "FAIL";
    }
    params.add(statusMessage).add(url);
    connector.update(updateQuery,params).setHandler(done -> {
      if(done.succeeded()){
        System.out.println("update successful");
      } else {
        done.cause().printStackTrace();
      }
    });
  }
}
