# FastDD

## Introduction

FastDD is a solution for Differitial Dependency discovery. Given an instance of a relational schema, FastDD efficiently computes the set of all minimal DDs.

## Requirements

* Java 11 or later
* Maven 3.1.0 or later

## Usage

### A Quick Example
After building the project with maven, it is already runnable with a simple example. This example takes as input the dataset [TODO], computes its DDs, and outputs relevant information including the number of DDs and running time.

### Configures
Most parameters and configures related to our experiment are in Config.java. Some of the most pertinent ones are listed here, and please refer to the code and comments for further detail.

```shell
java -jar FastDD.jar <fp> <rowLimit> <thresholds>
```

- `fp`: 
- `threshold`: 
- `rowLimit`: 

## Comparative Experiments

FastDD are compared to other two discovery methods, IE and Domino(RFD). The source code of Domino can be found here. And we implement DD-IE with our best effort here.

## License

fastdd is released under the [Apache 2.0 license](https://github.com/RangerShaw/FastADC/blob/master/LICENSE).
Some source code is imported from [DCFinder](https://github.com/HPI-Information-Systems/metanome-algorithms/tree/master/dcfinder) and [FastADC](https://github.com/RangerShaw/FastADC),
which is also under [Apache 2.0 license](https://github.com/HPI-Information-Systems/metanome-algorithms/blob/master/LICENSE).
