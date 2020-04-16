# Project NoSQL Query-Based Metrics
Query-based metrics for evaluating and comparing NoSQL document schemas (document structure) against a set o queries

# QBMetrics

QBMetrics (Query-based Metrics) is a tool written in Java that assists the user on an RDB to NoSQL document conversion process. It provides a graphical interface to calculate query-based metrics that assist on the choice of a most appropriate target NoSQL
schema (document structures), prior database conversion and migration. The tool uses Direct Acyclic Graphs (DAGs) to represent both the target NoSQL schema and the set of queries, where the vertices are entities and the edges are relationships. More specifically, a DAG represent a collection structure for a schema and an access pattern for a query.

To execute the test scenarios run the tool and access the "Demo" menu. The first scenario show the results of CAiSE 20 article, where four NoSQL schemas (A-D) are evaluated and compare through a set of queries (seven queries). The second one show the an experiment using only three schemas and queries. Another way is to execute the class run_CAiSE20, located in test package.

To run the test scenarios, run the tool and access the "Demo" menu. The first scenario shows the experiment results of the CAiSE 20 article, in which four NoSQL schemas (A-D) are evaluated and compared through a set of queries (seven queries). The second shows the experiment using only three schemas and queries. Another way is to directly run the class run_CAiSE20, located in the test package.

QBMetrics calculates the metrics over schemas and queries. The user can use the results to decide how schema is most appropriate for the application's requirements.

# Mongo Queries

Queries are represented through DAGs in our approach. However, we also encode all the queries using MongoDB's aggregator pipeline framework (for each NoSQL schema we implement the seven queries). The implementation of the queries was used to evaluate the proposed metrics.
