package com.vsverchkov.selectwithlargeclause;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestSelectWithBigCollectionApplication.class)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
public class ProductRecommendationBenchmarkTest extends AbstractBenchmark {

    private static final int PRODUCT_CNT = 20000;
    private static final int PRODUCT_RECOMMENDATION_CNT = 1000000;
    private static final int RECOMMENDATION_RANGE = 10000;

    private List<Integer> productIds = IntStream.range(1, RECOMMENDATION_RANGE).boxed().collect(Collectors.toList());

    private static ProductRecommendationRepository productRecommendationRepository;

    @Setup(Level.Iteration)
    public void setup() {
        Instant start = Instant.now();

        //shuffle product ids to get random ordering ids for each iteration
        Collections.shuffle(productIds);

        //regenerate data for each iteration
        productRecommendationRepository.deleteAll();
        productRecommendationRepository.generateData(PRODUCT_CNT, PRODUCT_RECOMMENDATION_CNT);

        System.out.println("\nData generation took " + (Instant.now().toEpochMilli() - start.toEpochMilli()) + " ms");
    }

    @Autowired
    public void setProductRecommendationRepository(ProductRecommendationRepository productRecommendationRepository) {
        ProductRecommendationBenchmarkTest.productRecommendationRepository = productRecommendationRepository;
    }

    @Benchmark
    public void findUsingJoinTempTableWithoutIndex(Blackhole blackhole) {
        blackhole.consume(
                productRecommendationRepository.findAllWithProductIdNotInUsingJoinWithoutIdx(productIds)
        );
    }

    @Benchmark
    public void findUsingJoinTempTableWithIndex(Blackhole blackhole) {
        blackhole.consume(
            productRecommendationRepository.findAllWithProductIdNotInUsingJoinWithIdx(productIds)
        );
    }

    @Benchmark
    public void findUsingWhereNotInFromTempTable(Blackhole blackhole) {
        blackhole.consume(
                productRecommendationRepository.findAllWithProductIdNotInUsingWhereNotIn(productIds)
        );
    }

}
