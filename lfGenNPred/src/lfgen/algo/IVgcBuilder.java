package lfgen.algo;

import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019年2月13日 下午6:21:30
*
* Class description:
*	
*/

public interface IVgcBuilder {
	VoltageGenCondition nowCondition();
	VoltageGenCondition nextCondition();
}
