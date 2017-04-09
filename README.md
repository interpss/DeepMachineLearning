# TensorFlow Based Deep Machine Learning (ML) For Power System Analysis

The purpose of this InterPSS project is to explore the possibility to apply the Deep Machine Learning (ML) approach to power system analysis. Google's [TensorFlow](https://www.tensorflow.org/) is used as the ML engine, while InterPSS object model is used as the power system analysis/simulation model. 

## System Architecture

![architecture](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_architecture.png)

InterPSS DML system includes the following three main components:

* **Google [TensorFlow](https://www.tensorflow.org/) ML engine**

TensorFlow is an open source software library by Google for numerical computation using data flow graphs. Nodes in the graph represent mathematical operations, while the graph edges represent the multidimensional data arrays (tensors) communicated between them. The flexible architecture allows you to deploy computation to one or more CPUs or GPUs. See the [Installation and Configuration](https://github.com/interpss/DeepMachineLearning/wiki/Runtime-Env-Setup#installation-and-configuration) page for instructions to install and configure TensorFlow on a Windows environment.   

* **InterPSS Power System Analysis Model Service**

Our purpose is to apply DML to power system analysis. The Power System Model Service module provide power system analysis model service to the DML engine for model training purpose. The analysis model is based on the [InterPSS object model](www.interpss.org) written in Java. The default TensorFlow programming language is Python. [Py4J](https://www.py4j.org/) is used to bridge communication between TensorFlow Python runtime env and InterPSS Java runtime env. 

* **Pluggable Training Data Generator**

In the DML approach, neural network (nn) model is used to represent power system for analysis purpose. The nn_model is first trained and then used for power system analysis. A nn model is in general trained for certain application purpose using a set of training data relevent to the context. The system architecture allows different traning case generator to be plugged-in for different model training purposes. A  [Training Case Generator Interface](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/src/org/interpss/service/train/ITrainCaseBuilder.java) is defined for the traning case generator implementation.    


## Neural Network (nn) Model

Neural networks typically consist of multiple layers, and the signal path traverses from the input, to the output layer of neural units. Back propagation is the use of forward stimulation to reset weights on the "front" neural units and this is sometimes done in combination with training or optimization where the correct result is known.

![nn_model](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_nn_layer.png)

A typical nn model is shown in the above figure. The output of a previous layer [x] is weighted-summarised [y] to feed the next layer.  


```      
      [y] = [W][x] + [b]
where, [W] - weight matrix
       [b] - bias vector
```

## Sample Case

In Loadflow study, network bus voltage is solved for a set of bus power (P,Q) as the input. Here the [IEEE 14-Bus System](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/IEEE14Bus.jpg) is used to demonstrate how to apply TensorFlow to power system analysis to predict network bus voltage based on the bus power input.

![net diagram](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/IEEE14Bus_small.jpg)

As shown in the above figure, in the sample case, power flow is flowing from the Gen Area to the Load Area. We wand to train a nn model to predict network bus voltage when the power flow from the Gen Area to the Load Area is adjusted, for example, by a scaling factor of 20% increase.

 * NN Model

A 3-larer nn model (dimension 28) is used for the network bus voltage prediction, see the define model section in the [Python File](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/py/loadflow.py) for details.

```      
             [ ------  Nuetral Network Model  ------ ]
   [P,Q] =>  [ Layer1(28) -> Layer2(28) - Output(28) ]  => [Vmsg, Vang]
```

 * Traing Case
 
Bus load [P,Q] in the sample network is scaled by multipling a factor in range (0.5~1.5) to create a set of traning cases. In the sample case, total 100 training cases are used to train the nn model. 

 * Model Testing

To test the accuracy of the NN model bus voltage prediction, a random scaling factor in range (0.5~1.5) is used to create bus power [P,Q] as the input. The bus power [P, Q] is fed into the NN model to get a bus voltage [Vmsg, Vang] prediction.   

```      
   [P,Q] => [ NN Model] => [Vmsg, Vang]
```

Then the bus voltage predition is appled to the power system analysis model to calculation bus mismatch info, as follows: 

```
   dPmax :  0.00602 (pu) at Bus : Bus4,     dQmax :  0.00454 (pu) at Bus : Bus5
```

## Links and References

* [Project Wiki](https://github.com/interpss/DeepMachineLearning/wiki)
