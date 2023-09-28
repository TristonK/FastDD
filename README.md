# FastDD

## Introduction

FastDD is a solution to Differitial Dependency discovery. Given an instance of a relational schema, FastDD efficiently computes the set of all minimal valid DDs.

## Requirements

* Java 11 or later
* Maven 3.1.0 or later

## Usage

After building the project with maven, you can get `FastDD.jar`. You can run our code by adding 1~3 params(See [Configures](https://github.com/TristonK/FastDD#configures)). 

For example, you can run
```shell
java -jar FastDD.jar ./FastDD-Exp/dataset/restaurant.csv
```
This example takes the dataset restaurant.csv as input, sample its thresholds, computes its minimal valid DCs, and outputs relevant information including the number of DDs and running time.

### Configures

If you want to run our code, you should input 1~3 params to specific your target.
```shell
java -jar FastDD.jar <fp> <rowLimit> <thresholds>
```

- `fp`: input file path.
- `threshold`: if you want to use specific thresholds, input filepath; Or we will sample the dataset and get thresholds.
- `rowLimit`: rows limit, using -1 or empty will use full dataset.

Most parameters and configures related to our experiment are in Config.java. Some of the most pertinent ones are listed here, and please refer to the code and comments for further detail.

- `PliShardLength`: pli shard size
- `ThreadSize`: thread size limit for multi-thread expriment

## Comparative Experiments

FastDD are compared to other two discovery methods, IE and Domino(RFD). The source code of Domino can be found [here](https://dast-unisa.github.io/Domino-SW/). And we implement DD-IE with our best effort [here](https://github.com/TristonK/FastDD-Exp).

Our datasets and related experiment information are provided [here](https://github.com/TristonK/FastDD-Exp)

## License

fastdd is released under the [Apache 2.0 license](https://github.com/RangerShaw/FastADC/blob/master/LICENSE).
Some basic data structure's source code is imported from [DCFinder](https://github.com/HPI-Information-Systems/metanome-algorithms/tree/master/dcfinder) and [FastADC](https://github.com/RangerShaw/FastADC),
which is also under [Apache 2.0 license](https://github.com/HPI-Information-Systems/metanome-algorithms/blob/master/LICENSE).
