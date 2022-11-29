/**
 * TerminalConnectionTalkingHandler is executed when an TerminalConnectionTalking 
 * event is received from the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;
import org.apache.log4j.Logger;

import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;

public class TerminalConnectionTalkingHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(TerminalConnectionTalkingHandler.class);
	
	public CallControlTerminalConnectionEvent event = null;

	public TerminalConnectionTalkingHandler(
			CallControlTerminalConnectionEvent event) {
		this.event = event;
	}

	public void execute(CallManager callmanager) {

		TerminalConnection termConn = event.getTerminalConnection();
		if(logger.isDebugEnabled())
		    logger.debug("TerminalConnectionTalkingHandler for " + 
		    		termConn.getTerminal().getName());

		// The TerminalConnectionTalking is received at the same time as the
		// ConnectionEstablished - this case is not of interest to us.  It is also 
		// received when a terminal goes off Hold.  In this case, update the UI.
		long callID = CallUtilities.getCallID(event.getCall());
		if (callmanager.hasHeldDevice(callID)) {

			String heldDeviceID = termConn.getTerminal().getName();

			callmanager.updateCall(callID, ConnectionState.UnHold,
					CallState.ACTIVE, heldDeviceID, null, null);

			callmanager.removeHeldDevice(callID);
		}
	}
}
