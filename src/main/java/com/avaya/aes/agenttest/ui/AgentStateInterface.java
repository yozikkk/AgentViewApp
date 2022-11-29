package com.avaya.aes.agenttest.ui;


import javax.telephony.callcenter.Agent;
import java.util.Observer;


/**
 * This Interface defines the AgentStates enum and 
 * notify method which is implemented by the AgentStateUI
 * for receiving the AgentState change notification
 */

public interface AgentStateInterface extends Observer{
	
	enum AgentState {
	
		LOGIN (Agent.LOG_IN),
		LOGOUT (Agent.LOG_OUT),
		READY (Agent.READY),
		NOT_READY (Agent.NOT_READY),
		BUSY(Agent.BUSY),
		WORK_NOT_READY(Agent.WORK_NOT_READY),
		MANUAL_LOGOFF(8);
		
		private int agentState;

		/**
		 * Sets the agent state
		 * @param agentState
		 */
		private AgentState(int agentState) {
			this.agentState = agentState;
		}
		
		/**
		 * Gets the state of the agent
		 * @return
		 */
		public int getState() {
			return this.agentState;
		}
		
		/**
		 * To get the state of the agent
		 * @param agentState 
		 * @return agent state
		 */
		public static AgentState getAgentState(int agentState) {
			AgentState state = null;
			for(AgentState tempState : AgentState.values()) {
				if(tempState.getState() == agentState) {
					state = tempState;
					break;
				}
			}
			return state;
		}
	}
}

