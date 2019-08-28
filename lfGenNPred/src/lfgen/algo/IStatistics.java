package lfgen.algo;
/**
* @author JeremyChenlk
* @version 2019年2月13日 下午4:46:25
*
* Class description:
*	
*/

public interface IStatistics {
	
	int getCallTimes();
	long getInitTimeUse();
	long getTimeUse();
	
	double getAverageTimeUse();
	void resetStatistics();
}
