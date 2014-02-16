Experimental extensions to scala.concurrent.

* AsyncContext - an ExecutionContext that can optionally combine multiple Runnables in a single call to execute()
* AsyncFunc0 - an `() => Future[A]` and an AsyncContext to run it in
* AsyncFunc1 - an `A => Future[B]` and an AsyncContext to run it in
* AsyncRuntime - an efficient evaluator for asynchronous operations - avoids the construction of intermediate Future values

Monadic operations (map, flatMap) on AsyncFunc0 shows around a 2x speedup compared to the same operations on Future. This is due to avoiding construction of intermediate Futures, and also possibly due to an optimisation which merges executions that operate in the same ExecutionContext.

TODO: Error handling.

    ::Benchmark Future.map::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.097
    Parameters(executionContext -> fjp, mapCount -> 100): 1.637
    Parameters(executionContext -> fjp, mapCount -> 200): 3.226
    Parameters(executionContext -> fjp, mapCount -> 300): 4.761
    Parameters(executionContext -> fjp, mapCount -> 400): 6.375
    Parameters(executionContext -> fjp, mapCount -> 500): 7.762
    Parameters(executionContext -> imm, mapCount -> 0): 0.011
    Parameters(executionContext -> imm, mapCount -> 100): 0.829
    Parameters(executionContext -> imm, mapCount -> 200): 1.658
    Parameters(executionContext -> imm, mapCount -> 300): 2.506
    Parameters(executionContext -> imm, mapCount -> 400): 3.346
    Parameters(executionContext -> imm, mapCount -> 500): 4.187
    
    ::Benchmark AsyncFunc0.map::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.101
    Parameters(executionContext -> fjp, mapCount -> 100): 0.656
    Parameters(executionContext -> fjp, mapCount -> 200): 1.239
    Parameters(executionContext -> fjp, mapCount -> 300): 1.783
    Parameters(executionContext -> fjp, mapCount -> 400): 2.397
    Parameters(executionContext -> fjp, mapCount -> 500): 2.984
    Parameters(executionContext -> imm, mapCount -> 0): 0.013
    Parameters(executionContext -> imm, mapCount -> 100): 0.568
    Parameters(executionContext -> imm, mapCount -> 200): 1.126
    Parameters(executionContext -> imm, mapCount -> 300): 1.689
    Parameters(executionContext -> imm, mapCount -> 400): 2.259
    Parameters(executionContext -> imm, mapCount -> 500): 2.821
    
    ::Benchmark Future.flatMap::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.108
    Parameters(executionContext -> fjp, mapCount -> 100): 2.827
    Parameters(executionContext -> fjp, mapCount -> 200): 5.439
    Parameters(executionContext -> fjp, mapCount -> 300): 8.034
    Parameters(executionContext -> fjp, mapCount -> 400): 10.744
    Parameters(executionContext -> fjp, mapCount -> 500): 13.381
    Parameters(executionContext -> imm, mapCount -> 0): 0.012
    Parameters(executionContext -> imm, mapCount -> 100): 1.54
    Parameters(executionContext -> imm, mapCount -> 200): 3.102
    Parameters(executionContext -> imm, mapCount -> 300): 4.639
    Parameters(executionContext -> imm, mapCount -> 400): 6.205
    Parameters(executionContext -> imm, mapCount -> 500): 7.779
    
    ::Benchmark AsyncFunc0.flatMap::
    cores: 8
    hostname: kina.local
    jvm-name: Java HotSpot(TM) 64-Bit Server VM
    jvm-vendor: Oracle Corporation
    jvm-version: 23.6-b04
    os-arch: x86_64
    os-name: Mac OS X
    Parameters(executionContext -> fjp, mapCount -> 0): 0.05
    Parameters(executionContext -> fjp, mapCount -> 100): 0.967
    Parameters(executionContext -> fjp, mapCount -> 200): 1.849
    Parameters(executionContext -> fjp, mapCount -> 300): 2.762
    Parameters(executionContext -> fjp, mapCount -> 400): 3.603
    Parameters(executionContext -> fjp, mapCount -> 500): 4.519
    Parameters(executionContext -> imm, mapCount -> 0): 0.013
    Parameters(executionContext -> imm, mapCount -> 100): 0.86
    Parameters(executionContext -> imm, mapCount -> 200): 1.712
    Parameters(executionContext -> imm, mapCount -> 300): 2.562
    Parameters(executionContext -> imm, mapCount -> 400): 3.416
    Parameters(executionContext -> imm, mapCount -> 500): 4.303
