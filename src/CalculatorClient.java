import java.rmi.Naming;

// Demo client for the shared-stack Calculator RMI service.

public class CalculatorClient {
    public static void main(String[] args){
        try{
            // Obtain a remote reference (stub) to the shared Calculator.
            Calculator cal = (Calculator) Naming.lookup(CalculatorServer.URL);

            // Make sure the stack starts empty so previous runs don't affect results.
            while (!cal.isEmpty()){
                cal.pop();
            }

            cal.pushValue(0);
            cal.pushValue(11);
            cal.pushValue(-9);
            cal.pushOperation("max");
            System.out.println("max result = " + cal.pop());

            cal.pushValue(18);
            cal.pushValue(30);
            cal.pushValue(42);
            cal.pushOperation("gcd");
            System.out.println("delayPop(3000) = " + cal.delayPop(3000));
            System.out.println("Empty or not = " + cal.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
