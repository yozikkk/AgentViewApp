
/**
 * This class represents an Agent and contains functionality typically 
 * performed by an Agent such as log-in/log-out,changing the Agent 
 * State etc.  This class periodically requests the current Agent state from the
 * Provider.  If the state has changed since the previous request, it sends 
 * Agent State change notifications to its registered observers. 
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.aes.agenttest.ui.AgentStateInterface.AgentState;
import com.avaya.aes.agenttest.worker.ChatChecker;
import com.avaya.jtapi.tsapi.*;
import org.apache.log4j.Logger;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.callcenter.Agent;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class AvayaCallCenterUserAgent extends Observable  {

	private static Logger logger = Logger.getLogger(AvayaCallCenterUserAgent.class);
	
	// The following variable values are given as input by the user
	private String terminalAddress;
	private String agentID;
	private String agentPassword;
	private int mode; 
	
	// If the Agent logs off without using the button on the Agent Viewer
	// screen, it was a manual logoff.
	private boolean isManualLogoff = true;
	
	/**
	 * Run method of AgentStateTimerInterval keeps checking
	 * the current state of the agent.
	 */
	private AgentStateTimerInterval timeInterval;

	// To be used to schedule a timer task
	private Timer timer; 
	
	// Agent data
	private LucentAddress lucentAddr;
	private LucentTerminal lucentTerm;
	private LucentV7Agent lucentAgent;
	
	// Call manager
	private CallManager cm;
	private ExecutorService service;
	private ChatChecker chatChecker;

	/**
	 * Class Constructor
	 * @param terminalAddress
	 * @param agentID
	 * @param agentPassword
	 * @param mode              0 = Auto mode, 1 = Manual mode
	 * @param callManager
	 * @throws Exception 
	 */
	public AvayaCallCenterUserAgent(String terminalAddress, String agentID,
			String agentPassword, int mode, CallManager callManager) throws Exception	{
		
		this.cm = callManager;
		this.terminalAddress = terminalAddress;
		this.agentID = agentID;
		this.agentPassword = agentPassword;
		this.mode = mode;
		timeInterval = new AgentStateTimerInterval();
		timer = new Timer();

		// Login the agent and start monitoring it
		agentLogin();
	}

	/**
	 * Starts monitor on the agent terminal
	 * @throws ResourceUnavailableException
	 * @throws MethodNotSupportedException
	 * @throws InvalidArgumentException
	 */
	private void agentStartMonitor() throws ResourceUnavailableException, 
	MethodNotSupportedException, InvalidArgumentException {
		try {
			cm.getJtapi().startMonitor(terminalAddress);
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Login function for the agent
	 * @throws Exception
	 */
	private void agentLogin() throws Exception {

		// Start monitoring the agent's terminal
		//chatChecker = new ChatChecker();
		//service = Executors.newFixedThreadPool(1);
		//service.execute(chatChecker);
		
		
		agentStartMonitor();
		
		// Get address/terminal from the AE Services
		lucentAddr = (LucentAddress)cm.getJtapi().getAddress(terminalAddress);
		lucentTerm = (LucentTerminal)cm.getJtapi().getTerminal(terminalAddress);
		
		// Check if any other agent is already logged in at this extension
		Agent[] agents = lucentTerm.getAgents();
		if (agents != null) {
			Exception agentAlreadyLoggedInEx = new Exception(
					" Agent " + agents[agents.length - 1].getAgentID() + 
					" is Logged In at the extension " + terminalAddress + ".");
			agents = null;
			throw agentAlreadyLoggedInEx;
		}
			
		// Log the agent on at the terminal
		lucentAgent = (LucentV7Agent)lucentTerm.addAgent(
				lucentAddr, null, Agent.LOG_IN, mode, agentID, agentPassword);

		// Start polling the Agent's state
		queryAgentState();
	}
	
	/**
	 * Send an Agent State update request from the UI to the AE Services
	 * @param newAgentState
	 * @param isPending - true means the state will not change while the agent
	 * is in a call
	 */
	public void setAgentState(int newAgentState, boolean isPending) {

		try {
			lucentAgent.setState(newAgentState, mode, 0, isPending);


		} catch (TsapiInvalidArgumentException e) {
			displayMessage("\nThis change of state is not allowed");
		} catch (TsapiInvalidStateException e) {
			displayMessage("\nAgent is not in a valid state to change its state");
		} 
		
		// Poll the new Agent state so it can be sent to the UI
		queryAgentState();
	}

	/**
	 * Logout the agent.
	 * @throws InvalidArgumentException
	 * @throws InvalidStateException
	 */
	public void agentLogout() throws InvalidArgumentException, 
									InvalidStateException {
		
		

		
		//chatChecker.stop();
		//service.shutdown();
		// Agent used the Logoff button so was not manual
		isManualLogoff = false;
		
		// Log the Agent off
		lucentTerm.removeAgent(lucentAgent);
		
		// Poll the new Agent state
		queryAgentState();
	}

	/**
	 * Stop Polling the Agent's state and monitoring the Agent's terminal.  
	 * This method is called when the application discovers that the agent has 
	 * logged off.
	 */
	public void agentStopMonitor() {
		
		// Stop listening for Connection and ConnectionTerminal events
		try {
			cm.getJtapi().stopMonitor(terminalAddress);
		} catch(Exception e) {
			logger.error("AvayaCallCenterUserAgent.agentStopMonitor() " + e.getMessage());
		}
		
		// Cancel the Agent State polling timer
		if (timeInterval != null)
			timeInterval.cancel();
		if(timer != null)
			timer.cancel();
	}

	/**
	 * Notify Subscribers about the agent state change
	 * @param agentState
	 */
	private void updateAgent(AgentState agentState) {

		setChanged();
		super.notifyObservers(agentState);
	}

	/**
	 * Queries State of the Agent by resetting the timer task
	 * Query Now and every 5 seconds afterwards.
	 *
	 * @param
	 */
	public void queryAgentState() {

		try {
			
			// Cancel the previous timer schedule
			if (timeInterval != null)
				timeInterval.cancel();

			timeInterval = null;
			timeInterval = new AgentStateTimerInterval();
			
			// Schedule the timer to query agent state.  This will query Now 
			// and every 5 seconds afterwards.
			timer.schedule(timeInterval, 0, 5 * 1000);

		} catch(Exception e) {

			logger.error("Exception in AvayaCallCenterUserAgent.queryAgentState(): " + 
					e.getMessage());
		}
	}

	/**
	 * @return the agentID
	 */
	public String getAgentID() {
		return agentID;
	}

	/**
	 * @return Lucent Agent
	 */
	public LucentV7Agent getLucentAgent() {
			return lucentAgent;
	}
	
	/**
	 * Send a notification to the UI
	 * @param msg
	 */
	private void displayMessage(String msg) {
		
		setChanged();
		super.notifyObservers(msg);
    }
	
	
	/**
	 * The run() method of AgentStateTimerInterval class checks the state 
	 * of the agent.  When agent's state changes, the state is displayed on 
	 * the text area of the UI.
	 */
	private class AgentStateTimerInterval extends TimerTask {
		
		private int previousState = LucentAgent.UNKNOWN;

		/**
		 * Class Constructor
		 */
		public AgentStateTimerInterval() {
		}

		public void run() {

			final int currentState = lucentAgent.getState();

			try {
				// If agent is not in valid state or if there is no change
				// in agent's state then do not update the UI
				if ((currentState == Agent.UNKNOWN) || 
						(currentState == previousState)) 
					return;

				if (currentState == Agent.LOG_OUT) {
					// If the agent logged off from the extension, inform the UI
					if (isManualLogoff) {
						displayMessage("Agent Manually Logged out");
						isManualLogoff = false;
					}
				} 
				
				// Update the UI with the new state
				updateAgent(AgentState.getAgentState(currentState));
				previousState = currentState;
			} catch(Exception e) {
				String str = "\nException in AvayaCallCenterUserAgent.TimerInterval(): "+ 
				         e.getMessage();
				displayMessage(str);
			}
		}
	}
}
