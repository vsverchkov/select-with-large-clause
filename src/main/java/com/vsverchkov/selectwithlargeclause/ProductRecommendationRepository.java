package com.vsverchkov.selectwithlargeclause;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
class ProductRecommendationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final int batchSize = 1000;

    @Transactional
    public List<ProductRecommendation> findAllWithProductIdNotInUsingJoinWithoutIdx(List<Integer> productIds) {
        jdbcTemplate.execute("CREATE TEMPORARY TABLE not_recommendation_temp (id int)");

        jdbcTemplate.batchUpdate(
                "INSERT INTO not_recommendation_temp (id) VALUES (?)",
                productIds,
                batchSize,
                (ps, argument) -> ps.setInt(1, argument)
        );

        List<ProductRecommendation> result = jdbcTemplate.query(
                """
                SELECT pr.id, pr.product_id1, pr.product_id2 
                FROM product_recommendation pr 
                LEFT JOIN not_recommendation_temp nrt 
                ON pr.product_id1 = nrt.id 
                WHERE nrt.id IS NULL
                """,
                (rs, rowNum) -> new ProductRecommendation(
                        rs.getInt("id"),
                        rs.getInt("product_id1"),
                        rs.getInt("product_id2")
                )
        );

        jdbcTemplate.execute("DROP TABLE not_recommendation_temp");

        return result;
    }

    @Transactional
    public List<ProductRecommendation> findAllWithProductIdNotInUsingJoinWithIdx(List<Integer> productIds) {
        jdbcTemplate.execute(
                """
                CREATE TEMPORARY TABLE not_recommendation_temp (id int); 
                CREATE INDEX not_recommendation_temp_idx ON not_recommendation_temp (id);
                """
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO not_recommendation_temp (id) VALUES (?)",
                productIds,
                batchSize,
                (ps, argument) -> ps.setInt(1, argument)
        );

        List<ProductRecommendation> result = jdbcTemplate.query(
                """
                SELECT pr.id, pr.product_id1, pr.product_id2 
                FROM product_recommendation pr 
                WHERE pr.product_id1 NOT IN (SELECT id FROM not_recommendation_temp)
                """,
                (rs, rowNum) -> new ProductRecommendation(
                        rs.getInt("id"),
                        rs.getInt("product_id1"),
                        rs.getInt("product_id2")
                )
        );

        jdbcTemplate.execute("DROP TABLE not_recommendation_temp");

        return result;
    }

    @Transactional
    public List<ProductRecommendation> findAllWithProductIdNotInUsingWhereNotIn(List<Integer> productIds) {
        jdbcTemplate.execute("CREATE TEMPORARY TABLE not_recommendation_temp (id int)");

        jdbcTemplate.batchUpdate(
                "INSERT INTO not_recommendation_temp (id) VALUES (?)",
                productIds,
                batchSize,
                (ps, argument) -> ps.setInt(1, argument)
        );

        List<ProductRecommendation> result = jdbcTemplate.query(
                """
                SELECT pr.id, pr.product_id1, pr.product_id2 
                FROM product_recommendation pr 
                WHERE pr.product_id1 NOT IN (SELECT id FROM not_recommendation_temp)
                """,
                (rs, rowNum) -> new ProductRecommendation(
                        rs.getInt("id"),
                        rs.getInt("product_id1"),
                        rs.getInt("product_id2")
                )
        );

        jdbcTemplate.execute("DROP TABLE not_recommendation_temp");

        return result;
    }

    @Transactional
    public void deleteAll() {
        jdbcTemplate.execute(
                """
                TRUNCATE TABLE product_recommendation CASCADE;
                ALTER SEQUENCE product_recommendation_id_seq RESTART WITH 1;

                TRUNCATE TABLE product CASCADE;
                ALTER SEQUENCE product_id_seq RESTART WITH 1;
                """
        );
    }

    @Transactional
    public void generateData(int productCnt, int recommendationCnt) {
        int recMultiplier = (recommendationCnt + (recommendationCnt / 10)) / productCnt;
        jdbcTemplate.update(
                """
                INSERT INTO product (name) 
                SELECT 'product' || generate_series 
                FROM generate_series(1, ?);
                
                INSERT INTO product_recommendation (product_id1, product_id2)
                SELECT p1.id, p2.id
                FROM product p1
                CROSS JOIN (SELECT id FROM product ORDER BY random() LIMIT ?) p2
                WHERE p1.id <> p2.id
                ORDER BY random()
                LIMIT ?;
                """,
                productCnt, recMultiplier, recommendationCnt
        );
    }

}
