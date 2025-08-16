import java.rmi.Naming;


//This is the Bonus automated testing where each client has its own per-client stacks
public class per_client_test {
    private static final String FACTORY_URL = CalculatorServer.PRIVATE_URL;
    private static final int N_CLIENTS = 5;
    private static final int DELAY_MS = 150;
    private static final int SHORT_DELAY_MS = 60;

    // Execute the five test cases in sequence, print "PASS FAIL", and provide the total result
    public static void main(String[] args) throws Exception {
        CalculatorPrivate factory;
        try {
            factory = (CalculatorPrivate) Naming.lookup(FACTORY_URL);
        } catch (Exception e){
            System.out.println("Cannot lookup CalculatorFactory at " + FACTORY_URL);
            e.printStackTrace(System.out);
            System.exit(1);
            return;
        }

        int passed = 0, failed = 0;

        try {
            testIsolation_PushPop(factory);
            passed++;
        } catch (Throwable t) {
            fail("isolation push/pop", t);
            failed++;
        }

        try {
            testIsolation_IsEmptyVisibility(factory);
            passed++;
        } catch (Throwable t) {
            fail("isEmpty isolation", t);
            failed++;
        }

        try {
            testOperationsOnOwnStack(factory);
            passed++;
        } catch (Throwable t) {
            fail("operations on per-client stack", t);
            failed++;
        }

        try {
            testManyClientsEachOwnValue(factory);
            passed++;
        } catch (Throwable t) {
            fail("many clients each own value", t);
            failed++;
        }

        try {
            testDelayPopDoesNotBlockOthers(factory);
            passed++;
        } catch (Throwable t) {
            fail("delayPop independence", t);
            failed++;
        }

        System.out.println("\n RESULT: passed=" + passed + " failed=" + failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    // The condition must be true; otherwise, an AssertionError is thrown
    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    //Assert that two integers are equal; Otherwise, throw an AssertionError
    private static void assertEquals(int expected, int actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " (expected " + expected + ", got " + actual + ")");
        }
    }

    private static void fail(String name, Throwable t) {
        System.out.println("[FAIL] " + name + ": " + t);
        t.printStackTrace(System.out);
    }

    // Clear the stack of the specified Calculator
    private static void clearStack(Calculator c) throws Exception {
        while (!c.isEmpty()) {
            try { c.pop(); } catch (Exception ignored) { break; }
        }
    }

    // Case 1: Two dedicated stacks do not interfere with each other
    private static void testIsolation_PushPop(CalculatorPrivate factory) throws Exception{
        Calculator A = factory.connect();
        Calculator B = factory.connect();
        clearStack(A);
        clearStack(B);

        A.pushValue(10);
        B.pushValue(99);

        assertEquals(10, A.pop(), "A pop should be 10");
        assertEquals(99, B.pop(), "B pop should be 99");
        assertTrue(A.isEmpty(), "A should be empty");
        assertTrue(B.isEmpty(), "B should be empty");

        System.out.println("[PASS] isolation push/pop");
    }

    // Case 2: Pushing A onto the stack does not affect the empty/non-empty state of B
    private static void testIsolation_IsEmptyVisibility(CalculatorPrivate factory) throws Exception{
        Calculator A = factory.connect();
        Calculator B = factory.connect();
        clearStack(A);
        clearStack(B);

        A.pushValue(7);
        assertTrue(B.isEmpty(), "B should still be empty even if A pushed a value");
        assertTrue(!A.isEmpty(), "A should not be empty after push");

        assertEquals(7, A.pop(), "A pop should be 7");
        assertTrue(A.isEmpty(), "A empty after pop");
        assertTrue(B.isEmpty(), "B still empty");

        System.out.println("[PASS] isEmpty isolation");
    }


    // Case 3: The operations on each independent stack do not affect each other
    private static void testOperationsOnOwnStack(CalculatorPrivate factory) throws Exception{
        Calculator A = factory.connect();
        Calculator B = factory.connect();
        clearStack(A);
        clearStack(B);

        A.pushValue(3);
        A.pushValue(7);
        A.pushOperation("max");
        assertEquals(7, A.pop(), "A max result");

        B.pushValue(18);
        B.pushValue(30);
        B.pushValue(42);
        B.pushOperation("gcd");
        assertEquals(6, B.delayPop(SHORT_DELAY_MS), "B gcd then delayPop");

        assertTrue(A.isEmpty(), "A empty");
        assertTrue(B.isEmpty(), "B empty");

        System.out.println("[PASS] operations on per-client stack");
    }

    // Case 4: 5 clients each only see their own values
    private static void testManyClientsEachOwnValue(CalculatorPrivate factory) throws Exception{
        Calculator[] clients = new Calculator[N_CLIENTS];

        for (int i = 0 ; i < N_CLIENTS; i++){
            clients[i] = factory.connect();
            clearStack(clients[i]);
        }

        for (int i = 0; i < N_CLIENTS; i++) {
            clients[i].pushValue(i + 1);
        }

        for (int i = 0; i < N_CLIENTS; i++) {
            int got = clients[i].pop();
            assertEquals(i + 1, got, "client" + i + " pop must equal its own pushed value");
            assertTrue(clients[i].isEmpty(), "client" + i + " should now be empty");
        }

        System.out.println("[PASS] many clients each own value");
    }

    // Case 5: A’s delayPop does not block B’s operation (independent)
    private static void testDelayPopDoesNotBlockOthers(CalculatorPrivate factory) throws Exception{
        Calculator A = factory.connect();
        Calculator B = factory.connect();
        clearStack(A);
        clearStack(B);

        A.pushValue(7);
        final int[] aRes = new int[1];

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    aRes[0] = A.delayPop(DELAY_MS);
                } catch (Exception ignored) {}
            }
        });
        t.start();

        B.pushValue(5);
        B.pushValue(9);
        B.pushOperation("min");
        int bGot = B.pop();
        assertEquals(5, bGot, "B min result should be 5");

        t.join();
        assertEquals(7, aRes[0], "A delayPop should return 7");

        assertTrue(A.isEmpty(), "A empty after delayPop");
        assertTrue(B.isEmpty(), "B empty after min");

        System.out.println("[PASS] delayPop independence");
    }
}
