package connection;

import services.CalculatorInterface;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

public class ClientToServerConnection {
    String address;
    String name;
    int port;
    CalculatorInterface ci;


    public ClientToServerConnection(String address, int port, String name) throws MalformedURLException, NotBoundException, RemoteException {
        this.address = address;
        this.port = port;
        this.name = name;

        this.ci = (CalculatorInterface) Naming.lookup("//" + address + ":" + port +"/" +name);
    }

    public void close() {
    }

    public CalculatorInterface getServerOperations() {
        return ci;
    }


}
