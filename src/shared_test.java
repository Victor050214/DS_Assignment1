import java.rmi.Naming;

// test for All clients share the same stack

public class shared_test {
    private static final String URL = CalculatorServer.URL;
    private static final int N_CLIENTS = 4;
    private static final int BASE_VALUE = 100;
    private static final int DELAY_MS = 120;

    //The waiting time for a single delayPop
    private static final int SINGLE_DELAY_MS = 80;

    public static void main(String[] args) throws  Exception {
         // receive the remote object on the shared stack
        Calculator cal = (Calculator) Naming.lookup(URL);

        int success = 0;
        int fail = 0;
        try{
            testSharedStackAcrossClients(cal);
            success++;
        } catch (Throwable t) {
            fail("shared-stack across clients", t);
            fail++;
        }

        try{
            testSingleClient_AllMethods(cal);
            success++;
        } catch (Throwable t) {
            fail("single client all methods", t);
            fail++;
        }

        try{
            testMultiClient_PushThenMax();
            success++;
        } catch (Throwable t) {
            fail("multi client push + max", t);
            fail++;
        }

        try{
            testMultiClient_PopPermutation(cal);
            success++;
        } catch (Throwable t) {
            fail("multi client pop permutation", t);
            fail++;
        }

        try{
            testMultiClient_DelayPopBurst(cal);
            success++;
        } catch (Throwable t) {
            fail("multi client delayPop burst", t);
            fail++;
        }

        System.out.println("\nRESULT: success=" + success + " fail=" + fail);
        System.exit(fail == 0 ? 0 : 1);
    }

    private static void assertTrue(boolean cond, String msg){
        if(!cond) throw new AssertionError(msg);
    }

    private static void assertEquals(int expected, int actual, String msg){
        if(expected != actual){
            throw new AssertionError(msg+"(expected " + expected + ", got " + actual + ")");
        }
    }

    private static void fail(String name, Throwable t){
        System.out.println("[FAIL] " + name + ": " + t);
        t.printStackTrace(System.out);
    }

    private static void clearStack(Calculator cal) throws Exception{
        while(!cal.isEmpty()){
            try{
                cal.pop();
            } catch (Exception ignored) {
                break;
            }
        }
    }

    // Checks whether the given array contains exactly the integers in the
    // continuous range [start, start + n - 1], each appearing **once** in any order.
    private static boolean equalsExactRange(int[] a, int start, int n){
        if(a.length!=n) return false;
        boolean[] seen = new boolean[n];
        for(int v: a){
            if(v < start || v >= start+n) return false;
            int index = v - start;
            if(seen[index]) return false;
            seen[index] = true;
        }

        for(int i = 0; i < n; i++){
            if(!seen[i]) return false;
        }

        return true;
    }

    // Case 1: Verify the shared stack
    private static void testSharedStackAcrossClients(Calculator cal) throws Exception{
        clearStack(cal);

        Calculator a = cal;
        Calculator b = (Calculator) Naming.lookup(URL);

        a.pushValue(42);
        int got = b.pop();
        assertEquals(42, got, "shared stack pop via another client");
        assertTrue(b.isEmpty(), "stack should be empty after cross-client pop");

        System.out.println("[PASS] shared-stack across clients");
    }

    // Case 2: A single client covers all remote methods
    private static void testSingleClient_AllMethods(Calculator cal) throws Exception{
        clearStack(cal);

        // min over [7,2,9] -> 2
        cal.pushValue(7);
        cal.pushValue(2);
        cal.pushValue(9);
        cal.pushOperation("min");
        assertEquals(2,cal.pop(), "min result");
        assertTrue(cal.isEmpty(), "empty after min");

        //max over [5, -3, 12,9] -> 12
        cal.pushValue(5);
        cal.pushValue(-3);
        cal.pushValue(12);
        cal.pushValue(9);
        cal.pushOperation("max");
        assertEquals(12,cal.pop(), "max result");
        assertTrue(cal.isEmpty(), "empty after max");

        //lcm over [4,8,16] -> 16
        cal.pushValue(4);
        cal.pushValue(8);
        cal.pushValue(16);
        cal.pushOperation("lcm");
        assertEquals(16,cal.pop(), "lcm result");

        //gcd over [18,30,42] -> 6
        cal.pushValue(18);
        cal.pushValue(30);
        cal.pushValue(42);
        cal.pushOperation("gcd");
        assertEquals(6,cal.delayPop(SINGLE_DELAY_MS), "delayPop after gcd");
        assertTrue(cal.isEmpty(), "empty after delayPop");

        System.out.println("[PASS] single-client all methods");
    }

