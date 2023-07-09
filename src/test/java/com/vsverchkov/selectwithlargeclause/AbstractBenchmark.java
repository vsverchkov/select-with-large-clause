package com.vsverchkov.selectwithlargeclause;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

abstract class AbstractBenchmark {

    private final static Integer MEASUREMENT_ITERATIONS = 1;
    private final static Integer WARMUP_ITERATIONS = 0;

    /**
     * Any benchmark, by extending this class, inherits this single @Test method for JUnit to run.
     */
    @Test
    public void executeJmhRunner() throws RunnerException {
        Options jmhRunnerOptions = new OptionsBuilder()
                // set the class name regex for benchmarks to search for to the current class
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .warmupIterations(WARMUP_ITERATIONS)
                .measurementIterations(MEASUREMENT_ITERATIONS)
                // do not use forking or the benchmark methods will not see references stored within its class
                .forks(0)
                // do not use multiple threads
                .threads(1)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.JSON)
                .result("/dev/null") // set this to a valid filename if you want reports
                .shouldFailOnError(true)
                .jvmArgs("-server")
                .build();

        new Runner(jmhRunnerOptions).run();
    }
}
