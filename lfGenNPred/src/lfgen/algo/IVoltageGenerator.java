package lfgen.algo;

import org.apache.commons.math3.complex.Complex;

import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019年2月13日 下午4:23:32
*
* Class description:
*	
*/

public interface IVoltageGenerator {
	Complex[] genVoltage(VoltageGenCondition c);
}
