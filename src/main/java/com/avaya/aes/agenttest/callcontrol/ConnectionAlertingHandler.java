/**
 * ConnectionAlertingHandler is executed when an Alerting event is received from
 * the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;
import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.callcontrol.CallControlConnectionEvent;

public class ConnectionAlertingHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(ConnectionAlertingHandler.class);

	public CallControlConnectionEvent event = null;

	public ConnectionAlertingHandler(CallControlConnectionEvent event) {
		this.event = event;
	}

	public void execute(CallManager callmanager) {

		String connName = event.getConnection().getAddress().getName();
		if(logger.isDebugEnabled())
			logger.debug("ConnectionAlertingHandler for " + connName);


		ConnectionState connectionState = ConnectionState.Unknown;
		Call call = event.getCall();
		long callID = CallUtilities.getCallID(call);

		String origCalledDeviceID = event.getCalledAddress().getName();
		String calledDeviceID;
		String callingDeviceID;
		
		switch (event.getCallControlCause()) {

		case CallControlConnectionEvent.CAUSE_CONFERENCE:

			// Blind Conference
			
			// The alerting device is the called party.
			calledDeviceID = event.getConnection().getAddress().getName();
			callingDeviceID = 
				callmanager.getDeviceID(call, event.getCallingAddress().getName());
			connectionState = ConnectionState.ConfAlerting;
			break;
			
		case CallControlConnectionEvent.CAUSE_TRANSFER:

			// Blind Transfer 
			
			// Take the device that is not transferring as the calling device
			// and take the alerting device as the called party.
			callingDeviceID = callmanager.getDeviceID(call, event.getCalledAddress().getName());
			calledDeviceID = event.getConnection().getAddress().getName();
			origCalledDeviceID = connName;
			
			// The new status of the Agent's Connection depends on who initiated 
			// the call
			if (calledDeviceID.equals(callmanager.getAgentDeviceID())) {
				connectionState = ConnectionState.Alerting;
			} else {
				connectionState = ConnectionState.Initiate;
			}
			break;
		default:
			
			// Normal two party call
			
			calledDeviceID = 
				callmanager.getDeviceID(call, event.getCalledAddress().getName());
			callingDeviceID = 
				callmanager.getDeviceID(call, event.getCallingAddress().getName());
			
			// The new status of the Agent's Connection depends on who initiated 
			// the call
			if (calledDeviceID.equals(callmanager.getAgentDeviceID())) {
				connectionState = ConnectionState.Alerting;
			} else {
				connectionState = ConnectionState.Initiate;
			}

			// Add the new call to the List of Calls
			callmanager.addNewCall(call, calledDeviceID);

			break;
		}
		
		// Update the UI with the new call status
		callmanager.updateCall(callID, connectionState, CallState.ACTIVE,
					calledDeviceID, callingDeviceID, origCalledDeviceID);
	}
}
