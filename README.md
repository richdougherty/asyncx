Experimental extensions to scala.concurrent.

* Async[A] - a reified asynchronous computation, can be evaluated into a Future[A]
* AsyncFunc[A,B] - a function that produces asynchronous computations
* AsyncRuntime - an efficient evaluator for asynchronous computations - avoids the construction of intermediate Future values, merges executions into the same AsyncContext where possible
* AsyncContext - an ExecutionContext that can optionally combine multiple Runnables in a single call to execute()

Microbenchmarks are promising, showing Async as 1.5-5x faster than Future for in some situations.

    ::Benchmark Future.map::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.107
    Parameters(executionContext -> fjp, mapCount -> 100): 1.636
    Parameters(executionContext -> fjp, mapCount -> 200): 3.167
    Parameters(executionContext -> fjp, mapCount -> 300): 4.624
    Parameters(executionContext -> fjp, mapCount -> 400): 6.201
    Parameters(executionContext -> fjp, mapCount -> 500): 7.736
    Parameters(executionContext -> imm, mapCount -> 0): 0.011
    Parameters(executionContext -> imm, mapCount -> 100): 0.838
    Parameters(executionContext -> imm, mapCount -> 200): 1.695
    Parameters(executionContext -> imm, mapCount -> 300): 2.545
    Parameters(executionContext -> imm, mapCount -> 400): 3.381
    Parameters(executionContext -> imm, mapCount -> 500): 4.168
    
    ::Benchmark Async.map::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.019
    Parameters(executionContext -> fjp, mapCount -> 100): 0.077
    Parameters(executionContext -> fjp, mapCount -> 200): 0.242
    Parameters(executionContext -> fjp, mapCount -> 300): 0.517
    Parameters(executionContext -> fjp, mapCount -> 400): 0.889
    Parameters(executionContext -> fjp, mapCount -> 500): 1.376
    Parameters(executionContext -> imm, mapCount -> 0): 0.01
    Parameters(executionContext -> imm, mapCount -> 100): 0.077
    Parameters(executionContext -> imm, mapCount -> 200): 0.24
    Parameters(executionContext -> imm, mapCount -> 300): 0.516
    Parameters(executionContext -> imm, mapCount -> 400): 0.902
    Parameters(executionContext -> imm, mapCount -> 500): 1.411
    
    ::Benchmark Future.flatMap::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.104
    Parameters(executionContext -> fjp, mapCount -> 100): 2.633
    Parameters(executionContext -> fjp, mapCount -> 200): 5.283
    Parameters(executionContext -> fjp, mapCount -> 300): 7.828
    Parameters(executionContext -> fjp, mapCount -> 400): 10.609
    Parameters(executionContext -> fjp, mapCount -> 500): 13.261
    Parameters(executionContext -> imm, mapCount -> 0): 0.011
    Parameters(executionContext -> imm, mapCount -> 100): 1.565
    Parameters(executionContext -> imm, mapCount -> 200): 3.138
    Parameters(executionContext -> imm, mapCount -> 300): 4.697
    Parameters(executionContext -> imm, mapCount -> 400): 6.26
    Parameters(executionContext -> imm, mapCount -> 500): 7.848
    
    ::Benchmark Async.flatMap::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.006
    Parameters(executionContext -> fjp, mapCount -> 100): 0.116
    Parameters(executionContext -> fjp, mapCount -> 200): 0.426
    Parameters(executionContext -> fjp, mapCount -> 300): 0.954
    Parameters(executionContext -> fjp, mapCount -> 400): 1.707
    Parameters(executionContext -> fjp, mapCount -> 500): 2.622
    Parameters(executionContext -> imm, mapCount -> 0): 0.006
    Parameters(executionContext -> imm, mapCount -> 100): 0.116
    Parameters(executionContext -> imm, mapCount -> 200): 0.427
    Parameters(executionContext -> imm, mapCount -> 300): 0.923
    Parameters(executionContext -> imm, mapCount -> 400): 1.607
    Parameters(executionContext -> imm, mapCount -> 500): 5.289
