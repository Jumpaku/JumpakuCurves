package jumpaku.curves.fsc.benchmark.generate

import org.openjdk.jmh.annotations.*


@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
open class GeneratorBenchmark {

    @Benchmark
    fun benchmarkGenerate() {

    }
}