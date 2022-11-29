
/**
 * This class is the core of the call control.  It provides the following 
 * functions:
 * 
 * 1. It owns the Event Transaction Queue which accepts events from the provider.
 * 2. It dispatches events from the Queue to the Worker thread for processing.
 * 3. It owns the Call list and HeldCall list.
 * 4. It provides several utilities for the Handler classes
 * 5. It informs the UI of changes in the state of the call
 * 
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.apicomms.JTAPIInterface;
import com.avaya.aes.agenttest.ui.CallStateInterface.CallState;
import com.avaya.aes.agenttest.ui.CallStateInterface.ConnectionState;
import org.apache.log4j.Logger;

import javax.telephony.Call;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.ProviderUnavailableException;
import java.io.IOException;
import java.util.*;

public final class CallManager extends Observable {

	public static final int MIN_CONFERENCE_CONN = 3;
	public static final int MIN_CONN = 2;
	
	// Agent details
	private AvayaCallCenterUserAgent agent = null;
	
	// Keep a list of all active calls
	private Map<Long, CallDetail> listOfCalls;
	
	// listHeldDevices is used to keep track of which calls are on hold.  This 
	// is needed to be able to interpret TerminalConnectionTalking events.
	private Map<Long, Call> listHeldDevices = new HashMap<Long, Call>();

	// jtapiInterface is used to communicate with the JTAPI API.
	private JTAPIInterface jtapiInterface = null;
	
	// log4j logger
	private static Logger logger = Logger.getLogger(CallManager.class);
	
	// The application internal event queue.  This object acts also as a
	// synchronisation point between the JTAPI event delivery thread and the
	// application worker thread.
	private Queue<EventTransaction> evtq;

	/**
	 * Class Constructor
	 * @throws IOException 
	 * @throws JtapiPeerUnavailableException 
	 * @throws ProviderUnavailableException 
	 */
	public CallManager() throws ProviderUnavailableException, 
	                   JtapiPeerUnavailableException, IOException {

		super();
		jtapiInterface = new JTAPIInterface(this);
		listOfCalls = new HashMap<Long, CallDetail>();
		
		// Create the event queue and the worker thread which will process
		// each event in turn
		evtq = new LinkedList<EventTransaction>();
		new WorkerThread(this, evtq).start();
	}

	/*
	 * (non-Javadoc) Saves events in the transaction queue one by one for further
	 * processing on the application worker thread (see: constructor).
	 * 
	 * @see javax.telephony.CallObserver#callChangedEvent(javax.telephony.
	 * events.CallEv[])
	 */
	public void queueTransaction(EventTransaction event) {
		if (event == null)
			return;

		// JTAPI event delivery thread should not be used by the application to
		// process an event. One good reason is to avoid potential deadlock or
		// data corruption when the application calls a JTAPI method while
		// running in the JTAPI event thread. The other reason is possible 
		// performance degradation since the JTAPI implementation owns 
		// the thread.
		synchronized (evtq) {
			evtq.add(event);
			// JTAPI issues a notification before it releases the lock of the
			// transaction queue.  This notification wakes up the application 
			// worker thread which takes the transaction and processes it.
			evtq.notifyAll();
		}
	}

	/**
	 * Notify the UI of an error
	 * 
	 * @param msg
	 */
	public void displayMessage(String msg) {

		setChanged();
		super.notifyObservers(msg);
	}

	/**
	 * Sets the reference to the agent object & values of lucent agent ID &
	 * device ID.
	 * 
	 * @param agentObj
	 */
	public void setAgent(AvayaCallCenterUserAgent agentObj) {

		agent = agentObj;
	}

	/**
	 * The Agent has answered the call using the Answer button on the UI.
	 * Queue this request for processing.  
	 * 
	 */
	public void agentAnswerCall(long callId){

		EventTransaction transactionlist = new EventTransaction(callId);
		transactionlist.add(new AgentAnswerHandler(callId));
		queueTransaction(transactionlist);
	}


	/**
	 * The Agent has disconnected from the call using the Drop button on the UI.
	 * Queue this request for processing.  
	 * 
	 * @param callId
	 */
	public void agentDisconnectCall(long callId){

		EventTransaction transactionlist = new EventTransaction(callId);
		transactionlist.add(new AgentDisconnectHandler(callId));
		queueTransaction(transactionlist);
	}

	/**
	 * Add new call object to Map
	 * 
	 * @param call
	 * @param calledDeviceID
	 */
	public void addNewCall(Call call, String calledDeviceID) {

		long callID = CallUtilities.getCallID(call);
		
		if(logger.isDebugEnabled())
		    logger.debug("New Call added.  ID = " + callID);
		
		if (!hasCall(callID)) {
		    CallDetail callObject = new CallDetail(call, calledDeviceID);
		    listOfCalls.put(callID, callObject);
		}
	}

	
	/**
	 * Notify subscribers of a change in the call status.
	 * 
	 * @param connectionState
	 * @param callState
	 * @param callID
	 * @param calledDeviceID
	 * @param callingDeviceID
	 * @param thirdDeviceID
	 */
	private void notifySubscribers(ConnectionState connectionState,
			CallState callState, long callID, String calledDeviceID,
			String callingDeviceID, String thirdDeviceID) {

		setChanged();
		super.notifyObservers(new CallControlUpdate(connectionState, callState,
				callID, calledDeviceID, callingDeviceID, thirdDeviceID));
	}

	/**
	 * Notify subscribers (the UI) that the status of the call has changed.
	 * Also, update or remove the Call Detail record if necessary.
	 * 
	 * @param callID
	 * @param connectionState
	 * @param callState
	 * @param calledDeviceID
	 * @param callingDeviceID
	 * @param thirdPartyDeviceID
	 */
	public void updateCall(long callID, ConnectionState connectionState,
			CallState callState, String calledDeviceID, String callingDeviceID,
			String thirdPartyDeviceID) {

		if(logger.isDebugEnabled())
		    logger.debug("updateCall() - CallState = " + callState);
		
		notifySubscribers(connectionState, callState, callID, calledDeviceID,
				callingDeviceID, thirdPartyDeviceID);

		// May need to remove the call block if the call is finished. Also,
		// may need to update the Conference state of the call
		if (hasCall(callID)) {

			CallDetail tempCallObj = getCall(callID);

			if (connectionState == ConnectionState.Null
					|| connectionState == ConnectionState.Transfered) {
				removeCall(callID);

			} else if (connectionState == ConnectionState.Conferenced) {

				tempCallObj.setCallConference(true);
			} else if (connectionState == ConnectionState.ThirdPartyDropped) {

				tempCallObj.setCallConference(false);
			}
		}
	}

	/**
	 * Clears the list of held devices and list of calls.  Called when the
	 * UI is shut down.
	 */
	private void clear() {
		if (listHeldDevices.size() != 0)
			listHeldDevices.clear();
		if (listOfCalls.size() != 0)
			listOfCalls.clear();
		agent = null;
	}
	
	/**
	 * Shuts down the connection to the provider
	 */
	public void shutdown() {
		clear();

		try {
			jtapiInterface.clearStream();
			jtapiInterface = null;
		} catch(IOException e) {
			logger.error(e.getMessage());
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	public void addHeldDevice(Call call) {
		
		if(logger.isDebugEnabled())
		    logger.debug("Add Held Device.  ID = " + CallUtilities.getCallID(call));
		
		listHeldDevices.put(CallUtilities.getCallID(call), call);
	}

	public void removeHeldDevice(long callId) {
		
		if(logger.isDebugEnabled())
		    logger.debug("Remove Held Device.  ID = " + callId);
		
		if(hasHeldDevice(callId))
		    listHeldDevices.remove(callId);
	}

	public boolean hasHeldDevice(long callId) {
		return listHeldDevices.containsKey(callId);
	}

	public boolean hasCall(long callId) {
		return listOfCalls.containsKey(Long.valueOf(callId));
	}

	public CallDetail getCall(long callId) {
		return listOfCalls.get(Long.valueOf(callId));
	}

	/**
	 * Remove a call from the map
	 * @param callID
	 */
	public void removeCall(long callID) {
		if(logger.isDebugEnabled())
		    logger.debug("Remove Call.  ID = " + callID);
		
		if (listOfCalls.containsKey(callID))
		    listOfCalls.remove(callID);
	}
	
	public String getAgentDeviceID() {
		return agent.getLucentAgent().getAgentTerminal().getName();
	}

	public String getAgentID() {
		return agent.getAgentID();
	}

	public int getNumberOfCalls() {
		return listOfCalls.size();
	}

	public AvayaCallCenterUserAgent getAgent() {
		return agent;
	}


	/**
	 * An Agent can be reached using a Hunt Group ID, Agent ID or Device ID.
	 * This method will always return the Device ID.
	 * 
	 * @param call
	 * @param deviceId
	 *            - This may actually be the Hunt Group ID or Agent ID
	 * @return
	 */
	public String getDeviceID(Call call, String deviceId) {

		String huntGroup = CallUtilities.getCallACDAddressName(call);

		if ((huntGroup != null && deviceId.equals(huntGroup)) ||
				deviceId.equals(getAgentID()))
			return getAgentDeviceID();

		return deviceId;
	}
	
	/**
	 * 
	 * @return The interface to the JTAPI.
	 */
	public JTAPIInterface getJtapi() {
	    return this.jtapiInterface;
	}
}
