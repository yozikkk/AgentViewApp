/**
 * ConnectionEstablishedHandler is executed when an Established event is 
 * received from the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;
import com.avaya.jtapi.tsapi.LucentEventCause;
import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.callcontrol.CallControlConnectionEvent;

public class ConnectionEstablishedHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(ConnectionEstablishedHandler.class);

	public CallControlConnectionEvent event = null;

	public ConnectionEstablishedHandler(CallControlConnectionEvent event) {
		this.event = event;
	}

	public void execute(CallManager callmanager) {

		String connectedDeviceID = event.getConnection().getAddress().getName();
		if(logger.isDebugEnabled())
			logger.debug("ConnectionEstablishedHandler for " + connectedDeviceID);

		Call call = event.getCall();
		long callID = CallUtilities.getCallID(call);

		Connection[] conn = call.getConnections();

		switch (event.getCallControlCause()) {

		case CallControlConnectionEvent.CAUSE_NORMAL:

			handleNormalState(callID, call, callmanager, conn, connectedDeviceID);
			break;
		case CallControlConnectionEvent.CAUSE_TRANSFER:

			handleTransferState(callID, call, callmanager, conn, connectedDeviceID);
			break;
		case CallControlConnectionEvent.CAUSE_CONFERENCE:

			handleConferenceState(callID, call, callmanager, conn);
			break;
		default:
			break;
		}

		// The Agent state may have changed, have this change propagated
		// to the UI
		callmanager.getAgent().queryAgentState();
	}

	public void handleNormalState(long callID, Call call,
			CallManager callmanager, Connection[] conn, String connectedDeviceID) {

		String callingDeviceID = callmanager.getDeviceID(call, event
				.getCallingAddress().getName());
		String calledDeviceID = callmanager.getDeviceID(call, event
				.getCalledAddress().getName());

		// The call could be a Transfer
		if (CallUtilities.getCallEventCause(call) == LucentEventCause.EC_TRANSFER) {
			handleTransferState(callID, call, callmanager, conn, connectedDeviceID);
		}

		// This event could be for a Conference call
		else if (conn != null && conn.length >= CallManager.MIN_CONFERENCE_CONN) {
			handleConferenceState(callID, call, callmanager, conn);
		}
		else if(calledDeviceID == connectedDeviceID){
			// If this event is due to the called party answering,
			// inform the UI
			callmanager.updateCall(callID, ConnectionState.Connect,
					CallState.ACTIVE, calledDeviceID, callingDeviceID,
					connectedDeviceID);
		}
	}
	
	/**
	 * When a call is transferred, the called & calling devices are with respect
	 * to the transfer consult call, rather than the final call.  These must be
	 * reorganised to generate an accurate output to the UI.
	 * 
	 * @param callID
	 * @param call
	 * @param callmanager
	 * @param conn
	 * @param connectedDeviceID
	 */
	public void handleTransferState(long callID, Call call,
			CallManager callmanager, Connection[] conn, String connectedDeviceID) {

		String callingDeviceID = "";
		String calledDeviceID = "";
		
		if (conn != null && conn.length > 0) {

			// Get the devices taking part in the call
			callingDeviceID = conn[0].getAddress().getName();
			if(conn.length > 1)
				calledDeviceID = conn[1].getAddress().getName();
			
			// We can receive Establish events before the call is answered.  If
			// any of the connections are not yet Connected, send an Alerting
			// update to the UI.
			for(Connection c:conn) {
				if(c.getState() != Connection.CONNECTED) {

					// Figure out which connection is being called and update
					// The UI
					if(c.getAddress().getName().equals(calledDeviceID))
						callmanager.updateCall(callID, ConnectionState.Alerting,
								CallState.ACTIVE, calledDeviceID, callingDeviceID,
								calledDeviceID);
					else
					    callmanager.updateCall(callID, ConnectionState.Alerting,
						    	CallState.ACTIVE, callingDeviceID, calledDeviceID,
						    	callingDeviceID);
					return;
				}
			}
		}
		callmanager.addNewCall(call, calledDeviceID);

		callmanager.updateCall(callID, ConnectionState.Connect,
				CallState.ACTIVE, calledDeviceID, callingDeviceID,
				calledDeviceID);
	}
	
	/**
	 * When a call is Conferenced, update the UI.  If necessary, add a New call
	 * to the List of Calls.  Also remove the old call from the List of Held Calls.
	 * 
	 * @param callID
	 * @param call
	 */
	public void handleConferenceState(long callID, Call call,
			CallManager callmanager, Connection[] conn) {

		if (conn != null && conn.length >= CallManager.MIN_CONFERENCE_CONN) {

			// Do not do anything until the last extension is active on the call
			for(Connection c:conn)
			{
				if(c.getState() != Connection.CONNECTED)
					return;
			}
			// Get device ID's participating in conference call
			String party1 = conn[0].getAddress().getName();
			String party2 = conn[1].getAddress().getName();
			String party3 = conn[2].getAddress().getName();

			if (!callmanager.hasCall(callID)) {
				// The Agent is moving from one call to a new one.  Remove the 
				// old call from the Map and add the new one
				callmanager.removeCall(CallUtilities.getOldCallID(call));
				String calledDeviceID = 
					callmanager.getDeviceID(call, event.getCalledAddress().getName());
				callmanager.addNewCall(call, calledDeviceID);

			}
			callmanager.updateCall(callID, ConnectionState.Conferenced,
					CallState.ACTIVE, party1, party2, party3);
		}

		// The original call was probably on hold.  Remove it from the hold list.
		callmanager.removeHeldDevice(CallUtilities.getOldCallID(call));
	}
}
