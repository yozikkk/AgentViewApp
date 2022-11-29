/**
 * All events in a Meta event must be received before any can be processed.  This 
 * class holds all the events associated with a single Meta Event.  Each time a
 * xxxCallMetaStarted is received, a new EventTransaction is created.  As each 
 * Event is received, it is added to the EventTransaction.  When a 
 * xxxCallMetaProgressEnded is received, the EventTransaction is placed on the
 * Event Queue of the CallManager.
 */
package com.avaya.aes.agenttest.callcontrol;

import java.util.ArrayList;
import java.util.List;


public class EventTransaction {

	private List<EventHandler> transaction = new ArrayList<EventHandler>();
	private int count = 0;
	private long callId;
	
	public EventTransaction(long callId)
	{
		this.callId = callId;
	}
	
	public void add(EventHandler task)
	{
		transaction.add(task);
	}
	
	public EventHandler next()
	{
		if(count < transaction.size())
		{
		    return transaction.get(count++);
		}
		else
			return null;
	}
	
	public boolean hasNext()
	{
		return count < transaction.size();
	}
	
	public long getCallId()
	{
		return callId;
	}
}
