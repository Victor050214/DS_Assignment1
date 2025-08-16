import java.rmi.Remote;
import java.rmi.RemoteException;

//create and return a Calculator remote object with a stack for the current caller's own
public interface CalculatorPrivate extends Remote {
    Calculator connect() throws RemoteException;
}
