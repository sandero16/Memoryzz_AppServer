package sample;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
public class Main {
    private void startServer(DBServerInterface stub) {
        try {
            Scanner scanner=new Scanner(System.in);
            System.out.println("Geef poortnummer: ");
            int poortnummer=scanner.nextInt();
            Registry registry = LocateRegistry.createRegistry(poortnummer);
            registry.rebind("Login", new AppImpl(stub, poortnummer));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("system is ready");
    }
    public static void main(String[] args) {
        Main main = new Main();
        DBServerInterface stub = main.connectToDatabase();
        main.startServer(stub);
    }

    private DBServerInterface connectToDatabase() {
        DBServerInterface stub = null;
        try {
            // fire to localhost port 2000
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 2000);

            // search for DBService
            stub = (DBServerInterface) myRegistry.lookup("DBService");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("connected with database server");
        return stub;
    }
}