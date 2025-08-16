import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

// Start the local RMI Registry， and Bind two remote objects, one for shared, one for dedicated

public class CalculatorServer {
    // RMI Registry port
    public static final int REGISTRY_PORT = 1099;

    // bind name for shared stack
    public static final String BIND_NAME = "Calculator";

    // bind name for dedicated stack
    public static final String PRIVATE_BIND_NAME = "CalculatorPrivate";

    // RMI URL for shared
    public static final String URL = "rmi://localhost:" + REGISTRY_PORT + "/" + BIND_NAME;

    // RMI URL for dedicated
    public static final String PRIVATE_URL = "rmi://localhost:" + REGISTRY_PORT + "/" + PRIVATE_BIND_NAME;

    public static void main(String[] args) {
        try{
            try{
                LocateRegistry.createRegistry(REGISTRY_PORT);
                System.out.println("RMI registry created on the port " + REGISTRY_PORT);
            } catch (Exception e) {
                System.out.println("Registry may already be running: "+ e.getMessage());
            }

            // Bind the remote object of the shared stack
            Calculator shared = new CalculatorImplementation();
            Naming.rebind(URL, shared);
            System.out.println("Calculator (shared) bound at " + URL);

            // Bind the remote object of the dedicated stack
            CalculatorPrivate factory = new CalculatorPrivateImpl();
            Naming.rebind(PRIVATE_URL, factory);
            System.out.println("CalculatorPrivate (per-client stacks) bound at " + PRIVATE_URL);

            System.out.println("Server is ready");
        } catch (Exception e) {
            System.err.println("Server error: " + e);
            e.printStackTrace();
        }
    }
}
