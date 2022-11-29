/**
 * This class listens for CallControlTerminalConnectionEvents from the 
 * AE Services.  Call Control events for a single Meta event are sent between a 
 * MetaProgressStarted event and a MetaProgressEnded event.  These Call Control
 * events form a single Transaction.  The application should not begin to 
 * process one Call Control event until all events of the Transaction have been 
 * received.
 * 
 * Each event type has an associated Handler class.  
 * CallControlTerminalConnectionProxy creates the appropriate Handler class for
 * each received Event and adds it to an EventTransaction.  Once the Transaction 
 * is complete, CallControlTerminalConnectionProxy places the EventTransaction
 * on the CallManager queue for processing.
 * 
 * Only the events that are used by the application have handler classes.  Other
 * events (eg. terminalConnectionRinging) are received and logged here to
 * show when they appear.  Methods such as 
 * terminalConnectionRinging(CallControlTerminalConnectionEvent event) need not
 * be implemented.
 */
package com.avaya.aes.agenttest.apicomms;

import com.avaya.aes.agenttest.callcontrol.*;
import com.avaya.jtapi.tsapi.adapters.CallControlTerminalConnectionListenerAdapter;
import com.avaya.jtapi.tsapi.impl.events.call.SingleCallMetaEventImpl;
import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.MetaEvent;
import javax.telephony.MultiCallMetaEvent;
import javax.telephony.callcontrol.CallControlConnectionEvent;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;

