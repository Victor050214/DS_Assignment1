import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Calculator extends Remote {

    // push a value to the top of the shared stack
    void pushValue(int val) throws RemoteException;

    // apply the operator, and push the result
    void pushOperation(String operator) throws RemoteException;

    //pop and return the top value
    int pop() throws RemoteException;

    //check whether the stack is empty or not
    boolean isEmpty() throws RemoteException;

    //waiting for the given millis
    int delayPop(int millis) throws RemoteException;
}
