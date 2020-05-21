package se.kry.codetest;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class BackgroundPoller {

  public void pollServices(DBConnector connector, HashMap<String, Service> services) {
    System.out.println("printing in poller");
    getDataFromDB(connector, services);
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
}
