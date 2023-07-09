# Handling a large list of values in a "IN" clause in a SQL query

When we need to query a table with a large list of values as a "IN" clause, we should use a different approach 
to avoid performance or restrictions issues.

For example, the PostgreSQL database doesn't support more than 32767 literals in an IN clause.

The solution is to **create a temporary table** and insert the values into it. Then, we can use the temporary table 
for the IN clause.

Here we have two options to get results:
The first one is to use a JOIN between the main table and the temporary table.

```sql
SELECT mt.*
FROM main_table mt
JOIN temp_table tt ON tt.id = mt.id
WHERE tt.id IS NOT NULL
```

The second one is to use a subquery from the temporary table with the NOT IN operator.
```sql
SELECT mt.*
FROM main_table mt
WHERE mt.id IN (SELECT id FROM temp_table)
```

Let's perform a benchmark to compare the performance of both options.

```shell
./gradlew clean test --tests com.vsverchkov.selectwithlargeclause.ProductRecommendationBenchmarkTest
```

For my setup:
- MacBook Pro 16-inch, 2019
- 2.6 GHz 6-Core Intel Core i7
- 32 GB 2667 MHz DDR4

, the results are:

| Benchmark                          | Mode | Score | Units |
|------------------------------------|------|-------|-------|
| findUsingJoinTempTableWithIndex    | avgt | 0.717 | s/op  |
| findUsingJoinTempTableWithoutIndex | avgt | 0.882 | s/op  |
| findUsingWhereNotInFromTempTable   | avgt | 0.568 | s/op  |

As we can see from the results, **the second variant (findUsingWhereNotInFromTempTable) is the most optimal**.

Thanks for reading!