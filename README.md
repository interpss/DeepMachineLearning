# TensorFlow Based Deep Machine Learning (DML) For Power System Analysis

## System Architecture

![architecture](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_architecture.png)

InterPSS DML system includes the following three main components:

* **Google [TensorFlow](https://www.tensorflow.org/) DML engine**

TensorFlow is an open source software library by Google for numerical computation using data flow graphs. Nodes in the graph represent mathematical operations, while the graph edges represent the multidimensional data arrays (tensors) communicated between them. The flexible architecture allows you to deploy computation to one or more CPUs or GPUs. See the [Installation and Configuration](https://github.com/interpss/DeepMachineLearning/wiki/Runtime-Env-Setup#installation-and-configuration) page for instructions to install and configure TensorFlow on a Windows environment.   

* **InterPSS Power System Analysis Model Service**

Our purpose is to apply DML to power system analysis. The Power System Model Service module provide power system analysis model service to the DML engine for model training purpose. The analysis model is based on the [InterPSS object model](www.interpss.org) written in Java. The default TensorFlow programming language is Python. [Py4J](https://www.py4j.org/) is used to bridge communication between TensorFlow Python runtime env and InterPSS Java runtime env. 

* **Pluggable Training Data Generator**

In the DML approach, neural network (nn) model is used to represent power system for analysis purpose. The nn_model is first trained and then used for power system analysis. A nn model is in general trained for certain application purpose using a set of training data relevent to the context. The system architecture allows different traning case generator to be plugged-in for different model training purposes. A  [Training Case Generator Interface](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/src/org/interpss/service/train/ITrainCaseBuilder.java) is defined for the traning case generator implementation.    


## Neural Network Model



## Training Data Generation


## Sample Case

![nn_model](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_nn_layer.png)

The [IEEE 14-Bus System](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/IEEE14Bus.jpg) is used to demonstrate how to apply TensorFlow to power system analysis. As shown in the above figure, in the sample case power flow is flowing from the Gen Area to the Load Area. We wand to train a nn model to predict network bus voltage when the power flow from the Gen Area to the Load Area is adjusted, for example, 20% increase.

 * NN Model

A 3-larer nn model 

```      
   [P,Q] => Layer1(28) => Layer2(28) => Output(28) => [Vmsg, Vang]
```

 * Traing Case Generator
 
 * Model Testing
 

## Links and References

* [Project Wiki](https://github.com/interpss/DeepMachineLearning/wiki)
