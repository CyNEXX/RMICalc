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

    /**
     * Creates the connection to the remote object
     * @param address The address to connect
     * @param port The port to connect through
     * @param name The name of the remote object
     * @throws MalformedURLException
     * @throws NotBoundException
     * @throws RemoteException
     */
    public ClientToServerConnection(String address, int port, String name) throws MalformedURLException, NotBoundException, RemoteException {
        this.address = address;
        this.port = port;
        this.name = name;

        this.ci = (CalculatorInterface) Naming.lookup("//" + address + ":" + port +"/" +name);
    }

    public void close() {
    }

    /**
     * Exposes the remote object interface
     * @return The interface that's used to access server operations
     */
    public CalculatorInterface getServerOperations() {
        return ci;
    }


}
