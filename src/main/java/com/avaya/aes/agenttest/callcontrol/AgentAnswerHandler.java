/**
 * AgentAnswerHandler is executed when the Agent answers a call from the 
 * AgentViewer UI.  It answers the call if the Agent's phone is ringing.
 */
package com.avaya.aes.agenttest.callcontrol;

import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.TerminalConnection;

public class AgentAnswerHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(AgentAnswerHandler.class);
	
	// CallId of the call
	public long callId;

	public AgentAnswerHandler(long callId) {
		this.callId = callId;
	}

	/**
	 * Handle the case where the user presses the Answer button on the UI.
	 */
	public void execute(CallManager callmanager) {

		if(logger.isDebugEnabled())
		    logger.debug("AgentAnswerHandler");

		// Check that the call is in a state where it can be answered and
		// get the Terminal Connection of the Agent. 
		TerminalConnection termConn = getTermConnIfAnswerable(callId, callmanager);

		// If the agent's terminal is in a valid state, answer it.
		try {
			if (termConn != null)
				termConn.answer();

		} catch (Exception e) {
			callmanager.displayMessage("Exception Occured. Cannot answer the call: " + 
					e.getMessage());
		}
	}

	/**
	 * Checks if the agent terminal connection is in RINGING state
	 * 
	 * @return TerminalConnection
	 */
	private TerminalConnection getTermConnIfAnswerable(long callId,
			CallManager callmanager) {

		Call call = callmanager.getCall(callId).getCall();

		// Make sure the call is active
		if (call == null)
			return null;

		// Find the Terminal Connection for the Agent and make sure it it Ringing
		Connection[] conns = call.getConnections();
		if (conns == null)
			return null;

		// Loop through each connection in the call and look for the Agent's
		// terminal
		for (Connection tempConn:conns) {

			if (!tempConn.getAddress().getName().equals(callmanager.getAgentDeviceID()))
				continue;

			// Each connection has one (or more) terminals.  Check them for
			// the Agent's terminal
			TerminalConnection termConns[] = tempConn.getTerminalConnections();
			for (TerminalConnection tempTermConn:termConns) {

				// If the terminal is the Agent and it is ringing return
				if (tempTermConn.getTerminal().getName().equals(
						callmanager.getAgentDeviceID())) {
					if (tempTermConn.getState() == TerminalConnection.RINGING)
						return tempTermConn;
				}
			}
		}
		
		// Either the Agent was not found or, more likely, it was not in the
		// RINGING state.
		return null;
	}
}
