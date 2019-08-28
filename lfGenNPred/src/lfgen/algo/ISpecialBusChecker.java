package lfgen.algo;

import lfgen.datatype.AclfCase;

/**
* @author JeremyChenlk
* @version 2019年2月13日 下午4:00:56
*
* Class description:
*	
*/

public interface ISpecialBusChecker {
	
	void correct(AclfCase aclfCase);

	String getMethodName(); 
}
