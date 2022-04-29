package repository;


import connection.ClientToServerConnection;
import model.Operation;

import java.rmi.RemoteException;

public class OperationsManager {

    private ClientToServerConnection conn;
    /*    private static OperationsManager instance = null;*/

    public OperationsManager(ClientToServerConnection conn) {
        this.conn = conn;
    }

    public Double resolve(Operation operation) throws RemoteException {
        System.out.println("OperationsManager: " + operation.getType().getName() + " " + operation);
        if (!operation.isEager()) {
            if (operation.hasX() && operation.hasY() && !(operation.getType().getName().equals("none"))) {

                switch (operation.getType().getName()) {
                    case "add": {
                        return conn.getServerOperations().add(operation.getX(), operation.getY());
                    }
                    case "substract": {
                        return conn.getServerOperations().substract(operation.getX(), operation.getY());
                    }
                    case "multiply": {
                        return conn.getServerOperations().multiply(operation.getX(), operation.getY());
                    }
                    case "divide": {
                        return conn.getServerOperations().divide(operation.getX(), operation.getY());
                    }
                    case "apowb": {
                        return conn.getServerOperations().aPowB(operation.getX(), operation.getY().intValue());
                    }
                    case "combnofk1": {
                        return (double) conn.getServerOperations().combNofK1(operation.getX().intValue(), operation.getY().intValue());
                    }
                }
            }
        } else {
            switch (operation.getType().getName()) {
                case "pow2": {
                    return conn.getServerOperations().pow2(operation.getX());
                }
                case "sqrt": {
                    return conn.getServerOperations().sqrt(operation.getX());
                }
                case "nfact": {
                    return (double) conn.getServerOperations().nFact(operation.getX().intValue());
                }
                case "percentage": {
                    return (double) conn.getServerOperations().percentage(operation.getX().intValue());
                }
                default: {
                    System.err.println("No such operation");
                    return null;
                }
            }

        }
        return null;
    }

}
