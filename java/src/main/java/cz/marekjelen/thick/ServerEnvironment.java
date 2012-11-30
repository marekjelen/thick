package cz.marekjelen.thick;

public class ServerEnvironment {

    private String address;
    private int port;
    private ServerRubyInterface application;

    public ServerRubyInterface getApplication() {
        return application;
    }

    public void setApplication(ServerRubyInterface application) {
        this.application = application;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
