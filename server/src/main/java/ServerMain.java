import server.ServerRMI;

public class ServerMain {
    public static void main(String ...args) {
        System.out.println("Starting server...");
        ServerRMI.start();
    };
}