    // Case 3:N clients concurrently push values 1...N; When the main thread performs max() once, it should obtain N
    private static void testMultiClient_PushThenMax() throws Exception{
        final Calculator main = (Calculator) Naming.lookup(URL);
        clearStack(main);

        Thread[] workers = new Thread[N_CLIENTS];
        for(int i = 0; i < N_CLIENTS; i++){
            final int val = i+1;
            workers[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                        try{
                            Calculator stub = (Calculator) Naming.lookup(URL);
                            stub.pushValue(val);
                        } catch (Exception ignored){}
                    }
            });
        }
        for(int i = 0; i < N_CLIENTS; i++) workers[i].start();
        for(int i = 0; i < N_CLIENTS; i++) workers[i].join();

        main.pushOperation("max");
        int result = main.pop();
        assertEquals(N_CLIENTS, result, "max over 1...N should be N");
        assertTrue(main.isEmpty(), "empty after max");

        System.out.println("[PASS] multi-client push + max");
    }
// Case 4: The main thread first presses in the (BASE VALUE...BASE VALUE+5-1);
// Start concurrent pop for N more clients. The result should be the permutation of this interval, and the stack should be emptied
    private static void testMultiClient_PopPermutation(Calculator cal) throws Exception{
        clearStack(cal);

        for(int i = 0; i < N_CLIENTS; i++){
            cal.pushValue(BASE_VALUE + i);
        }

        final int[] result = new int[N_CLIENTS];
        Thread[] workers = new Thread[N_CLIENTS];
        for(int i = 0; i < N_CLIENTS; i++){
            final int index = i;
            workers[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Calculator stub = (Calculator) Naming.lookup(URL);
                        result[index] = stub.pop();
                    } catch (Exception ignored){}
                }
            });
        }
        for(int i = 0; i < N_CLIENTS; i++) workers[i].start();
        for(int i = 0; i < N_CLIENTS; i++) workers[i].join();

        assertTrue(cal.isEmpty(), "empty after parallel pops");
        assertTrue(equalsExactRange(result, BASE_VALUE, N_CLIENTS), "popped results must equal {" + BASE_VALUE + ".." + (BASE_VALUE + N_CLIENTS - 1) + "}");

        System.out.println("[PASS] multi-client pop permutation");
    }

    // Case 5: The main thread is pressed in 1...5;
    // Then 5 clients concurrently perform delayPop(DELAY MS)
    private static void testMultiClient_DelayPopBurst(Calculator cal) throws Exception {
        clearStack(cal);

        for(int i = 1; i <= N_CLIENTS; i++) cal.pushValue(i);

        final int[] result = new int[N_CLIENTS];
        Thread[] workers = new Thread[N_CLIENTS];
        for(int i = 0; i < N_CLIENTS; i++){
            final int index = i;
            workers[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Calculator stub = (Calculator) Naming.lookup(URL);
                        result[index] = stub.delayPop(DELAY_MS);
                    } catch (Exception ignored){}
                }
            });
        }
        for(int i = 0; i < N_CLIENTS; i++) workers[i].start();
        for(int i = 0; i < N_CLIENTS; i++) workers[i].join();

        assertTrue(cal.isEmpty(), "empty after delayPop");
        assertTrue(equalsExactRange(result, 1, N_CLIENTS), "delayPop results must equal {1.." + N_CLIENTS + "}");

        System.out.println("[PASS] multi-client delayPop burst");
        }

    }
