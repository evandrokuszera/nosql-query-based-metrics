# Project NoSQL Query-Based Metrics
Query-based metrics for evaluating and comparing NoSQL document schemas (document structure) against a set o queries

# QueryMetrics

Project written in Java with the implementation of the metrics and a test scenario. The test scenario consists of executing the metrics over four generated NoSQL document schemas (document structure) considering a set of queries with seven distinct queries. Run the class located in the test package to see the results.

# Mongo Queries

Queries are represented through DAGs in our approach. However, we also encode queries using MongoDB's aggregator pipeline framework. For each NoSQL schema we implement the seven input queries for evaluation purposes.
