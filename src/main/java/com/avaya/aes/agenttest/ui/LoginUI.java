package com.avaya.aes.agenttest.ui;


import com.avaya.aes.agenttest.callcontrol.AvayaCallCenterUserAgent;
import com.avaya.aes.agenttest.callcontrol.CallManager;
import com.avaya.aes.agenttest.restapi.CallRestAPI;
import com.avaya.aes.agenttest.socketsimple.Client;
import com.avaya.jtapi.tsapi.LucentAgent;
import com.avaya.jtapi.tsapi.TsapiInvalidArgumentException;
import com.avaya.jtapi.tsapi.TsapiInvalidStateException;
import com.avaya.jtapi.tsapi.TsapiPlatformException;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.telephony.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


/**
 * LoginUI Class displays the AgentLogin dialog and receive user input to 
 * login an Agent. User need to provide:
 * 1. Station Extension : On which Agent Logs in
 * 2. Agent LoginID and Password
 * 3. Agent Mode : AutoIn or ManualIn
 * This class validates user input in all these fields, prompts an error 
 * message if these fields are empty or invalid.
 * If Credentials are valid, it creates
 * 1. AvayaCallCenterUserAgent class
 * 2. AgentStateUI class
 * Note : This class passes all events it receives from
 * AE Services :TSAPI service to TSAPIInterface class for further processing.
 */

