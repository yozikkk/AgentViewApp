/**
 * This class is a wrapper to hold the data sent from the CallManager to the UI
 * whenever a noteworthy event is received from the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;

public class CallControlUpdate {
	
	private ConnectionState connectionState;
	private CallState callState;
	private long callID;
	private String calledDeviceID;
	private String callingDeviceID;
	private String thirdDeviceID;
	
	public CallControlUpdate(ConnectionState connectionState,
			CallState callState, long callID, String calledDeviceID,
			String callingDeviceID, String thirdDeviceID)
	{
		this.connectionState = connectionState;
		this.callState = callState;
		this.callID = callID;
		this.calledDeviceID = calledDeviceID;
		this.callingDeviceID = callingDeviceID;
		this.thirdDeviceID = thirdDeviceID;
	}

	public ConnectionState getConnectionState() {
		return connectionState;
	}

	public CallState getCallState() {
		return callState;
	}

	public long getCallID() {
		return callID;
	}

	public String getCalledDeviceID() {
		return calledDeviceID;
	}

	public String getCallingDeviceID() {
		return callingDeviceID;
	}

	public String getThirdDeviceID() {
		return thirdDeviceID;
	}	
}