public class CallControlTerminalConnectionProxy extends
CallControlTerminalConnectionListenerAdapter {

	private static Logger logger = 
		Logger.getLogger(CallControlTerminalConnectionProxy.class);
	
	private CallManager callManager = null;
	
	private EventTransaction transactionlist = null;
	/**
	 * 
	 * @param jtapiInterface
	 */
	public CallControlTerminalConnectionProxy(CallManager callManager){
		this.callManager = callManager;
	}

	@Override
	public void singleCallMetaProgressStarted(MetaEvent event){
		
		if(transactionlist != null)
			logger.error("Error: singleCallMetaProgressStarted received " +
					"with open transaction");
		
		SingleCallMetaEventImpl metaEvent = (SingleCallMetaEventImpl)event;
		Call c = metaEvent.getCall();
		long callId = CallUtilities.getCallID(c);

		if(logger.isDebugEnabled())
		    logger.debug("singleCallMetaProgressStarted: callId = " + callId);
		transactionlist = new EventTransaction(callId);
	}

	@Override
	public void singleCallMetaProgressEnded(MetaEvent event){

		if(logger.isDebugEnabled())
		    logger.debug("singleCallMetaProgressEnded");

		if(transactionlist != null)
			callManager.queueTransaction(transactionlist);
		else
			logger.error("Error: singleCallMetaProgressEnded received " +
					"without open transaction");
		transactionlist = null;
	}

	@Override
	public void multiCallMetaTransferStarted(MetaEvent event){
		
		if(transactionlist != null)
			logger.error("Error: multiCallMetaTransferStarted received " +
					"with open transaction");
		
		MultiCallMetaEvent metaEvent = (MultiCallMetaEvent)event;
		Call c = metaEvent.getNewCall();
		long callId = CallUtilities.getCallID(c);

		if(logger.isDebugEnabled())
		    logger.debug("multiCallMetaTransferStarted: callId = " + callId);
		transactionlist = new EventTransaction(callId);
	}

	@Override
	public void multiCallMetaTransferEnded(MetaEvent event){

		if(logger.isDebugEnabled())
		    logger.debug("multiCallMetaTransferEnded");

		if(transactionlist != null)
			callManager.queueTransaction(transactionlist);
		else
			logger.error("Error: multiCallMetaTransferEnded received " +
					"without open transaction");
		transactionlist = null;
	}

	@Override
	public void multiCallMetaMergeStarted(MetaEvent event){
		
		if(transactionlist != null)
			logger.error("Error: multiCallMetaMergeStarted received " +
					"with open transaction");
		
		MultiCallMetaEvent metaEvent = (MultiCallMetaEvent)event;
		Call c = metaEvent.getNewCall();
		long callId = CallUtilities.getCallID(c);

		if(logger.isDebugEnabled())
		    logger.debug("multiCallMetaMergeStarted: callId = " + callId);
		transactionlist = new EventTransaction(callId);
	}

	@Override
	public void multiCallMetaMergeEnded(MetaEvent event){

		if(logger.isDebugEnabled())
		    logger.debug("multiCallMetaMergeEnded");

		if(transactionlist != null)
		    callManager.queueTransaction(transactionlist);
		else
			logger.error("Error: multiCallMetaMergeEnded received " +
					"without open transaction");
		transactionlist = null;
	}
	
	@Override
	public void singleCallMetaSnapshotStarted(MetaEvent event){

		if(logger.isDebugEnabled())
		    logger.debug("singleCallMetaSnapshotStarted");
	}

	@Override
	public void singleCallMetaSnapshotEnded(MetaEvent event){

		if(logger.isDebugEnabled())
		    logger.debug("singleCallMetaSnapshotEnded");
	}
	
	@Override
	public void terminalConnectionHeld(CallControlTerminalConnectionEvent event){

		if(logger.isDebugEnabled())
		    logger.debug("  terminalConnectionHeld");

		if(transactionlist == null)
		{
			logger.error("Error: terminalConnectionHeld received " +
					"without open transaction");
			
			// Create a new transaction
			long callId = CallUtilities.getCallID(event.getCall());
			transactionlist = new EventTransaction(callId);
		}
		transactionlist.add(new TerminalConnectionHeldHandler(event));
	}

	@Override
	public void terminalConnectionTalking(CallControlTerminalConnectionEvent event){


		if(logger.isDebugEnabled())
		    logger.debug("  terminalConnectionTalking");

		if(transactionlist == null)
		{
			logger.error("Error: terminalConnectionTalking received " +
					"without open transaction");
			
			// Create a new transaction
			long callId = CallUtilities.getCallID(event.getCall());
			transactionlist = new EventTransaction(callId);
		}
		transactionlist.add(new TerminalConnectionTalkingHandler(event));
	}

	@Override
	public void terminalConnectionUnknown(CallControlTerminalConnectionEvent event){


		if(logger.isDebugEnabled())
		    logger.debug("  terminalConnectionUnknown");

		if(transactionlist == null)
		{
			logger.error("Error: terminalConnectionUnknown received " +
					"without open transaction");
			
			// Create a new transaction
			long callId = CallUtilities.getCallID(event.getCall());
			transactionlist = new EventTransaction(callId);
		}
		transactionlist.add(new TerminalConnectionUnknownHandler(event));
	}
	

	@Override
	public void connectionAlerting(CallControlConnectionEvent event){


		if(logger.isDebugEnabled())
		    logger.debug("  connectionAlerting");

		if(transactionlist == null)
		{
			logger.error("Error: connectionAlerting received " +
					"without open transaction");
			
			// Create a new transaction
			long callId = CallUtilities.getCallID(event.getCall());
			transactionlist = new EventTransaction(callId);
		}
		transactionlist.add(new ConnectionAlertingHandler(event));
	}

	@Override
	public void connectionInitiated(CallControlConnectionEvent event){


		if(logger.isDebugEnabled())
		    logger.debug("  connectionInitiated");

		if(transactionlist == null)
		{
			logger.error("Error: connectionInitiated received " +
					"without open transaction");
			
			// Create a new transaction
			long callId = CallUtilities.getCallID(event.getCall());
			transactionlist = new EventTransaction(callId);
		}
		transactionlist.add(new ConnectionInitiatedHandler(event));
	}

	@Override
	public void connectionEstablished(CallControlConnectionEvent event){


		if(logger.isDebugEnabled())
		    logger.debug("  connectionEstablished");

		if(transactionlist == null)
		{
			logger.error("Error: connectionEstablished received " +
					"without open transaction");
			
			// Create a new transaction
			long callId = CallUtilities.getCallID(event.getCall());
			transactionlist = new EventTransaction(callId);
		}
		transactionlist.add(new ConnectionEstablishedHandler(event));
	}

	@Override
	public void connectionDisconnected(CallControlConnectionEvent event){


		if(logger.isDebugEnabled())
		    logger.debug("  connectionDisconnected");

		if(transactionlist == null)
		{
			logger.error("Error: connectionDisconnected received " +
					"without open transaction");
			
			// Create a new transaction
			long callId = CallUtilities.getCallID(event.getCall());
			transactionlist = new EventTransaction(callId);
		}
		transactionlist.add(new ConnectionDisconnectedHandler(event));
	}

	// Unused Methods.  Put here for testing
	@Override
	public void terminalConnectionDropped(CallControlTerminalConnectionEvent event) {

		if(logger.isDebugEnabled())
		    logger.debug("  terminalConnectionDropped");
		event.getTerminalConnection().getTerminal().getName();
	}
	@Override
	public void connectionUnknown(CallControlConnectionEvent event)  {

		if(logger.isDebugEnabled())
		    logger.debug("  connectionUnknown");
	}
	@Override
	public void connectionFailed(CallControlConnectionEvent event)   {

		if(logger.isDebugEnabled())
		    logger.debug("  connectionFailed");
	}
	@Override
	public void terminalConnectionInUse(CallControlTerminalConnectionEvent event)   {

		if(logger.isDebugEnabled())
		    logger.debug("  terminalConnectionInUse");
	}
	@Override
	public void connectionOffered(CallControlConnectionEvent event)   {

		if(logger.isDebugEnabled())
		    logger.debug("  connectionOffered");
	}
	@Override
	public void connectionQueued(CallControlConnectionEvent event)    {

		if(logger.isDebugEnabled())
		    logger.debug("  connectionQueued");
	}
	@Override
	public void terminalConnectionBridged(CallControlTerminalConnectionEvent event)  {

		if(logger.isDebugEnabled())
		    logger.debug("  terminalConnectionBridged");
	}
	@Override
	public void terminalConnectionRinging(CallControlTerminalConnectionEvent event)   {

		if(logger.isDebugEnabled())
		    logger.debug("  terminalConnectionRinging");
	}
	@Override
	public void connectionDialing(CallControlConnectionEvent event)    {

		if(logger.isDebugEnabled())
		    logger.debug("  connectionDialing");
	}
	@Override
	public void connectionNetworkAlerting(CallControlConnectionEvent event)   {

		if(logger.isDebugEnabled())
		    logger.debug("  connectionNetworkAlerting");
	}
	@Override
	public void connectionNetworkReached(CallControlConnectionEvent event)  {

		if(logger.isDebugEnabled())
		    logger.debug("  connectionNetworkReached");
	}
}
