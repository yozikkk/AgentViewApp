/**
 * This class contains several Static utility methods which are used by 
 * the Call Manager and Handler classes.
 */
package com.avaya.aes.agenttest.callcontrol;

import com.avaya.jtapi.tsapi.ITsapiCallIDPrivate;
import com.avaya.jtapi.tsapi.LucentCallInfo;
import com.avaya.jtapi.tsapi.LucentV7CallInfo;
import com.avaya.jtapi.tsapi.V7DeviceHistoryEntry;

import javax.telephony.Call;
import javax.telephony.callcenter.ACDAddress;

public class CallUtilities {

	/**
	 * Retrieve an ACD address associated with the call
	 * 
	 * @param call to get an ACD address
	 * @return ACD address name if call has associated ACD address, otherwise
	 *         null
	 */
	public static String getCallACDAddressName(Call call) {

		if (call instanceof LucentCallInfo) {

			LucentCallInfo lucentCall = (LucentCallInfo) call;
			ACDAddress address = lucentCall.getDeliveringACDAddress();

			return (address != null) ? address.getName() : null;
		}

		return null;
	}

	/**
	 * Retrieve an event cause from the device history for provided call object
	 * 
	 * @param call to check
	 * @return An event cause code or -1 if value cannot be retrieved
	 */
	public static short getCallEventCause(Call call) {

		if (call instanceof LucentV7CallInfo) {

			LucentV7CallInfo lucentCall = (LucentV7CallInfo) call;

			V7DeviceHistoryEntry[] deviceHistory = lucentCall
					.getDeviceHistory();

			if (deviceHistory == null || deviceHistory[0] == null)
				return -1;

			return deviceHistory[0].getEventCause();
		}

		return -1;
	}

	/**
	 * Return the call ID of a call
	 * 
	 * @param c
	 * @return Call Identifier
	 */
	public static long getCallID(Call c) {
		return ((c == null) ? 0 : ((ITsapiCallIDPrivate) c).getTsapiCallID());
	}

	/**
	 * Retrieve old call identifier from provided call
	 * The term 'old call' refers to the call that was in progress before a
	 * transfer or conference.
	 * 
	 * @param call
	 * @return Old call identifier or 0
	 */
	public static long getOldCallID(Call call) {

		if (call instanceof LucentV7CallInfo) {

			LucentV7CallInfo lucentCall = (LucentV7CallInfo) call;

			V7DeviceHistoryEntry[] deviceHistory = lucentCall
					.getDeviceHistory();

			try {
				if(deviceHistory.length == 0 || deviceHistory[0].getOldConnectionID() == null)
					return 0;
				return deviceHistory[0].getOldConnectionID().getCallID();
			} catch (RuntimeException e) {
				// Do not need to do anything here.
			}
		}

		return 0;
	}

	/**
	 * Retrieve old call device identifier from provided call.  
	 * The term 'old call' refers to the call that was in progress before a
	 * transfer or conference.
	 * 
	 * @param call
	 * @return Old call device identifier or null
	 */
	public static String getOldDeviceID(Call call) {

		if (call instanceof LucentV7CallInfo) {

			LucentV7CallInfo lucentCall = (LucentV7CallInfo) call;

			V7DeviceHistoryEntry[] deviceHistory =
				lucentCall.getDeviceHistory();

			try {
				return deviceHistory[0].getOldDeviceID();
			} catch (RuntimeException e) {
				// Do not need to do anything here.
			}
		}
		return null;
	}
}
