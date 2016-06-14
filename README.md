# Dynamic Information Retrieval Project

This is a framework implement as part of Felipe Moraes master student at LATIN-UFMG and his advisor Rodrygo Santos.
It contains several retrieval mechanims as suport for learning approaches such as online learning to rank.

--

## Requirements:
1. Java 8.
2. [Gradle](http://gradle.org/)
3. [Elasticsearch](https://www.elastic.co/products/elasticsearch)

## Compilation

```bash
gradle buuld
```

## Dynamic User Simulator

--

## FatResultSet

Generate rankings and parameter combination of all available Lucene similarity modules based on an initial module.
```bash
gradle FatResultSet
java -jar application/build/libs/FatResultSet.jar -t [topicsFile] -r [initial ranking similarity function]
```

#### topics file format

```bash
indexName topicId Text
```

