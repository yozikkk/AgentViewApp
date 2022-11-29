/**
 * ConnectionInitiatedHandler is executed when an Initiated event is 
 * received from the AE Services.
 */
package com.avaya.aes.agenttest.callcontrol;

import org.apache.log4j.Logger;

import javax.telephony.callcontrol.CallControlConnectionEvent;

public class ConnectionInitiatedHandler implements EventHandler {

	private static Logger logger = Logger.getLogger(ConnectionInitiatedHandler.class);
	
	public CallControlConnectionEvent event = null;
	
	public ConnectionInitiatedHandler(CallControlConnectionEvent event)
	{
		this.event = event;
	}
	
	public void execute(CallManager callmanager) {

		String connName = event.getConnection().getAddress().getName();
		if(logger.isDebugEnabled())
		    logger.debug("ConnectionInitiatedHandler for " + connName);

		// The Agent state has probably changed, have this change propagated
		// to the UI
		callmanager.getAgent().queryAgentState();
	}
}
