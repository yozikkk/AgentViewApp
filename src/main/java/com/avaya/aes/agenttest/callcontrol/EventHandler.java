/*
 * This interface is the parent of all the Event Handlers.  Each Event Handler 
 * must implement this interface or it cannot be added to the Event Queue.
 */
package com.avaya.aes.agenttest.callcontrol;

public interface EventHandler {

	public void execute(CallManager callManager);
}
