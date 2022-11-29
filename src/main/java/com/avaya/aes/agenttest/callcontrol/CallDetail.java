
/**
 * This class encapsulates Call related details to be stored during the life
 * of a call.
 */
package com.avaya.aes.agenttest.callcontrol;

import javax.telephony.Call;

public final class CallDetail {

	private Call call;
	private String calledDeviceID;
	private boolean callConference ;

	/**
	 * Constructor
	 * @param callParam
	 * @param calledDevIDParam
	 */
	public CallDetail(Call callParam, String calledDevIDParam) {

		super();

		call = callParam;
		callConference = false;
		calledDeviceID = calledDevIDParam;
	}

	/**
	 * @return the value of member variable calledDeviceID
	 */
	public String getCalledDeviceID() {
		
		return calledDeviceID;
	}
	
	/**
	 * @return Object of Call class stored as member variable
	 */
	public Call getCall() {

		return call;
	}

	/**
	 * @return the value of member variable isCallConference
	 */
	public boolean getCallConference() {

		return callConference;
	}
	
	/**
	 * @param isCallConference the isCallConference to set
	 */
	public void setCallConference(boolean isCallConference) {

		callConference = isCallConference;
	}
}