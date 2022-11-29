/**
 * This class is a wrapper for the JTAPI Client library. It performs the following
 * tasks:
 * 1. It obtains the Provider object.
 * 2. It monitors the Provider for In-Service and Out-Of-Service events.
 * 3. It shuts down the Provider when the application is closed.
 * 4. It registers/unregisters a CallControlTerminalConnectionProxy object to
 *    Start/Stop monitoring a Terminal.
 * 5. Uses the Provider to perform other utilities such as getAddress() and
 *    getTerminal().
 */
package com.avaya.aes.agenttest.apicomms;

import com.avaya.aes.agenttest.callcontrol.CallManager;
import com.avaya.jtapi.tsapi.TsapiPlatformException;
import com.avaya.jtapi.tsapi.adapters.ProviderListenerAdapter;
import org.apache.log4j.Logger;

import javax.telephony.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class JTAPIInterface extends ProviderListenerAdapter {
	
	private static final String PROPERTY_FILE = "Agent.properties";

	// log4j logger
	private static Logger logger = Logger.getLogger(CallManager.class);
	
	// Variable to hold instance of JtapiPeer object
	private JtapiPeer peer;
	
	// CallManager which will process incoming events and which can pass any
	// error messages to the user
	private CallManager callManager;
	
	// Variable to hold instance of Provider object
	private Provider myProvider;
	
	// gotProvider is true if a Provider has been allocated
	private boolean gotProvider = false;
	
	// The values of following variables are given in the Agent.properties file
	private String serverID;
	private String loginID;
	private String password;
	
	// Store for Terminal Connection Proxies.  There should be one per monitored
	// terminal.  This application monitors only one terminal.
	private Map<String,CallControlTerminalConnectionProxy> terminalConnectionProxies =
		new HashMap<String,CallControlTerminalConnectionProxy>();
	
	/**
	 * Constructor, opens a connection to the AE Services
	 * 
	 * @throws IOException
	 * @throws JtapiPeerUnavailableException
	 * @throws ProviderUnavailableException
	 */
	public JTAPIInterface(CallManager callManagerParam)
	        throws ProviderUnavailableException, JtapiPeerUnavailableException,
	        IOException {
		this.callManager = callManagerParam;
		openStream();
	}
	
	/**
	 * This method:
	 * 1. Calls readPropFile() to read the properties file. 
     * 2. Retrieves the peer object.  
     * 3. Obtains the provider object using the CTIlink, user name and password 
     *    specified in the properties file.
     * 4. Registers as a Provider listener so we will know when the provider is
     *    ready.
	 */
	private boolean openStream() throws JtapiPeerUnavailableException,
	        ProviderUnavailableException, IOException {
		
		readPropFile(); 
		peer = JtapiPeerFactory.getJtapiPeer(null); 
		
		try {
			myProvider = peer.getProvider(serverID + ";loginID=" + loginID
			        + ";passwd=" + password);
			myProvider.addProviderListener(this);
			
		} catch (TsapiPlatformException e) {
			throw new ProviderUnavailableException(e.getMessage());
		} catch (Exception e) {
			throw new ProviderUnavailableException(e.getMessage());
		}
		
		return (myProvider != null);
	}
	
	/**
	 * This function reads the properties file and retrieves CTILink, username
	 * and password for getting the provider object.
	 */
	private void readPropFile() throws IOException {
		Properties properties = new Properties();
		URL url = ClassLoader.getSystemResource(PROPERTY_FILE);
		properties.load(new FileInputStream(new File(url.getFile())));
		
		serverID = properties.getProperty("CTILink").trim();
		loginID = properties.getProperty("CTIUserID").trim();
		password = properties.getProperty("CTIUserPassword").trim();
	}
	
	/**
	 * This method removes the CallControlTerminalConnectionProxy on the 
	 * specified terminal.
	 * 
	 * @param terminalAddress
	 */
	public void stopMonitor(String terminalAddress) throws Exception {
		try {
			CallControlTerminalConnectionProxy terminalConnectionProxy =
				terminalConnectionProxies.get(terminalAddress);
			if (gotProvider && terminalConnectionProxy != null) {
				myProvider.getTerminal(terminalAddress).removeCallListener(
				        terminalConnectionProxy);
				terminalConnectionProxies.remove(terminalAddress);
			} else
				throwProviderNotInitializedEx();
			
		} catch (InvalidArgumentException e) {
			String str = "Error in Removing the " + "CallListener :"
			        + e.getMessage();
			callManager.displayMessage(str);
		}
	}
	
	/**
	 * This method adds a CallControlTerminalConnectionProxy for the specified 
	 * terminal address.  The application will receive events for all calls in
	 * which this terminal is connected.
	 * 
	 * @param terminalAddress
	 * @throws Exception
	 */
	public void startMonitor(String terminalAddress) throws Exception {
		
		if (gotProvider) {

			CallControlTerminalConnectionProxy terminalConnectionProxy =
				terminalConnectionProxies.get(terminalAddress);
			if(terminalConnectionProxy == null)
			{
			    terminalConnectionProxy = new CallControlTerminalConnectionProxy(
			            callManager);
			    terminalConnectionProxies.put(terminalAddress, terminalConnectionProxy);
			    myProvider.getTerminal(terminalAddress).addCallListener(
			            terminalConnectionProxy);
			}
		} else
			throwProviderNotInitializedEx();
	}
	
	/**
	 * Provider shutdown - Instructs the Provider to shut itself down and
	 * perform all necessary cleanup
	 */
	public void clearStream() throws Exception {
		if (gotProvider) {
			myProvider.shutdown();
			myProvider = null;
			gotProvider = false;
			peer = null;
			terminalConnectionProxies = 
				new HashMap<String,CallControlTerminalConnectionProxy>();
		} else
			throwProviderNotInitializedEx();
	}
	
	/**
	 * Gets the Address object from the Provider, given the address string
	 * 
	 * @param Address string
	 * @return address object corresponding to address string
	 * @throws InvalidArgumentException
	 */
	public Address getAddress(String address) throws InvalidArgumentException,
	        Exception {
		
		if (gotProvider) {
			return myProvider.getAddress(address);
		} else
			throwProviderNotInitializedEx();
		
		return null;
	}
	
	/**
	 * Gets the Terminal object from the Provider, given the terminal's
	 * address string
	 * 
	 * @param terminal string
	 * @return Terminal object
	 * @throws InvalidArgumentException
	 */
	public Terminal getTerminal(String terminal)
	        throws InvalidArgumentException, Exception {
		
		if (gotProvider) {
			return myProvider.getTerminal(terminal);
		} else
			throwProviderNotInitializedEx();
		
		return null;
	}
	
	/**
	 * Generate an exception to indicate that a request was received before
	 * the Provider was initialized.
	 * @throws Exception
	 */
	private void throwProviderNotInitializedEx() throws Exception {
		Exception providerNotInitializedEx = new Exception(
		        " JTAPI  provider not initialized. ");

		throw providerNotInitializedEx;
	}

	/**
	 * The following methods extend ProviderListenerAdapter which implements
	 * the ProviderListener interface.  We use the ProviderInService event to 
	 * flag that the provider can be used.  The providerOutOfService event
	 * informs us that the provider can no longer be used.  
	 * 
	 * This application just informs the user that the provider has been lost
	 * but a real application should tidy up and attempt to reconnect.
	 */
    public void providerInService(ProviderEvent arg0) {
		if(logger.isDebugEnabled())
		    logger.debug("providerInService");
		gotProvider = true;
	}

    public void providerOutOfService(ProviderEvent arg0) {
		if(logger.isDebugEnabled())
		    logger.debug("providerOutOfService");
		gotProvider = false;
		callManager.displayMessage("Lost connection with the JTAPI Provider");
    }
}
