# TensorFlow Based Deep Machine Learning (ML) For Power System Analysis

This project is for exploring application of Deep Machine Learning (ML) to power system analysis. Google's [TensorFlow](https://www.tensorflow.org/) is used as ML engine, while InterPSS is used to provide power system analysis/simulation model service: 1) to generate training data to train the neural network (NN) model; 2) provide service for checking model prediction accuracy. 

## System Architecture

![architecture](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_architecture.png)

The project software system architecture is shown in the above figure. It includes the following three main components:

* **Google [TensorFlow](https://www.tensorflow.org/) ML Engine**

TensorFlow is an open source software library by Google for numerical computation using data flow graphs. Nodes in the graph represent mathematical operations, while the graph edges represent the multidimensional data arrays (tensors) communicated between them. The flexible architecture allows you to deploy computation to one or more CPUs or GPUs. See the [Installation and Configuration](https://github.com/interpss/DeepMachineLearning/wiki/Runtime-Env-Setup#installation-and-configuration) page for instructions to install and configure TensorFlow on a Windows environment to run this project.   

* **Power System Analysis Model Service**

The Power System Model Service module provides service to the TensorFlow ML engine. The analysis model is based on [InterPSS](www.interpss.org) written in Java. The default TensorFlow programming language is Python. [Py4J](https://www.py4j.org/) is used to bridge the communication between TensorFlow Python runtime environment and InterPSS Java runtime environment. 

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

## Sample Case

In Loadflow study, network bus voltage is solved for a set of bus power injection (P,Q) as the input. Here we the [IEEE 14-Bus System](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/IEEE14Bus.jpg) to illustrate how to apply TensorFlow ML to Loadflow study to predict network bus voltage or branch active power flow using bus power injection as the input. Please note: bus voltage magnitude and angle for Swing-Bus, bus voltage magnitude and bus power P for PV-Bus, bus power PQ for PQ-Bus are given; bus power P,Q for Swing-Bus, voltage angle and bus power Q for PV-Bus, bus voltage magnitude and angle for PQ-Bus are sloved (predicted) in a Loadflow analysis. Please see the [Project Wiki](https://github.com/interpss/DeepMachineLearning/wiki/Runtime-Env-Setup) for instructions to install the software, configure the system and run this sample case. 

![net diagram](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/IEEE14Bus_small.jpg)

In the sample case, power is flowing from the Gen Area to the Load Area, as shown in the above figure. We want to train NN model to predict network Loadflow solution, for example, bus voltage or branch active power flow, when the total power flow from the Gen Area to the Load Area is adjusted, for example, by a tatol load scaling factor of 1.2 (20% increase from the base case).

```      
           [P,Q] =>  [  NN Model ]  => [Bus Voltage] or [Branch P Flow]
```

In the exercise, the bus load [P,Q] is scaled by multiplying a total load scaling factor in range (0.5~1.5) to create a set of traning cases to train two NN models, one for bus voltage prediction and the other for branch active power flow prediction. 

* **Bus Voltage Prediction**

A set of bus voltage prediction training cases (50) are generated by the Training Case Generator to train a NN model. After the training, the NN model is used to predict network bus voltage when network bus power injection is given. Network bus voltage prediction accuracy using the NN model is summarized in the figure below, where max bus power mismatch using the predicted bus voltage is plotted against the total load scaling factor.

![Bus mismatch info](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_busmismatch.png)

Comparison of the bus voltage prediction by the NN model with the accurate Loadflow results for a set of selected buses and three scaling factors are shown in the following table.     

![Result Comparison](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_busresult.png)


* **Branch Active Power Flow Prediction**

A set of branch active power prediction training cases (50) are generated by the Training Case Generator to train a NN model. After the training, the NN model is used to predict network branch active power flow when network bus power injection is given. Comparison of the branch active power flow prediction by the NN model with the accurate Loadflow results results for a set of selected branches and three scaling factors are shown in the following table:

![Result Comparison](https://github.com/interpss/DeepMachineLearning/blob/master/ipss.dml/doc/image/dmp_branchresult.png)

## Links and References

* [Project Wiki](https://github.com/interpss/DeepMachineLearning/wiki)
