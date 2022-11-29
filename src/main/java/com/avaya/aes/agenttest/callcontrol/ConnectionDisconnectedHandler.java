/**
 * ConnectionDisconnectedHandler is executed when an Disconnect event is 
 * received from the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;
import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.callcontrol.CallControlConnectionEvent;

public class ConnectionDisconnectedHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(ConnectionDisconnectedHandler.class);
	
	public CallControlConnectionEvent event = null;
	
	public ConnectionDisconnectedHandler(CallControlConnectionEvent event)
	{
		this.event = event;
	}
	
	public void execute(CallManager callmanager) {

		String connName = event.getConnection().getAddress().getName();
		if(logger.isDebugEnabled())
		    logger.debug("ConnectionDisconnectedHandler for " + connName);
		
		/*
		 * We receive CallCtlConnDisconnectedEv in following scenarios,
		 * 1.Device drops from the call
		 * 2.Call is transfered
		 * 3.Conference is established
		 */
		switch(event.getCallControlCause()) {
		
		case CallControlConnectionEvent.CAUSE_NORMAL:

			handleCallCtlConnDisconnectedEvent_NormalCause(event, callmanager);
			break;
		case CallControlConnectionEvent.CAUSE_TRANSFER:

			handleCallCtlConnDisconnectedEvent_TransferCause(event, callmanager);
			break;

		case CallControlConnectionEvent.CAUSE_CONFERENCE:

			handleCallCtlConnDisconnectedEvent_ConferenceCause(event, callmanager);
			break;
			
		default:
			break;
		}
		
		callmanager.getAgent().queryAgentState();
	}

	/**
	 * Handle a "Normal" disconnect.  That is, a disconnect which is not 
	 * associated with a Transfer or Conference.
	 * @param connEvent
	 * @param callmanager
	 */
	private void handleCallCtlConnDisconnectedEvent_NormalCause(
			CallControlConnectionEvent connEvent, CallManager callmanager) {

		Call call = connEvent.getCall();
		long callID = CallUtilities.getCallID(call);
		
		Connection[] conn = call.getConnections();

		// If the agent hung up, the dropped device is actually the
		// extension which the agent is logged into.
		String droppedDeviceID = callmanager.getDeviceID(call, 
				connEvent.getConnection().getAddress().getName());
	
		callmanager.removeHeldDevice(CallUtilities.getCallID(call));

		// If the Agent dropped the call
		if (droppedDeviceID.equals(callmanager.getAgentDeviceID())) {

			// The agent dropped out of conference, the call may still be up
			if (conn != null && conn.length >= CallManager.MIN_CONN) {
				callmanager.updateCall(callID, ConnectionState.Null,
						CallState.ACTIVE, null, null, droppedDeviceID);
			} else {
				callmanager.updateCall(callID, ConnectionState.Null,
						CallState.INVALID, null, null, droppedDeviceID);
			}
		} else {

			// The Other Party dropped the call
			callmanager.updateCall(callID, ConnectionState.ThirdPartyDropped,
					CallState.INVALID, null, null, droppedDeviceID);
		}
	}

	/**
	 * Handles the case where one party transfers the call to another.
	 * @param connEvent
	 */
	private void handleCallCtlConnDisconnectedEvent_TransferCause(
			CallControlConnectionEvent connEvent, CallManager callmanager) {

		Call call = connEvent.getCall();
		long callID = CallUtilities.getCallID(call);

		String droppedDeviceID = callmanager.getDeviceID(call, 
				connEvent.getConnection().getAddress().getName());
		
		Connection[] conns = call.getConnections();
		
		// If there was only one connection in the call, the call has now
		// ended
		if (conns == null || conns.length < CallManager.MIN_CONN) {

			callmanager.updateCall(callID, ConnectionState.Null,
				null, null, null, droppedDeviceID);

			return;
		}
			
		// Retrieve the old call that was transferred
		long oldCallID = CallUtilities.getOldCallID(call);
		String oldDroppedDeviceID = CallUtilities.getOldDeviceID(call);
		
		/*
		 * Determine the device to which the call is transferred.
		 * If the call is answered before transfer is completed, it is the stored
		 * calledDevice. In the case of blind transfer it is the device which
		 * is alerting. 
		 */
		String newDeviceID = null;

		if (callmanager.hasCall(callID)) {
			newDeviceID = callmanager.getCall(callID).getCalledDeviceID();
		} else {
			if (conns[0].getState() == Connection.ALERTING) {
				newDeviceID = conns[0].getAddress().getName();
			} else {
				newDeviceID = conns[1].getAddress().getName();
			}
		}

		// Inform the UI that the call is transferring.  This shows the ID of
		// the old call, the device transferring and the device being
		// transferred to.
		callmanager.updateCall(oldCallID, ConnectionState.Transfered, 
				CallState.INVALID, null, newDeviceID, oldDroppedDeviceID);
	}

	/**
	 * Handles call control connection disconnected events for conference cause.
	 * @param connEvent
	 */
	private void handleCallCtlConnDisconnectedEvent_ConferenceCause(
			CallControlConnectionEvent connEvent, CallManager callmanager) {

		Call call = connEvent.getCall();
		long callID = CallUtilities.getCallID(call);

		String droppedDeviceID = 
			callmanager.getDeviceID(call, 
					connEvent.getConnection().getAddress().getName());
	
		if (droppedDeviceID.equals(callmanager.getAgentDeviceID())) {
			
			// The Agent disconnected from the call
			callmanager.updateCall(callID, ConnectionState.Null,
				CallState.INVALID, null, null, droppedDeviceID);
		}
		else
		{
			// Another party dropped from the call so it is still up
			callmanager.updateCall(callID, ConnectionState.Null,
					CallState.ACTIVE, null, null, droppedDeviceID);
		}
	}
}
