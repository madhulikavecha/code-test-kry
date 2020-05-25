package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, Service> services = new HashMap<>();
  private DBConnector connector;
  DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  BackgroundPoller bgPoller;

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    Service service = new Service("kryService", "UNKNOWN");
    Date dateObj = new Date();
    service.setDate(df.format(dateObj));
    services.put("www.kry.se", service);
    bgPoller = new BackgroundPoller(vertx,connector);
    vertx.setPeriodic(1000 * 60, timerId -> bgPoller.pollServices(connector, services));
    services = bgPoller.getDataFromDB(connector, services);
    setRoutes(router);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());

    router.get("/service").handler(req -> {
      connector = new DBConnector(vertx);
      bgPoller = new BackgroundPoller(vertx,connector);
      services = bgPoller.getDataFromDB(connector, services);
      List<JsonObject> jsonServices = services
          .entrySet()
          .stream()
          .map(service ->
              new JsonObject()
                  .put("url", service.getKey())
                  .put("name", service.getValue().serviceName)
                  .put("status", service.getValue().serviceStatus)
                  .put("date", service.getValue().date))
          .collect(Collectors.toList());
      System.out.println(jsonServices);
      req.response()
          .putHeader("content-type", "application/json")
          .end(new JsonArray(jsonServices).encode());
    });

    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      Service service = new Service(jsonBody.getString("name"), "SERVICE CREATED");
      insertDataIntoDB(jsonBody.getString("url"), service);
      req.response()
          .putHeader("content-type", "text/plain")
          .end("OK");
    });

    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      deleteDataFromDB(jsonBody.getString("name"));
      req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
    });
  }

  private void insertDataIntoDB(String url, Service service) {
    JsonArray params = new JsonArray();
    Date dateObj = new Date();
    service.setDate(df.format(dateObj));
    services.put(url, service);
    params.add(url).add(service.serviceName).add("CREATED").add(service.date);
    String insertQuery = "INSERT INTO service (url, name, status, created_at) VALUES (?,?,?,?);commit;";
    Vertx vertx = Vertx.vertx();
    connector = new DBConnector(vertx);
    connector.update(insertQuery, params).setHandler(done -> {
      if(done.succeeded()){
        System.out.println("inserted");
      } else {
        done.cause().printStackTrace();
      }
    });
  }

  private void deleteDataFromDB(String serviceurl) {
    String deleteQuery = "DELETE FROM service WHERE url=?;commit;";
    System.out.println("service url is "+serviceurl);
    JsonArray params = new JsonArray();
    params.add(serviceurl);
    connector.update(deleteQuery,params).setHandler(done -> {
      if(done.succeeded()){
        System.out.println("delete successful");
        services.remove(serviceurl);
      } else {
        done.cause().printStackTrace();
      }
    });
  }
}



