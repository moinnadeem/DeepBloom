# DeepBloom

This repository implements learned Bloom filters from [The Case for Learned Index Structures](https://arxiv.org/abs/1712.01208) and [sandwiched Bloom filters](https://arxiv.org/pdf/1803.01474.pdf). 

## Entry Point

The best way to familiarize yourself with the code is to look at `src/test/java/edu.mit.BloomFilter/LearnedBloomFilter/LearnedPerformanceTest.java` for the learned Bloom filter, and `src/test/java/edu.mit.BloomFilter/SandwichedBloomFilter/SandwichedPerformanceTest.java`.

## Dependencies

The following libraries should be installed:
```
org.apache.spark:spark-core_2.10:2.2.2
org.apache.spark:spark-mllib-local_2.10:2.2.2
org.apache.spark:spark-mllib_2.10:2.2.2
org.apache.spark:spark-sql_2.10:2.2.2 
```

## Classes
Each class has an abstract class and an implementation class. These are visible in `src/main/java/edu.mit.BloomFilter/`. 

## Feedback
Questions? Comments? Concerns? Email the creators at [deepbloom@mit.edu](mailto:deepbloom@mit.edu)! 
