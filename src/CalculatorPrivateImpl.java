import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

//The server-side implementation of the dedicated stack provider (private)
public class CalculatorPrivateImpl extends UnicastRemoteObject implements CalculatorPrivate {
    public CalculatorPrivateImpl() throws RemoteException{
        super();
    }

    //Create and return a Calculator remote object with a dedicated stack for the current caller
    public Calculator connect() throws RemoteException{
        return new CalculatorPerClient();   // Each call will create a new CalculatorPerClient
    }
}
