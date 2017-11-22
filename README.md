# TensorFlow Based Deep Learning For Power System Analysis

This project is for exploring application of Deep Learning (DL) to power system analysis. Google's [TensorFlow](https://www.tensorflow.org/) is used as ML engine, while InterPSS is used to provide power system analysis/simulation model service: 1) to generate training data to train the neural network (NN) model; 2) provide service for checking model prediction accuracy. 

## System Architecture

![architecture](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_architecture.png)

The project software system architecture is shown in the above figure. It includes the following three main components:

* **Google [TensorFlow](https://www.tensorflow.org/) DL Engine**

TensorFlow is an open source software library by Google for numerical computation using data flow graphs. Nodes in the graph represent mathematical operations, while the graph edges represent the multidimensional data arrays (tensors) communicated between them. The flexible architecture allows you to deploy computation to one or more CPUs or GPUs. See the [Installation and Configuration](https://github.com/interpss/DeepMachineLearning/wiki/Runtime-Env-Setup#installation-and-configuration) page for instructions to install and configure TensorFlow on a Windows environment to run this project.   

* **Power System Analysis Model Service**

The Power System Model Service module provides service to the TensorFlow DL engine. The analysis model is based on [InterPSS](www.interpss.org) written in Java. The default TensorFlow programming language is Python. [Py4J](https://www.py4j.org/) is used to bridge the communication between TensorFlow Python runtime environment and InterPSS Java runtime environment. 

* **Training Case Generator**

The NN model is first trained and then used for power system analysis, for example, predicting network bus voltage for Loadflow analysis. An NN model is in general trained for certain purpose using a set of training data relevant to the problem to solve. The system architecture allows different traning case generators to be plugged-in for different model training purposes. A  [Training Case Generator Interface](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/src/org/interpss/service/train_data/ITrainCaseBuilder.java) is defined for the traning case generator implementation.    


## Neural Network (NN) Model

Neural networks typically consist of multiple layers, and the signal path traverses from the input to the output layer of neural units. Back propagation is the use of forward stimulation to reset weights on the "front" neural units and this is sometimes done in combination with training or optimization where the correct result (training data) is known.

![nn_model](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_nn_layer.png)

A typical NN model is shown in the above figure. The output of previous layer [x] is weighted-summarized to produce [y] for the next layer.  


```      
    [y] = [W][x] + [b]
where, [W] - weight matrix
       [b] - bias vector
```

## Links and References

* [Project Wiki](https://github.com/interpss/DeepMachineLearning/wiki)
* [Application of Machine Learning to Power Grid Analysis](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/IEEE%20PES%20Webinar%20-%20Application%20of%20ML%20to%20Power%20System%20Analysis.pdf) IEEE PES Webinar 2017-11-21

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Copyright (C) Apache Software Foundation
