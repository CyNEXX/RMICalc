package server;

import services.Calculator;
import services.CalculatorInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;


public class ServerRMI {
    public static void start() {

        Registry r = null;
        try {
            tests.Calculator.test(new Calculator());
            r = LocateRegistry.createRegistry(5000);
            r.rebind("Calculator", new Calculator());

            System.out.println("Server running");
        } catch (RemoteException e) {
            System.out.println("Server RMI error");
            e.printStackTrace();
        }

    }


}