public final class LoginUI extends JFrame 
	implements KeyListener, ActionListener {

	private static Logger logger = Logger.getLogger(LoginUI.class);
	
	// To avoid warning written below line of code 
	final static long serialVersionUID = 1;

	// Property File
	private static final String PROPERTY_FILE = "Agent.properties";
	
	// Represent Login button
	private JButton loginButton;

	// This button group is used to group Auto In and Manual In radio buttons. 
	private ButtonGroup buttonGroup1;

	// Exit button
	private JButton exitButton;
	
	// Label and text box to input AgentID
	private JLabel agentIDLabel;		
	private JTextField agentIDTextField;

	// Label and text box to input Agent loginPassword
	private JLabel agentPasswordLabel;
	private JPasswordField agentPasswordPasswordField;

	// Label and text box to input station extension
	private JLabel terminalAddressLabel;
	private JTextField terminalAddressTextField;

	// Label and radio buttons to select the agent mode
	private JLabel agentModeLabel;

	// Radio button to provide AutoIn option
	private JRadioButton autoInRadioButton;	

	// Radio button to provide ManualIn option
	private JRadioButton manualInRadioButton; 

	// AvayaCallCenterUserAgent Class instance.
	private AvayaCallCenterUserAgent agentObject;

	// Agent state User Interface Object
	private AgentStateUI agentStateUIObj;

	// CallManager Class instance.
	private CallManager callManager;

	// Will hold agent work mode i.e. AutoIn or ManualIn.
	private int agentWorkMode;

	// Will store agent password value to pass to AgentStateUI class.
	private String agentPassword;
	
	// Stores the terminal address as a string.
	private String terminalAddress;

	private String defaultStation;
	private String defaultAgent;
	private String defaultAgentPW;
	
	/**
	 * This constructor will be called by AgentViewApp class from main method 
	 * typically when application runs for the first time.
	 * @param callManagerObj
	 */
	public LoginUI(CallManager callManagerObj) {
		super();
		this.callManager = callManagerObj;
		getDefaultParameters();
		initInstance();
		setVisible(true);
	}

	/**
	 * Read the Station and Agent parameters from the properties file.  They
	 * will be used to fill in the Login fields.
	 */
	private void getDefaultParameters() {
		try
		{
			Properties properties = new Properties() ;
			URL url = ClassLoader.getSystemResource(PROPERTY_FILE);
			properties.load(new FileInputStream(new File(url.getFile())));

			String station = properties.getProperty("StationID");
			String agent = properties.getProperty("AgentID");
			String agentPW = properties.getProperty("AgentPW");
			if(station != null)
				defaultStation = station.trim();
			if(agent != null)
				defaultAgent = agent.trim();
			if(agentPW != null)
				defaultAgentPW = agentPW.trim();
		}
		catch(IOException e)
		{
			logger.error("Could Not get properties from " + PROPERTY_FILE);
		}
	}
	/**
	 * This method is called from within the constructor to initialize 
	 * the swing component variables on the form.
	 */
	private void initInstance() {

		// Initialization of the form components
		buttonGroup1 = new ButtonGroup();

		// label and text variable to enter station extension
		terminalAddressLabel = new JLabel();
		terminalAddressTextField = new JTextField(defaultStation);

		// label and text variable to enter Agent ID
		agentIDLabel = new JLabel();
		agentIDTextField = new JTextField(defaultAgent);

		// label and text variable to enter Agent loginPassword
		agentPasswordLabel = new JLabel();
		agentPasswordPasswordField = new JPasswordField(defaultAgentPW);

		// label and radio button variable to select the agent mode
		agentModeLabel = new JLabel();
		autoInRadioButton = new JRadioButton();
		manualInRadioButton = new JRadioButton();

		// button variable for login and exit buttons
		loginButton = new JButton();
		exitButton = new JButton();

		// Set title, color, size, name and layout for the LoginGUI form
		addWindowListener(win);
		setTitle("LoginGUI");	
		setBackground(new java.awt.Color(204, 255, 255));
		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		setMaximizedBounds(new java.awt.Rectangle(0, 0, 430, 350));
		setResizable(false);
		setName("LoginFrame"); 
		getContentPane().setLayout(null);

		// Set 'Station Extension' label component on the LoginGUI form
		terminalAddressLabel.setText("Station Extension:");
		getContentPane().add(terminalAddressLabel);
		terminalAddressLabel.setBounds(30, 30, 110, 20);

		// Set text component to get input for 'Station Extension' on the 
		// LoginGUI form
		terminalAddressTextField.setName("txt_TermAddr"); 
		getContentPane().add(terminalAddressTextField);
		terminalAddressTextField.setBounds(170, 30, 190, 19);
		terminalAddressTextField.getAccessibleContext().
			setAccessibleName("txt_TermAddr");
		terminalAddressTextField.addKeyListener(this);

		// Set 'Agent ID' label component on the LoginGUI form
		agentIDLabel.setText("Agent ID:");
		agentIDLabel.setName("lbl_AgentID");
		getContentPane().add(agentIDLabel);
		agentIDLabel.setBounds(30, 70, 60, 14);
		agentIDLabel.getAccessibleContext().setAccessibleName("lbl_AgentID");

		// Set text component to get input for Agent ID on the LoginGUI form
		agentIDTextField.setName("txt_AgentID");
		getContentPane().add(agentIDTextField);
		agentIDTextField.setBounds(170, 70, 190, 19);
		agentIDTextField.getAccessibleContext().
			setAccessibleName("txt_AgentID");
		agentIDTextField.addKeyListener(this);

		// Set 'Agent Password' Label on the LoginGUI form
		agentPasswordLabel.setText("Agent Password:");
		agentPasswordLabel.setName("lbl_AgentPasswd");
		getContentPane().add(agentPasswordLabel);
		agentPasswordLabel.setBounds(30, 110, 110, 20);

		// Set text component to get input for 'Agent Password' 
		// on LoginGUI form
		agentPasswordPasswordField.setName("txt_AgentPasswd");
		getContentPane().add(agentPasswordPasswordField);
		agentPasswordPasswordField.setBounds(170, 110, 190, 20);
		agentPasswordPasswordField.getAccessibleContext().
			setAccessibleName("txt_AgentPasswd");
		agentPasswordPasswordField.addKeyListener(this);

		// Set the 'Agent Mode' Selection label on LoginGUI form
		agentModeLabel.setText("Agent Work Mode:");
		agentModeLabel.setName("lbl_AgentMode");
		getContentPane().add(agentModeLabel);
		agentModeLabel.setBounds(30, 150, 120, 20);

		final JRadioButton autoInRButtonLocal = autoInRadioButton;
		
		// Set Radio button component to get input for Mode selection 
		// Radio button for AutoIn mode
		buttonGroup1.add(autoInRButtonLocal);
		autoInRButtonLocal.setActionCommand("autoInRadioButton");
		autoInRButtonLocal.setText("Auto In");
		autoInRButtonLocal.setMnemonic('a');
		autoInRButtonLocal.setName("autoInRadioButton");
		getContentPane().add(autoInRButtonLocal);
		autoInRButtonLocal.setBounds(170, 150, 70, 23);
		autoInRadioButton.addKeyListener(new KeyListener() 
		{
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 39) {
					
					autoInRadioButton.setSelected(true);
					manualInRadioButton.requestFocus();
					manualInRadioButton.setSelected(true);
				} else if(e.getKeyCode() == 10) {
					autoInRadioButton.setSelected(true);
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
		
		autoInRButtonLocal.getAccessibleContext().
			setAccessibleName("rbtn_AutoMode");

		// Radio button for ManualIn mode		
		buttonGroup1.add(manualInRadioButton);
		manualInRadioButton.setText("Manual In");
		manualInRadioButton.setMnemonic('m');
		getContentPane().add(manualInRadioButton);
		manualInRadioButton.setBounds(240, 150, 80, 23);
		// By default Auto in mode is selected
		autoInRadioButton.setSelected(true);
		
		manualInRadioButton.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == 37) {
					
					manualInRadioButton.setSelected(false);	
					autoInRadioButton.requestFocus();
					autoInRadioButton.setSelected(true);
				} else if(e.getKeyCode() == 10) {
					manualInRadioButton.setSelected(true);
				}
			}

			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == 37) {
					
					autoInRadioButton.setSelected(true);
					manualInRadioButton.setSelected(false);
				}
			}

			public void keyTyped(KeyEvent e) {
			}
			
		});

		// Set button component for 'Login' button on the LoginGUI form
		loginButton.setText("Login");
		loginButton.setMnemonic('l');
		getContentPane().add(loginButton);
		loginButton.setBounds(110, 200, 80, 23);
		loginButton.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent arg0) {
				
				if (arg0.getKeyCode() == 10) {
	
					try {
						onLoginRequest();
					} catch(Exception e) {
						logger.error(e.getMessage());
					}
				}
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}
			
		});

		loginButton.addActionListener(new ActionListener() {
			
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(
			 * java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				try {
					loginButton.setEnabled(false);
					onLoginRequest();
				} catch(Exception ex) {
					logger.error(ex.getMessage());
				}
			}
		});

		// Set button component for 'Exit' button on the LoginGUI form
		exitButton.setMnemonic('e');
		exitButton.setText("Exit");
		getContentPane().add(exitButton);
		exitButton.setBounds(200, 200, 90, 23);
		exitButton.getAccessibleContext().setAccessibleName("exitButton");
		exitButton.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(
			 * java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				onExit();
			}
		});

		//Set the position of the LoginGUI form on the screen
		java.awt.Dimension screenSize = java.awt.Toolkit.
		getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 396) / 2, 
				(screenSize.height - 331) / 2, 396, 275);
	}// End of function initComponents

	/**
	 * Called when Exit button on the LoginGUI is clicked.
	 */
	protected void onExit() {
		AgentViewApp.cleanUp();
		dispose();
	}

	/**
	 * Called from AgentStateUI when agent logs out.  
	 */
	public void agentLogOut()	{

		agentObject = null;
		agentStateUIObj = null;
		loginButton.setEnabled(true);
		setVisible(true);
	}

	/**
	 * Called when the user presses the Login button on the LoginGUI.  
	 */
	protected void onLoginRequest() throws Exception {
		
		if (isValidInput()) {

			// Fetching password value user has entered from UI.
			// This is required as by default it contain null.
			agentPassword = "";
			
			char[] passwd = agentPasswordPasswordField.getPassword(); 
			if (passwd != null) {
				agentPassword = String.valueOf(passwd);
			}
			
			// Setting agent work mode as per user selection on UI
			if (autoInRadioButton.isSelected()) {
				agentWorkMode = LucentAgent.MODE_AUTO_IN;
				
			} else if(manualInRadioButton.isSelected()) {
				agentWorkMode = LucentAgent.MODE_MANUAL_IN;
			}


			/*
			Client client = new Client();
			client.runClient();
			
			

			CallRestAPI rest = new CallRestAPI();
			

		      String pattern = "yyyy-MM-dd'T'HH:mm:ss";
		      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		      String date = simpleDateFormat.format(new Date());
		      
		    
		  	String json =  "{ "
		      		+ "\"agentid\":\""+agentIDTextField.getText()+"\","
		      		+ "\"loginTime\":\""+date+"\"}";

			rest.doPost(json, "addAgent");

*/
			try {
				// Create Agent
				agentObject = new AvayaCallCenterUserAgent
					(terminalAddressTextField.getText().trim(),
						agentIDTextField.getText().trim(), agentPassword, 
						agentWorkMode, callManager);

				callManager.setAgent(agentObject);
				
				// Create Agent State User Interface
				agentStateUIObj = new AgentStateUI(agentObject, callManager, 
						terminalAddressTextField.getText().trim(), 
						agentIDTextField.getText().trim(), 
						agentWorkMode,this);

				setVisible(false);
				agentStateUIObj.setVisible(true);
								
			} catch(JtapiPeerUnavailableException jtapiPeerExc) {
				JOptionPane.showMessageDialog(getContentPane(), "The Peer" +
					 " Object" +
						" is unavailable."+
						" Check the value for CTILink, CTIUserID or" +
						" CTIUserPassword "+
						"\nis specified correctly in the properties file."+
						"\nException message is: " + 
						jtapiPeerExc.getMessage(),"Exception:",JOptionPane
							.ERROR_MESSAGE);
				loginButton.setEnabled(true);
			} catch(ProviderUnavailableException prvUnExc) {
				JOptionPane.showMessageDialog(getContentPane(), "The Provider" +
					    " Object is unavailable."+
						" Check the value for CTILink, CTIUserID or" +
						" CTIUserPassword "+
						"\nis specified correctly in the properties file."+
						"\nException message is: " + 
						prvUnExc.getMessage(),"Exception:",JOptionPane
							.ERROR_MESSAGE);
				loginButton.setEnabled(true);
			}catch(TsapiInvalidArgumentException invArgExc) {
				JOptionPane.showMessageDialog(getContentPane(),"Please Check " +
					    "the Station extension entered ." +
						"Please try again. " +
						"\nException message is: " + invArgExc.getMessage(),
						"Exception:",JOptionPane.ERROR_MESSAGE);
				terminalAddressTextField.requestFocus();
				loginButton.setEnabled(true);
			} catch(TsapiInvalidStateException invStExc) {
				JOptionPane.showMessageDialog(getContentPane(),"AgentTerminal " +
						    "was in an invalid state." +
							"\nPlease try again. Exception message is: "+
							invStExc.getMessage(),
							"Exception:",JOptionPane.ERROR_MESSAGE);
				loginButton.setEnabled(true);
			} catch(TsapiPlatformException ex) {
				JOptionPane.showMessageDialog(getContentPane(),"Generic Error" +
					 " while adding the Agent"+
						"\nPossible causes:"+
						"\n1. Agent is already Logged In"+
						"\n2. Call is already in progress at the station"+
						"\n3. Station is out of Service"+
						"\n4. Agent Password is incorrect"+
						"\n5. Agent ID entered is incorrect"+
						"\nPlease verify and Try Again ." ,
						"Exception:",JOptionPane.ERROR_MESSAGE);
				terminalAddressTextField.requestFocus();
				loginButton.setEnabled(true);
			} catch(MethodNotSupportedException methodNtFndExc) {
				JOptionPane.showMessageDialog(getContentPane(),"Failed to add" +
					 " the " +
						"Observer to the Agent."+
						"\nPlease try again. Exception message is: "+ 
						methodNtFndExc.getMessage(),
						"Exception:",JOptionPane.ERROR_MESSAGE);
				loginButton.setEnabled(true);
			} catch(ResourceUnavailableException resUnavlblExc) {
				JOptionPane.showMessageDialog(getContentPane(),"Specified" +
					 " extension is NOT available." +
						"\nVerify Station Extension configuration on" +
							 " Communication Manager" +
					"\nPlease try again :"+resUnavlblExc.getMessage(),
					"Exception:",JOptionPane.ERROR_MESSAGE);
				loginButton.setEnabled(true);
			} catch(InvalidArgumentException invalidArgExc) {
				JOptionPane.showMessageDialog(getContentPane(),"Exception" +
					 " during creating" +
						 " extension terminal or address" +
						"\nPlease try again" + invalidArgExc.getMessage(),
						"Exception:",JOptionPane.ERROR_MESSAGE);
				loginButton.setEnabled(true);
			} catch (Exception e) {
				if (e.getMessage().contains("is Logged In")){
					JOptionPane.showMessageDialog(getContentPane(),
							"Can't proceed with Login: " +
							e.getMessage(), "Exception:",
							JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(getContentPane(),"Exception" +
							" during user " +
							"login: " + e.getMessage(),
							"Exception:",JOptionPane.ERROR_MESSAGE);
				}
				loginButton.setEnabled(true);
			}
		}
		else
			loginButton.setEnabled(true);
	}

	/**
	 * Method to handle action performed event
	 */
	public void actionPerformed(ActionEvent event) {
		if ((JButton)event.getSource() == exitButton) {
			System.exit(0);
		}
	}

	/**
	 * Function to handle key typed event.  It is used to validate user input
	 * into the Agent ID and Agent Password fields - only characters 0-9 are
	 * allowed.
	 */
	public void keyTyped(KeyEvent event) {
		if ( ((JTextField)event.getSource() == agentIDTextField) ||
				 ((JTextField)event.getSource() == agentPasswordPasswordField
					 ) ||
				 ((JTextField)event.getSource() == 
					terminalAddressTextField) ) {
			if ((event.getKeyChar() < KeyEvent.VK_0) || 
					event.getKeyChar() > KeyEvent.VK_9) {
				event.consume();
			}
		}
	}

	/**
	 * must implement abstract method
	 */
	public void keyPressed(KeyEvent arg0) {}

	/**
	 * must implement abstract method
	 */
	public void keyReleased(KeyEvent arg0) {}

	/**
	 * This function checks whether all the input
	 * fields are entered or not. If not appropriate message is displayed. 
	 * If all the required values are entered then those are stored in 
	 * the respective member variables.
	 * @return true if all the required values entered otherwise false.
	 */
	private boolean isValidInput() {
		boolean isValid = false;
		
		// Check if station extension is entered
		terminalAddress = terminalAddressTextField.getText();
		
		if ((terminalAddress == null) || 
				(terminalAddress.trim().equals(""))) {
			JOptionPane.showMessageDialog(getContentPane(),
				"Please enter value of the Station Extension field.");
			terminalAddressTextField.requestFocus();
		}
		
		// Check if Agent ID is entered
		else if ((agentIDTextField.getText() == null) || 
				(agentIDTextField.getText().trim().equals(""))) {
			JOptionPane.showMessageDialog(getContentPane(),
				"Please enter value of the Agent ID field.");
			agentIDTextField.requestFocus();
		} else if(!autoInRadioButton.isSelected() &&
				!manualInRadioButton.isSelected()) {
			JOptionPane.showMessageDialog(getContentPane(),
				"Please select the Agent mode.");
		} else {
			
			// if all the inputs are given properly assign 
			// them to corresponding variables
			isValid = true;
		}
	
		return isValid;
	}

	/**
	 * WindowListener for handling window closing event  
	 */
	private WindowListener win = new WindowAdapter() {
		
		public void windowClosing(WindowEvent e) {
			onExit();
		}
	};
}
