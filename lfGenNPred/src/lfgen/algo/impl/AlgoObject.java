package lfgen.algo.impl;

import lfgen.algo.IStatistics;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019年2月13日 下午5:02:46
*
* Class description:
*	
*/

public abstract class AlgoObject implements IStatistics {
	
	/*
	 * IStatistics
	 */
	private int callTimes = 0;
	protected long initTimeUse = 0;
	protected long timeUse = 0;
	
	/*
	 * method
	 */
	protected RefInfo ref = null;
	protected String methodName = null;

	public AlgoObject(RefInfo refInfo) {
		ref = refInfo;
		
		callTimes = 0;
		initTimeUse = 0;
		timeUse = 0;
		methodName = new String("Unkown_Algo");
	}
	
	public abstract String keyMsg();

	public void report() {
		if (!ref.isPrintReport())
			return;
		System.out.printf("[REPORT] %25s called %10d times, with time use(s) = %10.0f, in which init time use = %10.3f\n",
				methodName, callTimes, timeUse/1000.0, initTimeUse/1000.0);
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public RefInfo getRefInfo() {
		return ref;
	}

	@Override
	public int getCallTimes() {
		return this.callTimes;
	}

	@Override
	public long getInitTimeUse() {
		return this.initTimeUse;
	}

	@Override
	public long getTimeUse() {
		return this.timeUse;
	}

	@Override
	public double getAverageTimeUse() {
		return this.timeUse/this.callTimes;
	}
	
	@Override
	public void resetStatistics() {
		callTimes = 0;
		initTimeUse = 0;
		timeUse = 0;
		if (!ref.isPrintReport())
			return;
		System.out.printf("[REPORT] method %24s is reset, now called times =  %10d, with time use(s) = %10.0f, in which init time use = %10.3f\n",
				methodName, callTimes, timeUse/1000.0, initTimeUse/1000.0);
	}
	
	protected void addCallTimes() {
		this.callTimes += 1;
	}
	
	protected void addInitTime(long startTime) {
		long timeuse = System.currentTimeMillis() - startTime;
		this.initTimeUse += timeuse;
		this.timeUse += timeuse;
	}
	
	protected void addTimeUse(long startTime) {
		this.timeUse += System.currentTimeMillis() - startTime;
	}

}
