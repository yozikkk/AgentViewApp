/**
 * AgentDisconnectHandler is executed when the Agent drops a call from the 
 * AgentViewer UI.  If the Agent's terminal is in a valid state, it sends a
 * Disconnect request to the Provider. 
 */
package com.avaya.aes.agenttest.callcontrol;

import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.Connection;

public class AgentDisconnectHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(AgentDisconnectHandler.class);
	
	// CallId of the call
	public long callId;

	public AgentDisconnectHandler(long callId) {
		this.callId = callId;
	}

	public void execute(CallManager callmanager) {

		if(logger.isDebugEnabled())
		    logger.debug("AgentDisconnectHandler");

		// Get the Connection of the Agent
		Connection conn = getConnToDisconnect(callId, callmanager);

		// If the agent's terminal was in a call, disconnect
		try {
			if (conn != null)
				conn.disconnect();

		} catch (Exception e) {
			if (callmanager.getNumberOfCalls() != 0) {
				callmanager.displayMessage("Cannot drop a held call, retrieve the call first.");
			} else {
				callmanager.displayMessage("Exception Occured. Cannot drop the call: " + 
						e.getMessage());
			}
		}
	}


	/**
	 * Checks if the agent connection is in CONNECTED, ALERTING, INPROGRESS
	 * or FAILED state.
	 * 
	 * @return connection
	 */
	private Connection getConnToDisconnect(long callId,
			CallManager callmanager) {

		Connection tempConn = null;

		Call call = callmanager.getCall(callId).getCall();

		// Make sure the call is active
		if (call == null)
			return null;

		// Find the Agent's Connection and make sure it is active
		Connection[] conns = call.getConnections();
		if (conns == null)
			return null;

		for (int connCount = 0; connCount < conns.length; connCount++) {

			tempConn = conns[connCount];

			if (tempConn.getAddress().getName().equals(callmanager.getAgentDeviceID())) {

				if (tempConn.getState() == Connection.CONNECTED
						|| tempConn.getState() == Connection.ALERTING
						|| tempConn.getState() == Connection.INPROGRESS
						|| tempConn.getState() == Connection.FAILED) {

					return tempConn;
				}
			}
		}

		return null;
	}
}
