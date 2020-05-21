package se.kry.codetest;

public class Service {
    String serviceName;
    String serviceStatus;
    String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Service(String serviceName, String serviceStatus) {
        this.serviceName = serviceName;
        this.serviceStatus = serviceStatus;
    }

    public String getServiceName() {
        return serviceName;
    }
    public String getServiceStatus() {
        return serviceStatus;
    }

}
