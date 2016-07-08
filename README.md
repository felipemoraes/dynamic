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
gradle build
```

## Dynamic User Simulator

--

## FeaturedResultSet

Build a featured set of query and document. It construct query dependent features (several ranking scores from ranking models) and query independent features (given as an indenpendent file). 
You'll need to run with Gradle Scripts like this:
```bash
gradle FeaturedResultSet -PXargs="-t [topicsFile] -c [configFIle]"

```

### topics file format

```bash
topicId<integer> Text<String>
```

### config file example:
INITIAL_RANKING_MODEL LMDirichlet
INDEPENDENT_FEATURES queryIndependentFile.txt
DEPENDENT_FEATURES dependentFeatures.txt
ES_FIELDS text;title
ES_INDEX_NAME ebola_2015
ES_DOC_TYPE doc
OUTPUT_FILENAME output.txt
