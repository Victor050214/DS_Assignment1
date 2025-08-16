import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Stack;

// Each client has his own stack implementation of Calculator
public class CalculatorPerClient extends UnicastRemoteObject implements Calculator {
    private final Stack<Integer> stack = new Stack<Integer>();

    private static final int MIN_DELAY_MS = 0;

    public CalculatorPerClient() throws RemoteException{
        super();
    }

    // Push an integer onto the stack top exclusive to the current instance
    public void pushValue(int val) throws RemoteException{
        // Ensure that the same instance is safe in a multi-threads
        synchronized (stack){
            stack.push(val);
        }
    }

    public void pushOperation(String operator) throws RemoteException{
        String op = (operator == null) ? "": operator.trim().toLowerCase();

        synchronized (stack) {
            if (stack.empty()) {
                throw new RemoteException("pushOperation on empty stack");
            }

            if(op.equals("min")){
                int m = Integer.MAX_VALUE;
                while (!stack.empty()){
                    m = Math.min(m, stack.pop());
                }
                stack.push(m);
                return;
            }

            if(op.equals("max")){
                int m = Integer.MIN_VALUE;
                while (!stack.empty()){
                    m = Math.max(m, stack.pop());
                }
                stack.push(m);
                return;
            }

            if(op.equals("gcd")) {
                int g = 0;
                while (!stack.empty()){
                    g= gcd(g, stack.pop());
                }
                stack.push(Math.abs(g));
                return;
            }

            if(op.equals("lcm")) {
                int l = 1;
                while (!stack.empty()){
                    l= lcm(l, stack.pop());
                }
                stack.push(Math.abs(l));
                return;
            }

            throw new RemoteException("Unknown operator: " + operator + "(use min|max|gcd|lcm)");
        }
    }

    public int pop() throws RemoteException{
        synchronized (stack){
            if(stack.empty()) throw new RemoteException("pop on empty stack");
            return stack.pop();
        }
    }

    public boolean isEmpty() throws RemoteException{
        synchronized (stack) {
            return stack.empty();
        }
    }

    public int delayPop(int millis) throws RemoteException{
        int wait = Math.max(MIN_DELAY_MS, millis);
        try {
            Thread.sleep(wait);
            return pop();
        } catch (InterruptedException ie){
            Thread.currentThread().interrupt();
            throw new RemoteException("delayPop is interrupted", ie);
        }
    }

    private static int gcd(int a, int b){
        a = Math.abs(a);
        b = Math.abs(b);
        if(a==0) return b;
        if(b==0) return a;
        while (b != 0){
            int temporary = a%b;
            a=b;
            b=temporary;
        }
        return a;
    }

    private static int lcm(int a, int b){
        if(a == 0 || b == 0) return 0;
        int g = gcd(a,b);
        return (a / g)*b;
    }
}
