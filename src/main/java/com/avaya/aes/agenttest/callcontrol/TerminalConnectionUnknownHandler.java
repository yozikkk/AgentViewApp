/**
 * TerminalConnectionUnknownHandler is executed when an TerminalConnectionUnknown 
 * event is received from the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;
import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;

public class TerminalConnectionUnknownHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(TerminalConnectionUnknownHandler.class);

	public CallControlTerminalConnectionEvent event = null;

	public TerminalConnectionUnknownHandler(
			CallControlTerminalConnectionEvent event) {
		this.event = event;
	}

	public void execute(CallManager callmanager) {

		TerminalConnection termConn = event.getTerminalConnection();
		if(logger.isDebugEnabled())
			logger.debug("TerminalConnectionUnknownHandler for " + 
					termConn.getTerminal().getName());

		Call call = event.getCall();
		long callID = CallUtilities.getCallID(event.getCall());
		String connectedDeviceID = termConn.getTerminal().getName();

		// The CallControlTerminalConnectionEvent may be received during the
		// setup of a conference call.  In this case, use it to update the UI.
		switch(event.getCallControlCause()){
		case CallControlTerminalConnectionEvent.CAUSE_CONFERENCE:

			// Inform the UI that there is an attempt to add a new party to
			// the call.
			Connection[] conn = call.getConnections();

			if (conn != null && conn.length >= CallManager.MIN_CONFERENCE_CONN) {

				callmanager.updateCall(callID, ConnectionState.AddingNewParty,
						CallState.ACTIVE, null, null, connectedDeviceID);
			}
			break;
		default:
			break;
		}
	}
}
