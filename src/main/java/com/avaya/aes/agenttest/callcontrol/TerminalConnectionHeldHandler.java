/**
 * TerminalConnectionHeldHandler is executed when an Held event is 
 * received from the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;
import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;

public class TerminalConnectionHeldHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(TerminalConnectionHeldHandler.class);
	
	public CallControlTerminalConnectionEvent event = null;

	public TerminalConnectionHeldHandler(
			CallControlTerminalConnectionEvent event) {
		this.event = event;
	}

	public void execute(CallManager callmanager) {

		TerminalConnection termConn = event.getTerminalConnection();
		if(logger.isDebugEnabled())
		    logger.debug("TerminalConnectionHeldHandler for " + 
		    		termConn.getTerminal().getName());

		Call call = event.getCall();

		long callID = CallUtilities.getCallID(call);
		String heldDeviceID = termConn.getTerminal().getName();
		
		// Add this call to the list of calls which have a party on Hold
		callmanager.addHeldDevice(call);

		// Update the UI
		callmanager.updateCall(callID, ConnectionState.Hold, CallState.ACTIVE,
				heldDeviceID, null, null);
	}
}
