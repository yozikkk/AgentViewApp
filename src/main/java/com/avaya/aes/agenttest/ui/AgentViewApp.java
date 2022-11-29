package com.avaya.aes.agenttest.ui;


import com.avaya.aes.agenttest.callcontrol.CallManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.ProviderUnavailableException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;


/**
 * AgentViewApp is main application class. This class initializes the 
 * application and is responsible for instantiating the CallManager.
 */
public class AgentViewApp implements Runnable {
	
	private static Logger logger = Logger.getLogger(AgentViewApp.class);
	
	/**
	 * The Call Manager is the core of call processing.
	 */
	private static CallManager callManager = null;

	/**
	 * Opens a channel 
	 */
	private static FileChannel channel;

	/**
	 * Only one instance of this application may be running at any time.  The
	 * application uses a lock file to make sure that there is not already
	 * another instance running.
	 * @return true if no other instance is running already
	 * @throws FileNotFoundException
	 */
	private static boolean checkInstance()  {
		boolean isOnlyInstance = false;
		RandomAccessFile randomFile = null ;
		try {
			String strCurrDir;
			strCurrDir = new File(".").getAbsolutePath();
			randomFile = new RandomAccessFile(strCurrDir + "/Agent.bat", "rw");
		} catch(FileNotFoundException e) {
			logger.error(e.getMessage());
		}
	    catch(Exception e) {
		    logger.error(e.getMessage());
	    }
		if (randomFile != null) { 
			channel = randomFile.getChannel();
			try {
				if (channel.tryLock() != null) {
					isOnlyInstance = true;
				}
			} catch(IOException e) {
				logger.error(e.getMessage());
			}
		    catch(Exception e) {
		    	logger.error(e.getMessage());
		    }
		} else {
			isOnlyInstance = true ;
		}

		return isOnlyInstance;
	}

	/**
	 * Create the call processing infrastructure
	 * @return true if session is created successfully
	 */
	private static boolean createCallManager() {


		boolean isStreamOpen = false;
		String str = "";

		/*
		
		try {
			if (checkInstance()) {
				callManager = new CallManager();
				//System.out.println(callManager.getJtapi());

			    isStreamOpen = true ;
			} else {
				callManager = null;
				str = "Application instance is already running.";
				displayMessage(str);
			}


		 */
		try{
			callManager = new CallManager();
			isStreamOpen = true ;



		} catch(ProviderUnavailableException prvUnExc) {
			if (prvUnExc.getMessage().equals("initialization failed")) {
				str = "Please check CTI user credentials, either CTIUserID " +
				 	"\nor CTIUserPassword is wrong. \nException message value" +
						  ": " + prvUnExc.getMessage();
				displayMessage(str);
			} else {
				if (prvUnExc.getMessage().equals("server not found")) {
					
					str = "Error while opening stream with server. \n" +
						"Check the CTILink provided in the Agent.properties" +
							 " file \nException: " + 
						prvUnExc.getMessage();
				} else {
					str = "Error while opening stream with server." +
					"\nException message value: " +
						prvUnExc.getMessage();
				}
				displayMessage(str);
			}
			System.exit(0);
		} catch(JtapiPeerUnavailableException jtapiPeerExc) {
			str = "The Peer Object is not available" +
				"\n Exception message value: " + 
				jtapiPeerExc.getMessage();
			displayMessage(str);
		} catch(IOException ioex) {
			str = "An IO error occured while trying to open stream.";
			displayMessage(str);
		} catch(Exception ex) {
			ex.printStackTrace();
			str = "An error occured as the Agent.properties file is not " +
				"found at the application running location." + 
				"\nPlease copy the Agent.properties file to the " +
				"application running location and try again.";
			displayMessage(str);
		}
		return isStreamOpen;
	}

	/**
	 * Called at the time of exit, closes the stream
	 */
	public static void cleanUp() {
		try {

			// Disconnect from the JTAPI Provider
			callManager.shutdown();
			
			// Release the lock
			if (channel != null) {
					channel.close();
			}
		} catch(IOException e) {
			logger.error(e.getMessage());
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
		System.exit(0);
	}

	/**
	 * @param msg: Message to be displayed 
	 */
	public static void displayMessage(String msg) {
		
      JOptionPane.showMessageDialog(
    		  null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }

	/* (non-Javadoc) 
	 * Build login GUI and run it in the Swing Event Delivery Thread
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		// Creating an object will display the login screen
		new LoginUI(callManager); 
	}

	/**
	 * First method which will be called when the application starts running. 
	 * @param args
	 * @throws JtapiPeerUnavailableException
	 * @throws IOException
	 * @throws ProviderUnavailableException
	 */
	public static void main(String[] args) {
		
		
		
		
		if(createCallManager()) {

			SwingUtilities.invokeLater(new AgentViewApp());
		} else {
			
			// Failed to establish session 
			System.exit(0);
		}
	}
}

