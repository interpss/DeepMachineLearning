package lfgen.algo;

import org.apache.commons.math3.complex.Complex;

/**
* @author JeremyChenlk
* @version 2019年2月13日 下午4:39:49
*
* Class description:
*	
*/

public interface ILoadFlowGenerator {
	Complex[] genFlow(Complex[] voltage);
}
