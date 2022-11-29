package com.avaya.aes.agenttest.ui;


/**
 * This interface defines the Call/ConnectionState 
 * for a Call and a notify() method implemented by the 
 * AgentState UI for notification of the change in 
 * Call/ConnectionState of the Call.
 */

import javax.telephony.Call;
import java.util.Observer;


/**
 * This interface defines the Call/Connection State change notifications
 *  that an observer (AgentStateUI) needs to extend. 
 */

public interface CallStateInterface extends Observer{
	enum ConnectionState {
		None(-1),
		Null(0),
		Initiate(1),
		Alerting(2),
		Connect(3),
		Hold(4),
		Conferenced(5),
		Transfered(6),
		Unknown(7),
		UnHold(8),
		ThirdPartyDropped(9),
		AddingNewParty(10),
		ConfInitiate(11),
		ConfAlerting(12);		

		private int connectionState;
		
		/**
		 * Sets connection state
		 * @param connectionState
		 */
		private ConnectionState(int connectionState) {
			this.connectionState = connectionState;
		}

		/**
		 * To get the connection state
		 * @return connection state
		 */
		public int getState() {
			return this.connectionState;
		}
	}

	enum CallState {
		INVALID(Call.INVALID),
		IDLE(Call.IDLE),
		ACTIVE(Call.ACTIVE);
		
		private int callState;
		
		/**
		 * Sets the call state
		 * @param callState
		 */
		private CallState(int callState) {
			this.callState = callState;
		}

		/**
		 * To get the call state
		 * @return call state
		 */
		public int getState() {
			return this.callState;
		}
	}
}
