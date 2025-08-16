import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;

// the server side of the RMI remote object
public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {
    private static final int INIT_GCD = 0;  // initial value for gcd
    private static final int INIT_LCM = 1;  // initial value for lcm

    // Shared stack: All clients operate on the same shared instance
    private final Stack<Integer> stack = new Stack<Integer>();

    // Fair lock (true indicates fairness): Whoever waits longer is more likely to obtain the lock first
    private final ReentrantLock lock = new ReentrantLock(true);

    //pop the remote object
    public CalculatorImplementation() throws RemoteException{
        super();
    }

    // Push an integer onto the top of the stack
    public void pushValue(int val) throws RemoteException {
        lock.lock();
        try {
            stack.push(val);
        } finally {
            lock.unlock();
        }
    }

    // Push an operator, which triggers: pop up the all integers in the current stack, Then push the result back to the top of the stack.
    public void pushOperation(String operator) throws RemoteException{
        String op = (operator == null)? "": operator.trim().toLowerCase();
        lock.lock();

        try{
            if(stack.empty()){
                throw new RemoteException("pushOperation called on empty stack");
            }

            // min
            if(op.equals("min")) {
                int m = Integer.MAX_VALUE;
                while (!stack.empty()){
                    int v = stack.pop();
                    if(v<m) m = v;
                }
                stack.push(m);
                return;
            }

            // max
            if (op.equals("max")) {
                int m = Integer.MIN_VALUE;
                while (!stack.empty()) {
                    int v = stack.pop();
                    if (v > m) m = v;
                }
                stack.push(m);
                return;
            }

            // greatest common divisor
            if (op.equals("gcd")) {
                int g = INIT_GCD; // gcd(0, x) = |x|
                while (!stack.empty()) {
                    int v = stack.pop();
                    g = gcd(g, v);
                }
                if (g < 0) g = -g;
                stack.push(g);
                return;
            }

            // lease common multiple
            if (op.equals("lcm")) {
                int l = INIT_LCM; // lcm(1, x) = x
                while (!stack.empty()) {
                    int v = stack.pop();
                    l = lcm(l, v);
                }
                if (l < 0) l = -l;
                stack.push(l);
                return;
            }

            throw new RemoteException("Unknown operator: " + operator + "（use min|max|gcd|lcm)");
        } finally {
            lock.unlock();
        }
    }

    //pop and return the top value
    public int pop() throws RemoteException{
        lock.lock();
        try {
            if (stack.empty()) {
                throw new RemoteException("pop called on empty stack");
            }
            return stack.pop();
        } finally {
            lock.unlock();
        }
    }

    //check whether the stack is empty or not
    public boolean isEmpty() throws RemoteException{
        lock.lock();
        try {
            return stack.empty();
        } finally {
            lock.unlock();
        }
    }

    //waiting for the given millis
    public int delayPop(int millis) throws RemoteException{
        int wait = Math.max(0, millis);
        try{
            Thread.sleep(wait);
            return pop();
        } catch (InterruptedException e) {
            throw new RemoteException("delayPop interrupted", e);
        }
    }

    //method of gcd
    private static int gcd(int a, int b){
        if(a<0) a = -a;
        if(b<0) b = -b;
        if(a == 0) return b;
        if(b == 0) return a;

        while (b != 0 ) {
            int temporary = a % b;
            a = b;
            b = temporary;
        }

        return a;
    }

    //method of lcm
    private static int lcm(int a, int b){
        if(a == 0 || b == 0) {
            return 0;
        }

        int g = gcd(a,b);
        return (a / g) * b;
    }

}
