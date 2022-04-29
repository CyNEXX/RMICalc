package services;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Calculator extends UnicastRemoteObject implements CalculatorInterface {
    public Calculator() throws RemoteException {
        int a, b;
    }


    @Override
    public double add(double a, double b) throws RemoteException {
        return a + b;
    }

    @Override
    public double substract(double a, double b) throws RemoteException {
        return a - b;
    }

    @Override
    public double multiply(double a, double b) throws RemoteException {
        return a * b;
    }

    @Override
    public double divide(double a, double b) throws RemoteException {
        return a / b;
    }

    @Override
    public double pow2(double a) throws RemoteException {
        return Math.pow(a, 2);
    }

    @Override
    public double aPowB(double a, int b) throws RemoteException {
        return Math.pow(a, b);
    }

    @Override
    public double sqrt(double a) throws RemoteException {
        return Math.sqrt(a);
    }

    @Override
    public int nFact(int n) throws RemoteException {
        return recursiveNFact(n);
    }

    @Override
    public int combNofK1(int n, int k) throws RemoteException {
        return recursiveComb1(n, k);
    }

    @Override
    public int combNofK2(int n, int k) throws RemoteException {
        return recursiveComb2(n, k);
    }

    @Override
    public double percentage(double a) throws RemoteException {
        return a / 100;
    }

    private int recursiveNFact(int n) {
        if (n <= 1) return 1;
        return n * recursiveNFact(n - 1);
    }

    private int recursiveComb1(int n, int k) {
        if (n == k || k == 0) return 1;
        return recursiveComb1(n - 1, k - 1) + recursiveComb1(n - 1, k);
    }

    private int recursiveComb2(int n, int k) {
        if (n == k || k == 0) return 1;
        return (n) * recursiveComb2(n - 1, k - 1) / k;
    }
}
