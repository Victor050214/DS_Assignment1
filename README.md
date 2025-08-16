This guide shows (step-by-step) how to compile, start the RMI registry + server, run clients/tests, and simulate multiple clients (Tested with OpenJDK 24).
# 1. Files

1.1 Core (shared stack):

Calculator.java — RMI interface

CalculatorImplementation.java — server-side implementation (shared stack)

CalculatorServer.java — starts the RMI registry and binds services

CalculatorClient.java — simple demo client

shared_test.java — automated tests for the shared stack (single + multi-client)

    

1.2 Bonus (per-client private stacks):

CalculatorPerClient.java — Calculator implementation with its own stack 

CalculatorPrivate.java — remote interface to get a private Calculator 

CalculatorPrivateImpl.java — implementation that returns a new CalculatorPerClient per call 

per_client_test.java — automated tests for per-client stacks

---
# 2. Compile all Java files

    rm -rf out
    
    javac -d out *.java
---
# 3. Start the RMI registry and launch the server(in the first terminal)

    java -cp out CalculatorServer
---
# 4. Run the client(s) and test all remote operations

4.1 Shared stack tests: java -cp out shared_test

4.2 Per-client private stacks (Bonus) tests: java -cp out per_client_test

4.3 demo test: java -cp out CalculatorClient
---
# 5. Simulate multiple clients

Both shared_test.java and per_client_test.java create more than 3 threads, each performing its own Naming.lookup(...). This satisfies the “many clients” requirement automatically.

---
# 6. Summary
        rm -rf out
        javac -d out *.java
        java -cp out CalculatorServer

        java -cp out shared_test

OR

        rm -rf out
        javac -d out *.java
        java -cp out CalculatorServer
        
        java -cp out per_client_test