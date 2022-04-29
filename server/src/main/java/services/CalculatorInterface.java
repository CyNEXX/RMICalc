package services;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculatorInterface extends Remote {
    double add(double a, double b) throws RemoteException;

    double substract(double a, double b) throws RemoteException;

    double multiply(double a, double b) throws RemoteException;

    double divide(double a, double b) throws RemoteException;

    double pow2(double a) throws RemoteException;

    double aPowB(double a, int b) throws RemoteException;

    double sqrt(double a) throws RemoteException;

    int nFact(int n) throws RemoteException;

    int  combNofK1(int n, int k) throws RemoteException;

    int combNofK2(int n, int k) throws RemoteException;

    double percentage(double a) throws RemoteException;
}
